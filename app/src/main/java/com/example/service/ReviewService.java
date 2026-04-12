package com.example.service;

import com.example.model.Review;
import com.example.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // Submit or update a review for a fixture
    public void submitReview(Long userId, Long fixtureId, String body) {
        Optional<Review> existing = reviewRepository
                .findByUserIdAndFixtureId(userId, fixtureId);

        Review review = existing.orElse(new Review());
        review.setUserId(userId);
        review.setFixtureId(fixtureId);
        review.setBody(body);
        review.setCreatedAt(ZonedDateTime.now());

        reviewRepository.save(review);
    }

    // Get a user's review for a fixture
    public Optional<Review> getUserReview(Long userId, Long fixtureId) {
        return reviewRepository.findByUserIdAndFixtureId(userId, fixtureId);
    }

    // Get all reviews for a fixture
    public List<Review> getFixtureReviews(Long fixtureId) {
        return reviewRepository.findByFixtureId(fixtureId);
    }

    // Get all reviews written by a user — for the Reviews page
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}