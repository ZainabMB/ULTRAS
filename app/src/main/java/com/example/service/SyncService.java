package com.example.service;

import com.example.model.Fixture;
import com.example.model.League;
import com.example.model.Team;
import com.example.repository.FixtureRepository;
import com.example.repository.LeagueRepository;
import com.example.repository.TeamRepository;
import tools.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.time.LocalDate;

@Service
public class SyncService {

    @Autowired
    private SportmonksService sportmonksService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private LeagueRepository leagueRepository;


    // Find your season ID at:
    // https://api.sportmonks.com/v3/football/seasons?api_token=YOUR_TOKEN


    @PostConstruct
    public void syncOnStartup() {
        System.out.println(">>> Starting Sportmonks sync...");

        // Fetch leagues once — reuse for syncLeagues and season loop
        JsonNode leaguesResponse = sportmonksService.getAllLeagues();
        JsonNode leagues = leaguesResponse.get("data");

        if (leagues == null || !leagues.isArray()) {
            System.out.println(">>> No leagues found.");
            return;
        }

        // Sync leagues first, passing the already-fetched data
        syncLeagues(leagues);

        // Then sync teams and fixtures per season
        for (JsonNode league : leagues) {
            JsonNode seasons = league.get("seasons");
            if (seasons == null) continue;

            for (JsonNode season : seasons) {
                String seasonName = season.get("name").asText();

                if ("2025/2026".equals(seasonName)) {
                    Long seasonId = season.get("id").asLong();
                    Long leagueId = league.get("id").asLong();
                    System.out.println(">>> Found season: " + seasonName + " in " + league.get("name").asText() + " (ID: " + seasonId + ")");
                    syncTeams(seasonId);
                    syncFixtures(seasonId, leagueId);
                }
            }
        }

        System.out.println(">>> Sync complete.");
    }

    // ─────────────────────────────────────────
    // SYNC LEAGUES
    // Takes already-fetched leagues data — no extra API call
    // ─────────────────────────────────────────
    public void syncLeagues(JsonNode leagues) {
        System.out.println(">>> Syncing leagues...");

        for (JsonNode league : leagues) {
            JsonNode seasons = league.get("seasons");
            if (seasons == null) continue;

            // Only save leagues that have a 2024/2025 season
            boolean has2024 = false;
            for (JsonNode season : seasons) {
                if ("2024/2025".equals(season.get("name").asText())) {
                    has2024 = true;
                    break;
                }
            }
            if (!has2024) continue;

            Long leagueId = league.get("id").asLong();
            String leagueName = league.get("name").asText();

            League l = leagueRepository.findById(leagueId).orElse(new League());
            l.setLeagueId(leagueId);
            l.setLeagueName(leagueName);
            l.setCreatedAt(ZonedDateTime.now());


            leagueRepository.save(l);
            System.out.println(">>> Saved league: " + leagueName + " (ID: " + leagueId + ")");
        }

    }

    // ─────────────────────────────────────────
    // SYNC TEAMS
    // ─────────────────────────────────────────
    public void syncTeams(Long seasonId) {
        try {
            JsonNode response = sportmonksService.getTeamsBySeason(seasonId);
            JsonNode teams = response.get("data");

            if (teams == null || !teams.isArray()) {
                System.out.println(">>> No teams found.");
                return;
            }

            for (JsonNode t : teams) {
                Long teamId = t.get("id").asLong();

                if (teamRepository.existsById(teamId)) continue;

                Team team = new Team();
                team.setTeamId(teamId);
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
    // Now also stores leagueId per fixture
    // ─────────────────────────────────────────
    public void syncFixtures(Long seasonId, Long leagueId) {
        try {
            JsonNode response = sportmonksService.getFixturesBySeason(seasonId);
            JsonNode fixtures = response.get("data").get("fixtures");

            if (fixtures == null || !fixtures.isArray()) {
                System.out.println(">>> No fixtures found.");
                return;
            }

            for (JsonNode f : fixtures) {
                Long fixtureId = f.get("id").asLong();

                // Only save completed fixtures
                if (f.has("state")) {
                    String stateName = f.get("state").get("short_name").asText();
                    if (!"FT".equals(stateName)) {
                        System.out.println(">>> Skipping fixture " + fixtureId + " — not finished (" + stateName + ")");
                        continue;
                    }
                } else {
                    // No state info — skip to be safe
                    continue;
                }

                if (fixtureRepository.existsById(fixtureId)) continue;


                Fixture fixture = new Fixture();
                fixture.setFixtureId(fixtureId);
                fixture.setLeagueId(leagueId);
                fixture.setCreatedAt(ZonedDateTime.now());

                // Date
                if (f.has("starting_at") && !f.get("starting_at").isNull()) {
                    String dateStr = f.get("starting_at").asText().substring(0, 10);
                    fixture.setDate(LocalDate.parse(dateStr));
                }

                // Home and away team IDs
                if (f.has("participants")) {
                    for (JsonNode p : f.get("participants")) {
                        String location = p.get("meta").get("location").asText();
                        Long teamId = p.get("id").asLong();
                        if ("home".equals(location)) {
                            fixture.setHomeTeamId(teamId);
                        } else if ("away".equals(location)) {
                            fixture.setAwayTeamId(teamId);
                        }
                    }
                }

                // Scores — only store if CURRENT and FT
                if (f.has("scores")) {
                    for (JsonNode score : f.get("scores")) {
                        boolean isCurrent = "CURRENT".equals(score.get("description").asText());
                        JsonNode scoreNode = score.get("score");

                        if (isCurrent && "home".equals(scoreNode.get("participant").asText())) {
                            JsonNode goals = scoreNode.get("goals");
                            if (goals != null && !goals.isNull()) {
                                fixture.setHomeScore(goals.asInt());
                            }
                        }

                        if (isCurrent && "away".equals(scoreNode.get("participant").asText())) {
                            JsonNode goals = scoreNode.get("goals");
                            if (goals != null && !goals.isNull()) {
                                fixture.setAwayScore(goals.asInt());
                            }
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