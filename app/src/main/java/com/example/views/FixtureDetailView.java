package com.example.views;

import com.example.model.dto.FixtureResponse;
import com.example.service.FixtureService;
import com.example.service.RatingService;
import com.example.service.ReviewService;
import com.example.views.components.ReviewDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

@Route("fixture")
@PageTitle("Fixture - Ultras")
public class FixtureDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final FixtureService fixtureService;
    private final RatingService ratingService;
    private final ReviewService reviewService;
    private Long userId;

    public FixtureDetailView(FixtureService fixtureService,
                             RatingService ratingService,
                             ReviewService reviewService) {
        this.fixtureService = fixtureService;
        this.ratingService = ratingService;
        this.reviewService = reviewService;
        this.userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");
    }

    @Override
    public void setParameter(BeforeEvent event, Long fixtureId) {
        FixtureResponse fixture = fixtureService.getFixtureDetail(fixtureId);

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
        back.addClickListener(e -> UI.getCurrent().navigate("matches"));

        Span leagueTitle = new Span(fixture.getLeagueName() != null ? fixture.getLeagueName() : "Match");
        leagueTitle.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("flex", "1").set("text-align", "center");

        header.add(back, leagueTitle);

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

        Button detailsTab = new Button("Match details");
        detailsTab.getStyle().set("flex", "1").set("background-color", "white")
                .set("border", "1px solid #ccc").set("border-radius", "20px")
                .set("padding", "8px").set("cursor", "pointer");

        Button reviewsTab = new Button("Reviews");
        reviewsTab.getStyle().set("flex", "1").set("background-color", "white")
                .set("border", "1px solid #ccc").set("border-radius", "20px")
                .set("padding", "8px").set("cursor", "pointer");

        tabs.add(detailsTab, reviewsTab);

        Div contentBox = new Div();
        contentBox.getStyle()
                .set("margin", "0 16px 16px 16px")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("min-height", "200px")
                .set("background-color", "white");

        card.add(teamsRow, divider, ratingSection, tabs, contentBox);
        add(header, card);
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

    // Half star rating — left half of each star = .5, right half = full
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

        // Each star is a div containing a grey base star and a clipped gold star on top
        Div[] starWraps = new Div[5];
        Div[] goldLayers = new Div[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            final double halfScore = i + 0.5;
            final double fullScore = i + 1.0;

            Div wrap = new Div();
            wrap.getStyle()
                    .set("position", "relative")
                    .set("width", "28px")
                    .set("height", "28px")
                    .set("cursor", "pointer")
                    .set("display", "inline-block");

            // Grey base star
            Span greystar = new Span("★");
            greystar.getStyle()
                    .set("position", "absolute")
                    .set("inset", "0")
                    .set("font-size", "28px")
                    .set("color", "#ddd")
                    .set("line-height", "1")
                    .set("user-select", "none");

            // Gold overlay star — width clipped to show full, half or none
            Div goldLayer = new Div();
            goldLayer.getStyle()
                    .set("position", "absolute")
                    .set("inset", "0")
                    .set("font-size", "28px")
                    .set("color", "#f5b301")
                    .set("line-height", "1")
                    .set("overflow", "hidden")
                    .set("transition", "width 0.1s ease")
                    .set("user-select", "none");

            Span goldStar = new Span("★");
            goldLayer.add(goldStar);

            goldLayers[i] = goldLayer;
            starWraps[i] = wrap;
            wrap.add(greystar, goldLayer);
            row.add(wrap);

            // Click — left half = .5, right half = full

// Left half overlay — clicking gives .5
            Div leftHalf = new Div();
            leftHalf.getStyle()
                    .set("position", "absolute")
                    .set("left", "0")
                    .set("top", "0")
                    .set("width", "50%")
                    .set("height", "100%")
                    .set("z-index", "2")
                    .set("cursor", "pointer");

// Right half overlay — clicking gives full
            Div rightHalf = new Div();
            rightHalf.getStyle()
                    .set("position", "absolute")
                    .set("right", "0")
                    .set("top", "0")
                    .set("width", "50%")
                    .set("height", "100%")
                    .set("z-index", "1")
                    .set("cursor", "pointer");

            leftHalf.addClickListener(e -> {
                if (userId == null) {
                    Notification.show("Please sign in to rate", 2000,
                            Notification.Position.MIDDLE);
                    return;
                }
                saveAndUpdateCss(fixtureId, teamId, halfScore, goldLayers);
            });

            rightHalf.addClickListener(e -> {
                if (userId == null) {
                    Notification.show("Please sign in to rate", 2000,
                            Notification.Position.MIDDLE);
                    return;
                }
                saveAndUpdateCss(fixtureId, teamId, fullScore, goldLayers);
            });

            wrap.add(greystar, goldLayer, leftHalf, rightHalf);
            row.add(wrap);
        }

        // Set initial gold widths
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
            if (score >= full) {
                width = "100%";
            } else if (score >= half) {
                width = "50%";
            } else {
                width = "0%";
            }
            goldLayers[j].getStyle().set("width", width);
        }
    }

    // Save rating and update star colours immediately
    private void saveAndUpdate(Long fixtureId, Long teamId, double score,
                               Span[] leftHalves, Span[] rightHalves) {
        if (teamId == null) {
            ratingService.submitFixtureRating(userId, fixtureId, score);
        } else {
            ratingService.submitTeamRating(userId, fixtureId, teamId, score);
        }

        // Update colours immediately
        for (int j = 0; j < 5; j++) {
            double halfScore = j + 0.5;
            double fullScore = j + 1.0;
            leftHalves[j].getStyle().set("color", score >= halfScore ? "#f5b301" : "#ccc");
            rightHalves[j].getStyle().set("color", score >= fullScore ? "#f5b301" : "#ccc");
        }

        Notification.show("Rating saved!", 1500, Notification.Position.BOTTOM_START);
    }
}