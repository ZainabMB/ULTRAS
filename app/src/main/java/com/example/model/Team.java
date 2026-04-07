package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teams", schema = "ultras")
public class Team {

    @Id
    // No @GeneratedValue — we set this directly from Sportmonks
    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "average_rating")
    private Float averageRating;

    @Column(name = "logo")
    private String logo;

    @Column(name = "short_code")
    private String shortCode;

    // Getters and setters
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Float getAverageRating() { return averageRating; }
    public void setAverageRating(Float averageRating) { this.averageRating = averageRating; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
}