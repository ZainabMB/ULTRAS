package com.example.repository;

import com.example.model.Fixture;
import com.example.model.dto.FixtureResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    @Query("""
        SELECT f, th, ta
        FROM Fixture f
        JOIN Team th ON th.teamId = f.homeTeamId
        JOIN Team ta ON ta.teamId = f.awayTeamId
        WHERE f.leagueId = :leagueId
    """)
    List<Object[]> findFixturesWithTeamsByLeagueId(Long leagueId);

    @Query("""
    SELECT f, th, ta, l
    FROM Fixture f
    JOIN Team th ON th.teamId = f.homeTeamId
    JOIN Team ta ON ta.teamId = f.awayTeamId
    JOIN League l ON l.leagueId = f.leagueId
    WHERE f.date = :date
""")
    List<Object[]> findFixturesByDate(LocalDate date);

    // ---------- earliest date for MATCHES page ----------
    @Query("SELECT MIN(f.date) FROM Fixture f")
    LocalDate findEarliestDate();

    // ---------- earliest date for LEAGUE page ----------
    @Query("SELECT MIN(f.date) FROM Fixture f WHERE f.leagueId = :leagueId")
    LocalDate findEarliestDateByLeagueId(Long leagueId);

    @Query("""
    SELECT f, th, ta, l
    FROM Fixture f
    JOIN Team th ON th.teamId = f.homeTeamId
    JOIN Team ta ON ta.teamId = f.awayTeamId
    JOIN League l ON l.leagueId = f.leagueId
    WHERE f.leagueId = :leagueId AND f.date = :date
""")
    List<Object[]> findFixturesWithTeamsByLeagueIdAndDate(Long leagueId, LocalDate date);

    //query for a single fixture
    @Query("""
SELECT f, th, ta, l
FROM Fixture f
LEFT JOIN Team th ON th.teamId = f.homeTeamId
LEFT JOIN Team ta ON ta.teamId = f.awayTeamId
LEFT JOIN League l ON l.leagueId = f.leagueId
WHERE f.fixtureId = :fixtureId
""")
    List<Object[]> findFixtureDetail(Long fixtureId);


    @Query("""
        SELECT f FROM Fixture f
        WHERE (
            f.homeTeamId IN (
                SELECT t.teamId FROM Team t
                WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            OR
            f.awayTeamId IN (
                SELECT t.teamId FROM Team t
                WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :query, '%'))
            )
        )
        ORDER BY f.date DESC
        """)
    List<Fixture> searchByTeamName(@Param("query") String query);

    // Used for "Celtic vs Rangers" style search — match both teams
    @Query("""
        SELECT f FROM Fixture f
        WHERE (
            f.homeTeamId IN (
                SELECT t.teamId FROM Team t
                WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :team1, '%'))
            )
            AND
            f.awayTeamId IN (
                SELECT t.teamId FROM Team t
                WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :team2, '%'))
            )
        )
        OR (
            f.homeTeamId IN (
                SELECT t.teamId FROM Team t
                WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :team2, '%'))
            )
            AND
            f.awayTeamId IN (
                SELECT t.teamId FROM Team t
                WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :team1, '%'))
            )
        )
        ORDER BY f.date DESC
        """)
    List<Fixture> searchByBothTeams(@Param("team1") String team1, @Param("team2") String team2);
}

