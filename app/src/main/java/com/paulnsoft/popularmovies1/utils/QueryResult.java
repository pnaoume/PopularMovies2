package com.paulnsoft.popularmovies1.utils;

import java.io.Serializable;
import java.util.List;

public class QueryResult implements Serializable{
    public  long page;
    public List<Result> results;
    public  long total_pages;
    public  long total_results;

    public void setPage(long page) {
        this.page = page;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public void setTotal_pages(long total_pages) {
        this.total_pages = total_pages;
    }

    public void setTotal_results(long total_results) {
        this.total_results = total_results;
    }

    public QueryResult(long page,List<Result> results, long total_pages, long total_results){
        this.page = page;
        this.results = results;
        this.total_pages = total_pages;
        this.total_results = total_results;
    }

}