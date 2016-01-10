package com.paulnsoft.popularmovies1.utils;

import java.io.Serializable;

public class Result implements Serializable {
    public  boolean adult;
    public  String backdrop_path;
    public  int[] genre_ids;
    public  long id;
    public  String original_language;
    public  String original_title;
    public  String overview;
    public  String release_date;
    public  String poster_path;
    public  double popularity;
    public  String title;
    public  boolean video;
    public  double vote_average;
    public  long vote_count;

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public void setGenre_ids(int[] genre_ids) {
        this.genre_ids = genre_ids;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    public void setVote_count(long vote_count) {
        this.vote_count = vote_count;
    }

    public Result(boolean adult, String backdrop_path, int[] genre_ids, long id, String original_language, String original_title, String overview, String release_date, String poster_path, double popularity, String title, boolean video, double vote_average, long vote_count){
        this.adult = adult;
        this.backdrop_path = backdrop_path;
        this.genre_ids = genre_ids;
        this.id = id;
        this.original_language = original_language;
        this.original_title = original_title;
        this.overview = overview;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.popularity = popularity;
        this.title = title;
        this.video = video;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
    }
}