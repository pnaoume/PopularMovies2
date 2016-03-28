package com.paulnsoft.popularmovies2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements  MoviesGridFragment.Callback
{
private MoviesGridFragment moviesGridFragment;
    private Toolbar toolbar;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), MovieDetailFragment.TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }
        moviesGridFragment = ((MoviesGridFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movies_grid));
        moviesGridFragment.setDisplayMode(mTwoPane);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        moviesGridFragment.onCreateOptionsMenu(menu, getMenuInflater());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        moviesGridFragment.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onItemSelected(Bundle params) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(params);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_detail_container, fragment)
                .commit();

    }
}
