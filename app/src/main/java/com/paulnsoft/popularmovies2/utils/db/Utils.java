package com.paulnsoft.popularmovies2.utils.db;


import android.database.Cursor;

import java.util.Vector;

public class Utils {

    private long id;
    private String title;
    private String plot;
    private String releaseDate;
    private long voteAverage;
    private byte[] smallImage;
    private byte[] bigImage;

    public static Movie fillMovieFromCursor(Cursor cursor) {
        Movie movie = new Movie();
        movie.setId(cursor.getLong(cursor.getColumnIndex(MoviesDB.MOVIE_ID)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(MoviesDB.MOVIE_NAME)));
        movie.setPlot(cursor.getString(cursor.getColumnIndex(MoviesDB.MOVIE_PLOT)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MoviesDB.MOVIE_RELEASE_DATE)));
        movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MoviesDB.MOVIE_VOTE_AVERAGE)));
        movie.setBigImage(cursor.getBlob(cursor.getColumnIndex(MoviesDB.MOVIE_BIG_IMAGE)));
        movie.setSmallImage(cursor.getBlob(cursor.getColumnIndex(MoviesDB.MOVIE_SMALL_IMAGE)));
        return movie;
    }

    public static Vector<Movie> getAllMoviesFromCursor(Cursor cursor) {
        Vector<Movie> movies = new Vector<>();
        while (!cursor.isAfterLast()) {
            movies.add(fillMovieFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return movies;
    }
}
