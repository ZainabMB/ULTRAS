package com.example.views;

import com.example.model.League;
import com.example.model.Team;
import com.example.model.dto.FixtureResponse;
import com.example.repository.RatingRepository;
import com.example.repository.TeamRepository;
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
    private final RatingRepository ratingRepository;
    private final TeamRepository teamRepository;

    private Long leagueId;
    private VerticalLayout contentArea;
    private Button detailsTab;
    private Button matchesTab;

    public LeagueDetailView(LeagueService leagueService,
                            FixtureService fixtureService,
                            RatingRepository ratingRepository,
                            TeamRepository teamRepository) {
        this.leagueService = leagueService;
        this.fixtureService = fixtureService;
        this.ratingRepository = ratingRepository;
        this.teamRepository = teamRepository;

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

        // ── Nav bar ───────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", "white")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #e0e0e0");

        Button backBtn = new Button("←");
        backBtn.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "18px");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("leagues"));

        Span navTitle = new Span(league.getLeagueName());
        navTitle.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("flex", "1").set("text-align", "center");

        Button searchBtn = new Button("🔍");
        searchBtn.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "18px");

        nav.add(backBtn, navTitle, searchBtn);

        // ── Tabs ──────────────────────────────────
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

        // ── Content area ──────────────────────────
        contentArea = new VerticalLayout();
        contentArea.setWidthFull();
        contentArea.setPadding(true);
        contentArea.setSpacing(false);
        contentArea.getStyle().set("gap", "16px");

        showDetailsTab(league);

        add(nav, tabs, contentArea);
    }

    private void styleTab(Button tab, boolean active) {
        if (active) {
            tab.getStyle()
                    .set("background-color", "#1a1a1a")
                    .set("color", "white")
                    .set("border", "none")
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

    // ── Details tab — stats ───────────────────────
    private void showDetailsTab(League league) {
        contentArea.removeAll();
        styleTab(detailsTab, true);
        styleTab(matchesTab, false);

        // Average league rating
        contentArea.add(buildAvgRatingCard());

        // Top 3 rated teams
        contentArea.add(buildTopTeamsCard());

        // Highest rated head to head
        contentArea.add(buildTopH2HCard());
    }

    // ── Matches tab ───────────────────────────────
    private void showMatchesTab() {
        contentArea.removeAll();
        styleTab(detailsTab, false);
        styleTab(matchesTab, true);

        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.getStyle().set("gap", "8px");

        Button todayBtn = new Button("Today");
        Button yesterdayBtn = new Button("Yesterday");

        DatePicker datePicker = new DatePicker();
        datePicker.setPlaceholder("Pick a date");

        LocalDate earliest = fixtureService.getEarliestFixtureDateForLeague(leagueId);
        LocalDate today = LocalDate.now();
        datePicker.setMin(earliest);
        datePicker.setMax(today);

        filterBar.add(todayBtn, yesterdayBtn, datePicker);

        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setSpacing(false);

        loadFixturesForDate(LocalDate.now(), list);

        todayBtn.addClickListener(e -> loadFixturesForDate(LocalDate.now(), list));
        yesterdayBtn.addClickListener(e -> loadFixturesForDate(LocalDate.now().minusDays(1), list));
        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) loadFixturesForDate(e.getValue(), list);
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
                .set("min-width", "60px").set("display", "flex")
                .set("flex-direction", "column").set("align-items", "center");
        dateCol.add(new Span(fixture.getDate().toString()));
        dateCol.add(new Span(fixture.getState() != null ? fixture.getState() : "FT"));

        Div teamsCol = new Div();
        teamsCol.getStyle().set("flex", "1").set("display", "flex")
                .set("flex-direction", "column");
        teamsCol.add(new Span(fixture.getHomeTeamName() + "  " + fixture.getHomeScore()));
        teamsCol.add(new Span(fixture.getAwayTeamName() + "  " + fixture.getAwayScore()));

        card.add(dateCol, teamsCol);
        return card;
    }

    // ── Average league rating card ────────────────
    private Div buildAvgRatingCard() {
        Div card = buildStatCard("Average League Rating");

        Double avg = ratingRepository.findAverageLeagueRating(leagueId);

        Span value;
        if (avg != null) {
            int full = (int) avg.doubleValue();
            boolean half = (avg - full) >= 0.5;

            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < full; i++) stars.append("★");
            if (half) stars.append("½");
            for (int i = full + (half ? 1 : 0); i < 5; i++) stars.append("☆");

            value = new Span(stars + "  " + String.format("%.2f", avg) + " / 5");
            value.getStyle().set("color", "#f5b301");
        } else {
            value = new Span("No ratings yet");
            value.getStyle().set("color", "#999");
        }

        value.getStyle().set("font-size", "20px").set("font-weight", "bold");
        card.add(value);
        return card;
    }

    // ── Top 3 rated teams card ────────────────────
    private Div buildTopTeamsCard() {
        Div card = buildStatCard("Top 3 Rated Teams");

        List<Object[]> topTeams = ratingRepository.findTopRatedTeamsInLeague(leagueId);

        if (topTeams.isEmpty()) {
            Span empty = new Span("No team ratings yet");
            empty.getStyle().set("color", "#999").set("font-size", "13px");
            card.add(empty);
            return card;
        }

        String[] medals = {"🥇", "🥈", "🥉"};
        int count = Math.min(3, topTeams.size());

        for (int i = 0; i < count; i++) {
            Object[] row = topTeams.get(i);
            Long teamId = ((Number) row[0]).longValue();
            Double avgScore = ((Number) row[1]).doubleValue();

            String teamName = teamRepository.findById(teamId)
                    .map(Team::getTeamName).orElse("Unknown");

            Div teamRow = new Div();
            teamRow.getStyle()
                    .set("display", "flex").set("align-items", "center")
                    .set("justify-content", "space-between")
                    .set("padding", "10px 0")
                    .set("border-bottom", i < count - 1 ? "1px solid #f0f0f0" : "none");

            Span nameSpan = new Span(medals[i] + "  " + teamName);
            nameSpan.getStyle().set("font-size", "14px").set("font-weight", "bold");

            Span scoreSpan = new Span(String.format("%.2f", avgScore) + " / 5");
            scoreSpan.getStyle().set("font-size", "13px").set("color", "#f5b301")
                    .set("font-weight", "bold");

            teamRow.add(nameSpan, scoreSpan);
            card.add(teamRow);
        }

        return card;
    }

    // ── Highest rated H2H card -------
    private Div buildTopH2HCard() {
        Div card = buildStatCard("Highest Rated Fixture (Head to Head)");

        List<Object[]> h2hResults = ratingRepository.findTopRatedHeadToHeadInLeague(leagueId);

        if (h2hResults.isEmpty()) {
            Span empty = new Span("No fixture ratings yet");
            empty.getStyle().set("color", "#999").set("font-size", "13px");
            card.add(empty);
            return card;
        }

        Object[] top = h2hResults.get(0);
        Long homeTeamId = ((Number) top[0]).longValue();
        Long awayTeamId = ((Number) top[1]).longValue();
        Double avgScore = ((Number) top[2]).doubleValue();

        String homeName = teamRepository.findById(homeTeamId)
                .map(Team::getTeamName).orElse("?");
        String awayName = teamRepository.findById(awayTeamId)
                .map(Team::getTeamName).orElse("?");

        Span matchup = new Span(homeName + " vs " + awayName);
        matchup.getStyle().set("font-size", "16px").set("font-weight", "bold")
                .set("display", "block").set("margin-bottom", "6px");

        Span score = new Span("Avg rating: " + String.format("%.2f", avgScore) + " / 5");
        score.getStyle().set("font-size", "13px").set("color", "#f5b301")
                .set("font-weight", "bold");

        Span note = new Span("Average across all meetings between these teams");
        note.getStyle().set("font-size", "11px").set("color", "#999")
                .set("display", "block").set("margin-top", "4px");

        card.add(matchup, score, note);
        return card;
    }

    // ── Stat card helper ----------------
    private Div buildStatCard(String title) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "16px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "bold").set("font-size", "14px")
                .set("display", "block").set("margin-bottom", "12px")
                .set("color", "#333");

        card.add(titleSpan);
        return card;
    }
}