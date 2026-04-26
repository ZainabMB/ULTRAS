package com.example.views.components;

import com.example.model.Fixture;
import com.example.model.Team;
import com.example.model.dto.FixtureResponse;
import com.example.repository.FixtureRepository;
import com.example.repository.RatingRepository;
import com.example.repository.TeamRepository;
import com.example.service.SportmonksService;
import com.vaadin.flow.component.UI;
import tools.jackson.databind.JsonNode;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MatchDetailsComponent extends VerticalLayout {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    private static final int GOAL_TYPE         = 14;
    private static final int YELLOW_CARD_TYPE  = 18;
    private static final int RED_CARD_TYPE     = 15;
    private static final int SUBSTITUTION_TYPE = 16;

    public MatchDetailsComponent(FixtureResponse fixture,
                                 SportmonksService sportmonksService,
                                 RatingRepository ratingRepository,
                                 FixtureRepository fixtureRepository,
                                 TeamRepository teamRepository) {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle().set("gap", "12px").set("box-sizing", "border-box");

        try {
            JsonNode data = sportmonksService.getFixtureFullDetail(fixture.getFixtureId()).get("data");
            add(buildEventsSection(data, fixture));
            add(buildH2HSection(fixture, ratingRepository, fixtureRepository, teamRepository));
            add(buildLineupsSection(data, fixture));
        } catch (Exception e) {
            Span error = new Span("");
            error.getStyle().set("color", GREY_TEXT).set("font-size", "13px");
            add(error);
        }
    }

    // ── KEY EVENTS ────────────────────────────────
    private Div buildEventsSection(JsonNode data, FixtureResponse fixture) {
        Div card = buildCard("Key Events");

        if (!data.has("events") || data.get("events").isEmpty()) {
            card.add(noData("No events available."));
            return card;
        }

        List<JsonNode> events = new ArrayList<>();
        data.get("events").forEach(events::add);
        events.sort((a, b) -> {
            int minA = a.has("minute") ? a.get("minute").asInt() : 0;
            int minB = b.has("minute") ? b.get("minute").asInt() : 0;
            return Integer.compare(minA, minB);
        });

        for (JsonNode event : events) {
            int typeId = event.has("type_id") ? event.get("type_id").asInt() : 0;
            if (typeId != GOAL_TYPE && typeId != YELLOW_CARD_TYPE
                    && typeId != RED_CARD_TYPE && typeId != SUBSTITUTION_TYPE) continue;

            String minute = event.has("minute") ? event.get("minute").asText() + "'" : "?'";
            String playerName = "Unknown";
            if (event.has("player") && event.get("player").has("name")) {
                playerName = event.get("player").get("name").asText();
            }

            Long eventTeamId = event.has("participant_id") ? event.get("participant_id").asLong() : null;
            boolean isHome = eventTeamId != null && eventTeamId.equals(fixture.getHomeTeamId());

            String icon = switch (typeId) {
                case GOAL_TYPE         -> "⚽";
                case YELLOW_CARD_TYPE  -> "🟨";
                case RED_CARD_TYPE     -> "🟥";
                case SUBSTITUTION_TYPE -> "🔄";
                default                -> "•";
            };

            Div row = new Div();
            row.getStyle()
                    .set("display", "flex").set("align-items", "center")
                    .set("padding", "6px 0")
                    .set("border-bottom", "1px solid " + BORDER)
                    .set("gap", "6px").set("width", "100%");

            if (isHome) {
                Span iconSpan = new Span(icon); iconSpan.getStyle().set("font-size", "13px");
                Span minSpan = new Span(minute); minSpan.getStyle().set("font-size", "11px").set("color", GREY_TEXT).set("min-width", "28px");
                Span nameSpan = new Span(playerName); nameSpan.getStyle().set("font-size", "12px").set("color", WHITE).set("flex", "1");
                row.add(iconSpan, minSpan, nameSpan);
            } else {
                Span nameSpan = new Span(playerName); nameSpan.getStyle().set("font-size", "12px").set("color", WHITE).set("flex", "1").set("text-align", "right");
                Span minSpan = new Span(minute); minSpan.getStyle().set("font-size", "11px").set("color", GREY_TEXT).set("min-width", "28px").set("text-align", "right");
                Span iconSpan = new Span(icon); iconSpan.getStyle().set("font-size", "13px");
                row.add(nameSpan, minSpan, iconSpan);
            }

            card.add(row);
        }

        return card;
    }

    // ── HEAD TO HEAD ──────────────────────────────
    private Div buildH2HSection(FixtureResponse fixture,
                                RatingRepository ratingRepository,
                                FixtureRepository fixtureRepository,
                                TeamRepository teamRepository) {
        Div card = buildCard("Head to Head — Last 5");

        List<Fixture> h2hFixtures = fixtureRepository
                .findHeadToHead(fixture.getHomeTeamId(), fixture.getAwayTeamId());

        if (h2hFixtures.isEmpty()) {
            card.add(noData("No previous meetings found."));
            return card;
        }

        // Header row
        Div headerRow = new Div();
        headerRow.getStyle()
                .set("display", "flex").set("justify-content", "space-between")
                .set("padding", "0 0 8px 0")
                .set("border-bottom", "1px solid " + BORDER)
                .set("margin-bottom", "4px");

        Span homeHeader = new Span(fixture.getHomeTeamName());
        homeHeader.getStyle().set("font-size", "11px").set("font-weight", "bold").set("color", GREY_TEXT);

        Span awayHeader = new Span(fixture.getAwayTeamName());
        awayHeader.getStyle().set("font-size", "11px").set("font-weight", "bold").set("color", GREY_TEXT);

        headerRow.add(homeHeader, awayHeader);
        card.add(headerRow);

        int count = 0;
        for (Fixture f : h2hFixtures) {
            if (count >= 5) break;

            String homeName = teamRepository.findById(f.getHomeTeamId()).map(Team::getTeamName).orElse("?");
            String awayName = teamRepository.findById(f.getAwayTeamId()).map(Team::getTeamName).orElse("?");
            Double avgRating = ratingRepository.findAverageFixtureRating(f.getFixtureId());

            String starsStr = avgRating != null ? buildStars(avgRating) : "—";

            Div row = new Div();
            row.getStyle()
                    .set("display", "flex").set("align-items", "center")
                    .set("padding", "8px 0")
                    .set("border-bottom", "1px solid " + BORDER)
                    .set("cursor", "pointer").set("width", "100%");

            row.getElement().addEventListener("mouseover",
                    e -> row.getStyle().set("background-color", "#303036"));
            row.getElement().addEventListener("mouseout",
                    e -> row.getStyle().set("background-color", "transparent"));

            final Long fId = f.getFixtureId();
            row.addClickListener(e -> UI.getCurrent().navigate("fixture/" + fId + "?from=matches"));

            Span dateSpan = new Span(f.getDate() != null ? f.getDate().toString().substring(2) : "?");
            dateSpan.getStyle().set("font-size", "11px").set("color", GREY_TEXT).set("min-width", "80px");

            Span scoreSpan = new Span(homeName + "  " + f.getHomeScore() + " – " + f.getAwayScore() + "  " + awayName);
            scoreSpan.getStyle().set("font-size", "12px").set("color", WHITE)
                    .set("flex", "1").set("text-align", "center");

            Span starsSpan = new Span(starsStr);
            starsSpan.getStyle()
                    .set("font-size", "12px")
                    .set("color", avgRating != null ? "#f5b301" : GREY_TEXT)
                    .set("min-width", "60px").set("text-align", "right");

            row.add(dateSpan, scoreSpan, starsSpan);
            card.add(row);
            count++;
        }

        return card;
    }

    // ── LINEUPS ───────────────────────────────────
    private Div buildLineupsSection(JsonNode data, FixtureResponse fixture) {
        Div card = buildCard("Lineups");

        if (!data.has("lineups") || data.get("lineups").isEmpty()) {
            card.add(noData("Lineups not available."));
            return card;
        }

        List<JsonNode> homePlayers = new ArrayList<>();
        List<JsonNode> awayPlayers = new ArrayList<>();

        for (JsonNode player : data.get("lineups")) {
            Long teamId = player.has("team_id") ? player.get("team_id").asLong() : null;
            if (teamId == null) continue;
            if (teamId.equals(fixture.getHomeTeamId())) homePlayers.add(player);
            else if (teamId.equals(fixture.getAwayTeamId())) awayPlayers.add(player);
        }

        List<JsonNode> homeStarters = filterByType(homePlayers, 11);
        List<JsonNode> homeSubs     = filterByType(homePlayers, 12);
        List<JsonNode> awayStarters = filterByType(awayPlayers, 11);
        List<JsonNode> awaySubs     = filterByType(awayPlayers, 12);

        homeStarters.sort((a, b) -> getFormationField(a) - getFormationField(b));
        awayStarters.sort((a, b) -> getFormationField(a) - getFormationField(b));

        // Team headers
        Div teamHeaders = new Div();
        teamHeaders.getStyle().set("display", "flex").set("justify-content", "space-between")
                .set("margin-bottom", "8px").set("width", "100%");

        Span homeH = new Span(fixture.getHomeTeamName());
        homeH.getStyle().set("font-weight", "bold").set("font-size", "12px").set("color", WHITE);

        Span awayH = new Span(fixture.getAwayTeamName());
        awayH.getStyle().set("font-weight", "bold").set("font-size", "12px").set("color", WHITE);

        teamHeaders.add(homeH, awayH);
        card.add(teamHeaders);

        // Starters label
        Span startersLabel = new Span("Starting XI");
        startersLabel.getStyle().set("font-size", "11px").set("color", BLUE)
                .set("display", "block").set("margin", "4px 0");
        card.add(startersLabel);

        int maxStarters = Math.max(homeStarters.size(), awayStarters.size());
        for (int i = 0; i < maxStarters; i++) {
            card.add(buildPlayerRow(
                    i < homeStarters.size() ? homeStarters.get(i) : null,
                    i < awayStarters.size() ? awayStarters.get(i) : null));
        }

        if (!homeSubs.isEmpty() || !awaySubs.isEmpty()) {
            Span subsLabel = new Span("Substitutes");
            subsLabel.getStyle().set("font-size", "11px").set("color", BLUE)
                    .set("display", "block").set("margin", "10px 0 4px 0");
            card.add(subsLabel);

            int maxSubs = Math.max(homeSubs.size(), awaySubs.size());
            for (int i = 0; i < maxSubs; i++) {
                card.add(buildPlayerRow(
                        i < homeSubs.size() ? homeSubs.get(i) : null,
                        i < awaySubs.size() ? awaySubs.get(i) : null));
            }
        }

        return card;
    }

    private Div buildPlayerRow(JsonNode homePlayer, JsonNode awayPlayer) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("padding", "4px 0")
                .set("border-bottom", "1px solid #222226")
                .set("width", "100%");

        Div homeDiv = new Div();
        homeDiv.getStyle().set("flex", "1").set("display", "flex")
                .set("align-items", "center").set("gap", "4px");

        if (homePlayer != null) {
            Span jersey = new Span(getJersey(homePlayer));
            jersey.getStyle().set("font-size", "10px").set("color", GREY_TEXT).set("min-width", "18px");
            Span name = new Span(getPlayerName(homePlayer));
            name.getStyle().set("font-size", "11px").set("color", WHITE);
            homeDiv.add(jersey, name);
        }

        Div awayDiv = new Div();
        awayDiv.getStyle().set("flex", "1").set("display", "flex")
                .set("align-items", "center").set("justify-content", "flex-end").set("gap", "4px");

        if (awayPlayer != null) {
            Span name = new Span(getPlayerName(awayPlayer));
            name.getStyle().set("font-size", "11px").set("color", WHITE);
            Span jersey = new Span(getJersey(awayPlayer));
            jersey.getStyle().set("font-size", "10px").set("color", GREY_TEXT).set("min-width", "18px").set("text-align", "right");
            awayDiv.add(name, jersey);
        }

        row.add(homeDiv, awayDiv);
        return row;
    }

    // ── Card builder ──────────────────────────────
    private Div buildCard(String title) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "10px")
                .set("padding", "14px")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("margin-bottom", "10px");

        Div headerRow = new Div();
        headerRow.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "8px").set("margin-bottom", "10px");

        Div accentBar = new Div();
        accentBar.getStyle().set("width", "3px").set("height", "14px")
                .set("background-color", BLUE).set("border-radius", "2px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "bold").set("font-size", "12px")
                .set("color", GREY_TEXT).set("letter-spacing", "0.5px");

        headerRow.add(accentBar, titleSpan);
        card.add(headerRow);
        return card;
    }

    // ── Helpers ───────────────────────────────────
    private String buildStars(double avg) {
        int full = (int) avg;
        boolean half = (avg - full) >= 0.5;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < full; i++) sb.append("★");
        if (half) sb.append("½");
        for (int i = full + (half ? 1 : 0); i < 5; i++) sb.append("☆");
        return sb.toString();
    }

    private List<JsonNode> filterByType(List<JsonNode> players, int typeId) {
        return players.stream()
                .filter(p -> p.has("type_id") && p.get("type_id").asInt() == typeId)
                .collect(Collectors.toList());
    }

    private int getFormationField(JsonNode player) {
        return player.has("formation_field") && !player.get("formation_field").isNull()
                ? player.get("formation_field").asInt() : 99;
    }

    private String getPlayerName(JsonNode player) {
        if (player.has("player") && player.get("player").has("name"))
            return player.get("player").get("name").asText();
        return "Unknown";
    }

    private String getJersey(JsonNode player) {
        return player.has("jersey_number") && !player.get("jersey_number").isNull()
                ? player.get("jersey_number").asText() : "-";
    }

    private Span noData(String msg) {
        Span s = new Span(msg);
        s.getStyle().set("color", GREY_TEXT).set("font-size", "13px");
        return s;
    }
}