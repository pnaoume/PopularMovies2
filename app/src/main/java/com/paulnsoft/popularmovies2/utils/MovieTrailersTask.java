package com.paulnsoft.popularmovies2.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.utils.trailers.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieTrailersTask extends AsyncTask<String, Integer, String> {
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie";
    private long filmID;
    public MovieTrailersTask() {

    }
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
            for (com.paulnsoft.popularmovies2.utils.trailers.Result res : trailers.getResults()) {
                addTrailer(res);
            }
        } else {
            displayToast(R.string.problem_getting_trailers_list);
        }
    }
}