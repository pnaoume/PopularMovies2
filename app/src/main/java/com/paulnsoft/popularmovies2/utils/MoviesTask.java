package com.paulnsoft.popularmovies2.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.paulnsoft.popularmovies2.R;
import com.paulnsoft.popularmovies2.MoviesGridFragment;
import com.paulnsoft.popularmovies2.utils.httprequests.MoviesAPI;
import retrofit.RestAdapter;

public class MoviesTask extends AsyncTask<Void, Integer, QueryResult> {
    private MoviesGridFragment moviesGridFragment;
    private String mSortingMethod;
    private long mPage;
    private String mKey;
    private static final String TAG = "MoviesTask";
    public static final String requestURL = "http://api.themoviedb.org";

    public MoviesTask(MoviesGridFragment fragment, String sortMethod, String key, Long page) {
        moviesGridFragment = fragment;
        mSortingMethod = sortMethod;
        mKey = key;
        mPage = page;
    }

    @Override
    protected QueryResult doInBackground(Void... params) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(requestURL)
                .build();
        MoviesAPI commAPI = adapter.create(MoviesAPI.class);
        QueryResult theResult = commAPI.getMovies(mSortingMethod, mKey, mPage);
        return theResult;
    }

    @Override
    protected void onPostExecute(QueryResult queryResult) {
        super.onPostExecute(queryResult);
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