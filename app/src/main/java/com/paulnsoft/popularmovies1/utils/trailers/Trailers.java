package com.paulnsoft.popularmovies1.utils.trailers;

import java.util.ArrayList;
import java.util.List;

public class Trailers {

    private Integer id;
    private List<Result> results = new ArrayList<Result>();

    /**
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The results
     */
    public List<Result> getResults() {
        return results;
    }

    /**
     *
     * @param results
     * The results
     */
    public void setResults(List<Result> results) {
        this.results = results;
    }

}
