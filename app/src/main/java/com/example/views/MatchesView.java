package com.example.views;

import com.example.model.dto.FixtureResponse;
import com.example.repository.FixtureRepository;
import com.example.repository.TeamRepository;
import com.example.service.FixtureService;
import com.example.views.components.SearchComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

@Route("matches")
@PageTitle("Ultras - Matches")
public class MatchesView extends VerticalLayout {

    // ── Theme ─────────────────────────────────────
    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    private final FixtureService fixtureService;
    private final FixtureRepository fixtureRepository;
    private final TeamRepository teamRepository;

    public MatchesView(@Autowired FixtureService fixtureService,
                       FixtureRepository fixtureRepository,
                       TeamRepository teamRepository) {
        this.fixtureService = fixtureService;
        this.fixtureRepository = fixtureRepository;
        this.teamRepository = teamRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", DARK);

        buildView();
    }

    private void buildView() {

        // ── Nav bar ───────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "14px 20px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("gap", "16px");

        // Left: profile icon
        Button profileBtn = new Button("👤");
        profileBtn.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("padding", "0").set("color", GREY_TEXT)
                .set("min-width", "36px").set("flex-shrink", "0");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        // Centre: ULTRAS title
        Span navTitle = new Span("ULTRAS");
        navTitle.getStyle()
                .set("font-weight", "bold").set("font-size", "18px")
                .set("letter-spacing", "4px").set("color", WHITE)
                .set("flex-shrink", "0");

        // Right: search — styled grey to match dark theme
        SearchComponent search = new SearchComponent(fixtureRepository, teamRepository);
        search.getStyle()
                .set("max-width", "220px")
                .set("flex-shrink", "0")
                .set("--lumo-contrast-10pct", "#3a3a3e")
                .set("--lumo-base-color", "#2a2a2e")
                .set("--lumo-body-text-color", GREY_TEXT)
                .set("--lumo-secondary-text-color", GREY_TEXT);

        // Spacer pushes title to true centre
        Div leftGroup = new Div();
        leftGroup.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "12px").set("flex", "1");
        leftGroup.add(profileBtn, navTitle);

        Div rightGroup = new Div();
        rightGroup.getStyle().set("display", "flex").set("align-items", "center")
                .set("flex", "1").set("justify-content", "flex-end");
        rightGroup.add(search);

        nav.add(leftGroup, rightGroup);

        // ── Tabs ──────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "10px 16px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("gap", "8px");

        Button matchesTab = new Button("Matches");
        matchesTab.getStyle()
                .set("background-color", BLUE)
                .set("color", WHITE)
                .set("border", "none")
                .set("border-radius", "20px")
                .set("padding", "6px 20px")
                .set("cursor", "pointer")
                .set("font-weight", "600");

