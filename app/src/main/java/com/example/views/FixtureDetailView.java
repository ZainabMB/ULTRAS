package com.example.views;


import com.example.model.League;
import com.example.model.dto.FixtureResponse;
import com.example.service.FixtureService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.time.LocalDate;
import java.util.List;

@Route("fixture")
@PageTitle("Fixture - Ultras")
public class FixtureDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final FixtureService fixtureService;

    public FixtureDetailView(FixtureService fixtureService) {
        this.fixtureService = fixtureService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f5f5f5");
    }

    @Override
    public void setParameter(BeforeEvent event, Long fixtureId) {

        FixtureResponse fixture = fixtureService.getFixtureDetail(fixtureId);

        if (fixture == null) {
            add(new H3("Fixture not found"));
            return;
        }

        removeAll(); // Clear UI before building

        buildHeader(fixture);
        buildTeamsSection(fixture);
        buildScoreSection(fixture);
        buildUserSection(fixture);
    }

    private void buildHeader(FixtureResponse f) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        Button back = new Button("←");
        back.addClickListener(e -> UI.getCurrent().navigate(""));

        Span league = new Span(f.getLeagueName());
        league.getStyle().set("font-weight", "bold").set("font-size", "18px");

        header.add(back, league);
        add(header);
    }

    private void buildTeamsSection(FixtureResponse f) {
        HorizontalLayout teams = new HorizontalLayout();
        teams.setWidthFull();
        teams.setAlignItems(Alignment.CENTER);
        teams.setJustifyContentMode(JustifyContentMode.CENTER);

        Image homeLogo = new Image(f.getHomeTeamLogo(), "home");
        homeLogo.setWidth("60px");

        Image awayLogo = new Image(f.getAwayTeamLogo(), "away");
        awayLogo.setWidth("60px");

        Span vs = new Span("vs");
        vs.getStyle().set("font-size", "20px").set("font-weight", "bold");

        teams.add(homeLogo, vs, awayLogo);
        add(teams);
    }

    private void buildScoreSection(FixtureResponse f) {
        Span scoreText = new Span(f.getHomeScore() + " - " + f.getAwayScore());
        scoreText.getStyle().set("font-size", "32px").set("font-weight", "bold");

        add(scoreText);
    }

    private void buildUserSection(FixtureResponse f) {
        Div userSection = new Div();
        userSection.getStyle()
                .set("background-color", "white")
                .set("padding", "16px")
                .set("border-radius", "8px")
                .set("margin-top", "16px");

        userSection.add(new H4("Your Review"));

        Button writeReview = new Button("Write a review");
        writeReview.addClickListener(e ->
                UI.getCurrent().navigate("review/" + f.getFixtureId())
        );

        userSection.add(writeReview);
        add(userSection);
    }
}


