package com.example.views.components;

import com.example.model.Fixture;
import com.example.model.Team;
import com.example.repository.FixtureRepository;
import com.example.repository.TeamRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;

public class SearchComponent extends VerticalLayout {

    public SearchComponent(FixtureRepository fixtureRepository,
                           TeamRepository teamRepository) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("position", "relative").set("width", "100%");

        // ── Search bar with icon ──────────────────
        HorizontalLayout searchBar = new HorizontalLayout();
        searchBar.setWidthFull();
        searchBar.setAlignItems(Alignment.CENTER);
        searchBar.setPadding(false);
        searchBar.setSpacing(false);
        searchBar.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("background-color", "white")
                .set("padding", "0 10px")
                .set("gap", "6px");

        Span searchIcon = new Span("🔍");
        searchIcon.getStyle().set("font-size", "14px").set("color", "#999");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search fixtures...");
        searchField.setWidthFull();
        searchField.getStyle()
                .set("border", "none")
                .set("font-size", "13px")
                .set("--lumo-contrast-10pct", "transparent");

        // Fire on every keystroke — no enter needed
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        searchBar.add(searchIcon, searchField);

        // ── Dropdown ──────────────────────────────
        Div dropdown = new Div();
        dropdown.getStyle()
                .set("position", "absolute")
                .set("top", "calc(100% + 4px)")
                .set("left", "0")
                .set("right", "0")
                .set("background-color", "white")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.08)")
                .set("z-index", "100")
                .set("max-height", "320px")
                .set("overflow-y", "auto")
                .set("display", "none");

        // ── Search logic ──────────────────────────
        searchField.addValueChangeListener(e -> {
            String raw = e.getValue().trim();

            if (raw.length() < 2) {
                dropdown.removeAll();
                dropdown.getStyle().set("display", "none");
                return;
            }

            List<Fixture> results;

            // Check if query looks like two teams e.g. "Celtic Rangers" or "Celtic vs Rangers"
            String normalised = raw.toLowerCase().replace(" vs ", " ").trim();
            String[] words = normalised.split("\\s+");

            if (words.length >= 2) {
                // Try to find fixtures matching both parts of the query
                // e.g. "celtic rang" → team1="celtic rang first half", try both-team search
                // Split at midpoint of words to guess team1 vs team2
                int mid = words.length / 2;
                StringBuilder t1 = new StringBuilder();
                StringBuilder t2 = new StringBuilder();
                for (int i = 0; i < mid; i++) t1.append(words[i]).append(" ");
                for (int i = mid; i < words.length; i++) t2.append(words[i]).append(" ");

                List<Fixture> bothResults = fixtureRepository.searchByBothTeams(
                        t1.toString().trim(), t2.toString().trim());

                if (!bothResults.isEmpty()) {
                    results = bothResults;
                } else {
                    // Fall back to single team search on full query
                    results = fixtureRepository.searchByTeamName(raw);
                }
            } else {
                results = fixtureRepository.searchByTeamName(raw);
            }

            dropdown.removeAll();

            if (results.isEmpty()) {
                Div empty = new Div();
                empty.getStyle().set("padding", "12px 16px")
                        .set("color", "#999").set("font-size", "13px");
                empty.add(new Span("No fixtures found for \"" + raw + "\""));
                dropdown.add(empty);
            } else {
                results.stream().limit(8).forEach(fixture ->
                        dropdown.add(buildResultRow(fixture, teamRepository,
                                searchField, dropdown)));
            }

            dropdown.getStyle().set("display", "block");
        });

        searchField.addBlurListener(e -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            dropdown.getStyle().set("display", "none");
        });

        searchField.addFocusListener(e -> {
            if (!searchField.getValue().trim().isEmpty()) {
                dropdown.getStyle().set("display", "block");
            }
        });

        add(searchBar, dropdown);
    }

    private Div buildResultRow(Fixture fixture, TeamRepository teamRepository,
                               TextField searchField, Div dropdown) {
        Div row = new Div();
        row.getStyle()
                .set("padding", "10px 16px")
                .set("cursor", "pointer")
                .set("border-bottom", "1px solid #f5f5f5")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("background-color", "white");

        row.getElement().addEventListener("mouseover", e ->
                row.getStyle().set("background-color", "#f9f9f9"));
        row.getElement().addEventListener("mouseout", e ->
                row.getStyle().set("background-color", "white"));

        String homeName = teamRepository.findById(fixture.getHomeTeamId())
                .map(Team::getTeamName).orElse("?");
        String awayName = teamRepository.findById(fixture.getAwayTeamId())
                .map(Team::getTeamName).orElse("?");

        Div left = new Div();
        left.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");

        Span matchLabel = new Span(homeName + " vs " + awayName);
        matchLabel.getStyle().set("font-size", "13px").set("font-weight", "bold");

        Span dateLabel = new Span(fixture.getDate() != null ? fixture.getDate().toString() : "");
        dateLabel.getStyle().set("font-size", "11px").set("color", "#999");

        left.add(matchLabel, dateLabel);

        Span score = new Span(fixture.getHomeScore() + " - " + fixture.getAwayScore());
        score.getStyle().set("font-size", "13px").set("font-weight", "bold")
                .set("color", "#555");

        row.add(left, score);

        row.addClickListener(e -> {
            dropdown.getStyle().set("display", "none");
            searchField.clear();
            UI.getCurrent().navigate("fixture/" + fixture.getFixtureId());
        });

        return row;
    }
}