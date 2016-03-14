package com.paulnsoft.popularmovies2.utils.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MoviesDB {

    private static final String TAG = "MoviesDB";
    private static final String DATABASE_NAME = "movies.db";
    private static final String DATABASE_TABLE = "movies";
    private DatabaseHelper databaseHelper;
    protected SQLiteDatabase sqLiteDatabase;
    private Context context;

    private static final int DATABASE_VERSION = 3;

    public static final String MOVIE_ID = "movieId";
    public static final String MOVIE_NAME = "movieName";
    public static final String MOVIE_PLOT = "plot";
    public static final String MOVIE_RELEASE_DATE = "releaseDate";
    public static final String MOVIE_VOTE_AVERAGE = "voteAverage";
    public static final String MOVIE_BIG_IMAGE = "bigImage";
    public static final String MOVIE_SMALL_IMAGE = "smallImage";

    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " (" +
                    MOVIE_ID + " integer primary key, " +
                    MOVIE_NAME + " text not null, " +
                    MOVIE_PLOT + " text not null, " +
                    MOVIE_RELEASE_DATE + " text not null, " +
                    MOVIE_VOTE_AVERAGE + " float not null, " +
                    MOVIE_BIG_IMAGE + " blob, " +
                    MOVIE_SMALL_IMAGE + " blob ); ";

    public MoviesDB(Context context) {
        this.context = context;
    }

    public MoviesDB open() {
        databaseHelper = new DatabaseHelper(context);
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (sqLiteDatabase != null)
            sqLiteDatabase.close();
    }

    public long addMovie(long movieId, String movieName, String moviePlot,
                         String releaseDate, double voteAverage, byte [] bigImage,
                         byte [] smallImage) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MOVIE_ID, movieId);
        contentValues.put(MOVIE_NAME, movieName);
        contentValues.put(MOVIE_PLOT, moviePlot);
        contentValues.put(MOVIE_RELEASE_DATE, releaseDate);
        contentValues.put(MOVIE_VOTE_AVERAGE, voteAverage);
        contentValues.put(MOVIE_BIG_IMAGE, bigImage);
        contentValues.put(MOVIE_SMALL_IMAGE, smallImage);

        return sqLiteDatabase.insert(DATABASE_TABLE, null, contentValues);
    }

    public long addMovie(Movie movie) {
        return addMovie(movie.getId(),
                movie.getTitle(),
                movie.getPlot(),
                movie.getReleaseDate(),
                movie.getVoteAverage(),
                movie.getBigImage(),
                movie.getSmallImage());
    }


    public Cursor getallFavoriteMovies() throws SQLException {
        return getMovieWhere(null);
    }

    public boolean movieExists(long id) throws SQLException {
        String whereQuery = MOVIE_ID+"='"+id+"'";
        return getMovieWhere(whereQuery).getCount() > 0;
    }

    public Cursor getMovie(long id) throws SQLException {
        String whereQuery = MOVIE_ID+"='"+id+"'";
        return getMovieWhere(whereQuery);
    }

    public boolean deleteFavoriteMovie(long id) {
        return sqLiteDatabase.delete(DATABASE_TABLE, MOVIE_ID + "='" + id +"'", null) > 0;
    }

    private Cursor getMovieWhere(String whereQuery) throws SQLException {
        Cursor mCursor;

        mCursor = sqLiteDatabase.query(true, DATABASE_TABLE, new String[] {MOVIE_ID,
                MOVIE_NAME, MOVIE_PLOT, MOVIE_RELEASE_DATE, MOVIE_VOTE_AVERAGE,
                MOVIE_BIG_IMAGE, MOVIE_SMALL_IMAGE}, whereQuery, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase,int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(sqLiteDatabase);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
