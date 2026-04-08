package com.example.repository;

import com.example.model.Fixture;
import com.example.model.dto.FixtureResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    SELECT f, th, ta
    FROM Fixture f
    JOIN Team th ON th.teamId = f.homeTeamId
    JOIN Team ta ON ta.teamId = f.awayTeamId
    WHERE f.date = :date
""")
        // Used for the matches feed screen — get all fixtures on a given date
    List<Object[]> findFixturesByDate(LocalDate date);


    //query for finding earliest date on the db for the datapicker
    @Query("SELECT MIN(f.date) FROM Fixture f WHERE f.leagueId = :leagueId")



    //----------find earliest dates for date pickers -------//

    //match page
    LocalDate findEarliestDate();

    //individual leagues
    LocalDate findEarliestDateByLeagueId(Long leagueId);

    //query for join on teamid from fixtures
    @Query("""
    SELECT f, th, ta
    FROM Fixture f
    JOIN Team th ON th.teamId = f.homeTeamId
    JOIN Team ta ON ta.teamId = f.awayTeamId
    WHERE f.leagueId = :leagueId AND f.date = :date
""")
    List<Object[]> findFixturesWithTeamsByLeagueIdAndDate(Long leagueId, LocalDate date); }




