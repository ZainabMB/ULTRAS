package com.example.service;

import com.example.model.Fixture;
import com.example.model.Team;
import com.example.model.dto.FixtureResponse;
import com.example.repository.FixtureRepository;
import com.example.repository.TeamRepository;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FixtureService {

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SportmonksService sportmonksService;

    // Get all finished fixtures, most recent first
    public List<FixtureResponse> getRecentFixtures() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<Fixture> fixtures = fixtureRepository.findAll();
        List<FixtureResponse> responses = new ArrayList<>();

        for (Fixture fixture : fixtures) {

            // FILTER FIRST (before API call)
            if (fixture.getDate() == null ||
                    !(fixture.getDate().isEqual(today) || fixture.getDate().isEqual(yesterday))) {
                continue;
            }

            try {
                JsonNode data = sportmonksService
                        .getFixtureById(fixture.getFixtureId())
                        .get("data");

                String state = "";
                if (data.has("state") && data.get("state").has("short_name")) {
                    state = data.get("state").get("short_name").asText();
                }

                if (!"FT".equals(state)) continue;

                FixtureResponse dto = new FixtureResponse();
                dto.setFixtureId(fixture.getFixtureId());
                dto.setDate(fixture.getDate());
                dto.setHomeScore(fixture.getHomeScore());
                dto.setAwayScore(fixture.getAwayScore());
                dto.setState(state);

                if (data.has("participants")) {
                    for (JsonNode p : data.get("participants")) {
                        String location = p.get("meta").get("location").asText();

                        if ("home".equals(location)) {
                            dto.setHomeTeamName(p.get("name").asText());
                            dto.setHomeTeamLogo(p.get("image_path").asText());
                        } else if ("away".equals(location)) {
                            dto.setAwayTeamName(p.get("name").asText());
                            dto.setAwayTeamLogo(p.get("image_path").asText());
                        }
                    }
                }

                if (data.has("league")) {
                    dto.setLeagueName(data.get("league").get("name").asText());
                }

                responses.add(dto);

            } catch (Exception e) {
                System.out.println(">>> Could not load fixture " + fixture.getFixtureId());
            }
        }

        responses.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return responses;
    }

    //fixtures using league id
    public List<FixtureResponse> getFixturesByLeagueId(Long leagueId) {

        List<Object[]> rows = fixtureRepository.findFixturesWithTeamsByLeagueId(leagueId);

        List<FixtureResponse> list = new ArrayList<>();

        for (Object[] row : rows) {
            Fixture f = (Fixture) row[0];
            Team home = (Team) row[1];
            Team away = (Team) row[2];

            FixtureResponse dto = new FixtureResponse();
            dto.setFixtureId(f.getFixtureId());
            dto.setDate(f.getDate());
            dto.setHomeScore(f.getHomeScore());
            dto.setAwayScore(f.getAwayScore());

            dto.setHomeTeamName(home.getTeamName());
            dto.setHomeTeamLogo(home.getLogo());

            dto.setAwayTeamName(away.getTeamName());
            dto.setAwayTeamLogo(away.getLogo());

            list.add(dto);
        }

        return list;
    }


    //method for getting fixtures depending on the league selected and date
    public List<FixtureResponse> getFixturesByLeagueAndDate(Long leagueId, LocalDate date) {

        List<Object[]> rows = fixtureRepository.findFixturesWithTeamsByLeagueIdAndDate(leagueId, date);

        List<FixtureResponse> list = new ArrayList<>();

        for (Object[] row : rows) {
            Fixture f = (Fixture) row[0];
            Team home = (Team) row[1];
            Team away = (Team) row[2];

            FixtureResponse dto = new FixtureResponse();
            dto.setFixtureId(f.getFixtureId());
            dto.setDate(f.getDate());
            dto.setHomeScore(f.getHomeScore());
            dto.setAwayScore(f.getAwayScore());

            dto.setHomeTeamName(home.getTeamName());
            dto.setHomeTeamLogo(home.getLogo());

            dto.setAwayTeamName(away.getTeamName());
            dto.setAwayTeamLogo(away.getLogo());

            list.add(dto);
        }

        return list;
    }


    //method for getting all fixtures depending on date
    public List<FixtureResponse> getFixturesByDate( LocalDate date) {

        List<Object[]> rows = fixtureRepository.findFixturesByDate(date);

        List<FixtureResponse> list = new ArrayList<>();

        for (Object[] row : rows) {
            Fixture f = (Fixture) row[0];
            Team home = (Team) row[1];
            Team away = (Team) row[2];

            FixtureResponse dto = new FixtureResponse();
            dto.setFixtureId(f.getFixtureId());
            dto.setDate(f.getDate());
            dto.setHomeScore(f.getHomeScore());
            dto.setAwayScore(f.getAwayScore());

            dto.setHomeTeamName(home.getTeamName());
            dto.setHomeTeamLogo(home.getLogo());

            dto.setAwayTeamName(away.getTeamName());
            dto.setAwayTeamLogo(away.getLogo());

            list.add(dto);
        }

        return list;
    }




    public LocalDate getEarliestFixtureDateForLeague(Long leagueId) {
        return fixtureRepository.findEarliestDateByLeagueId(leagueId);
    }

    public LocalDate getEarliestFixtureDate() {
        return fixtureRepository.findEarliestDate();
    }

    //method for date picker in league







}