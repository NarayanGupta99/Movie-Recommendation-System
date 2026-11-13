package com.netflix.recommender.controller;

import com.netflix.recommender.model.MovieMatch;

import com.netflix.recommender.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@CrossOrigin(origins = "http://localhost:63342")
public class MoviesController {

    private MovieService moviesService;

    @Autowired
    public MoviesController( MovieService moviesService){
        this.moviesService = moviesService;
    }

    @GetMapping("/search")
    public List<MovieMatch> Search(@RequestParam  String query) {

        return moviesService.search(query);
    }

    @GetMapping("/{title}/similar")
    public List<MovieMatch> similarMovies(@PathVariable String title) {
            return moviesService.similarMovies(title);
    }
}
