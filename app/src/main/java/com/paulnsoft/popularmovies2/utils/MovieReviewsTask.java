package com.paulnsoft.popularmovies2.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.paulnsoft.popularmovies2.MovieDetailActivity;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.utils.httprequests.MoviesAPI;
import com.paulnsoft.popularmovies2.utils.reviews.Reviews;

import retrofit.RestAdapter;

public class MovieReviewsTask extends AsyncTask<Void, Integer, Reviews> {
    private static final String TAG = "MovieReviewsTask";
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie";
    private long mFilmID;
    private String mKey;
    private MovieDetailActivity mActivity;
    public MovieReviewsTask(long id, String key, MovieDetailActivity activity) {
        mFilmID = id;
        mKey = key;
        mActivity = activity;
    }

    @Override
    protected Reviews doInBackground(Void... params) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .build();
        MoviesAPI commAPI = adapter.create(MoviesAPI.class);
        Reviews theResult = commAPI.getReviews(mFilmID, mKey);
        return theResult;
    }

    @Override
    protected void onPostExecute(Reviews reviews) {
        super.onPostExecute(reviews);
        if(reviews != null) {
            Log.i(TAG, "Reviews list is: " + reviews.getResults() + " long");
            for (com.paulnsoft.popularmovies2.utils.reviews.Result res : reviews.getResults()) {
                mActivity.addReview(res);
            }
        } else {
            mActivity.displayToast(R.string.problem_getting_reviews_list);
        }
    }
}