package com.paulnsoft.popularmovies2.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.utils.reviews.Reviews;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieReviewsTask extends AsyncTask<Void, Integer, Reviews> {
    private static final String TAG = "MovieReviewsTask";
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie";
    private long filmID;
    public MovieReviewsTask() {

    }
    @Override
    protected Reviews doInBackground(Void... params) {
        Reviews reviews = null;
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(params[0]).openConnection();
            resultString = IOUtils.toString(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    @Override
    protected void onPostExecute(Reviews s) {
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