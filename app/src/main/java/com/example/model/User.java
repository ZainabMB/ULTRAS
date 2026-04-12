package com.example.model;


import jakarta.persistence.*;

import java.time.ZonedDateTime;
@Entity
@Table(name = "user", schema = "ultras")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "fav_team_id")
    private Long favTeamId;

    @Column(name = "email", nullable= false)
    private String email;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    //getters and setters
    public Long getUserId() { return userId;}

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Long getFavTeamId() {
        return favTeamId;
    }

    public void setFavTeamId(Long favTeamId) {
        this.favTeamId = favTeamId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
