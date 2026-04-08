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

    public JsonNode getAllLeagues() {
        String url = baseUrl + "/leagues?api_token=" + apiToken + "&include=seasons";
        return fetch(url);
    }

    public JsonNode getLeagueById(Long leagueId) {
        String url = baseUrl + "/leagues/" + leagueId + "?api_token=" + apiToken;
        return fetch(url);
    }

    public JsonNode getStandings(Long seasonId) {
        String url = baseUrl + "/standings/seasons/" + seasonId + "?api_token=" + apiToken;
        return fetch(url);
    }

    // ─────────────────────────────────────────
    // TEAMS
    // ─────────────────────────────────────────

    public JsonNode getTeamById(Long teamId) {
        String url = baseUrl + "/teams/" + teamId + "?api_token=" + apiToken;
        return fetch(url);
    }

    public JsonNode getTeamsBySeason(Long seasonId) {
        String url = baseUrl + "/teams/seasons/" + seasonId + "?api_token=" + apiToken;
        return fetch(url);
    }

    // ─────────────────────────────────────────
    // FIXTURES
    // ─────────────────────────────────────────

    // Used for the matches feed — gets full fixture details
    // including team names, logos, scores, and state (FT/NS/LIVE)
    public JsonNode getFixtureById(Long fixtureId) {
        String url = baseUrl + "/fixtures/" + fixtureId
                + "?api_token=" + apiToken
                + "&include=participants;scores;state;league";
        return fetch(url);
    }

    // Used by date — for future live feed use
    public JsonNode getFixturesByDate(String date) {
        String url = baseUrl + "/fixtures/date/" + date
                + "?api_token=" + apiToken
                + "&include=participants;scores;state;league";
        return fetch(url);
    }

    // Used during sync — gets all fixtures for a season
    public JsonNode getFixturesBySeason(Long seasonId) {
        String url = baseUrl + "/seasons/" + seasonId
                + "?api_token=" + apiToken
                + "&include=fixtures.participants;fixtures.scores;fixtures.state";
        return fetch(url);
    }

    // ─────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────

    private JsonNode fetch(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Sportmonks API call failed: " + e.getMessage(), e);
        }
    }
}