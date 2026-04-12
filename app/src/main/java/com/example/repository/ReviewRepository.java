package com.example.repository;

import com.example.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // One review per user per fixture
    Optional<Review> findByUserIdAndFixtureId(Long userId, Long fixtureId);

    // All reviews for a fixture
    List<Review> findByFixtureId(Long fixtureId);
}