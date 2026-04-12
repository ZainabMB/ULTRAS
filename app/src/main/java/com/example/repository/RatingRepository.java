package com.example.repository;

import com.example.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Get user's fixture rating (team_id is null)
    Optional<Rating> findByUserIdAndFixtureIdAndTeamIdIsNull(Long userId, Long fixtureId);

    // Get user's team rating for a specific fixture
    Optional<Rating> findByUserIdAndFixtureIdAndTeamId(Long userId, Long fixtureId, Long teamId);

    // Average rating for a fixture across all users
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.fixtureId = :fixtureId AND r.teamId IS NULL")
    Double findAverageFixtureRating(@Param("fixtureId") Long fixtureId);

    // Average rating for a team in a specific fixture
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.fixtureId = :fixtureId AND r.teamId = :teamId")
    Double findAverageTeamRating(@Param("fixtureId") Long fixtureId, @Param("teamId") Long teamId);

    // User's highest rated team (team_id not null) — returns teamId
    @Query("SELECT r.teamId FROM Rating r WHERE r.userId = :userId AND r.teamId IS NOT NULL " +
            "GROUP BY r.teamId ORDER BY AVG(r.score) DESC")
    java.util.List<Long> findTopRatedTeamByUser(@Param("userId") Long userId);

    // User's highest rated fixture (team_id is null) — returns fixtureId
    @Query("SELECT r.fixtureId FROM Rating r WHERE r.userId = :userId AND r.teamId IS NULL " +
            "ORDER BY r.score DESC")
    java.util.List<Long> findTopRatedFixtureByUser(@Param("userId") Long userId);
//individual rating fixture for each user: this will appear in profile-diary
    List<Rating> findByUserIdAndTeamIdIsNull(Long userId);
}