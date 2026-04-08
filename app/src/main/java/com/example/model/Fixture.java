package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "fixture", schema = "ultras")
public class Fixture {

    @Id
    // set this directly from Sportmonks
    @Column(name = "fixture_id")
    private Long fixtureId;

    @Column(name = "home_team_id")
    private Long homeTeamId;

    @Column(name = "away_team_id")
    private Long awayTeamId;

    @Column(name = "home_score")
    private int homeScore;

    @Column(name = "away_score")
    private int awayScore;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    // Getters and setters
    public Long getFixtureId() { return fixtureId; }
    public void setFixtureId(Long fixtureId) { this.fixtureId = fixtureId; }

    public Long getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(Long homeTeamId) { this.homeTeamId = homeTeamId; }

    public Long getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(Long awayTeamId) { this.awayTeamId = awayTeamId; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getAwayScore() {return awayScore;}
    public void setAwayScore(int awayScore) {this.awayScore = awayScore;}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}