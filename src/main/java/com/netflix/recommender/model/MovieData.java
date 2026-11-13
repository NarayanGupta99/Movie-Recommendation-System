package com.netflix.recommender.model;

import org.springframework.stereotype.Component;


public class MovieData {   //class representing each object of json movies
    private String title;
    private String description;

    public MovieData(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public MovieData() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
