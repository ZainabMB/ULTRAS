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

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public SearchComponent(FixtureRepository fixtureRepository,
                           TeamRepository teamRepository) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("position", "relative").set("width", "100%");

        // ── Search bar ────────────────────────────
        HorizontalLayout searchBar = new HorizontalLayout();
        searchBar.setWidthFull();
        searchBar.setAlignItems(Alignment.CENTER);
        searchBar.setPadding(false);
        searchBar.setSpacing(false);
        searchBar.getStyle()
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "8px")
                .set("background-color", DARK_CARD)
                .set("padding", "0 10px")
                .set("gap", "6px");

        Span searchIcon = new Span("🔍");
        searchIcon.getStyle().set("font-size", "13px").set("color", GREY_TEXT);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search fixtures...");
        searchField.setWidthFull();
        searchField.getStyle()
                .set("border", "none")
                .set("font-size", "13px")
                .set("--lumo-contrast-10pct", "transparent")
                .set("--lumo-base-color", DARK_CARD)
                .set("--lumo-body-text-color", WHITE)
                .set("--vaadin-input-field-background", DARK_CARD)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-placeholder-color", GREY_TEXT);

        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchBar.add(searchIcon, searchField);

        // ── Dropdown ──────────────────────────────
        Div dropdown = new Div();
        dropdown.getStyle()
                .set("position", "absolute")
                .set("top", "calc(100% + 4px)")
                .set("left", "0")
                .set("right", "0")
                .set("background-color", DARK_CARD)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.4)")
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

            String normalised = raw.toLowerCase().replace(" vs ", " ").trim();
            String[] words = normalised.split("\\s+");

            if (words.length >= 2) {
                int mid = words.length / 2;
                StringBuilder t1 = new StringBuilder();
                StringBuilder t2 = new StringBuilder();
                for (int i = 0; i < mid; i++) t1.append(words[i]).append(" ");
                for (int i = mid; i < words.length; i++) t2.append(words[i]).append(" ");

                List<Fixture> bothResults = fixtureRepository.searchByBothTeams(
                        t1.toString().trim(), t2.toString().trim());

                results = !bothResults.isEmpty() ? bothResults
                        : fixtureRepository.searchByTeamName(raw);
            } else {
                results = fixtureRepository.searchByTeamName(raw);
            }

            dropdown.removeAll();

            if (results.isEmpty()) {
                Div empty = new Div();
                empty.getStyle()
                        .set("padding", "12px 16px")
                        .set("color", GREY_TEXT)
                        .set("font-size", "13px");
                empty.add(new Span("No fixtures found for \"" + raw + "\""));
                dropdown.add(empty);
            } else {
                results.stream().limit(8).forEach(fixture ->
                        dropdown.add(buildResultRow(fixture, teamRepository, searchField, dropdown)));
            }

            dropdown.getStyle().set("display", "block");
        });

        searchField.addBlurListener(e -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            dropdown.getStyle().set("display", "none");
        });

        searchField.addFocusListener(e -> {
            if (!searchField.getValue().trim().isEmpty())
                dropdown.getStyle().set("display", "block");
        });

        add(searchBar, dropdown);
    }

    private Div buildResultRow(Fixture fixture, TeamRepository teamRepository,
                               TextField searchField, Div dropdown) {
        Div row = new Div();
        row.getStyle()
                .set("padding", "10px 14px")
                .set("cursor", "pointer")
                .set("border-bottom", "1px solid " + BORDER)
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("background-color", DARK_CARD);

        row.getElement().addEventListener("mouseover", e ->
                row.getStyle().set("background-color", "#303036"));
        row.getElement().addEventListener("mouseout", e ->
                row.getStyle().set("background-color", DARK_CARD));

        String homeName = teamRepository.findById(fixture.getHomeTeamId())
                .map(Team::getTeamName).orElse("?");
        String awayName = teamRepository.findById(fixture.getAwayTeamId())
                .map(Team::getTeamName).orElse("?");

        Div left = new Div();
        left.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");

        Span matchLabel = new Span(homeName + " vs " + awayName);
        matchLabel.getStyle().set("font-size", "13px").set("font-weight", "bold")
                .set("color", WHITE);

        Span dateLabel = new Span(fixture.getDate() != null ? fixture.getDate().toString() : "");
        dateLabel.getStyle().set("font-size", "11px").set("color", GREY_TEXT);

        left.add(matchLabel, dateLabel);

        Span score = new Span(fixture.getHomeScore() + " - " + fixture.getAwayScore());
        score.getStyle().set("font-size", "13px").set("font-weight", "bold")
                .set("color", BLUE);

        row.add(left, score);

        row.addClickListener(e -> {
            dropdown.getStyle().set("display", "none");
            searchField.clear();
            UI.getCurrent().navigate("fixture/" + fixture.getFixtureId());
        });

        return row;
    }
}