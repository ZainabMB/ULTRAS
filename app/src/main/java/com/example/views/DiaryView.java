package com.example.views;

import com.example.model.Fixture;
import com.example.model.Rating;
import com.example.model.Team;
import com.example.repository.FixtureRepository;
import com.example.repository.RatingRepository;
import com.example.repository.TeamRepository;
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

@Route("diary")
@PageTitle("My Diary - Ultras")
public class DiaryView extends VerticalLayout {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public DiaryView(RatingRepository ratingRepository,
                     FixtureRepository fixtureRepository,
                     TeamRepository teamRepository) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", DARK);

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
        back.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("color", GREY_TEXT).set("padding", "0");
        back.addClickListener(e -> UI.getCurrent().navigate("profile"));

        Span title = new Span("My Diary");
        title.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("color", WHITE).set("flex", "1").set("text-align", "center");

        header.add(back, title);

        // ── Content ───────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle().set("gap", "12px");

        if (userId == null) {
            Span msg = new Span("Please sign in to view your diary.");
            msg.getStyle().set("color", GREY_TEXT).set("font-size", "14px");
            content.add(msg);
        } else {
            List<Rating> ratings = ratingRepository.findByUserIdAndTeamIdIsNull(userId);

            if (ratings.isEmpty()) {
                Div emptyState = new Div();
                emptyState.getStyle().set("text-align", "center").set("padding", "48px 16px");
                Span empty = new Span("You haven't rated any fixtures yet.");
                empty.getStyle().set("color", GREY_TEXT).set("font-size", "14px");
                emptyState.add(empty);
                content.add(emptyState);
            } else {
                ratings.sort((a, b) -> {
                    Optional<Fixture> fa = fixtureRepository.findById(a.getFixtureId());
                    Optional<Fixture> fb = fixtureRepository.findById(b.getFixtureId());
                    if (fa.isPresent() && fb.isPresent())
                        return fb.get().getDate().compareTo(fa.get().getDate());
                    return 0;
                });

                for (Rating rating : ratings) {
                    content.add(buildDiaryCard(rating, fixtureRepository, teamRepository));
                }
            }
        }

        add(header, content);
    }

    private Div buildDiaryCard(Rating rating,
                               FixtureRepository fixtureRepository,
                               TeamRepository teamRepository) {
        Div card = new Div();
        card.setWidthFull();
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
                UI.getCurrent().navigate("fixture/" + rating.getFixtureId() + "?from=diary"));

        Optional<Fixture> fixtureOpt = fixtureRepository.findById(rating.getFixtureId());

        String matchLabel = "Fixture #" + rating.getFixtureId();
        String dateLabel = "";
        String scoreLabel = "";

        if (fixtureOpt.isPresent()) {
            Fixture f = fixtureOpt.get();
            String home = teamRepository.findById(f.getHomeTeamId()).map(Team::getTeamName).orElse("?");
            String away = teamRepository.findById(f.getAwayTeamId()).map(Team::getTeamName).orElse("?");
            matchLabel = home + " vs " + away;
            dateLabel = f.getDate() != null ? f.getDate().toString() : "";
            scoreLabel = f.getHomeScore() + " - " + f.getAwayScore();
        }

        // Top row
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(Alignment.CENTER);

        Span match = new Span(matchLabel);
        match.getStyle().set("font-weight", "bold").set("font-size", "13px")
                .set("color", WHITE).set("flex", "1");

        Span date = new Span(dateLabel);
        date.getStyle().set("font-size", "11px").set("color", GREY_TEXT);

        topRow.add(match, date);

        // Bottom row
        HorizontalLayout bottomRow = new HorizontalLayout();
        bottomRow.setWidthFull();
        bottomRow.setAlignItems(Alignment.CENTER);
        bottomRow.getStyle().set("margin-top", "6px");

        Span score = new Span("FT  " + scoreLabel);
        score.getStyle().set("font-size", "13px").set("color", GREY_TEXT).set("flex", "1");

        double userScore = rating.getScore();
        int fullStars = (int) userScore;
        boolean hasHalf = (userScore - fullStars) >= 0.5;

        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < fullStars; i++) starStr.append("★");
        if (hasHalf) starStr.append("½");
        for (int i = fullStars + (hasHalf ? 1 : 0); i < 5; i++) starStr.append("☆");

        Span stars = new Span(starStr.toString());
        stars.getStyle().set("font-size", "13px").set("color", "#f5b301");

        bottomRow.add(score, stars);
        card.add(topRow, bottomRow);
        return card;
    }
}