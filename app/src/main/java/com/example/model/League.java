package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "league", schema = "ultras")
public class League {

    @Id
    //set from sportmonks
    @Column(name = "league_id")
    private Long leagueId;

    @Column(name= "league_name")
    private String leagueName;

    @Column(name="average_rating")
    private double averageRating;

    @Column(name="highest_rated_team")
    private Long highestRatedTeam;

    @Column(name="highest_rated_fixture")
    private Long highestRatedFixture;


    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    //getters and setters


    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getHighestRatedTeam() {
        return highestRatedTeam;
    }

    public void setHighestRatedTeam(Long highestRatedTeam) {
        this.highestRatedTeam = highestRatedTeam;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public Long getHighestRatedFixture() {
        return highestRatedFixture;
    }

    public void setHighestRatedFixture(Long highestRatedFixture) {
        this.highestRatedFixture = highestRatedFixture;
    }
}
