package com.example.views;

import com.example.model.League;
import com.example.repository.FixtureRepository;
import com.example.repository.TeamRepository;
import com.example.service.LeagueService;
import com.example.views.components.SearchComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("leagues")
@PageTitle("Ultras - Leagues")
public class LeagueView extends VerticalLayout {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public LeagueView(LeagueService leagueService,
                      FixtureRepository fixtureRepository,
                      TeamRepository teamRepository) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", DARK);

        // ── Nav ───────────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "14px 20px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("display", "flex").set("justify-content", "space-between")
                .set("gap", "16px");

        Button profileBtn = new Button("👤");
        profileBtn.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("color", GREY_TEXT).set("padding", "0").set("flex-shrink", "0");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        Span navTitle = new Span("ULTRAS");
        navTitle.getStyle().set("font-weight", "bold").set("font-size", "18px")
                .set("letter-spacing", "4px").set("color", WHITE).set("flex-shrink", "0");

        Div leftGroup = new Div();
        leftGroup.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "12px").set("flex", "1");
        leftGroup.add(profileBtn, navTitle);

        SearchComponent search = new SearchComponent(fixtureRepository, teamRepository);
        search.getStyle().set("max-width", "220px").set("flex-shrink", "0");

        Div rightGroup = new Div();
        rightGroup.getStyle().set("display", "flex").set("align-items", "center")
                .set("flex", "1").set("justify-content", "flex-end");
        rightGroup.add(search);

        nav.add(leftGroup, rightGroup);

        // ── Tabs ──────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "10px 16px")
                .set("border-bottom", "1px solid " + BORDER)
                .set("gap", "8px");

        Button matchesTab = new Button("Matches");
        matchesTab.getStyle()
                .set("background-color", "transparent").set("color", GREY_TEXT)
                .set("border", "1px solid " + BORDER).set("border-radius", "20px")
                .set("padding", "6px 20px").set("cursor", "pointer");
        matchesTab.addClickListener(e -> UI.getCurrent().navigate("matches"));

        Button leaguesTab = new Button("Leagues");
        leaguesTab.getStyle()
                .set("background-color", BLUE).set("color", WHITE)
                .set("border", "none").set("border-radius", "20px")
                .set("padding", "6px 20px").set("cursor", "pointer")
                .set("font-weight", "600");

        tabs.add(matchesTab, leaguesTab);

        // ── League list ───────────────────────────
        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setPadding(true);
        list.setSpacing(false);
        list.getStyle().set("gap", "10px");

        List<League> leagues = leagueService.getAllLeagues();

        for (League league : leagues) {
            Div leagueRow = new Div();
            leagueRow.setWidthFull();
            leagueRow.getStyle()
                    .set("background-color", DARK_CARD)
                    .set("border", "1px solid " + BORDER)
                    .set("border-radius", "10px")
                    .set("padding", "14px 16px")
                    .set("cursor", "pointer")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "space-between");

            leagueRow.getElement().addEventListener("mouseover",
                    e -> leagueRow.getStyle().set("background-color", "#303036"));
            leagueRow.getElement().addEventListener("mouseout",
                    e -> leagueRow.getStyle().set("background-color", DARK_CARD));

            Div leftSide = new Div();
            leftSide.getStyle().set("display", "flex").set("align-items", "center").set("gap", "10px");

            Div accentBar = new Div();
            accentBar.getStyle().set("width", "3px").set("height", "18px")
                    .set("background-color", BLUE).set("border-radius", "2px");

            Span name = new Span(league.getLeagueName());
            name.getStyle().set("font-size", "14px").set("font-weight", "bold").set("color", WHITE);

            leftSide.add(accentBar, name);

            Span chevron = new Span("›");
            chevron.getStyle().set("font-size", "20px").set("color", BLUE);

            leagueRow.add(leftSide, chevron);
            leagueRow.addClickListener(e ->
                    UI.getCurrent().navigate("league/" + league.getLeagueId()));

            list.add(leagueRow);
        }

        add(nav, tabs, list);
    }
}