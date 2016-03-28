package com.paulnsoft.popularmovies2;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

import com.paulnsoft.popularmovies2.utils.MovieReviewsTask;
import com.paulnsoft.popularmovies2.utils.MovieTrailersTask;
import com.paulnsoft.popularmovies2.utils.NetworkState;
import com.paulnsoft.popularmovies2.utils.db.Movie;
import com.paulnsoft.popularmovies2.utils.db.MoviesDB;
import com.paulnsoft.popularmovies2.utils.db.Utils;
import com.paulnsoft.popularmovies2.utils.trailers.Result;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailFragment  extends Fragment{

    public static final String TAG = "MovieDetailFragment";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.movie_details_fragment, container, false);
        poster = (ImageView) rootView.findViewById(R.id.movie_poster);
        synopsis = (TextView)rootView.findViewById(R.id.movie_plot);
        releaseDate = (TextView)rootView.findViewById(R.id.movie_release_date_display);
        voteAverage = (TextView)rootView.findViewById(R.id.movie_rating_display);
        navToolBar = (Toolbar)rootView.findViewById(R.id.toolbar);
        trailerList = (ListView)rootView.findViewById(R.id.trailerList);
        commentsList = (ListView)rootView.findViewById(R.id.commentsList);
        flButton =
                (FloatingActionButton)rootView.findViewById(R.id.floating_button);
        if(arguments != null) {
            com.paulnsoft.popularmovies2.utils.Result result =
                    (com.paulnsoft.popularmovies2.utils.Result)arguments.
                    getSerializable(MovieDetailActivity.MOVIE_EXTRA);
            if(result != null ) {
                smallImage = arguments.getByteArray(MovieDetailActivity.MOVIE_SMALL_IMAGE_EXTRA);
                currentMovie = result;
                currentMovieId = result.id;
                navToolBar.setTitle(result.title);
                Log.i(TAG, "Movie id: " + result.id);
                Picasso.with(getActivity()).load(MovieDetailActivity.imagesPrefix + result.poster_path).
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
                Movie movie = (Movie)arguments.getSerializable(MovieDetailActivity.MOVIE_EXTRA_DB);
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
            moviesDB = new MoviesDB(getActivity());
            setFloatingButtonStar();
            flButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    floatingButtonClicked();
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
        }
        return rootView;
    }

    private void setFloatingButtonStar() {
        moviesDB.open();
        if( moviesDB.movieExists(currentMovieId)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flButton.setImageResource(R.drawable.full_star);
                }
            });

            currentMovieIsStarred = true;
        } else {
            currentMovieIsStarred = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flButton.setImageResource(R.drawable.ic_action);
                }
            });

        }
        moviesDB.close();
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

    private void requestTrailers(long id) {
        if (NetworkState.isConnected(getActivity())) {
            new MovieTrailersTask(id,getString(R.string.moviesdb_key), this).execute();
        } else {
            displayToast(R.string.network_unreachable);
        }
    }

    private void requestReviews(long id) {
        if (NetworkState.isConnected(getActivity())) {
            new MovieReviewsTask(id,getActivity().getString(R.string.moviesdb_key), this).execute();
        } else {
            displayToast(R.string.network_unreachable);
        }
    }

    public void displayToast(final int stringRes) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        stringRes, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void addReview(com.paulnsoft.popularmovies2.utils.reviews.Result res) {
        reviewsAdapter.addReview(res.getUrl(), res.getAuthor());
        reviewsAdapter.notifyDataSetChanged();
    }

    public void addTrailer(Result res) {
        if(res.getSite().toLowerCase().equals("youtube")) {
            trailersAdapter.addTrailer(MovieDetailActivity.youtubePrefix+res.getKey());
            trailersAdapter.notifyDataSetChanged();
        }
    }

    private void setFloatingButtonStar(final int resourceId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flButton.setImageResource(resourceId);
            }
        });
    }

}
