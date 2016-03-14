package com.paulnsoft.popularmovies2.utils.db;

import android.os.AsyncTask;

import com.paulnsoft.popularmovies2.MoviesGridFragment;
import com.paulnsoft.popularmovies2.utils.MoviesListAdapter;

import java.util.Vector;

public class DBTask extends AsyncTask<String, Integer, Vector<Movie>> {

    private MoviesDB movieDB;
    MoviesListAdapter mMoviesListAdapter;

    public DBTask(MoviesDB movieDB, MoviesListAdapter mMoviesListAdapter) {
        this.movieDB = movieDB;
        this.mMoviesListAdapter = mMoviesListAdapter;
    }

    @Override
    protected Vector<Movie> doInBackground(String... params) {
        movieDB.open();
        Vector<Movie> movies = Utils.getAllMoviesFromCursor(movieDB.getallFavoriteMovies());
        movieDB.close();
        return movies;
    }

    @Override
    protected void onPostExecute( Vector<Movie> mvs) {
        for(Movie mv: mvs) {
            mMoviesListAdapter.addStoredMovie(mv);
            mMoviesListAdapter.notifyDataSetChanged();
        }
    }
}