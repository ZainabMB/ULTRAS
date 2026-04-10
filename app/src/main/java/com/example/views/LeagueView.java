package com.example.views;


import com.example.model.Fixture;
import com.example.model.League;
import com.example.model.dto.FixtureResponse;
import com.example.service.FixtureService;
import com.example.service.LeagueService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@Route("leagues")
@PageTitle("Ultras - Leagues")
public class LeagueView extends VerticalLayout {

    public LeagueView(LeagueService leagueService) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");

        // ── Top nav ──────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", "white")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #e0e0e0");

        Button profileBtn = new Button("👤");
        profileBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-size", "18px")
                .set("padding", "0");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        Span navTitle = new Span("ULTRAS");
        navTitle.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "16px")
                .set("flex", "1")
                .set("text-align", "center");

        Button searchBtn = new Button("🔍");
        searchBtn.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("font-size", "18px")
                .set("padding", "0");

        nav.add(profileBtn, navTitle, searchBtn);

        // ── Tabs ─────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.getStyle()
                .set("background-color", "white")
                .set("padding", "8px 16px")
                .set("border-bottom", "1px solid #e0e0e0")
                .set("gap", "8px");

        Button matchesTab = new Button("Matches");
        matchesTab.getStyle()
                .set("background-color", "white")
                .set("color", "#1a1a1a")
                .set("border", "1px solid #ccc")
                .set("border-radius", "20px")
                .set("padding", "6px 20px");
        matchesTab.addClickListener(e -> UI.getCurrent().navigate("matches"));

        Button leaguesTab = new Button("Leagues");
        leaguesTab.getStyle()
                .set("background-color", "#1a1a1a")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "20px")
                .set("padding", "6px 20px");

        tabs.add(matchesTab, leaguesTab);



        // ── League list ──────────────────────────
        H2 title = new H2("Leagues");

        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setPadding(true);

        List<League> leagues = leagueService.getAllLeagues();

        for (League league : leagues) {
            Button leagueBtn = new Button(league.getLeagueName());
            leagueBtn.setWidthFull();
            leagueBtn.getStyle()
                    .set("text-align", "left")
                    .set("background-color", "white")
                    .set("color", "black")
                    .set("border", "1px solid #ddd")
                    .set("border-radius", "8px");

            leagueBtn.addClickListener(e ->
                    UI.getCurrent().navigate("league/" + league.getLeagueId()));

            list.add(leagueBtn);
        }

        // Add everything to the layout
        add(nav, tabs,  title, list);
    }
}


