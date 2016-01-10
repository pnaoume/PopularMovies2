package com.paulnsoft.popularmovies1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.paulnsoft.popularmovies1.utils.NetworkState;
import com.paulnsoft.popularmovies1.utils.QueryResult;
import com.paulnsoft.popularmovies1.utils.Result;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MoviesGridFragment extends Fragment{
    private static String TAG = "PopMoviesGridFragment";
    private static final String CLASSIFICATION_KEY = "CLASSIFICATION_KEY";
    private static final int READ_EXTERNAL_STORAGE_RUNTIME_PERMISSION = 201;
    GridView mMovies;
    private static final String imagesPrefix = "http://image.tmdb.org/t/p/w185/";
    private static final String requestURL = "http://api.themoviedb.org/3/discover/movie?sort_by=";
    MoviesListAdapter mMoviesListAdapter;
    private boolean displayingMoviesByRatings;
    private long lastLoadedPage;
    private long totalNumberOfPages;
    private long totalNumberOfMovies;
    private  String key;

    private String generateURLForDecreasingPopularity(long page) {
        return requestURL+"popularity.desc&api_key="+key+"&page="+(int)page;
    }

    private String generateURLForDecreasingRating(long page) {
        return requestURL+"vote_average.desc&api_key="+key+"&page="+(int)page;
    }


    public class MoviesTask extends AsyncTask<String, Integer, String> {
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
                lastLoadedPage = queryResult.page;
                totalNumberOfMovies = queryResult.total_results;
                totalNumberOfPages = queryResult.total_pages;
                Log.i(TAG, "Fetched list is: " + queryResult.results.size() + " long");
                for (Result res : queryResult.results) {
                    addResult(res);
                }
            } else {
                displayToast(R.string.problem_getting_movies_list);
            }
        }
    }

    private void addResult(final Result result) {
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
        } else {
            displayingMoviesByRatings = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);
        mMovies = (GridView) rootView.findViewById(R.id.gridView);
        mMoviesListAdapter = new MoviesListAdapter(inflater);
        mMovies.setAdapter(mMoviesListAdapter);
        lastLoadedPage = 1;
        mMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Result res = mMoviesListAdapter.getMovie(position);
                if (res != null) {
                    Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                    intent.putExtra(MovieDetailActivity.MOVIE_EXTRA, res);
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
            readKeyAndRequestMovies();
        }
        return rootView;
    }

    private void requestMovies(boolean organizeByRatings) {
        if (NetworkState.isConnected(getActivity().getApplicationContext())) {
            if(organizeByRatings) {
                new MoviesTask().execute(generateURLForDecreasingRating(1));
            } else {
                new MoviesTask().execute(generateURLForDecreasingPopularity(1));
            }
        } else {
            displayToast(R.string.network_unreachable);
        }
    }

    private void readKeyAndRequestMovies() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File file = new File(externalStorageDirectory, "key.txt");
        Log.i(TAG, "Trying to find key: " + file.toString());
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String firstLine = br.readLine();
            key = firstLine;
            Log.i(TAG, "Key found");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Key not found" + e);
            key = null;
        } catch (IOException e) {
            Log.i(TAG, "Key not found" + e);
            key = null;
        }
        requestMovies(displayingMoviesByRatings);
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

    private void displayToast(final int stringRes) {
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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_rating) {
            if(!displayingMoviesByRatings) {
                clearList();
                if(NetworkState.isConnected(getActivity().getApplicationContext())) {
                    new MoviesTask().execute(generateURLForDecreasingRating(1));
                }
                else {
                    displayToast(R.string.network_unreachable);
                }
                displayingMoviesByRatings = true;
            }
        } else if(id == R.id.sort_pop) {
            if(displayingMoviesByRatings) {
                clearList();
                if(NetworkState.isConnected(getActivity().getApplicationContext())) {
                    new MoviesTask().execute(generateURLForDecreasingPopularity(1));
                } else {
                    displayToast(R.string.network_unreachable);
                }
                displayingMoviesByRatings = false;
            }

        }

        return super.onOptionsItemSelected(item);
    }


    private class MoviesListAdapter extends BaseAdapter {
        private ArrayList<Result> mMovies;
        private LayoutInflater mInflator;

        public Result getMovie(int position) {
            return mMovies.get(position);
        }

        public MoviesListAdapter(LayoutInflater inflator) {
            super();
            mMovies = new ArrayList<>();
            mInflator = inflator;
        }


        public void clear() {
            mMovies.clear();
        }

        @Override
        public int getCount() {
            return mMovies.size();
        }

        public void addResult(Result result){
            if(!mMovies.contains(result))
            {
                mMovies.add(result);
            }
        }

        @Override
        public Object getItem(int position) {
            return mMovies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i(TAG, "Getting view at position: " + position);
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.list_item_movie, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Result movie = mMovies.get(position);
            viewHolder.movieTitle.setText(movie.original_title);

            Picasso.with(getActivity().getApplicationContext()).load(imagesPrefix + movie.poster_path).
                    into(viewHolder.movieThumbNail, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Log.i(TAG, "Error loading image!");
                            viewHolder.movieThumbNail.setImageResource(R.mipmap.ic_launcher);

                        }
                    });
            if(((mMovies.size()-position) < 4) && (lastLoadedPage < totalNumberOfPages)) {
                if(displayingMoviesByRatings) {
                    if(NetworkState.isConnected(getActivity().getApplicationContext())) {
                        new MoviesTask().execute(generateURLForDecreasingRating(lastLoadedPage + 1));
                    } else {
                        displayToast(R.string.network_unreachable);
                    }
                } else {
                    if(NetworkState.isConnected(getActivity().getApplicationContext())) {
                        new MoviesTask().execute(generateURLForDecreasingPopularity(lastLoadedPage + 1));
                    }
                    else {
                        displayToast(R.string.network_unreachable);
                    }
                }
            }
            return convertView;
        }
    }
    static class ViewHolder {

        @Bind(R.id.imageView)
        ImageView movieThumbNail;

        @Bind(R.id.movie_title)
        TextView movieTitle;


        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
