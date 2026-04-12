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

    public UserReviewsView(ReviewService reviewService,
                           FixtureRepository fixtureRepository,
                           TeamRepository teamRepository) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");

        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

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
        back.addClickListener(e -> UI.getCurrent().navigate("profile"));

        Span title = new Span("My Reviews");
        title.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("flex", "1").set("text-align", "center");

        header.add(back, title);

        // ── Content ───────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle().set("gap", "12px");

        if (userId == null) {
            content.add(new Paragraph("Please sign in to view your reviews."));
        } else {
            List<Review> reviews = reviewService.getUserReviews(userId);

            if (reviews.isEmpty()) {
                Paragraph empty = new Paragraph("You haven't written any reviews yet.");
                empty.getStyle().set("color", "#999").set("text-align", "center")
                        .set("margin-top", "32px");
                content.add(empty);
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
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "14px 16px")
                .set("cursor", "pointer");

        // Navigate to fixture page on click
        card.addClickListener(e ->
                UI.getCurrent().navigate("fixture/" + review.getFixtureId() + "?from=reviews"));
        // Fixture label — home vs away
        String matchLabel = "Fixture #" + review.getFixtureId();
        Optional<Fixture> fixtureOpt = fixtureRepository.findById(review.getFixtureId());
        if (fixtureOpt.isPresent()) {
            Fixture f = fixtureOpt.get();
            String home = teamRepository.findById(f.getHomeTeamId())
                    .map(Team::getTeamName).orElse("?");
            String away = teamRepository.findById(f.getAwayTeamId())
                    .map(Team::getTeamName).orElse("?");
            matchLabel = home + " vs " + away;
        }

        // Top row — match label + date
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(Alignment.CENTER);

        Span match = new Span(matchLabel);
        match.getStyle().set("font-weight", "bold").set("font-size", "13px").set("flex", "1");

        Span date = new Span(review.getCreatedAt() != null
                ? review.getCreatedAt().toLocalDate().toString() : "");
        date.getStyle().set("font-size", "11px").set("color", "#999");

        topRow.add(match, date);

        // Review body
        Paragraph body = new Paragraph(review.getBody());
        body.getStyle()
                .set("font-size", "13px")
                .set("color", "#333")
                .set("margin", "6px 0 0 0")
                .set("line-height", "1.5");

        card.add(topRow, body);
        return card;
    }
}