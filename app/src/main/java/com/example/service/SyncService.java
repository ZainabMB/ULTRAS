package com.example.service;

import com.example.model.Fixture;
import com.example.model.Team;
import com.example.repository.FixtureRepository;
import com.example.repository.TeamRepository;
import tools.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SyncService {

    @Autowired
    private SportmonksService sportmonksService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FixtureRepository fixtureRepository;

    // Find your season ID at:
    // https://api.sportmonks.com/v3/football/seasons?api_token=YOUR_TOKEN
    private static final Long SEASON_ID = 23614L; // Scottish Prem 2024/25 - update this

    // Runs once automatically when the app starts
    @PostConstruct
    public void syncOnStartup() {
        System.out.println(">>> Starting Sportmonks sync...");
        syncTeams();
        syncFixtures();
        System.out.println(">>> Sync complete.");
    }

    // ─────────────────────────────────────────
    // SYNC TEAMS
    // ─────────────────────────────────────────
    public void syncTeams() {
        try {
            JsonNode response = sportmonksService.getTeamsBySeason(SEASON_ID);
            JsonNode teams = response.get("data");

            if (teams == null || !teams.isArray()) {
                System.out.println(">>> No teams found.");
                return;
            }

            for (JsonNode t : teams) {
                Long teamId = t.get("id").asLong();

                // Skip if already saved — Sportmonks ID is our primary key
                if (teamRepository.existsById(teamId)) {
                    continue;
                }

                Team team = new Team();
                team.setTeamId(teamId);  // Sportmonks ID goes directly into team_id
                team.setTeamName(t.get("name").asText());
                team.setShortCode(t.has("short_code") ? t.get("short_code").asText() : "");
                team.setLogo(t.has("image_path") ? t.get("image_path").asText() : "");

                teamRepository.save(team);
                System.out.println(">>> Saved team: " + team.getTeamName() + " (ID: " + teamId + ")");
            }

        } catch (Exception e) {
            System.out.println(">>> Team sync failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // SYNC FIXTURES
    // ─────────────────────────────────────────
    public void syncFixtures() {
        try {
            JsonNode response = sportmonksService.getFixturesBySeason(SEASON_ID);
            JsonNode fixtures = response.get("data");

            if (fixtures == null || !fixtures.isArray()) {
                System.out.println(">>> No fixtures found.");
                return;
            }

            for (JsonNode f : fixtures) {
                Long fixtureId = f.get("id").asLong();

                // Skip if already saved
                if (fixtureRepository.existsById(fixtureId)) {
                    continue;
                }

                Fixture fixture = new Fixture();
                fixture.setFixtureId(fixtureId); // Sportmonks ID goes directly into fixture_id

                // Parse date from "2025-12-12 15:00:00"
                if (f.has("starting_at")) {
                    String dateStr = f.get("starting_at").asText().substring(0, 10);
                    fixture.setDate(LocalDate.parse(dateStr));
                }

                // participants: position 1 = home, position 2 = away
                if (f.has("participants")) {
                    for (JsonNode p : f.get("participants")) {
                        int position = p.get("meta").get("position").asInt();
                        Long teamId = p.get("id").asLong(); // This IS our team_id in DB

                        if (position == 1) {
                            fixture.setHomeTeamId(teamId);
                        } else {
                            fixture.setAwayTeamId(teamId);
                        }
                    }
                }

                fixtureRepository.save(fixture);
                System.out.println(">>> Saved fixture ID: " + fixtureId);
            }

        } catch (Exception e) {
            System.out.println(">>> Fixture sync failed: " + e.getMessage());
        }
    }
}