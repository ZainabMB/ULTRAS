package com.example.views;

import com.example.model.Fixture;
import com.example.model.Team;
import com.example.model.User;
import com.example.repository.FixtureRepository;
import com.example.repository.RatingRepository;
import com.example.repository.TeamRepository;
import com.example.repository.UserRepository;
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

@Route("profile")
@PageTitle("Profile - Ultras")
public class ProfileView extends VerticalLayout {

    public ProfileView(UserRepository userRepository,
                       TeamRepository teamRepository,
                       FixtureRepository fixtureRepository,
                       RatingRepository ratingRepository) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f5f5f5");

        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        if (userId == null) {
            add(new H3("Please sign in to view your profile."));
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            add(new H3("User not found."));
            return;
        }

        User user = userOpt.get();

        // ── Top nav bar ───────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.getStyle()
                .set("background-color", "white")
                .set("padding", "12px 16px")
                .set("border-bottom", "1px solid #e0e0e0");

        Button back = new Button("←");
        back.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "18px");
        back.addClickListener(e -> UI.getCurrent().navigate("matches"));

        nav.add(back);

        // ── Main card ─────────────────────────────
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "white")
                .set("margin", "16px")
                .set("border-radius", "8px")
                .set("border", "1px solid #e0e0e0")
                .set("width", "calc(100% - 32px)")
                .set("box-sizing", "border-box")
                .set("padding", "24px 16px");

        // Settings icon top right
        Div cardTop = new Div();
        cardTop.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("width", "100%");

        Span settingsIcon = new Span("⚙");
        settingsIcon.getStyle()
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("color", "#555");
        settingsIcon.addClickListener(e -> UI.getCurrent().navigate("settings"));

        cardTop.add(settingsIcon);

        // ── Profile section ───────────────────────
        VerticalLayout profileSection = new VerticalLayout();
        profileSection.setAlignItems(Alignment.CENTER);
        profileSection.setPadding(false);
        profileSection.setSpacing(false);
        profileSection.getStyle().set("gap", "8px").set("margin-bottom", "24px");

        // Avatar circle
        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "80px")
                .set("height", "80px")
                .set("border-radius", "50%")
                .set("border", "2px solid #333")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "36px")
                .set("color", "#555");
        avatar.add(new Span("👤"));

        // Username
        Span username = new Span(user.getUsername());
        username.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "bold")
                .set("margin-top", "8px");

        // Favourite team — show "[Team] Fan" or prompt to set
        String favTeamText = "No favourite team set";
        if (user.getFavTeamId() != null) {
            favTeamText = teamRepository.findById(user.getFavTeamId())
                    .map(t -> t.getTeamName() + " Fan")
                    .orElse("No favourite team set");
        }
        Span favTeam = new Span(favTeamText);
        favTeam.getStyle()
                .set("font-size", "14px")
                .set("text-decoration", "underline")
                .set("cursor", "pointer")
                .set("color", "#333");

        profileSection.add(avatar, username, favTeam);

        // ── TOTS / MOTS box ───────────────────────
        Div statsBox = new Div();
        statsBox.setWidthFull();
        statsBox.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("margin-bottom", "16px");

        // Team of the season — user's highest avg rated team
        Span totsLabel = new Span("Team of the season");
        totsLabel.getStyle().set("font-size", "13px").set("color", "#999");

        String totsValue = "No team ratings yet";
        List<Long> topTeams = ratingRepository.findTopRatedTeamByUser(userId);
        if (!topTeams.isEmpty()) {
            totsValue = teamRepository.findById(topTeams.get(0))
                    .map(Team::getTeamName)
                    .orElse("Unknown team");
        }

        Span totsName = new Span(totsValue);
        totsName.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "bold")
                .set("margin-top", "4px")
                .set("display", "block");

        // Spacer
        Div spacer = new Div();
        spacer.getStyle().set("height", "16px");

        // Match of the season — user's highest rated fixture
        Span motsLabel = new Span("Match of the season");
        motsLabel.getStyle().set("font-size", "13px").set("color", "#999");

        String motsValue = "No fixture ratings yet";
        List<Long> topFixtures = ratingRepository.findTopRatedFixtureByUser(userId);
        if (!topFixtures.isEmpty()) {
            Long topFixtureId = topFixtures.get(0);
            Optional<Fixture> topFixture = fixtureRepository.findById(topFixtureId);
            if (topFixture.isPresent()) {
                Fixture f = topFixture.get();
                String homeName = teamRepository.findById(f.getHomeTeamId())
                        .map(Team::getTeamName).orElse("?");
                String awayName = teamRepository.findById(f.getAwayTeamId())
                        .map(Team::getTeamName).orElse("?");
                motsValue = homeName + " vs " + awayName;
            }
        }

        Span motsName = new Span(motsValue);
        motsName.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "bold")
                .set("margin-top", "4px")
                .set("display", "block");

        statsBox.add(totsLabel, totsName, spacer, motsLabel, motsName);

        // ── Reviews row ───────────────────────────
        Div reviewsRow = buildNavRow("Reviews", "reviews");

        // ── Diary row ─────────────────────────────
        Div diaryRow = buildNavRow("Diary", "diary");

        card.add(cardTop, profileSection, statsBox, reviewsRow, diaryRow);
        add(nav, card);
    }

    // Clickable nav row with chevron — like the wireframe
    private Div buildNavRow(String label, String route) {
        Div row = new Div();
        row.setWidthFull();
        row.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "14px 16px")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("margin-bottom", "12px")
                .set("cursor", "pointer")
                .set("background-color", "white");

        Span text = new Span(label);
        text.getStyle().set("font-size", "14px").set("font-weight", "bold");

        Span chevron = new Span("›");
        chevron.getStyle().set("font-size", "20px").set("color", "#555");

        row.add(text, chevron);
        row.addClickListener(e -> UI.getCurrent().navigate(route));
        return row;
    }
}