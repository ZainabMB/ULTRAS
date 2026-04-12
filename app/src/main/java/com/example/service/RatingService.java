package com.example.service;

import com.example.model.Rating;
import com.example.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    // ─────────────────────────────────────────
    // SUBMIT
    // ─────────────────────────────────────────

    // Rate a fixture (teamId = null)
    public void submitFixtureRating(Long userId, Long fixtureId, double score) {
        System.out.println(">>> submitFixtureRating called: userId=" + userId + " fixtureId=" + fixtureId + " score=" + score);

        Optional<Rating> existing = ratingRepository
                .findByUserIdAndFixtureIdAndTeamIdIsNull(userId, fixtureId);

        System.out.println(">>> existing rating found: " + existing.isPresent());

        Rating rating = existing.orElse(new Rating());
        rating.setUserId(userId);
        rating.setFixtureId(fixtureId);
        rating.setTeamId(null);
        rating.setScore(score);
        rating.setCreatedAt(ZonedDateTime.now());

        ratingRepository.save(rating);
        System.out.println(">>> rating saved successfully");
    }

    // Rate a team within a fixture (teamId = team being rated)
    public void submitTeamRating(Long userId, Long fixtureId, Long teamId, double score) {
        System.out.println(">>> submitTeamRating: userId=" + userId + " fixtureId=" + fixtureId + " teamId=" + teamId + " score=" + score);

        // Must use findByUserIdAndFixtureIdAndTeamId — NOT the IsNull version
        Optional<Rating> existing = ratingRepository
                .findByUserIdAndFixtureIdAndTeamId(userId, fixtureId, teamId);

        Rating rating = existing.orElse(new Rating());
        rating.setUserId(userId);
        rating.setFixtureId(fixtureId);
        rating.setTeamId(teamId);  // ← make sure this is set
        rating.setScore(score);
        rating.setCreatedAt(ZonedDateTime.now());

        ratingRepository.save(rating);
        System.out.println(">>> team rating saved for teamId=" + teamId);
    }
    // ─────────────────────────────────────────
    // GET USER RATING
    // ─────────────────────────────────────────

    // Get logged in user's fixture rating
    public Optional<Rating> getUserFixtureRating(Long userId, Long fixtureId) {
        return ratingRepository.findByUserIdAndFixtureIdAndTeamIdIsNull(userId, fixtureId);
    }

    // Get logged in user's team rating for a specific fixture
    public Optional<Rating> getUserTeamRating(Long userId, Long fixtureId, Long teamId) {
        return ratingRepository.findByUserIdAndFixtureIdAndTeamId(userId, fixtureId, teamId);
    }

    // ─────────────────────────────────────────
    // AVERAGES
    // ─────────────────────────────────────────

    // Average rating for a fixture across all users
    public Double getAverageFixtureRating(Long fixtureId) {
        return ratingRepository.findAverageFixtureRating(fixtureId);
    }

    // Average rating for a team in a specific fixture
    public Double getAverageTeamRating(Long fixtureId, Long teamId) {
        return ratingRepository.findAverageTeamRating(fixtureId, teamId);
    }
}