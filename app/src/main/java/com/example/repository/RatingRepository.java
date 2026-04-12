package com.example.repository;

import com.example.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Get user's rating for a fixture
    Optional<Rating> findByUserIdAndFixtureIdAndTeamIdIsNull(Long userId, Long fixtureId);

    // Get user's rating for a team in a fixture
    Optional<Rating> findByUserIdAndFixtureIdAndTeamId(Long userId, Long fixtureId, Long teamId);

    // Average rating for a fixture
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.fixtureId = :fixtureId AND r.teamId IS NULL")
    Double findAverageFixtureRating(@Param("fixtureId") Long fixtureId);

    // Average rating for a team in a fixture
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.fixtureId = :fixtureId AND r.teamId = :teamId")
    Double findAverageTeamRating(@Param("fixtureId") Long fixtureId, @Param("teamId") Long teamId);
}