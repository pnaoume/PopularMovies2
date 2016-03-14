package com.paulnsoft.popularmovies2.utils.httprequests;
import com.paulnsoft.popularmovies2.utils.QueryResult;
import com.paulnsoft.popularmovies2.utils.reviews.Reviews;
import com.paulnsoft.popularmovies2.utils.trailers.Trailers;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface MoviesAPI {
    @GET("/3/discover/movie")
    public QueryResult getMovies( @Query("sort_by") String sortMethod/**/,
                          @Query("api_key") String key, @Query("page") Long page);

    @GET("/{id}/videos")
    public Trailers getTrailers(@Path("id") Long movieId,
                                @Query("api_key") String key);

    @GET("/{id}/reviews")
    public Reviews getReviews(@Path("id") Long movieId,
                              @Query("api_key") String key);
}
