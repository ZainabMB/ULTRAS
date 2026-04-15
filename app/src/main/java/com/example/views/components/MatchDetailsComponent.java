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

public class MatchDetailsComponent extends VerticalLayout {

    // Event type IDs from Sportmonks
    private static final int GOAL_TYPE = 14;
    private static final int YELLOW_CARD_TYPE = 18;
    private static final int RED_CARD_TYPE = 15;
    private static final int SUBSTITUTION_TYPE = 16;

    public MatchDetailsComponent(FixtureResponse fixture,
                                 SportmonksService sportmonksService,
                                 RatingRepository ratingRepository,
                                 FixtureRepository fixtureRepository,
                                 TeamRepository teamRepository) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "16px");
        setWidthFull();

        try {
            JsonNode data = sportmonksService.getFixtureFullDetail(fixture.getFixtureId()).get("data");

            add(buildEventsSection(data, fixture));
            add(buildH2HSection(fixture, ratingRepository, fixtureRepository, teamRepository));
            add(buildLineupsSection(data, fixture));

        } catch (Exception e) {
            Paragraph error = new Paragraph("Could not load match details.");
            error.getStyle().set("color", "#999").set("font-size", "13px");
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

        // Sort by minute
        events.sort((a, b) -> {
            int minA = a.has("minute") ? a.get("minute").asInt() : 0;
            int minB = b.has("minute") ? b.get("minute").asInt() : 0;
            return Integer.compare(minA, minB);
        });

        for (JsonNode event : events) {
            int typeId = event.has("type_id") ? event.get("type_id").asInt() : 0;

            // Only show goals, cards and substitutions
            if (typeId != GOAL_TYPE && typeId != YELLOW_CARD_TYPE
                    && typeId != RED_CARD_TYPE && typeId != SUBSTITUTION_TYPE) continue;

            String minute = event.has("minute") ? event.get("minute").asText() + "'" : "?'";
            String playerName = "Unknown";
            if (event.has("player") && event.get("player").has("name")) {
                playerName = event.get("player").get("name").asText();
            }

            // Which team — home or away
            Long eventTeamId = event.has("participant_id")
                    ? event.get("participant_id").asLong() : null;
            boolean isHome = eventTeamId != null && eventTeamId.equals(fixture.getHomeTeamId());

            String icon = switch (typeId) {
                case GOAL_TYPE -> "⚽";
                case YELLOW_CARD_TYPE -> "🟨";
                case RED_CARD_TYPE -> "🟥";
                case SUBSTITUTION_TYPE -> "🔄";
                default -> "•";
            };

            Div row = new Div();
            row.getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("padding", "6px 0")
                    .set("border-bottom", "1px solid #f5f5f5")
                    .set("gap", "8px");

            if (isHome) {
                // Home: icon + player left-aligned
                Span iconSpan = new Span(icon);
                iconSpan.getStyle().set("font-size", "14px");

                Span minuteSpan = new Span(minute);
                minuteSpan.getStyle().set("font-size", "12px").set("color", "#999")
                        .set("min-width", "32px");

                Span nameSpan = new Span(playerName);
                nameSpan.getStyle().set("font-size", "13px").set("flex", "1");

                row.add(iconSpan, minuteSpan, nameSpan);
            } else {
                // Away: right-aligned
                Span nameSpan = new Span(playerName);
                nameSpan.getStyle().set("font-size", "13px").set("flex", "1")
                        .set("text-align", "right");

                Span minuteSpan = new Span(minute);
                minuteSpan.getStyle().set("font-size", "12px").set("color", "#999")
                        .set("min-width", "32px").set("text-align", "right");

                Span iconSpan = new Span(icon);
                iconSpan.getStyle().set("font-size", "14px");

                row.add(nameSpan, minuteSpan, iconSpan);
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

        // Header
        Div headerRow = new Div();
        headerRow.getStyle()
                .set("display", "flex").set("justify-content", "space-between")
                .set("padding", "0 0 8px 0").set("border-bottom", "1px solid #e0e0e0")
                .set("margin-bottom", "4px");

        Span homeHeader = new Span(fixture.getHomeTeamName());
        homeHeader.getStyle().set("font-size", "12px").set("font-weight", "bold")
                .set("color", "#555");

        Span awayHeader = new Span(fixture.getAwayTeamName());
        awayHeader.getStyle().set("font-size", "12px").set("font-weight", "bold")
                .set("color", "#555");

        headerRow.add(homeHeader, awayHeader);
        card.add(headerRow);

        int count = 0;
        for (Fixture f : h2hFixtures) {
            if (count >= 5) break;

            // Work out which team was home in this specific fixture
            String homeTeamName = teamRepository.findById(f.getHomeTeamId())
                    .map(Team::getTeamName).orElse("?");
            String awayTeamName = teamRepository.findById(f.getAwayTeamId())
                    .map(Team::getTeamName).orElse("?");

            // Get avg rating from DB
            Double avgRating = ratingRepository.findAverageFixtureRating(f.getFixtureId());

            // Stars string
            String starsStr = "Not rated";
            if (avgRating != null) {
                int full = (int) avgRating.doubleValue();
                boolean half = (avgRating - full) >= 0.5;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < full; i++) sb.append("★");
                if (half) sb.append("½");
                for (int i = full + (half ? 1 : 0); i < 5; i++) sb.append("☆");
                starsStr = sb.toString();
            }

            Div row = new Div();
            row.getStyle()
                    .set("display", "flex").set("align-items", "center")
                    .set("padding", "8px 0").set("border-bottom", "1px solid #f5f5f5")
                    .set("cursor", "pointer");

            // Clicking navigates to that fixture
            row.addClickListener(e ->
                    UI.getCurrent().navigate("fixture/" + f.getFixtureId() + "?from=matches"));

            Span dateSpan = new Span(f.getDate() != null ? f.getDate().toString() : "?");
            dateSpan.getStyle().set("font-size", "11px").set("color", "#999")
                    .set("min-width", "90px");

            // Show actual home vs away for that fixture
            Span scoreSpan = new Span(homeTeamName + "  " + f.getHomeScore() + " - " + f.getAwayScore() + "  " + awayTeamName);
            scoreSpan.getStyle().set("font-size", "13px").set("font-weight", "bold")
                    .set("flex", "1").set("text-align", "center");

            Span starsSpan = new Span(starsStr);
            starsSpan.getStyle()
                    .set("font-size", "13px")
                    .set("color", avgRating != null ? "#f5b301" : "#ccc")
                    .set("min-width", "80px").set("text-align", "right");

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

            if (teamId.equals(fixture.getHomeTeamId())) {
                homePlayers.add(player);
            } else if (teamId.equals(fixture.getAwayTeamId())) {
                awayPlayers.add(player);
            }
        }

        // Split starters (type_id=11) and subs (type_id=12) for each team
        List<JsonNode> homeStarters = filterByType(homePlayers, 11);
        List<JsonNode> homeSubs = filterByType(homePlayers, 12);
        List<JsonNode> awayStarters = filterByType(awayPlayers, 11);
        List<JsonNode> awaySubs = filterByType(awayPlayers, 12);

        // Sort starters by formation_field
        homeStarters.sort((a, b) -> getFormationField(a) - getFormationField(b));
        awayStarters.sort((a, b) -> getFormationField(a) - getFormationField(b));

        // Team headers
        HorizontalLayout teamHeaders = new HorizontalLayout();
        teamHeaders.setWidthFull();
        teamHeaders.getStyle().set("margin-bottom", "8px");

        Span homeHeader = new Span(fixture.getHomeTeamName());
        homeHeader.getStyle().set("font-weight", "bold").set("font-size", "13px")
                .set("flex", "1");

        Span awayHeader = new Span(fixture.getAwayTeamName());
        awayHeader.getStyle().set("font-weight", "bold").set("font-size", "13px")
                .set("flex", "1").set("text-align", "right");

        teamHeaders.add(homeHeader, awayHeader);
        card.add(teamHeaders);

        // Starters
        Span startersLabel = new Span("Starting XI");
        startersLabel.getStyle().set("font-size", "11px").set("color", "#999")
                .set("display", "block").set("margin", "8px 0 4px 0");
        card.add(startersLabel);

        int maxStarters = Math.max(homeStarters.size(), awayStarters.size());
        for (int i = 0; i < maxStarters; i++) {
            card.add(buildPlayerRow(
                    i < homeStarters.size() ? homeStarters.get(i) : null,
                    i < awayStarters.size() ? awayStarters.get(i) : null
            ));
        }

        // Subs
        if (!homeSubs.isEmpty() || !awaySubs.isEmpty()) {
            Span subsLabel = new Span("Substitutes");
            subsLabel.getStyle().set("font-size", "11px").set("color", "#999")
                    .set("display", "block").set("margin", "12px 0 4px 0");
            card.add(subsLabel);

            int maxSubs = Math.max(homeSubs.size(), awaySubs.size());
            for (int i = 0; i < maxSubs; i++) {
                card.add(buildPlayerRow(
                        i < homeSubs.size() ? homeSubs.get(i) : null,
                        i < awaySubs.size() ? awaySubs.get(i) : null
                ));
            }
        }

        return card;
    }

    // ── Player row — home left, away right ────────
    private Div buildPlayerRow(JsonNode homePlayer, JsonNode awayPlayer) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("padding", "4px 0").set("border-bottom", "1px solid #f9f9f9");

        // Home player
        Div homeDiv = new Div();
        homeDiv.getStyle().set("flex", "1").set("display", "flex")
                .set("align-items", "center").set("gap", "6px");

        if (homePlayer != null) {
            Span jersey = new Span(getJersey(homePlayer));
            jersey.getStyle().set("font-size", "11px").set("color", "#999")
                    .set("min-width", "20px");
            Span name = new Span(getPlayerName(homePlayer));
            name.getStyle().set("font-size", "12px");
            homeDiv.add(jersey, name);
        }

        // Away player
        Div awayDiv = new Div();
        awayDiv.getStyle().set("flex", "1").set("display", "flex")
                .set("align-items", "center").set("justify-content", "flex-end")
                .set("gap", "6px");

        if (awayPlayer != null) {
            Span name = new Span(getPlayerName(awayPlayer));
            name.getStyle().set("font-size", "12px");
            Span jersey = new Span(getJersey(awayPlayer));
            jersey.getStyle().set("font-size", "11px").set("color", "#999")
                    .set("min-width", "20px").set("text-align", "right");
            awayDiv.add(name, jersey);
        }

        row.add(homeDiv, awayDiv);
        return row;
    }

    // ── Helpers ───────────────────────────────────
    private List<JsonNode> filterByType(List<JsonNode> players, int typeId) {
        return players.stream()
                .filter(p -> p.has("type_id") && p.get("type_id").asInt() == typeId)
                .collect(java.util.stream.Collectors.toList());
    }

    private int getFormationField(JsonNode player) {
        return player.has("formation_field") && !player.get("formation_field").isNull()
                ? player.get("formation_field").asInt() : 99;
    }

    private String getPlayerName(JsonNode player) {
        if (player.has("player") && player.get("player").has("name")) {
            return player.get("player").get("name").asText();
        }
        return "Unknown";
    }

    private String getJersey(JsonNode player) {
        return player.has("jersey_number") && !player.get("jersey_number").isNull()
                ? player.get("jersey_number").asText() : "-";
    }

    private Div buildCard(String title) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("margin-bottom", "12px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "bold").set("font-size", "14px")
                .set("display", "block").set("margin-bottom", "12px")
                .set("color", "#333");

        card.add(titleSpan);
        return card;
    }

    private Span noData(String msg) {
        Span s = new Span(msg);
        s.getStyle().set("color", "#999").set("font-size", "13px");
        return s;
    }
}