package com.example.views;

import com.example.model.Review;
import com.example.model.dto.FixtureResponse;
import com.example.repository.FixtureRepository;
import com.example.repository.RatingRepository;
import com.example.repository.TeamRepository;
import com.example.repository.UserRepository;
import com.example.service.*;
import com.example.views.components.MatchDetailsComponent;
import com.example.views.components.ReviewDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

@Route("fixture")
@PageTitle("Fixture - Ultras")
public class FixtureDetailView extends VerticalLayout implements HasUrlParameter<Long>, BeforeEnterObserver {

    // ── Class-level fields ────────────────────────
    private final FixtureService fixtureService;
    private final RatingService ratingService;
    private final ReviewService reviewService;
    private final SportmonksService sportmonksService;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamRepository teamRepository;
    private final UserService userService;
    private Long userId;
    private String returnUrl = "matches";

    // Kept as fields so tab click listeners can reference them
    private Button detailsTab;
    private Button reviewsTab;
    private Div contentBox;
    private FixtureResponse fixture;

    public FixtureDetailView(FixtureService fixtureService,
                             RatingService ratingService,
                             ReviewService reviewService,
                             SportmonksService sportmonksService,
                             RatingRepository ratingRepository,
                             UserRepository userRepository,
                             UserService userService,
                             FixtureRepository fixtureRepository,
                             TeamRepository teamRepository) {
        this.fixtureService = fixtureService;
        this.ratingService = ratingService;
        this.reviewService = reviewService;
        this.sportmonksService = sportmonksService;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.fixtureRepository = fixtureRepository;
        this.teamRepository = teamRepository;
        this.userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation()
                .getQueryParameters().getParameters();
        if (params.containsKey("from")) {
            returnUrl = params.get("from").get(0);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long fixtureId) {
        fixture = fixtureService.getFixtureDetail(fixtureId);

        if (fixture == null) {
            add(new H3("Fixture not found"));
            return;
        }

        removeAll();

        String matchLabel = fixture.getHomeTeamName() + " vs " + fixture.getAwayTeamName();

        // ── Header ───────────────────────────────
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
                .set("background-color", "white")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #e0e0e0");

        Button back = new Button("←");
        back.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "18px");
        back.addClickListener(e -> UI.getCurrent().navigate(returnUrl));

// Stack league name + date vertically in the centre
        VerticalLayout titleCol = new VerticalLayout();
        titleCol.setPadding(false);
        titleCol.setSpacing(false);
        titleCol.setAlignItems(Alignment.CENTER);
        titleCol.getStyle().set("flex", "1");

        Span leagueTitle = new Span(fixture.getLeagueName() != null ? fixture.getLeagueName() : "Match");
        leagueTitle.getStyle().set("font-weight", "bold").set("font-size", "16px");

        Span dateSpan = new Span(fixture.getDate() != null ? fixture.getDate().toString() : "");
        dateSpan.getStyle().set("font-size", "12px").set("color", "#999");

        titleCol.add(leagueTitle, dateSpan);
        header.add(back, titleCol);

        // ── Main card ─────────────────────────────
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "white")
                .set("margin", "16px")
                .set("border-radius", "8px")
                .set("border", "1px solid #e0e0e0")
                .set("width", "calc(100% - 32px)")
                .set("box-sizing", "border-box");

        // ── Teams row ─────────────────────────────
        HorizontalLayout teamsRow = new HorizontalLayout();
        teamsRow.setWidthFull();
        teamsRow.setAlignItems(Alignment.CENTER);
        teamsRow.setJustifyContentMode(JustifyContentMode.CENTER);
        teamsRow.getStyle().set("padding", "24px 16px 16px 16px");

        teamsRow.add(
                buildTeamCol(fixture.getHomeTeamLogo(), fixture.getHomeTeamName(),
                        fixtureId, fixture.getHomeTeamId()),
                buildVsCol(fixture.getHomeScore(), fixture.getAwayScore()),
                buildTeamCol(fixture.getAwayTeamLogo(), fixture.getAwayTeamName(),
                        fixtureId, fixture.getAwayTeamId())
        );

        // ── Divider ───────────────────────────────
        Hr divider = new Hr();
        divider.getStyle().set("margin", "0 16px").set("border", "none")
                .set("border-top", "1px solid #e0e0e0");

        // ── Rating section ────────────────────────
        Div ratingSection = new Div();
        ratingSection.setWidthFull();
        ratingSection.getStyle().set("padding", "12px 16px");

        HorizontalLayout overallRow = new HorizontalLayout();
        overallRow.setAlignItems(Alignment.CENTER);
        overallRow.setWidthFull();
        overallRow.getStyle().set("margin-bottom", "8px");

        Span overallLabel = new Span("Overall match rating:");
        overallLabel.getStyle().set("font-size", "13px").set("margin-right", "8px");

        HorizontalLayout fixtureStars = buildClickableStars(fixtureId, null);
        fixtureStars.getStyle().set("flex", "1");

