package com.example.views;

import com.example.model.Fixture;
import com.example.model.Review;
import com.example.model.Team;
import com.example.repository.FixtureRepository;
import com.example.repository.TeamRepository;
import com.example.service.ReviewService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Optional;

@Route("reviews")
@PageTitle("My Reviews - Ultras")
public class UserReviewsView extends VerticalLayout {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public UserReviewsView(ReviewService reviewService,
                           FixtureRepository fixtureRepository,
                           TeamRepository teamRepository) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background-color", DARK)
                .set("min-height", "100vh");

        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        // ── Header ───────────────────────────────
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "14px 20px")
                .set("border-bottom", "1px solid " + BORDER);

        Button back = new Button("←");
        back.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("color", GREY_TEXT).set("padding", "0");
        back.addClickListener(e -> UI.getCurrent().navigate("profile"));

        Span title = new Span("My Reviews");
        title.getStyle()
                .set("font-weight", "bold").set("font-size", "16px")
                .set("color", WHITE).set("flex", "1").set("text-align", "center");

        header.add(back, title);

        // ── Content ───────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle().set("gap", "12px");

        if (userId == null) {
            Span msg = new Span("Please sign in to view your reviews.");
            msg.getStyle().set("color", GREY_TEXT).set("font-size", "14px");
            content.add(msg);
        } else {
            List<Review> reviews = reviewService.getUserReviews(userId);

            if (reviews.isEmpty()) {
                Div emptyState = new Div();
                emptyState.getStyle()
                        .set("text-align", "center").set("padding", "48px 16px");
                Span empty = new Span("You haven't written any reviews yet.");
                empty.getStyle().set("color", GREY_TEXT).set("font-size", "14px");
                emptyState.add(empty);
                content.add(emptyState);
            } else {
                for (Review review : reviews) {
                    content.add(buildReviewCard(review, fixtureRepository, teamRepository));
                }
            }
        }

        add(header, content);
    }

    private Div buildReviewCard(Review review,
                                FixtureRepository fixtureRepository,
                                TeamRepository teamRepository) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "10px")
                .set("padding", "14px 16px")
                .set("cursor", "pointer").set("width", "95%");

        card.getElement().addEventListener("mouseover",
                e -> card.getStyle().set("background-color", "#303036"));
        card.getElement().addEventListener("mouseout",
                e -> card.getStyle().set("background-color", DARK_CARD));

        card.addClickListener(e ->
                UI.getCurrent().navigate("fixture/" + review.getFixtureId() + "?from=reviews"));

        // Resolve fixture label
        String matchLabel = "Fixture #" + review.getFixtureId();
        String dateLabel  = "";

        Optional<Fixture> fixtureOpt = fixtureRepository.findById(review.getFixtureId());
        if (fixtureOpt.isPresent()) {
            Fixture f = fixtureOpt.get();
            String home = teamRepository.findById(f.getHomeTeamId())
                    .map(Team::getTeamName).orElse("?");
            String away = teamRepository.findById(f.getAwayTeamId())
                    .map(Team::getTeamName).orElse("?");
            matchLabel = home + " vs " + away;
            dateLabel  = f.getDate() != null ? f.getDate().toString() : "";
        }

        // Top row — match label + fixture date
        Div topRow = new Div();
        topRow.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "space-between").set("margin-bottom", "8px");

        Span match = new Span(matchLabel);
        match.getStyle()
                .set("font-weight", "bold").set("font-size", "13px").set("color", WHITE);

        Span date = new Span(dateLabel);
        date.getStyle().set("font-size", "11px").set("color", GREY_TEXT);

        topRow.add(match, date);

        // Review written date
        Span writtenOn = new Span(review.getCreatedAt() != null
                ? "Written " + review.getCreatedAt().toLocalDate() : "");
        writtenOn.getStyle()
                .set("font-size", "11px").set("color", BLUE)
                .set("display", "block").set("margin-bottom", "6px");

        // Review body
        Paragraph body = new Paragraph(review.getBody());
        body.getStyle()
                .set("font-size", "13px").set("color", GREY_TEXT)
                .set("margin", "0").set("line-height", "1.5");

        card.add(topRow, writtenOn, body);
        return card;
    }
}