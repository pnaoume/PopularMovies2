package com.paulnsoft.popularmovies2.utils.httprequests;
import com.paulnsoft.popularmovies2.utils.QueryResult;
import com.paulnsoft.popularmovies2.utils.reviews.Reviews;
import com.paulnsoft.popularmovies2.utils.trailers.Trailers;

import retrofit.http.GET;
import retrofit.http.Path;

public interface MoviesAPI {
    @GET("/3/discover/movie?sort_by={sorting}.desc&api_key={key}&page={page}")
    public QueryResult getMovies( @Path("sorting") String sortMethod,
                          @Path("key") String key, @Path("page") Long page);

    @GET("/{id}/videos?api_key={key}")
    public Trailers getTrailers(@Path("id") Long movieId,
                                @Path("key") String key);

    @GET("/{id}/reviews?api_key={key}")
    public Reviews getReviews(@Path("id") Long movieId,
                                 @Path("key") String key);
}