        Span writeReview = new Span("Write a review");
        writeReview.getStyle()
                .set("color", "green").set("font-size", "13px")
                .set("text-decoration", "underline").set("cursor", "pointer");
        writeReview.addClickListener(e -> {
            if (userId == null) {
                Notification.show("Please sign in", 2000, Notification.Position.MIDDLE);
                return;
            }
            new ReviewDialog(userId, fixtureId, matchLabel, reviewService,
                    () -> UI.getCurrent().getPage().reload()).open();
        });

        overallRow.add(overallLabel, fixtureStars, writeReview);

        HorizontalLayout avgRow = new HorizontalLayout();
        avgRow.setAlignItems(Alignment.CENTER);

        Span avgLabel = new Span("Average rating:");
        avgLabel.getStyle().set("font-size", "13px").set("margin-right", "8px");

        Span avgValue = new Span(fixture.getAverageRating() != null
                ? String.format("%.1f / 5", fixture.getAverageRating()) : "No ratings yet");
        avgValue.getStyle().set("font-size", "13px").set("color", "#555");

        avgRow.add(avgLabel, avgValue);
        ratingSection.add(overallRow, avgRow);

        // ── Tabs ──────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setJustifyContentMode(JustifyContentMode.CENTER);
        tabs.getStyle().set("padding", "12px 16px").set("gap", "12px");

        // Content box — class field so tab listeners can swap content
        contentBox = new Div();
        contentBox.getStyle()
                .set("margin", "0 16px 16px 16px")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("min-height", "200px")
                .set("background-color", "white")
                .set("padding", "16px");

        // Tabs — class fields so they can reference each other
        detailsTab = new Button("Match details");
        reviewsTab = new Button("Reviews");

        styleTabButton(detailsTab, true);
        styleTabButton(reviewsTab, false);

        detailsTab.addClickListener(e -> {
            styleTabButton(detailsTab, true);
            styleTabButton(reviewsTab, false);
            showMatchDetails();
        });

        reviewsTab.addClickListener(e -> {
            styleTabButton(reviewsTab, true);
            styleTabButton(detailsTab, false);
            showReviews(fixtureId);
        });

        tabs.add(detailsTab, reviewsTab);

        // Default — match details
        showMatchDetails();

