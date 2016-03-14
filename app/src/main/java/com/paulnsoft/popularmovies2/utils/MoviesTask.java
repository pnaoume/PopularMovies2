package com.paulnsoft.popularmovies2.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.MoviesGridFragment;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoviesTask extends AsyncTask<String, Integer, String> {
    private MoviesGridFragment moviesGridFragment;
    private static final String TAG = "MoviesTask";
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
        QueryResult queryResult = gson.fromJson(s, QueryResult.class);
        if(queryResult != null) {
            moviesGridFragment.lastLoadedPage = queryResult.page;
            moviesGridFragment.totalNumberOfMovies = queryResult.total_results;
            moviesGridFragment.totalNumberOfPages = queryResult.total_pages;
            Log.i(TAG, "Fetched list is: " + queryResult.results.size() + " long");
            for (Result res : queryResult.results) {
                moviesGridFragment.addResult(res);
            }
        } else {
            moviesGridFragment.displayToast(R.string.problem_getting_movies_list);
        }
    }
}