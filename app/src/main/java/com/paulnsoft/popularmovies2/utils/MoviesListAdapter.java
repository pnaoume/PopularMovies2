package com.paulnsoft.popularmovies2.utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paulnsoft.popularmovies2.MoviesGridFragment;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.utils.db.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MoviesListAdapter extends BaseAdapter {
    private ArrayList<Result> mMovies;
    private ArrayList<Movie> mstoredMovies;
    private LayoutInflater mInflator;
    private boolean displayingFavorites;
    private MoviesGridFragment mFragment;
    private static final String TAG = "MoviesListAdapter";
    public static final String voteAverage = "vote_average";
    public static final String popularity = "popularity";

    public static final String imagesPrefix = "http://image.tmdb.org/t/p/w185/";

    private String mKey;

    public Result getMovie(int position) {
        return mMovies.get(position);
    }

    public MoviesListAdapter(LayoutInflater inflator, MoviesGridFragment fragment, String key) {
        super();
        mMovies = new ArrayList<>();
        mstoredMovies = new ArrayList<>();
        mInflator = inflator;
        mFragment = fragment;
        mKey = key;
    }

    public void setDisplayMode(boolean displayingFavorites) {
        this.displayingFavorites = displayingFavorites;
    }
    public void clear() {
        mMovies.clear();
        mstoredMovies.clear();
    }

    @Override
    public int getCount() {
        if(!displayingFavorites) {
            return mMovies.size();
        } else {
            return mstoredMovies.size();
        }
    }

    public void addResult(Result result){
        if(!mMovies.contains(result))
        {
            mMovies.add(result);
        }
    }

    public void addStoredMovie(Movie mv){
        if(!mstoredMovies.contains(mv))
        {
            mstoredMovies.add(mv);
        }
    }

    @Override
    public Object getItem(int position) {
        if(!displayingFavorites) {
            return mMovies.get(position);
        } else {
            return mstoredMovies.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "Getting view at position: " + position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.list_item_movie, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(!displayingFavorites) {
            Result movie = mMovies.get(position);
            viewHolder.movieTitle.setText(movie.original_title);

            Picasso.with(mFragment.getActivity()).load(imagesPrefix + movie.poster_path).
                    into(viewHolder.movieThumbNail, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Log.i(TAG, "Error loading image!");
                            viewHolder.movieThumbNail.setImageResource(R.mipmap.ic_launcher);

                        }
                    });
            if (((mMovies.size() - position) < 4) &&
                    (mFragment.lastLoadedPage < mFragment.totalNumberOfPages)) {
                if (mFragment.displayingMoviesByRatings) {
                    if (NetworkState.isConnected(mFragment.getActivity())) {
                        new MoviesTask(mFragment, voteAverage, mKey, mFragment.lastLoadedPage + 1).
                                execute();
                    } else {
                        mFragment.displayToast(R.string.network_unreachable);
                    }
                } else {
                    if (NetworkState.isConnected(mFragment.getActivity())) {
                        new MoviesTask(mFragment, popularity, mKey, mFragment.lastLoadedPage + 1).
                                execute();
                    } else {
                        mFragment.displayToast(R.string.network_unreachable);
                    }
                }
            }
        } else {
            Movie movie = mstoredMovies.get(position);
            viewHolder.movieTitle.setText(movie.getTitle());
            viewHolder.movieThumbNail.setImageBitmap(
                    mFragment.getBitmapFromStream(movie.getSmallImage()));
        }
        return convertView;
    }

    static class ViewHolder {

        @Bind(R.id.imageView)
        ImageView movieThumbNail;

        @Bind(R.id.movie_title)
        TextView movieTitle;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}