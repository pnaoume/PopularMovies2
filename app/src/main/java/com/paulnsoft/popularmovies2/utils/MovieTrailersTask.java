package com.paulnsoft.popularmovies2.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.paulnsoft.popularmovies2.MovieDetailActivity;
import com.paulnsoft.popularmovies2.MovieDetailFragment;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.utils.httprequests.MoviesAPI;
import com.paulnsoft.popularmovies2.utils.trailers.*;


import retrofit.RestAdapter;

public class MovieTrailersTask extends AsyncTask<Void, Integer, Trailers> {
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie";
    private static final String TAG = "MovieTrailersTask";
    private long mFilmID;
    private MovieDetailActivity mActivity;
    private String mKey;
    private MovieDetailFragment mFragment;
    public MovieTrailersTask(long id, String key, MovieDetailActivity activity) {
        mFilmID = id;
        mKey = key;
        mActivity = activity;
    }

    public MovieTrailersTask(long id, String key, MovieDetailFragment movieDetailFragment) {
        mFilmID = id;
        mKey = key;
        mFragment = movieDetailFragment;
    }
    @Override
    protected Trailers doInBackground(Void... params) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .build();
        MoviesAPI commAPI = adapter.create(MoviesAPI.class);
        Trailers theResult = commAPI.getTrailers(mFilmID, mKey);
        return theResult;
    }

    @Override
    protected void onPostExecute(Trailers trailers) {
        super.onPostExecute(trailers);
        if(trailers != null) {
            Log.i(TAG, "Trailer list is: " + trailers.getResults() + " long");
            for (com.paulnsoft.popularmovies2.utils.trailers.Result res : trailers.getResults()) {
                if(mActivity != null) {
                    mActivity.addTrailer(res);
                }
                if(mFragment != null) {
                    mFragment.addTrailer(res);
                }
            }
        } else {
            if(mActivity != null) {
                mActivity.displayToast(R.string.problem_getting_trailers_list);
            }
            if(mFragment != null) {
                mFragment.displayToast(R.string.problem_getting_trailers_list);
            }
        }
    }
}