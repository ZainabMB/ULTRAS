package com.example.service;

import com.example.model.Rating;
import com.example.model.Review;
import com.example.model.dto.FixtureResponse;
import com.example.repository.RatingRepository;
import com.example.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// service/FixtureService.java
@Service
public class FixtureService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private SportmonksService sportmonksService;  // handles API calls

    // Get a fixture with its ratings and reviews + Sportmonks data merged
    public FixtureResponse getFixtureWithDetails(Long fixtureId) {

        // 1. Get user-generated data from YOUR DB
        List<Rating> ratings = ratingRepository
                .findByTargetTypeAndTargetId("fixture", fixtureId);

        List<Review> reviews = reviewRepository
                .findByTargetTypeAndTargetId("fixture", fixtureId);

        Double avgScore = ratings.stream()
                .mapToInt(r -> r.getScore())
                .average()
                .orElse(0.0);

        // 2. Get fixture details from Sportmonks
        SportmonksFixture fixture = sportmonksService.getFixtureById(fixtureId);

        // 3. Merge into one response object
        FixtureResponse response = new FixtureResponse();
        response.setFixtureId(fixtureId);
        response.setHometeam(fixture.getParticipants().get(0).getName());
        response.setAwayTeam(fixture.getParticipants().get(1).getName());
        response.setScore(fixture.getScores());
        response.setDate(fixture.getStartingAt());
        response.setAverageRating(avgScore);
        response.setReviews(reviews);

        return response;
    }
}
