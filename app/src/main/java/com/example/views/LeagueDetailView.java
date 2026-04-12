package com.example.views;

import com.example.model.League;
import com.example.model.dto.FixtureResponse;
import com.example.service.FixtureService;
import com.example.service.LeagueService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.time.LocalDate;
import java.util.List;

@Route("league/:leagueId")
@PageTitle("Ultras - League")
public class LeagueDetailView extends VerticalLayout implements BeforeEnterObserver {


    private final LeagueService leagueService;
    private final FixtureService fixtureService;

    private Long leagueId;

    // UI components reused when switching tabs
    private VerticalLayout contentArea;
    private Button detailsTab;
    private Button matchesTab;

    public LeagueDetailView(LeagueService leagueService, FixtureService fixtureService) {
        this.leagueService = leagueService;
        this.fixtureService = fixtureService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idStr = event.getRouteParameters().get("leagueId").orElse(null);
        if (idStr == null) {
            event.forwardTo("leagues");
            return;
        }

        leagueId = Long.valueOf(idStr);
        buildView();
    }

    private void buildView() {
        removeAll();

        League league = leagueService.getLeagueById(leagueId);

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

        Button backBtn = new Button("←");
        backBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-size", "18px");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("leagues"));

        Span navTitle = new Span(league.getLeagueName());
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

        nav.add(backBtn, navTitle, searchBtn);

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

        detailsTab = new Button("Details");
        matchesTab = new Button("Matches");

        styleTab(detailsTab, true);
        styleTab(matchesTab, false);

        detailsTab.addClickListener(e -> showDetailsTab(league));
        matchesTab.addClickListener(e -> showMatchesTab());

        tabs.add(detailsTab, matchesTab);

        // ───────────────────────────────────────────────
        // CONTENT AREA (changes when switching tabs)
        // ───────────────────────────────────────────────
        contentArea = new VerticalLayout();
        contentArea.setWidthFull();
        contentArea.setPadding(true);
        contentArea.setSpacing(false);

        // Default tab
        showDetailsTab(league);

        add(nav, tabs, contentArea);
    }

    private void styleTab(Button tab, boolean active) {
        if (active) {
            tab.getStyle()
                    .set("background-color", "#1a1a1a")
                    .set("color", "white")
                    .set("border-radius", "20px")
                    .set("padding", "6px 20px");
        } else {
            tab.getStyle()
                    .set("background-color", "white")
                    .set("color", "#1a1a1a")
                    .set("border", "1px solid #ccc")
                    .set("border-radius", "20px")
                    .set("padding", "6px 20px");
        }
    }

    // ───────────────────────────────────────────────
    // DETAILS TAB
    // ───────────────────────────────────────────────
    private void showDetailsTab(League league) {
        contentArea.removeAll();
        styleTab(detailsTab, true);
        styleTab(matchesTab, false);

        H3 title = new H3("League Details");
        Paragraph info = new Paragraph("More league information can go here.");

        contentArea.add(title, info);
    }

    // ───────────────────────────────────────────────
    // MATCHES TAB
    // ───────────────────────────────────────────────
    private void showMatchesTab() {
        contentArea.removeAll();
        styleTab(detailsTab, false);
        styleTab(matchesTab, true);

        // FILTER BAR
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.getStyle().set("gap", "8px");

        Button todayBtn = new Button("Today");
        Button yesterdayBtn = new Button("Yesterday");

        DatePicker datePicker = new DatePicker();
        datePicker.setPlaceholder("Pick a date");

        // restrict date picker range
        LocalDate earliest = fixtureService.getEarliestFixtureDateForLeague(leagueId);
        LocalDate today = LocalDate.now();

        datePicker.setMin(earliest);
        datePicker.setMax(today);

        filterBar.add(todayBtn, yesterdayBtn, datePicker);

        // MATCH LIST
        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setSpacing(false);

        // default: show today’s fixtures
        loadFixturesForDate(LocalDate.now(), list);

        todayBtn.addClickListener(e -> loadFixturesForDate(LocalDate.now(), list));
        yesterdayBtn.addClickListener(e -> loadFixturesForDate(LocalDate.now().minusDays(1), list));
        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                loadFixturesForDate(e.getValue(), list);
            }
        });

        contentArea.add(filterBar, list);
    }

    private void loadFixturesForDate(LocalDate date, VerticalLayout list) {
        list.removeAll();

        List<FixtureResponse> fixtures = fixtureService.getFixturesByLeagueAndDate(leagueId, date);

        if (fixtures.isEmpty()) {
            Paragraph empty = new Paragraph("No fixtures for this date.");
            empty.getStyle().set("color", "#999");
            list.add(empty);
            return;
        }

        for (FixtureResponse f : fixtures) {
            list.add(buildMatchCard(f));
        }
    }

    //for each fixture (div˜)

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

        Div dateCol = new Div();
        dateCol.getStyle()
                .set("min-width", "60px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");

        dateCol.add(new Span(fixture.getDate().toString()));
        dateCol.add(new Span(fixture.getState()));

        Div teamsCol = new Div();
        teamsCol.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("flex-direction", "column");

        teamsCol.add(new Span(fixture.getHomeTeamName() + " " + fixture.getHomeScore()));
        teamsCol.add(new Span(fixture.getAwayTeamName() + " " + fixture.getAwayScore()));

        card.add(dateCol, teamsCol);
        return card;
    }
}

