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

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    private final LeagueService leagueService;
    private final FixtureService fixtureService;
    private final RatingRepository ratingRepository;
    private final TeamRepository teamRepository;

    private Long leagueId;
    private VerticalLayout contentArea;
    private Button detailsTab;
    private Button matchesTab;

    public LeagueDetailView(LeagueService leagueService, FixtureService fixtureService,
                            RatingRepository ratingRepository, TeamRepository teamRepository) {
        this.leagueService = leagueService;
        this.fixtureService = fixtureService;
        this.ratingRepository = ratingRepository;
        this.teamRepository = teamRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", DARK);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idStr = event.getRouteParameters().get("leagueId").orElse(null);
        if (idStr == null) { event.forwardTo("leagues"); return; }
        leagueId = Long.valueOf(idStr);
        buildView();
    }

    private void buildView() {
        removeAll();
        League league = leagueService.getLeagueById(leagueId);

        // ── Nav ───────────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "14px 20px")
                .set("border-bottom", "1px solid " + BORDER);

        Button backBtn = new Button("←");
        backBtn.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("color", GREY_TEXT).set("padding", "0");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("leagues"));

        Span navTitle = new Span(league.getLeagueName());
        navTitle.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("color", WHITE).set("flex", "1").set("text-align", "center");

        nav.add(backBtn, navTitle);

        // ── Tabs ──────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "10px 16px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("gap", "8px");

        detailsTab = new Button("Details");
        matchesTab = new Button("Matches");
        styleTab(detailsTab, true);
        styleTab(matchesTab, false);
        detailsTab.addClickListener(e -> showDetailsTab(league));
        matchesTab.addClickListener(e -> showMatchesTab());
        tabs.add(detailsTab, matchesTab);

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
            tab.getStyle().set("background-color", BLUE).set("color", WHITE)
                    .set("border", "none").set("border-radius", "20px")
                    .set("padding", "6px 20px").set("cursor", "pointer").set("font-weight", "600");
        } else {
            tab.getStyle().set("background-color", "transparent").set("color", GREY_TEXT)
                    .set("border", "1px solid " + BORDER).set("border-radius", "20px")
                    .set("padding", "6px 20px").set("cursor", "pointer");
        }
    }

    // ── Details tab ───────────────────────────────
    private void showDetailsTab(League league) {
        contentArea.removeAll();
        styleTab(detailsTab, true);
        styleTab(matchesTab, false);
        contentArea.add(buildAvgRatingCard(), buildTopTeamsCard(), buildTopH2HCard());
    }

    // ── Matches tab ───────────────────────────────
    private void showMatchesTab() {
        contentArea.removeAll();
        styleTab(detailsTab, false);
        styleTab(matchesTab, true);

        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(Alignment.CENTER);
        filterBar.getStyle().set("gap", "8px");

        Button todayBtn = buildFilterBtn("Today", true);
        Button yesterdayBtn = buildFilterBtn("Yesterday", false);

        DatePicker datePicker = new DatePicker();
        datePicker.setPlaceholder("Pick a date");
        datePicker.getStyle()
                .set("--vaadin-input-field-background", DARK_CARD)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-placeholder-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER)
                .set("--lumo-base-color", DARK_CARD)
                .set("color", GREY_TEXT).set("font-size", "13px");

        LocalDate earliest = fixtureService.getEarliestFixtureDateForLeague(leagueId);
        datePicker.setMin(earliest);
        datePicker.setMax(LocalDate.now());
        filterBar.add(todayBtn, yesterdayBtn, datePicker);

        // Feed area — same structure as MatchesView
        VerticalLayout feed = new VerticalLayout();
        feed.setWidthFull();
        feed.setPadding(false);
        feed.setSpacing(false);
        feed.getStyle().set("gap", "10px");

        loadFixturesForDate(LocalDate.now(), feed);

        todayBtn.addClickListener(e -> {
            setActive(todayBtn, true); setActive(yesterdayBtn, false);
            loadFixturesForDate(LocalDate.now(), feed);
        });
        yesterdayBtn.addClickListener(e -> {
            setActive(todayBtn, false); setActive(yesterdayBtn, true);
            loadFixturesForDate(LocalDate.now().minusDays(1), feed);
        });
        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                setActive(todayBtn, false); setActive(yesterdayBtn, false);
                loadFixturesForDate(e.getValue(), feed);
            }
        });

        contentArea.add(filterBar, feed);
    }

    // ── Load fixtures — wrapped in a league group card like MatchesView ───
    private void loadFixturesForDate(LocalDate date, VerticalLayout feed) {
        feed.removeAll();

        List<FixtureResponse> fixtures = fixtureService.getFixturesByLeagueAndDate(leagueId, date);

        if (fixtures.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle().set("text-align", "center").set("padding", "32px 0");
            Span empty = new Span("No fixtures for this date.");
            empty.getStyle().set("color", GREY_TEXT).set("font-size", "13px");
            emptyState.add(empty);
            feed.add(emptyState);
            return;
        }

        // Wrap all fixtures in one group card — same pattern as MatchesView league group
        Div leagueGroup = new Div();
        leagueGroup.setWidthFull();
        leagueGroup.getStyle()
                .set("background-color", DARK_CARD)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "10px")
                .set("overflow", "hidden");

        for (FixtureResponse f : fixtures) {
            leagueGroup.add(buildMatchCard(f));
        }

        feed.add(leagueGroup);
    }

    // ── Match card — identical structure to MatchesView.buildMatchCard ────
    private Div buildMatchCard(FixtureResponse fixture) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px 14px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px")
                .set("background-color", "transparent")
                .set("transition", "background-color 0.15s");

        card.getElement().addEventListener("mouseover",
                e -> card.getStyle().set("background-color", "#303036"));
        card.getElement().addEventListener("mouseout",
                e -> card.getStyle().set("background-color", "transparent"));

        card.addClickListener(e ->
                UI.getCurrent().navigate("fixture/" + fixture.getFixtureId() + "?from=league/" + leagueId));
        // ── Status/date column ────────────────────
        Div statusCol = new Div();
        statusCol.getStyle()
                .set("min-width", "52px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "2px");

        Span dateSpan = new Span(fixture.getDate() != null
                ? fixture.getDate().toString().substring(5) : "");
        dateSpan.getStyle().set("font-size", "10px").set("color", GREY_TEXT);

        Span stateSpan = new Span(fixture.getState() != null ? fixture.getState() : "FT");
        stateSpan.getStyle()
                .set("font-size", "11px").set("font-weight", "bold")
                .set("color", BLUE).set("letter-spacing", "0.5px");

        statusCol.add(dateSpan, stateSpan);

        // ── Vertical divider ──────────────────────
        Div divider = new Div();
        divider.getStyle()
                .set("width", "1px").set("height", "36px")
                .set("background-color", BORDER);

        // ── Teams + scores column ─────────────────
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

    // ── Stat cards ────────────────────────────────
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
            value.getStyle().set("color", GREY_TEXT);
        }
        value.getStyle().set("font-size", "20px").set("font-weight", "bold");
        card.add(value);
        return card;
    }

    private Div buildTopTeamsCard() {
        Div card = buildStatCard("Top 3 Rated Teams");
        List<Object[]> topTeams = ratingRepository.findTopRatedTeamsInLeague(leagueId);
        if (topTeams.isEmpty()) {
            Span empty = new Span("No team ratings yet");
            empty.getStyle().set("color", GREY_TEXT).set("font-size", "13px");
            card.add(empty);
            return card;
        }
        String[] medals = {"🥇", "🥈", "🥉"};
        int count = Math.min(3, topTeams.size());
        for (int i = 0; i < count; i++) {
            Object[] row = topTeams.get(i);
            Long teamId = ((Number) row[0]).longValue();
            Double avgScore = ((Number) row[1]).doubleValue();
            String teamName = teamRepository.findById(teamId).map(Team::getTeamName).orElse("Unknown");

            Div teamRow = new Div();
            teamRow.getStyle().set("display", "flex").set("align-items", "center")
                    .set("justify-content", "space-between").set("padding", "10px 0")
                    .set("border-bottom", i < count - 1 ? "1px solid " + BORDER : "none");

            Span nameSpan = new Span(medals[i] + "  " + teamName);
            nameSpan.getStyle().set("font-size", "14px").set("font-weight", "bold").set("color", WHITE);

            Span scoreSpan = new Span(String.format("%.2f", avgScore) + " / 5");
            scoreSpan.getStyle().set("font-size", "13px").set("color", "#f5b301").set("font-weight", "bold");

            teamRow.add(nameSpan, scoreSpan);
            card.add(teamRow);
        }
        return card;
    }

    private Div buildTopH2HCard() {
        Div card = buildStatCard("Highest Rated Head to Head");
        List<Object[]> h2hResults = ratingRepository.findTopRatedHeadToHeadInLeague(leagueId);
        if (h2hResults.isEmpty()) {
            Span empty = new Span("No fixture ratings yet");
            empty.getStyle().set("color", GREY_TEXT).set("font-size", "13px");
            card.add(empty);
            return card;
        }
        Object[] top = h2hResults.get(0);
        String homeName = teamRepository.findById(((Number) top[0]).longValue()).map(Team::getTeamName).orElse("?");
        String awayName = teamRepository.findById(((Number) top[1]).longValue()).map(Team::getTeamName).orElse("?");
        Double avgScore = ((Number) top[2]).doubleValue();

        Span matchup = new Span(homeName + " vs " + awayName);
        matchup.getStyle().set("font-size", "16px").set("font-weight", "bold")
                .set("color", WHITE).set("display", "block").set("margin-bottom", "6px");

        Span score = new Span("Avg rating: " + String.format("%.2f", avgScore) + " / 5");
        score.getStyle().set("font-size", "13px").set("color", "#f5b301").set("font-weight", "bold");

        Span note = new Span("Average across all meetings between these teams");
        note.getStyle().set("font-size", "11px").set("color", GREY_TEXT)
                .set("display", "block").set("margin-top", "4px");

        card.add(matchup, score, note);
        return card;
    }

    private Div buildStatCard(String title) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", DARK_CARD).set("border", "1px solid " + BORDER)
                .set("border-radius", "10px").set("padding", "16px").set("width", "95%");

        Div headerRow = new Div();
        headerRow.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "8px").set("margin-bottom", "12px");

        Div accentBar = new Div();
        accentBar.getStyle().set("width", "3px").set("height", "14px")
                .set("background-color", BLUE).set("border-radius", "2px").set("flex-shrink", "0");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "bold").set("font-size", "13px")
                .set("color", GREY_TEXT).set("letter-spacing", "0.5px");

        headerRow.add(accentBar, titleSpan);
        card.add(headerRow);
        return card;
    }

    private Button buildFilterBtn(String label, boolean active) {
        Button btn = new Button(label);
        setActive(btn, active);
        return btn;
    }

    private void setActive(Button btn, boolean active) {
        if (active) {
            btn.getStyle().set("background-color", BLUE).set("color", WHITE)
                    .set("border", "none").set("border-radius", "20px")
                    .set("padding", "6px 16px").set("font-size", "13px")
                    .set("font-weight", "600").set("cursor", "pointer");
        } else {
            btn.getStyle().set("background-color", "transparent").set("color", GREY_TEXT)
                    .set("border", "1px solid " + BORDER).set("border-radius", "20px")
                    .set("padding", "6px 16px").set("font-size", "13px")
                    .set("font-weight", "400").set("cursor", "pointer");
        }
    }
}