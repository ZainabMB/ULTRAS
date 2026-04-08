package com.example.model.dto;

import com.example.model.Fixture;
import com.example.model.Team;
import com.example.repository.FixtureRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FixtureResponse {

    private Long fixtureId;
    private LocalDate date;
    private String homeTeamName;
    private String awayTeamName;
    private String homeTeamLogo;
    private String awayTeamLogo;
    private int homeScore;
    private int awayScore;
    private String state;       // FT, NS, LIVE
    private String leagueName;
    private Double averageRating;

    public Long getFixtureId() { return fixtureId; }
    public void setFixtureId(Long fixtureId) { this.fixtureId = fixtureId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getHomeTeamName() { return homeTeamName; }
    public void setHomeTeamName(String homeTeamName) { this.homeTeamName = homeTeamName; }

    public String getAwayTeamName() { return awayTeamName; }
    public void setAwayTeamName(String awayTeamName) { this.awayTeamName = awayTeamName; }

    public String getHomeTeamLogo() { return homeTeamLogo; }
    public void setHomeTeamLogo(String homeTeamLogo) { this.homeTeamLogo = homeTeamLogo; }

    public String getAwayTeamLogo() { return awayTeamLogo; }
    public void setAwayTeamLogo(String awayTeamLogo) { this.awayTeamLogo = awayTeamLogo; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLeagueName() { return leagueName; }
    public void setLeagueName(String leagueName) { this.leagueName = leagueName; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public static FixtureResponse fromEntity(Fixture fixture) {
        FixtureResponse dto = new FixtureResponse();
        dto.setFixtureId(fixture.getFixtureId());
        dto.setDate(fixture.getDate());
        dto.setHomeScore(fixture.getHomeScore());
        dto.setAwayScore(fixture.getAwayScore());
        dto.setLeagueId(fixture.getLeagueId());
        return dto;
    }

    private void setLeagueId(Long leagueId) {
    }



}