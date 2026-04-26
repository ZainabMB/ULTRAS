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

    // ─────────────────────────────────────────
    // TEAMS
    // ─────────────────────────────────────────

    public JsonNode getTeamsBySeason(Long seasonId) {
        String url = baseUrl + "/teams/seasons/" + seasonId + "?api_token=" + apiToken;
        return fetch(url);
    }

    // ─────────────────────────────────────────
    // FIXTURES
    // ─────────────────────────────────────────

    // Basic fixture detail — used for the feed card (team names, logos, score, state)
    public JsonNode getFixtureById(Long fixtureId) {
        String url = baseUrl + "/fixtures/" + fixtureId
                + "?api_token=" + apiToken
                + "&include=participants;scores;state;league";
        return fetch(url);
    }

    // Full fixture detail — used for match details tab
    // includes events (goals/cards/subs), lineups with player names and positions
    public JsonNode getFixtureFullDetail(Long fixtureId) {
        String url = baseUrl + "/fixtures/" + fixtureId
                + "?api_token=" + apiToken
                + "&include=participants;scores;state;league"
                + ";events.type;events.player"
                + ";lineups.player;lineups.position"
                + ";formations";
        return fetch(url);
    }

    // Used during sync — gets all fixtures for a season
    public JsonNode getFixturesBySeason(Long seasonId) {
        String url = baseUrl + "/seasons/" + seasonId
                + "?api_token=" + apiToken
                + "&include=fixtures.participants;fixtures.scores";
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
