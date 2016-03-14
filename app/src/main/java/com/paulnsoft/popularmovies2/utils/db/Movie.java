package com.paulnsoft.popularmovies2.utils.db;

import java.io.Serializable;

public class Movie implements Serializable{
    private long id;
    private String title;
    private String plot;
    private String releaseDate;
    private double voteAverage;
    private byte[] smallImage;
    private byte[] bigImage;

    public Movie() {

    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public void setSmallImage(byte[] smallImage) {
        this.smallImage = smallImage;
    }

    public void setBigImage(byte[] bigImage) {
        this.bigImage = bigImage;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPlot() {
        return plot;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public byte[] getSmallImage() {
        return smallImage;
    }

    public byte[] getBigImage() {
        return bigImage;
    }
}
