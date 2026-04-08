package com.example.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SportmonksService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${sportmonks.api.base-url}")
    private String baseUrl;

    @Value("${sportmonks.api.token}")
    private String apiToken;

    // ─────────────────────────────────────────
    // LEAGUES
    // ─────────────────────────────────────────

    // Get all leagues
    public JsonNode getAllLeagues() {
        String url = baseUrl + "/leagues?api_token=" + apiToken + "&include=seasons";
        return fetch(url);
    }

    // Get a single league by ID
    public JsonNode getLeagueById(int leagueId) {
        String url = baseUrl + "/leagues/" + leagueId + "?api_token=" + apiToken;
        return fetch(url);
    }

    // Get league standings by season ID
    public JsonNode getStandings(Long seasonId) {
        String url = baseUrl + "/standings/seasons/" + seasonId
                + "?api_token=" + apiToken;
        return fetch(url);
    }

    // ─────────────────────────────────────────
    // TEAMS
    // ─────────────────────────────────────────

    // Get a single team by ID (includes logo, name, short code)
    public JsonNode getTeamById(Long teamId) {
        String url = baseUrl + "/teams/" + teamId + "?api_token=" + apiToken;
        return fetch(url);
    }

    // Get all teams in a season
    public JsonNode getTeamsBySeason(Long seasonId) {
        String url = baseUrl + "/teams/seasons/" + seasonId
                + "?api_token=" + apiToken;
        return fetch(url);
    }

    // ─────────────────────────────────────────
    // FIXTURES (MATCHES)
    // ─────────────────────────────────────────

    // Get fixtures by date - for the matches feed
    // e.g. date = "2025-12-12"
    public JsonNode getFixturesByDate(String date) {
        String url = baseUrl + "/fixtures/date/" + date
                + "?api_token=" + apiToken
                + "&include=participants;scores;league";
        return fetch(url);
    }

    // Get a single fixture by ID with full details
    // includes: both teams, score, events (goals/cards), match stats
    public JsonNode getFixtureById(Long fixtureId) {
        String url = baseUrl + "/fixtures/" + fixtureId
                + "?api_token=" + apiToken
                + "&include=participants;scores;events;statistics";
        return fetch(url);
    }

    // Get fixtures by league season
    public JsonNode getFixturesBySeason(Long seasonId) {
        String url = baseUrl + "/seasons/" + seasonId
                + "?api_token=" + apiToken
                + "&include=fixtures.participants;fixtures.scores";

        return fetch(url);
    }

    // ─────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────

    // All methods above use this — makes the HTTP call and returns parsed JSON
    private JsonNode fetch(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Sportmonks API call failed: " + e.getMessage(), e);
        }
    }
}