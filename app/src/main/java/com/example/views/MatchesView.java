package com.example.views;

import com.example.model.dto.FixtureResponse;
import com.example.service.FixtureService;
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

    private final FixtureService fixtureService;

    public MatchesView(@Autowired FixtureService fixtureService) {
        this.fixtureService = fixtureService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");

        buildView();
    }

    private void buildView() {

        // ───────────────────────────────────────────────
        // NAV BAR
        // ───────────────────────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", "white")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #e0e0e0");

        Button profileBtn = new Button("👤");
        profileBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-size", "18px");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        Span navTitle = new Span("ULTRAS");
        navTitle.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "16px")
                .set("flex", "1")
                .set("text-align", "center");

        Button searchBtn = new Button("🔍");
        searchBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-size", "18px");

        nav.add(profileBtn, navTitle, searchBtn);

        // ───────────────────────────────────────────────
        // TABS
        // ───────────────────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.getStyle()
                .set("background-color", "white")
                .set("padding", "8px 16px")
                .set("border-bottom", "1px solid #e0e0e0")
                .set("gap", "8px");

        Button matchesTab = new Button("Matches");
        matchesTab.getStyle()
                .set("background-color", "#1a1a1a")
                .set("color", "white")
                .set("border-radius", "20px")
                .set("padding", "6px 20px");

        Button leaguesTab = new Button("Leagues");
        leaguesTab.getStyle()
                .set("background-color", "white")
                .set("color", "#1a1a1a")
                .set("border", "1px solid #ccc")
                .set("border-radius", "20px")
                .set("padding", "6px 20px");
        leaguesTab.addClickListener(e -> UI.getCurrent().navigate("leagues"));

        tabs.add(matchesTab, leaguesTab);

        // ───────────────────────────────────────────────
        // FILTER BAR
        // ───────────────────────────────────────────────
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.getStyle()
                .set("padding", "8px 16px")
                .set("gap", "8px");

        Button todayBtn = new Button("Today");
        Button yesterdayBtn = new Button("Yesterday");

        DatePicker datePicker = new DatePicker();
        datePicker.setPlaceholder("Pick a date");

        LocalDate earliest = fixtureService.getEarliestFixtureDate();
        LocalDate today = LocalDate.now();

        datePicker.setMin(earliest);
        datePicker.setMax(today);

        filterBar.add(todayBtn, yesterdayBtn, datePicker);

        // ───────────────────────────────────────────────
        // FEED AREA
        // ───────────────────────────────────────────────
        VerticalLayout feed = new VerticalLayout();
        feed.setWidthFull();
        feed.setPadding(true);
        feed.setSpacing(false);
        feed.getStyle().set("gap", "12px");

        // Default load: today
        loadFixturesForDate(today, feed);

        todayBtn.addClickListener(e -> loadFixturesForDate(today, feed));
        yesterdayBtn.addClickListener(e -> loadFixturesForDate(today.minusDays(1), feed));
        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                loadFixturesForDate(e.getValue(), feed);
            }
        });

        add(nav, tabs, filterBar, feed);
    }

    // ───────────────────────────────────────────────
    // LOAD FIXTURES FOR A GIVEN DATE
    // ───────────────────────────────────────────────
    private void loadFixturesForDate(LocalDate date, VerticalLayout feed) {
        feed.removeAll();

        List<FixtureResponse> fixtures = fixtureService.getFixturesByDate(date);

        if (fixtures.isEmpty()) {
            Paragraph empty = new Paragraph("No fixtures found for this date.");
            empty.getStyle()
                    .set("color", "#999")
                    .set("text-align", "center")
                    .set("margin-top", "32px");
            feed.add(empty);
            return;
        }

        // Group fixtures by league
   Map<String, List<FixtureResponse>> byLeague = new LinkedHashMap<>();
      for (FixtureResponse f : fixtures) {
            String league = f.getLeagueName() != null ? f.getLeagueName() : "Unknown League";
            byLeague.computeIfAbsent(league, k -> new ArrayList<>()).add(f);
        }

        // Render each league group
        for (Map.Entry<String, List<FixtureResponse>> entry : byLeague.entrySet()) {
            Div leagueGroup = new Div();
            leagueGroup.setWidthFull();
            leagueGroup.getStyle()
                    .set("background-color", "white")
                    .set("border", "1px solid #e0e0e0")
                    .set("border-radius", "8px")
                    .set("overflow", "hidden")
                    .set("margin-bottom", "12px");

            // League header
            Div leagueHeader = new Div();
            leagueHeader.getStyle()
                    .set("padding", "8px 12px")
                    .set("border-bottom", "1px solid #f0f0f0")
                    .set("background-color", "#fafafa");

            Span leagueName = new Span(entry.getKey());
            leagueName.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "13px");

            leagueHeader.add(leagueName);
            leagueGroup.add(leagueHeader);

            // Match cards
            for (FixtureResponse fixture : entry.getValue()) {
                leagueGroup.add(buildMatchCard(fixture));
            }

            feed.add(leagueGroup);
        }
    }

    // ───────────────────────────────────────────────
    // MATCH CARD
    // ───────────────────────────────────────────────
    private Div buildMatchCard(FixtureResponse fixture) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "10px 12px")
                .set("border-bottom", "1px solid #f0f0f0")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        card.addClickListener(e ->
                UI.getCurrent().navigate("fixture/" + fixture.getFixtureId()));

        // Date + FT
        Div dateCol = new Div();
        dateCol.getStyle()
                .set("min-width", "60px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");

        dateCol.add(new Span(fixture.getDate().toString()));
        dateCol.add(new Span(fixture.getState() != null ? fixture.getState() : "FT"));

        // Divider
        Div divider = new Div();
        divider.getStyle()
                .set("width", "1px")
                .set("height", "36px")
                .set("background-color", "#e0e0e0");

        // Teams
        Div teamsCol = new Div();
        teamsCol.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "3px");

        teamsCol.add(buildTeamRow(fixture.getHomeTeamName(), fixture.getHomeScore()));
        teamsCol.add(buildTeamRow(fixture.getAwayTeamName(), fixture.getAwayScore()));

        card.add(dateCol, divider, teamsCol);
        return card;
    }

    private Div buildTeamRow(String teamName, int score) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between");

        row.add(new Span(teamName), new Span(String.valueOf(score)));
        return row;
    }
}