        Button leaguesTab = new Button("Leagues");
        leaguesTab.getStyle()
                .set("background-color", "transparent")
                .set("color", GREY_TEXT)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "20px")
                .set("padding", "6px 20px")
                .set("cursor", "pointer");
        leaguesTab.addClickListener(e -> UI.getCurrent().navigate("leagues"));

        tabs.add(matchesTab, leaguesTab);

        // ── Filter bar ────────────────────────────
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(Alignment.CENTER);
        filterBar.getStyle()
                .set("padding", "10px 16px")
                .set("background-color", DARK)
                .set("gap", "8px");

        Button todayBtn = buildFilterButton("Today", true);
        Button yesterdayBtn = buildFilterButton("Yesterday", false);

        DatePicker datePicker = new DatePicker();
        datePicker.setPlaceholder("Pick a date");
        datePicker.getStyle()
                .set("--vaadin-input-field-background", DARK_CARD)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-placeholder-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER)
                .set("--lumo-contrast-10pct", BORDER)
                .set("--lumo-base-color", DARK_CARD)
                .set("--lumo-body-text-color", WHITE)
                .set("--lumo-secondary-text-color", GREY_TEXT)
                .set("color", GREY_TEXT)
                .set("font-size", "13px");

        LocalDate earliest = fixtureService.getEarliestFixtureDate();
        LocalDate today = LocalDate.now();
        datePicker.setMin(earliest);
        datePicker.setMax(today);

        filterBar.add(todayBtn, yesterdayBtn, datePicker);

        // ── Feed area ─────────────────────────────
        VerticalLayout feed = new VerticalLayout();
        feed.setWidthFull();
        feed.setPadding(false);
        feed.setSpacing(false);
        feed.getStyle()
                .set("padding", "16px")
                .set("gap", "12px")
                .set("background-color", DARK);

        loadFixturesForDate(today, feed);

        // Filter button active state toggles
        todayBtn.addClickListener(e -> {
            setFilterActive(todayBtn, true);
            setFilterActive(yesterdayBtn, false);
            datePicker.clear();
            loadFixturesForDate(today, feed);
        });

        yesterdayBtn.addClickListener(e -> {
            setFilterActive(todayBtn, false);
            setFilterActive(yesterdayBtn, true);
            datePicker.clear();
            loadFixturesForDate(today.minusDays(1), feed);
        });

        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                setFilterActive(todayBtn, false);
                setFilterActive(yesterdayBtn, false);
                loadFixturesForDate(e.getValue(), feed);
            }
        });

        add(nav, tabs, filterBar, feed);
    }

    // ── Load fixtures for date ────────────────────
    private void loadFixturesForDate(LocalDate date, VerticalLayout feed) {
        feed.removeAll();

        List<FixtureResponse> fixtures = fixtureService.getFixturesByDate(date);

        if (fixtures.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("text-align", "center").set("padding", "48px 16px");

            Span emptyMsg = new Span("No fixtures found for this date");
            emptyMsg.getStyle().set("color", GREY_TEXT).set("font-size", "14px");

            emptyState.add(emptyMsg);
            feed.add(emptyState);
            return;
        }

        // Group by league
        Map<String, List<FixtureResponse>> byLeague = new LinkedHashMap<>();
        for (FixtureResponse f : fixtures) {
            String league = f.getLeagueName() != null ? f.getLeagueName() : "Unknown League";
            byLeague.computeIfAbsent(league, k -> new ArrayList<>()).add(f);
        }

        for (Map.Entry<String, List<FixtureResponse>> entry : byLeague.entrySet()) {
            Div leagueGroup = new Div();
            leagueGroup.setWidthFull();
            leagueGroup.getStyle()
                    .set("background-color", DARK_CARD)
                    .set("border", "1px solid " + BORDER)
                    .set("border-radius", "10px")
                    .set("overflow", "hidden")
                    .set("margin-bottom", "12px");

            // League header
            Div leagueHeader = new Div();
            leagueHeader.getStyle()
                    .set("padding", "8px 14px")
                    .set("border-bottom", "1px solid " + BORDER)
                    .set("background-color", "#222226")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("gap", "8px");

            // Blue left accent bar
            Div accentBar = new Div();
            accentBar.getStyle()
                    .set("width", "3px").set("height", "16px")
                    .set("background-color", BLUE)
                    .set("border-radius", "2px");

            Span leagueName = new Span(entry.getKey());
            leagueName.getStyle()
                    .set("font-weight", "bold").set("font-size", "12px")
                    .set("color", WHITE).set("letter-spacing", "0.5px");

            leagueHeader.add(accentBar, leagueName);
            leagueGroup.add(leagueHeader);

            for (FixtureResponse fixture : entry.getValue()) {
                leagueGroup.add(buildMatchCard(fixture));
            }

            feed.add(leagueGroup);
        }
    }

    // ── Match card ────────────────────────────────
    private Div buildMatchCard(FixtureResponse fixture) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px 14px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px")
                .set("transition", "background-color 0.15s");

        card.getElement().addEventListener("mouseover",
                e -> card.getStyle().set("background-color", "#303036"));
        card.getElement().addEventListener("mouseout",
                e -> card.getStyle().set("background-color", "transparent"));

        card.addClickListener(e ->
                UI.getCurrent().navigate("fixture/" + fixture.getFixtureId() + "?from=matches"));

        // Status/date column
        Div statusCol = new Div();
        statusCol.getStyle()
                .set("min-width", "52px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "2px");

        Span dateSpan = new Span(fixture.getDate() != null
                ? fixture.getDate().toString().substring(5) : ""); // MM-DD
        dateSpan.getStyle().set("font-size", "10px").set("color", GREY_TEXT);

        Span stateSpan = new Span(fixture.getState() != null ? fixture.getState() : "FT");
        stateSpan.getStyle()
                .set("font-size", "11px").set("font-weight", "bold")
                .set("color", BLUE).set("letter-spacing", "0.5px");

        statusCol.add(dateSpan, stateSpan);

        // Divider
        Div divider = new Div();
        divider.getStyle()
                .set("width", "1px").set("height", "36px")
                .set("background-color", BORDER);

        // Teams column
        Div teamsCol = new Div();
        teamsCol.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "4px");

        teamsCol.add(
                buildTeamRow(fixture.getHomeTeamName(), fixture.getHomeScore()),
                buildTeamRow(fixture.getAwayTeamName(), fixture.getAwayScore())
        );

        card.add(statusCol, divider, teamsCol);
        return card;
    }

    private Div buildTeamRow(String teamName, int score) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center");

        Span name = new Span(teamName);
        name.getStyle().set("font-size", "13px").set("color", WHITE);

        Span scoreSpan = new Span(String.valueOf(score));
        scoreSpan.getStyle()
                .set("font-size", "13px").set("font-weight", "bold")
                .set("color", WHITE);

        row.add(name, scoreSpan);
        return row;
    }

    // ── Filter button helpers ─────────────────────
    private Button buildFilterButton(String label, boolean active) {
        Button btn = new Button(label);
        setFilterActive(btn, active);
        return btn;
    }

    private void setFilterActive(Button btn, boolean active) {
        if (btn == null) return;
        if (active) {
            btn.getStyle()
                    .set("background-color", BLUE)
                    .set("color", WHITE)
                    .set("border", "none")
                    .set("border-radius", "20px")
                    .set("padding", "6px 16px")
                    .set("font-size", "13px")
                    .set("font-weight", "600")
                    .set("cursor", "pointer");
        } else {
            btn.getStyle()
                    .set("background-color", "transparent")
                    .set("color", GREY_TEXT)
                    .set("border", "1px solid " + BORDER)
                    .set("border-radius", "20px")
                    .set("padding", "6px 16px")
                    .set("font-size", "13px")
                    .set("font-weight", "400")
                    .set("cursor", "pointer");
        }
    }
}