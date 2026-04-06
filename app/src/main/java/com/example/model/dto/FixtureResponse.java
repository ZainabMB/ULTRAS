package com.example.model.dto;

import com.example.model.Review;

import java.util.List;

public class FixtureResponse {
    private Long fixtureId;
    private String homeTeam;
    private String awayTeam;
    private String score;
    private String date;
    private Double averageRating;    // from  DB
    private List<Review> reviews;    // from  DB
    // getters and setters
}
