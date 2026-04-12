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

    // User's highest rated team (team_id not null)
    @Query("SELECT r.teamId FROM Rating r WHERE r.userId = :userId AND r.teamId IS NOT NULL " +
            "GROUP BY r.teamId ORDER BY AVG(r.score) DESC")
    List<Long> findTopRatedTeamByUser(@Param("userId") Long userId);

    // User's highest rated fixture (team_id is null)
    @Query("SELECT r.fixtureId FROM Rating r WHERE r.userId = :userId AND r.teamId IS NULL " +
            "ORDER BY r.score DESC")
    List<Long> findTopRatedFixtureByUser(@Param("userId") Long userId);

    // Get all fixture ratings by a user (diary)
    List<Rating> findByUserIdAndTeamIdIsNull(Long userId);

    // ─────────────────────────────────────────
    // LEAGUE STATS
    // ─────────────────────────────────────────

    // Average rating of all fixtures in a league
    @Query("""
        SELECT AVG(r.score) FROM Rating r
        JOIN Fixture f ON r.fixtureId = f.fixtureId
        WHERE f.leagueId = :leagueId AND r.teamId IS NULL
        """)
    Double findAverageLeagueRating(@Param("leagueId") Long leagueId);

    // Top 3 highest rated teams in a league (by average team rating across fixtures)
    // Returns [teamId, avgScore] pairs
    @Query("""
        SELECT r.teamId, AVG(r.score) as avgScore FROM Rating r
        JOIN Fixture f ON r.fixtureId = f.fixtureId
        WHERE f.leagueId = :leagueId AND r.teamId IS NOT NULL
        GROUP BY r.teamId
        ORDER BY avgScore DESC
        """)
    List<Object[]> findTopRatedTeamsInLeague(@Param("leagueId") Long leagueId);

    // Highest rated head-to-head fixture pair in a league
    // Groups by home+away team combo, averages all fixture ratings between them
    // Returns [homeTeamId, awayTeamId, avgScore]
    @Query("""
        SELECT f.homeTeamId, f.awayTeamId, AVG(r.score) as avgScore FROM Rating r
        JOIN Fixture f ON r.fixtureId = f.fixtureId
        WHERE f.leagueId = :leagueId AND r.teamId IS NULL
        GROUP BY f.homeTeamId, f.awayTeamId
        ORDER BY avgScore DESC
        """)
    List<Object[]> findTopRatedHeadToHeadInLeague(@Param("leagueId") Long leagueId);
}