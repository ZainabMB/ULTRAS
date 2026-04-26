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
public class FixtureDetailView extends VerticalLayout
        implements HasUrlParameter<Long>, BeforeEnterObserver {

    // ── Theme ─────────────────────────────────────
    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    // ── Dependencies ──────────────────────────────
    private final FixtureService    fixtureService;
    private final RatingService     ratingService;
    private final ReviewService     reviewService;
    private final SportmonksService sportmonksService;
    private final RatingRepository  ratingRepository;
    private final UserRepository    userRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamRepository    teamRepository;
    private final UserService       userService;

    // ── State ─────────────────────────────────────
    private Long            userId;
    private String          returnUrl = "matches"; // set in beforeEnter
    private Button          detailsTab;
    private Button          reviewsTab;
    private Div             contentBox;
    private FixtureResponse fixture;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────
    public FixtureDetailView(FixtureService fixtureService,
                             RatingService ratingService,
                             ReviewService reviewService,
                             SportmonksService sportmonksService,
                             RatingRepository ratingRepository,
                             UserRepository userRepository,
                             UserService userService,
                             FixtureRepository fixtureRepository,
                             TeamRepository teamRepository) {
        this.fixtureService    = fixtureService;
        this.ratingService     = ratingService;
        this.reviewService     = reviewService;
        this.sportmonksService = sportmonksService;
        this.ratingRepository  = ratingRepository;
        this.userRepository    = userRepository;
        this.userService       = userService;
        this.fixtureRepository = fixtureRepository;
        this.teamRepository    = teamRepository;
        this.userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background-color", DARK)
                .set("min-height", "100vh");
    }

    // ─────────────────────────────────────────────
    // ROUTING — beforeEnter runs before setParameter
    // ─────────────────────────────────────────────

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Read ?from= param here so returnUrl is ready when setParameter runs
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
            Span msg = new Span("Fixture not found");
            msg.getStyle().set("color", GREY_TEXT);
            add(msg);
            return;
        }

        removeAll();
        buildView(fixtureId);
    }

    // ─────────────────────────────────────────────
    // BUILD VIEW
    // ─────────────────────────────────────────────

    private void buildView(Long fixtureId) {
        String matchLabel = fixture.getHomeTeamName() + " vs " + fixture.getAwayTeamName();

        add(buildHeader());

        Div scrollWrapper = new Div();
        scrollWrapper.setWidthFull();
        scrollWrapper.getStyle()
                .set("background-color", DARK)
                .set("padding", "16px")
                .set("box-sizing", "border-box")
                .set("flex", "1");

        scrollWrapper.add(buildCard(fixtureId, matchLabel));
        add(scrollWrapper);
    }

    // ─────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────

    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "14px 20px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("flex-shrink", "0");

        Button back = new Button("←");
        back.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("color", GREY_TEXT).set("padding", "0");
        back.addClickListener(e -> UI.getCurrent().navigate(returnUrl));

        VerticalLayout titleCol = new VerticalLayout();
        titleCol.setPadding(false);
        titleCol.setSpacing(false);
        titleCol.setAlignItems(Alignment.CENTER);
        titleCol.getStyle().set("flex", "1");

        Span leagueTitle = new Span(fixture.getLeagueName() != null
                ? fixture.getLeagueName() : "Match");
        leagueTitle.getStyle()
                .set("font-weight", "bold").set("font-size", "15px").set("color", WHITE);

        Span dateSpan = new Span(fixture.getDate() != null
                ? fixture.getDate().toString() : "");
        dateSpan.getStyle().set("font-size", "12px").set("color", GREY_TEXT);

        titleCol.add(leagueTitle, dateSpan);
        header.add(back, titleCol);
        return header;
    }

    // ─────────────────────────────────────────────
    // MAIN CARD
    // ─────────────────────────────────────────────

    private Div buildCard(Long fixtureId, String matchLabel) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border-radius", "12px")
                .set("border", "1px solid " + BORDER)
                .set("box-sizing", "border-box")
                .set("overflow", "hidden");

        card.add(
                buildTeamsRow(fixtureId),
                buildDivider(),
                buildRatingSection(fixtureId, matchLabel),
                buildTabsRow(),
                buildContentBox(fixtureId)
        );

        return card;
    }

    // ─────────────────────────────────────────────
    // TEAMS ROW
    // ─────────────────────────────────────────────

    private HorizontalLayout buildTeamsRow(Long fixtureId) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.setJustifyContentMode(JustifyContentMode.CENTER);
        row.getStyle().set("padding", "28px 16px 20px 16px");

        row.add(
                buildTeamCol(fixture.getHomeTeamLogo(), fixture.getHomeTeamName(),
                        fixtureId, fixture.getHomeTeamId()),
                buildVsCol(fixture.getHomeScore(), fixture.getAwayScore()),
                buildTeamCol(fixture.getAwayTeamLogo(), fixture.getAwayTeamName(),
                        fixtureId, fixture.getAwayTeamId())
        );

        return row;
    }

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
                .set("border-radius", "50%").set("border", "2px solid " + BLUE)
                .set("overflow", "hidden").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("background-color", DARK);

        if (logoUrl != null && !logoUrl.isEmpty()) {
            Image logo = new Image(logoUrl, teamName);
            logo.setWidth("60px");
            logo.setHeight("60px");
            logo.getStyle().set("object-fit", "contain");
            logoCircle.add(logo);
        }

        Span name = new Span(teamName != null ? teamName : "");
        name.getStyle()
                .set("font-size", "13px").set("font-weight", "bold")
                .set("text-align", "center").set("color", WHITE);

        col.add(logoCircle, name, buildClickableStars(fixtureId, teamId));
        return col;
    }

    private VerticalLayout buildVsCol(int homeScore, int awayScore) {
        VerticalLayout col = new VerticalLayout();
        col.setAlignItems(Alignment.CENTER);
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("padding", "0 20px").set("gap", "4px");

        Span vs = new Span("vs");
        vs.getStyle().set("font-size", "13px").set("color", GREY_TEXT);

        Span score = new Span(homeScore + " - " + awayScore);
        score.getStyle()
                .set("font-size", "32px").set("font-weight", "bold").set("color", WHITE);

        col.add(vs, score);
        return col;
    }

    // ─────────────────────────────────────────────
    // RATING SECTION
    // ─────────────────────────────────────────────

    private Div buildRatingSection(Long fixtureId, String matchLabel) {
        Div section = new Div();
        section.setWidthFull();
        section.getStyle().set("padding", "12px 16px");

        // Use plain Div — HorizontalLayout's internal flex fights with flex-shrink
        Div overallRow = new Div();
        overallRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("width", "95%")
                .set("margin-bottom", "8px")
                .set("gap", "8px")
                .set("overflow", "hidden");

        Span overallLabel = new Span("Overall:");
        overallLabel.getStyle()
                .set("font-size", "13px").set("color", GREY_TEXT)
                .set("flex-shrink", "0");

        HorizontalLayout fixtureStars = buildClickableStars(fixtureId, null);
        fixtureStars.getStyle()
                .set("flex", "1")
                .set("min-width", "0");

        Span writeReview = new Span("Write a Review");
        writeReview.getStyle()
                .set("color", BLUE).set("font-size", "13px").set("font-weight", "600")
                .set("cursor", "pointer").set("flex-shrink", "0")
                .set("white-space", "normal");
        writeReview.addClickListener(e -> {
            if (userId == null) {
                Notification.show("Please sign in", 2000, Notification.Position.MIDDLE);
                return;
            }
            new ReviewDialog(userId, fixtureId, matchLabel, reviewService,
                    () -> UI.getCurrent().getPage().reload()).open();
        });

        overallRow.add(overallLabel, fixtureStars, writeReview);

        // Average rating row
        Div avgRow = new Div();
        avgRow.getStyle().set("display", "flex").set("align-items", "center").set("gap", "6px");

        Span avgLabel = new Span("Avg rating:");
        avgLabel.getStyle().set("font-size", "12px").set("color", GREY_TEXT);

        Span avgValue = new Span(fixture.getAverageRating() != null
                ? String.format("%.1f / 5", fixture.getAverageRating()) : "No ratings yet");
        avgValue.getStyle().set("font-size", "12px").set("color", "#f5b301");

        avgRow.add(avgLabel, avgValue);
        section.add(overallRow, avgRow);
        return section;
    }

    // ─────────────────────────────────────────────
    // TABS
    // ─────────────────────────────────────────────

    private HorizontalLayout buildTabsRow() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setJustifyContentMode(JustifyContentMode.CENTER);
        tabs.getStyle().set("padding", "12px 16px").set("gap", "12px");

        detailsTab = new Button("Match details");
        reviewsTab  = new Button("Reviews");

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
            showReviews(fixture.getFixtureId());
        });

        tabs.add(detailsTab, reviewsTab);
        return tabs;
    }

    private void styleTabButton(Button btn, boolean active) {
        if (btn == null) return;
        btn.getStyle()
                .set("flex", "1")
                .set("background-color", active ? BLUE : "transparent")
                .set("color", active ? WHITE : GREY_TEXT)
                .set("border", active ? "none" : "1px solid " + BORDER)
                .set("border-radius", "20px")
                .set("padding", "8px")
                .set("cursor", "pointer")
                .set("font-weight", active ? "600" : "400");
    }

    private Div buildContentBox(Long fixtureId) {
        contentBox = new Div();
        contentBox.setWidthFull();
        contentBox.getStyle()
                .set("border-top", "1px solid " + BORDER)
                .set("min-height", "200px")
                .set("background-color", "#1e1e22")
                .set("padding", "16px")
                .set("box-sizing", "border-box");

        showMatchDetails();
        return contentBox;
    }

    // ─────────────────────────────────────────────
    // TAB CONTENT
    // ─────────────────────────────────────────────

    private void showMatchDetails() {
        contentBox.removeAll();
        contentBox.add(new MatchDetailsComponent(
                fixture, sportmonksService, ratingRepository,
                fixtureRepository, teamRepository));
    }

    private void showReviews(Long fixtureId) {
        contentBox.removeAll();
        List<Review> reviews = reviewService.getFixtureReviews(fixtureId);

        if (reviews.isEmpty()) {
            Span empty = new Span("No reviews yet. Be the first to write one!");
            empty.getStyle().set("color", GREY_TEXT).set("font-size", "13px");
            contentBox.add(empty);
            return;
        }

        reviews.forEach(r -> contentBox.add(buildReviewCard(r)));
    }

    private Div buildReviewCard(Review review) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("margin-bottom", "8px");

        Div topRow = new Div();
        topRow.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "space-between").set("margin-bottom", "6px");

        String usernameText = userRepository.findById(review.getUserId())
                .map(u -> u.getUsername()).orElse("Unknown user");

        Span username = new Span(usernameText);
        username.getStyle()
                .set("font-weight", "bold").set("font-size", "13px").set("color", WHITE);

        Span date = new Span(review.getCreatedAt() != null
                ? review.getCreatedAt().toLocalDate().toString() : "");
        date.getStyle().set("font-size", "11px").set("color", GREY_TEXT);

        topRow.add(username, date);

        Paragraph body = new Paragraph(review.getBody());
        body.getStyle()
                .set("font-size", "13px").set("color", GREY_TEXT)
                .set("margin", "0").set("line-height", "1.5");

        card.add(topRow, body);
        return card;
    }

    // ─────────────────────────────────────────────
    // STAR RATING
    // Each star is split into two invisible halves.
    // Left half = 0.5 score, right half = full score.
    // Gold overlay width is animated to reflect the rating.
    // ─────────────────────────────────────────────

    private HorizontalLayout buildClickableStars(Long fixtureId, Long teamId) {
        HorizontalLayout row = new HorizontalLayout();
        row.setPadding(false);
        row.setSpacing(false);
        row.getStyle().set("gap", "4px");

        // Preload user's existing rating so stars are filled on page load
        double existing = 0;
        if (userId != null) {
            existing = teamId == null
                    ? ratingService.getUserFixtureRating(userId, fixtureId)
                    .map(r -> r.getScore()).orElse(0.0)
                    : ratingService.getUserTeamRating(userId, fixtureId, teamId)
                    .map(r -> r.getScore()).orElse(0.0);
        }

        Div[] goldLayers = new Div[5];

        for (int i = 0; i < 5; i++) {
            final double halfScore = i + 0.5;
            final double fullScore = i + 1.0;

            Div wrap = new Div();
            wrap.getStyle()
                    .set("position", "relative").set("width", "26px").set("height", "26px")
                    .set("cursor", "pointer").set("display", "inline-block");

            // Grey base star
            Span greyStar = new Span("★");
            greyStar.getStyle()
                    .set("position", "absolute").set("inset", "0")
                    .set("font-size", "26px").set("color", BORDER)
                    .set("line-height", "1").set("user-select", "none");

            // Gold overlay — clipped by width to show 0%, 50% or 100%
            Div goldLayer = new Div();
            goldLayer.getStyle()
                    .set("position", "absolute").set("inset", "0")
                    .set("font-size", "26px").set("color", "#f5b301")
                    .set("line-height", "1").set("overflow", "hidden")
                    .set("transition", "width 0.1s ease").set("user-select", "none");
            goldLayer.add(new Span("★"));
            goldLayers[i] = goldLayer;

            // Left invisible hit area → half star
            Div leftHalf = new Div();
            leftHalf.getStyle()
                    .set("position", "absolute").set("left", "0").set("top", "0")
                    .set("width", "50%").set("height", "100%")
                    .set("z-index", "2").set("cursor", "pointer");

            // Right invisible hit area → full star
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
            String width = score >= full ? "100%" : score >= half ? "50%" : "0%";
            goldLayers[j].getStyle().set("width", width);
        }
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private Hr buildDivider() {
        Hr hr = new Hr();
        hr.getStyle()
                .set("margin", "0 16px").set("border", "none")
                .set("border-top", "1px solid " + BORDER);
        return hr;
    }
}