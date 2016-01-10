package com.paulnsoft.popularmovies1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.paulnsoft.popularmovies1.utils.Result;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailActivity  extends AppCompatActivity {
    private static  final String TAG = "MovieDetailActivity";
    public static final String MOVIE_EXTRA = "MOVIE_EXTRA";
    private static final String imagesPrefix = "http://image.tmdb.org/t/p/w780/";

    @Bind(R.id.movie_title_display)
    TextView movieTitle;

    @Bind(R.id.movie_poster)
    ImageView poster;

    @Bind(R.id.movie_plot)
    TextView synopsis;

    @Bind(R.id.movie_release_date_display)
    TextView releaseDate;

    @Bind(R.id.movie_rating_display)
    TextView voteAverage;

    @Bind(R.id.navigationToolbar)
    Toolbar navToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);
        ButterKnife.bind(this);
        navToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Intent intent = getIntent();
        if(intent != null) {
            Result  result = (Result)intent.getSerializableExtra(MOVIE_EXTRA);
            movieTitle.setText(result.original_title);
            Picasso.with(getApplicationContext()).load(imagesPrefix + result.poster_path).
                    into(poster, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Log.i(TAG, "Error loading image!");
                            poster.setImageResource(R.mipmap.ic_launcher);

                        }
                    });
            synopsis.setText(result.overview);
            releaseDate.setText(result.release_date);
            voteAverage.setText(""+result.vote_average);
        }

    }
}
