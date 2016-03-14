package com.paulnsoft.popularmovies2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.paulnsoft.popularmovies2.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;

public class MainActivity extends AppCompatActivity {
private MoviesGridFragment moviesGridFragment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        moviesGridFragment = ((MoviesGridFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movies_grid));
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
}
