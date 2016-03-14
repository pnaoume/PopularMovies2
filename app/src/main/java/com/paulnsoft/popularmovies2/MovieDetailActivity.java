package com.paulnsoft.popularmovies2;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.utils.NetworkState;
import com.paulnsoft.popularmovies2.utils.db.Movie;
import com.paulnsoft.popularmovies2.utils.db.MoviesDB;
import com.paulnsoft.popularmovies2.utils.db.Utils;
import com.paulnsoft.popularmovies2.utils.reviews.Reviews;
import com.paulnsoft.popularmovies2.utils.trailers.Result;
import com.paulnsoft.popularmovies2.utils.trailers.Trailers;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailActivity  extends AppCompatActivity {
    private static  final String TAG = "MovieDetailActivity";
    public static final String MOVIE_EXTRA = "MOVIE_EXTRA";
    public static final String MOVIE_EXTRA_DB = "MOVIE_EXTRA_DB";
    public static final String MOVIE_SMALL_IMAGE_EXTRA = "MOVIE_SMALL_IMAGE_EXTRA";
    private static final String imagesPrefix = "http://image.tmdb.org/t/p/w780/";
    private static final String youtubePrefix = "https://www.youtube.com/watch?v=";
    private static final String API_PREFIX = "http://api.themoviedb.org/3/movie/";
    private static final String REVIEWS_SUFFIX = "/reviews?api_key=";
    private static final String TRAILERS_SUFFIX = "/videos?api_key=";
    @Bind(R.id.movie_poster)
    ImageView poster;

    @Bind(R.id.movie_plot)
    TextView synopsis;

    @Bind(R.id.movie_release_date_display)
    TextView releaseDate;

    @Bind(R.id.movie_rating_display)
    TextView voteAverage;

    @Bind(R.id.toolbar)
    Toolbar navToolBar;

    @Bind(R.id.trailerList)
    ListView trailerList;

    @Bind(R.id.commentsList)
    ListView commentsList;

    @Bind(R.id.floating_button)
    FloatingActionButton flButton;

    private SimpleTrailerAdapter trailersAdapter;
    private SimpleReviewAdapter reviewsAdapter;
    private MoviesDB moviesDB;
    private com.paulnsoft.popularmovies2.utils.Result currentMovie;
    private Movie currentMovieDb;
    private byte[] smallImage;
    private long currentMovieId;
    private boolean currentMovieIsStarred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);
        ButterKnife.bind(this);
        navToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        trailersAdapter = new SimpleTrailerAdapter();
        trailerList.setAdapter(trailersAdapter);
        trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String trailerUrl = (String) trailersAdapter.getItem(position);
                Log.i(TAG, "Trailers clicked, selected url is: " + trailerUrl);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                startActivity(browserIntent);
            }
        });
        reviewsAdapter = new SimpleReviewAdapter();
        commentsList.setAdapter(reviewsAdapter);
        commentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String reviewUrl = (String) reviewsAdapter.getItem(position);
                Log.i(TAG, "Reviews clicked");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl));
                startActivity(browserIntent);
            }
        });
        Intent intent = getIntent();
        if(intent != null) {
            com.paulnsoft.popularmovies2.utils.Result result = (com.paulnsoft.popularmovies2.utils.Result)intent.getSerializableExtra(MOVIE_EXTRA);
            if(result != null ) {
                smallImage = intent.getByteArrayExtra(MOVIE_SMALL_IMAGE_EXTRA);
                currentMovie = result;
                currentMovieId = result.id;
                navToolBar.setTitle(result.title);
                Log.i(TAG, "Movie id: " + result.id);
                Picasso.with(getApplicationContext()).load(imagesPrefix + result.poster_path).
                        into(poster, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Log.i(TAG, "Error loading image!");
                                poster.setImageResource(R.mipmap.ic_launcher);

                            }
                        });

                synopsis.setText(result.overview);
                releaseDate.setText(result.release_date);
                voteAverage.setText("" + result.vote_average);
                requestTrailers(result.id);
                requestReviews(result.id);
            } else {
                Movie movie = (Movie)intent.getSerializableExtra(MOVIE_EXTRA_DB);
                if(movie != null) {
                    currentMovieDb = movie;
                    navToolBar.setTitle(currentMovieDb.getTitle());
                    smallImage = currentMovieDb.getSmallImage();
                    currentMovieId = currentMovieDb.getId();
                    synopsis.setText(currentMovieDb.getPlot());
                    releaseDate.setText(currentMovieDb.getReleaseDate());
                    voteAverage.setText("" + currentMovieDb.getVoteAverage());
                    requestTrailers(currentMovieDb.getId());
                    requestReviews(currentMovieDb.getId());
                    new DBTask().execute(currentMovieId);
                }
            }
        }
        moviesDB = new MoviesDB(this);
        setFloatingButtonStar();
        flButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingButtonClicked();
            }
        });
    }

    private void setFloatingButtonStar() {
        moviesDB.open();
        if( moviesDB.movieExists(currentMovieId)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flButton.setImageResource(R.drawable.full_star);
                }
            });

            currentMovieIsStarred = true;
        } else {
            currentMovieIsStarred = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flButton.setImageResource(R.drawable.ic_action);
                }
            });

        }
        moviesDB.close();
    }

    public class DBTask extends AsyncTask<Long, Integer, Movie> {

        @Override
        protected Movie doInBackground(Long... params) {
            moviesDB.open();
            Movie movie = Utils.fillMovieFromCursor(moviesDB.getMovie(params[0]));
            moviesDB.close();
            return movie;
        }

        @Override
        protected void onPostExecute( Movie mv) {
            poster.setImageBitmap(MoviesGridFragment.getBitmapFromStream(mv.getBigImage()));

        }
    }

    private void setFloatingButtonStar(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flButton.setImageResource(resourceId);
            }
        });
    }

    public void floatingButtonClicked () {
        Log.i(TAG, "Button clicked!");
        if(!currentMovieIsStarred) {
            Movie movie = new Movie();
            movie.setId(currentMovieId);
            movie.setTitle(navToolBar.getTitle().toString());
            movie.setSmallImage(smallImage);
            movie.setPlot(synopsis.getText().toString());
            movie.setReleaseDate(releaseDate.getText().toString());
            movie.setVoteAverage(Double.parseDouble(voteAverage.getText().toString()));
            movie.setBigImage(MoviesGridFragment.
                    extractImageByteStream(((BitmapDrawable) poster.getDrawable()).getBitmap()));
            moviesDB.open();
            Log.i(TAG, "Movie added: " + moviesDB.addMovie(movie));
            moviesDB.close();
            currentMovieIsStarred = true;
            setFloatingButtonStar(R.drawable.full_star);
        } else {
            moviesDB.open();
            Log.i(TAG, "Movie deleted: " +
                    moviesDB.deleteFavoriteMovie(currentMovieId));
            moviesDB.close();
            currentMovieIsStarred = false;
            setFloatingButtonStar(R.drawable.ic_action);
        }

    }
    public static class SimpleTrailerAdapter
            extends BaseAdapter {

        private List<String> mTrailerUrls;

        public SimpleTrailerAdapter() {
            super();
            mTrailerUrls = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mTrailerUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return mTrailerUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trailer_item, parent, false);
            ViewHolder vh = new ViewHolder(view);

            vh.mTrailerUrl = mTrailerUrls.get(position);
            vh.mTextView.setText(parent.getContext().getString(R.string.watch_trailer) +
                    " " + (position+1));
            view.setTag(vh);
            return view;
        }

        public static class ViewHolder{

            public final View mView;
            public String mTrailerUrl;
            public final TextView mTextView;
            public ViewHolder(View itemView) {
                mView = itemView;
                mTextView = (TextView) itemView.findViewById(R.id.trailer_text);
            }
        }

        public void addTrailer(String url) {
            mTrailerUrls.add(url);
        }

    }

    public static class SimpleReviewAdapter
            extends BaseAdapter {

        private List<String> mReviewUrls;
        private List<String> mAuthornames;

        public SimpleReviewAdapter() {
            super();
            mReviewUrls = new ArrayList<>();
            mAuthornames = new ArrayList<>();
        }

        public void addReview(String reviewUrl, String authorName) {
            mReviewUrls.add(reviewUrl);
            mAuthornames.add(authorName);
        }

        @Override
        public int getCount() {
            return mReviewUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return mReviewUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_item, parent, false);
            ViewHolder vh = new ViewHolder(view);
            vh.mReviewUrl = mReviewUrls.get(position);
            vh.mCommenterName.setText(mAuthornames.get(position));
            vh.mCommentContent.setText(R.string.view_comment_online);
            view.setTag(vh);
            return view;
        }

        public static class ViewHolder {
            public final View mView;
            public String mReviewUrl;
            public final TextView mCommenterName;
            public final TextView mCommentContent;
            public ViewHolder(View itemView) {
                mView = itemView;
                mCommenterName = (TextView) itemView.findViewById(R.id.commenter_name);
                mCommentContent = (TextView) itemView.findViewById(R.id.comment_content);
            }
        }
    }



    public class MovieTrailersTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String resultString = null;
            try {
                HttpURLConnection conn = (HttpURLConnection)new URL(params[0]).openConnection();
                resultString = IOUtils.toString(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
             Trailers trailers = gson.fromJson(s, Trailers.class);
            if(trailers != null) {
                Log.i(TAG, "Trailer list is: " + trailers.getResults() + " long");
                for (Result res : trailers.getResults()) {
                    addTrailer(res);
                }
            } else {
                displayToast(R.string.problem_getting_trailers_list);
            }
        }
    }

    private void addTrailer(Result res) {
        if(res.getSite().toLowerCase().equals("youtube")) {
            trailersAdapter.addTrailer(youtubePrefix+res.getKey());
            trailersAdapter.notifyDataSetChanged();
        }
    }

    public class MovieReviewsTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String resultString = null;
            try {
                HttpURLConnection conn = (HttpURLConnection)new URL(params[0]).openConnection();
                resultString = IOUtils.toString(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
            Reviews reviews = gson.fromJson(s, Reviews.class);
            if(reviews != null) {
                Log.i(TAG, "Reviews list is: " + reviews.getResults() + " long");
                for (com.paulnsoft.popularmovies2.utils.reviews.Result res : reviews.getResults()) {
                    addReview(res);
                }
            } else {
                displayToast(R.string.problem_getting_reviews_list);
            }
        }
    }

    private void addReview(com.paulnsoft.popularmovies2.utils.reviews.Result res) {
        reviewsAdapter.addReview(res.getUrl(), res.getAuthor());
        reviewsAdapter.notifyDataSetChanged();
    }

    private void requestTrailers(long id) {
        if (NetworkState.isConnected(getApplicationContext())) {
            new MovieTrailersTask().execute(generateURLForTrailers(id));
        } else {
            displayToast(R.string.network_unreachable);
        }
    }

    private String generateURLForTrailers(long id) {
        StringBuilder stResult= new StringBuilder();
        stResult.append(API_PREFIX);
        stResult.append(id);
        stResult.append(TRAILERS_SUFFIX);
        stResult.append(readKey());
        Log.i(TAG, "Returning url for trailers: " + stResult.toString());
        return stResult.toString();
    }


    private void requestReviews(long id) {
        if (NetworkState.isConnected(getApplicationContext())) {
            new MovieReviewsTask().execute(generateURLForReviews(id));
        } else {
            displayToast(R.string.network_unreachable);
        }
    }

    private String generateURLForReviews(long id) {
        StringBuilder stResult= new StringBuilder();
        stResult.append(API_PREFIX);
        stResult.append(id);
        stResult.append(REVIEWS_SUFFIX);
        stResult.append(readKey());
        Log.i(TAG, "Returning url for reviews: " + stResult.toString());
        return stResult.toString();
    }

    static String readKey() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File file = new File(externalStorageDirectory, "key.txt");
        Log.i(TAG, "Trying to find key: " + file.toString());
        String key = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String firstLine = br.readLine();
            key = firstLine;
            Log.i(TAG, "Key found");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Key not found" + e);
            key = null;
        } catch (IOException e) {
            Log.i(TAG, "Key not found" + e);
            key = null;
        }
        return key;
    }

    private void displayToast(final int stringRes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        stringRes, Toast.LENGTH_LONG).show();
            }
        });
    }

}
