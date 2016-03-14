package com.paulnsoft.popularmovies2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.paulnsoft.popularmovies2.utils.MoviesListAdapter;
import com.paulnsoft.popularmovies2.utils.MoviesTask;
import com.paulnsoft.popularmovies2.utils.NetworkState;
import com.paulnsoft.popularmovies2.utils.Result;
import com.paulnsoft.popularmovies2.utils.db.DBTask;
import com.paulnsoft.popularmovies2.utils.db.Movie;
import com.paulnsoft.popularmovies2.utils.db.MoviesDB;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

import static com.paulnsoft.popularmovies2.utils.MoviesListAdapter.generateURLForDecreasingPopularity;
import static com.paulnsoft.popularmovies2.utils.MoviesListAdapter.generateURLForDecreasingRating;


public class MoviesGridFragment extends Fragment{
    private static String TAG = "PopMoviesGridFragment";
    private static final String CLASSIFICATION_KEY = "CLASSIFICATION_KEY";
    private static final String STORED_MOVIES_KEY = "STORED_MOVIES_KEY";
    private static final int READ_EXTERNAL_STORAGE_RUNTIME_PERMISSION = 201;
    GridView mMovies;
    MoviesListAdapter mMoviesListAdapter;
    public boolean displayingMoviesByRatings;
    private boolean displayingStoredMovies;
    public long lastLoadedPage;
    public long totalNumberOfPages;
    public long totalNumberOfMovies;
    private String key;
    private MoviesDB movieDB;

    public void addResult(final Result result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMoviesListAdapter.addResult(result);
                mMoviesListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void clearList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMoviesListAdapter.clear();
                mMoviesListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(CLASSIFICATION_KEY)) {
            displayingMoviesByRatings = savedInstanceState.getBoolean(CLASSIFICATION_KEY);
        } else if (savedInstanceState != null && savedInstanceState.containsKey(STORED_MOVIES_KEY)){
            displayingStoredMovies = savedInstanceState.getBoolean(STORED_MOVIES_KEY);
        }
        movieDB = new MoviesDB(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);
        mMovies = (GridView) rootView.findViewById(R.id.gridView);
        mMoviesListAdapter = new MoviesListAdapter(inflater, this, key);
        mMovies.setAdapter(mMoviesListAdapter);
        lastLoadedPage = 1;
        mMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!displayingStoredMovies) {
                    final Result res = mMoviesListAdapter.getMovie(position);
                    if (res != null) {
                        Picasso.with(getActivity().getApplicationContext()).load(MoviesListAdapter.imagesPrefix + res.poster_path).
                                into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                                        intent.putExtra(MovieDetailActivity.MOVIE_EXTRA, res);
                                        intent.putExtra(MovieDetailActivity.MOVIE_SMALL_IMAGE_EXTRA,
                                                extractImageByteStream(bitmap));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                    }
                } else {
                    final Movie res = (Movie) mMoviesListAdapter.getItem(position);
                    res.setBigImage(null);
                    Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                    intent.putExtra(MovieDetailActivity.MOVIE_EXTRA_DB, res);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
        //Request permission here if on M
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_RUNTIME_PERMISSION);
        } else {
            if(!displayingStoredMovies) {
                readKeyAndRequestMovies();
            } else {
                getStoredMovies();
            }
        }
        return rootView;
    }

    public static byte[] extractImageByteStream(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromStream(byte[] array) {
       return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    private void requestMovies(boolean organizeByRatings) {
        if (NetworkState.isConnected(getActivity().getApplicationContext())) {
            if(organizeByRatings) {
                new MoviesTask().execute(generateURLForDecreasingRating(1, key));
            } else {
                new MoviesTask().execute(generateURLForDecreasingPopularity(1, key));
            }
        } else {
            displayToast(R.string.network_unreachable);
        }
    }

    private void getStoredMovies() {
        displayingStoredMovies = true;
        mMoviesListAdapter.setDisplayMode(true);
        new DBTask(movieDB, mMoviesListAdapter).execute("");
    }

    private void readKeyAndRequestMovies() {
        key = getResources().getString(R.string.moviesdb_key);
        if(!TextUtils.isEmpty(key)) {
            requestMovies(displayingMoviesByRatings);
        } else {
            displayToast(R.string.problem_incorrect_key);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_RUNTIME_PERMISSION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readKeyAndRequestMovies();
                }
                break;
        }
    }

    public void displayToast(final int stringRes) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(),
                        stringRes, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CLASSIFICATION_KEY, displayingMoviesByRatings);
        outState.putBoolean(STORED_MOVIES_KEY, displayingStoredMovies);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_rating) {
            if(!displayingMoviesByRatings || displayingStoredMovies) {
                clearList();
                mMoviesListAdapter.setDisplayMode(false);
                displayingStoredMovies = false;
                if(NetworkState.isConnected(getActivity().getApplicationContext())) {
                    new MoviesTask().execute(generateURLForDecreasingRating(1, key));
                }
                else {
                    displayToast(R.string.network_unreachable);
                }
                displayingMoviesByRatings = true;
            }
        } else if(id == R.id.sort_pop) {
            if(displayingMoviesByRatings || displayingStoredMovies) {
                clearList();
                mMoviesListAdapter.setDisplayMode(false);
                displayingStoredMovies = false;
                if(NetworkState.isConnected(getActivity().getApplicationContext())) {
                    new MoviesTask().execute(generateURLForDecreasingPopularity(1, key));
                } else {
                    displayToast(R.string.network_unreachable);
                }
                displayingMoviesByRatings = false;
            }

        } else if(id == R.id.show_fav) {
            if(!displayingStoredMovies) {
                clearList();
                getStoredMovies();
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