        card.add(teamsRow, divider, ratingSection, tabs, contentBox);
        add(header, card);
    }

    // ── Tab: match details ────────────────────────
    private void showMatchDetails() {
        contentBox.removeAll();
        contentBox.add(new MatchDetailsComponent(
                fixture,
                sportmonksService,
                ratingRepository,
                fixtureRepository,
                teamRepository
        ));
    }

    // ── Tab: reviews ──────────────────────────────
    private void showReviews(Long fixtureId) {
        contentBox.removeAll();

        List<Review> reviews = reviewService.getFixtureReviews(fixtureId);

        if (reviews.isEmpty()) {
            Paragraph empty = new Paragraph("No reviews yet. Be the first to write one!");
            empty.getStyle().set("color", "#999").set("font-size", "13px");
            contentBox.add(empty);
            return;
        }

        for (Review review : reviews) {
            contentBox.add(buildReviewCard(review));
        }
    }

    // ── Review card ───────────────────────────────
    private Div buildReviewCard(Review review) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border-bottom", "1px solid #f0f0f0")
                .set("margin-bottom", "8px");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setAlignItems(Alignment.CENTER);
        topRow.setWidthFull();
        topRow.getStyle().set("margin-bottom", "6px");

        String usernameText = userRepository.findById(review.getUserId())
                .map(u -> u.getUsername()).orElse("Unknown user");

        Span username = new Span(usernameText);
        username.getStyle().set("font-weight", "bold").set("font-size", "13px").set("flex", "1");

        Span date = new Span(review.getCreatedAt() != null
                ? review.getCreatedAt().toLocalDate().toString() : "");
        date.getStyle().set("font-size", "11px").set("color", "#999");

        topRow.add(username, date);

        Paragraph body = new Paragraph(review.getBody());
        body.getStyle().set("font-size", "13px").set("color", "#333")
                .set("margin", "0").set("line-height", "1.5");

        card.add(topRow, body);
        return card;
    }

    // ── Tab button styling ────────────────────────
    private void styleTabButton(Button btn, boolean active) {
        if (btn == null) return;
        btn.getStyle()
                .set("flex", "1")
                .set("background-color", active ? "#1a1a1a" : "white")
                .set("color", active ? "white" : "#1a1a1a")
                .set("border", active ? "none" : "1px solid #ccc")
                .set("border-radius", "20px")
                .set("padding", "8px")
                .set("cursor", "pointer");
    }

    // ── Team column ───────────────────────────────
    private VerticalLayout buildTeamCol(String logoUrl, String teamName,
                                        Long fixtureId, Long teamId) {
        VerticalLayout col = new VerticalLayout();
        col.setAlignItems(Alignment.CENTER);
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("gap", "6px");

        Div logoCircle = new Div();
        logoCircle.getStyle()
                .set("width", "72px").set("height", "72px")
                .set("border-radius", "50%").set("border", "2px solid #ccc")
                .set("overflow", "hidden").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("background-color", "#f5f5f5");

        if (logoUrl != null && !logoUrl.isEmpty()) {
            Image logo = new Image(logoUrl, teamName);
            logo.setWidth("60px");
            logo.setHeight("60px");
            logo.getStyle().set("object-fit", "contain");
            logoCircle.add(logo);
        }

        Span name = new Span(teamName != null ? teamName : "");
        name.getStyle().set("font-size", "14px").set("font-weight", "bold")
                .set("text-align", "center");

        HorizontalLayout stars = buildClickableStars(fixtureId, teamId);
        col.add(logoCircle, name, stars);
        return col;
    }

    // ── VS col ────────────────────────────────────
    private VerticalLayout buildVsCol(int homeScore, int awayScore) {
        VerticalLayout col = new VerticalLayout();
        col.setAlignItems(Alignment.CENTER);
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("padding", "0 24px").set("gap", "4px");

        Span vs = new Span("vs");
        vs.getStyle().set("font-size", "16px").set("color", "#999");

        Span score = new Span(homeScore + " - " + awayScore);
        score.getStyle().set("font-size", "28px").set("font-weight", "bold")
                .set("color", "#222");

        col.add(vs, score);
        return col;
    }

    // ── Clickable half stars ──────────────────────
    private HorizontalLayout buildClickableStars(Long fixtureId, Long teamId) {
        HorizontalLayout row = new HorizontalLayout();
        row.setPadding(false);
        row.setSpacing(false);
        row.getStyle().set("gap", "4px");

        double existing = 0;
        if (userId != null) {
            if (teamId == null) {
                existing = ratingService.getUserFixtureRating(userId, fixtureId)
                        .map(r -> r.getScore()).orElse(0.0);
            } else {
                existing = ratingService.getUserTeamRating(userId, fixtureId, teamId)
                        .map(r -> r.getScore()).orElse(0.0);
            }
        }

        Div[] goldLayers = new Div[5];

        for (int i = 0; i < 5; i++) {
            final double halfScore = i + 0.5;
            final double fullScore = i + 1.0;

            Div wrap = new Div();
            wrap.getStyle()
                    .set("position", "relative").set("width", "28px").set("height", "28px")
                    .set("cursor", "pointer").set("display", "inline-block");

            Span greyStar = new Span("★");
            greyStar.getStyle()
                    .set("position", "absolute").set("inset", "0")
                    .set("font-size", "28px").set("color", "#ddd")
                    .set("line-height", "1").set("user-select", "none");

            Div goldLayer = new Div();
            goldLayer.getStyle()
                    .set("position", "absolute").set("inset", "0")
                    .set("font-size", "28px").set("color", "#f5b301")
                    .set("line-height", "1").set("overflow", "hidden")
                    .set("transition", "width 0.1s ease").set("user-select", "none");
            goldLayer.add(new Span("★"));
            goldLayers[i] = goldLayer;

            Div leftHalf = new Div();
            leftHalf.getStyle()
                    .set("position", "absolute").set("left", "0").set("top", "0")
                    .set("width", "50%").set("height", "100%")
                    .set("z-index", "2").set("cursor", "pointer");

            Div rightHalf = new Div();
            rightHalf.getStyle()
                    .set("position", "absolute").set("right", "0").set("top", "0")
                    .set("width", "50%").set("height", "100%")
                    .set("z-index", "1").set("cursor", "pointer");

            leftHalf.addClickListener(e -> {
                if (userId == null) {
                    Notification.show("Please sign in to rate", 2000, Notification.Position.MIDDLE);
                    return;
                }
                saveAndUpdateCss(fixtureId, teamId, halfScore, goldLayers);
            });

            rightHalf.addClickListener(e -> {
                if (userId == null) {
                    Notification.show("Please sign in to rate", 2000, Notification.Position.MIDDLE);
                    return;
                }
                saveAndUpdateCss(fixtureId, teamId, fullScore, goldLayers);
            });

            wrap.add(greyStar, goldLayer, leftHalf, rightHalf);
            row.add(wrap);
        }

        updateGoldLayers(goldLayers, existing);
        return row;
    }

    private void saveAndUpdateCss(Long fixtureId, Long teamId, double score, Div[] goldLayers) {
        if (teamId == null) {
            ratingService.submitFixtureRating(userId, fixtureId, score);
        } else {
            ratingService.submitTeamRating(userId, fixtureId, teamId, score);
        }
        updateGoldLayers(goldLayers, score);
        Notification.show("Rating saved!", 1500, Notification.Position.BOTTOM_START);
    }

    private void updateGoldLayers(Div[] goldLayers, double score) {
        for (int j = 0; j < 5; j++) {
            double full = j + 1.0;
            double half = j + 0.5;
            String width;
            if (score >= full) width = "100%";
            else if (score >= half) width = "50%";
            else width = "0%";
            goldLayers[j].getStyle().set("width", width);
        }
    }
}