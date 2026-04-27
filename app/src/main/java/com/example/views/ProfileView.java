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

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public ProfileView(UserRepository userRepository,
                       TeamRepository teamRepository,
                       FixtureRepository fixtureRepository,
                       RatingRepository ratingRepository) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", DARK);

        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        if (userId == null) {
            getStyle().set("align-items", "center").set("justify-content", "center");
            Span msg = new Span("Please sign in to view your profile.");
            msg.getStyle().set("color", GREY_TEXT).set("font-size", "14px");
            Button signIn = new Button("Sign in");
            signIn.getStyle().set("background-color", BLUE).set("color", WHITE)
                    .set("border", "none").set("border-radius", "8px")
                    .set("padding", "10px 24px").set("cursor", "pointer")
                    .set("margin-top", "12px");
            signIn.addClickListener(e -> UI.getCurrent().navigate("signin"));
            add(msg, signIn);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            add(new Paragraph("User not found."));
            return;
        }

        User user = userOpt.get();

        // ── Nav ───────────────────────────────────
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(Alignment.CENTER);
        nav.getStyle()
                .set("background-color", DARK_NAV)
                .set("padding", "14px 20px")
                .set("border-bottom", "1px solid " + BORDER);

        Button back = new Button("←");
        back.getStyle().set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("font-size", "20px")
                .set("color", GREY_TEXT).set("padding", "0");
        back.addClickListener(e -> UI.getCurrent().navigate("matches"));

        Span navTitle = new Span("Profile");
        navTitle.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("color", WHITE).set("flex", "1").set("text-align", "center");

        Span settingsIcon = new Span("⚙");
        settingsIcon.getStyle().set("font-size", "20px").set("cursor", "pointer")
                .set("color", GREY_TEXT);
        settingsIcon.addClickListener(e -> UI.getCurrent().navigate("settings"));

        nav.add(back, navTitle, settingsIcon);

        // ── Profile card ──────────────────────────
        Div card = new Div();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("margin", "16px")
                .set("border-radius", "12px")
                .set("border", "1px solid " + BORDER)
                .set("width", "calc(100% - 32px)")
                .set("box-sizing", "border-box")
                .set("padding", "24px 16px")
                ;

        // Avatar
        VerticalLayout profileSection = new VerticalLayout();
        profileSection.setAlignItems(Alignment.CENTER);
        profileSection.setPadding(false);
        profileSection.setSpacing(false);
        profileSection.getStyle().set("gap", "8px").set("margin-bottom", "24px");

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "80px").set("height", "80px")
                .set("border-radius", "50%")
                .set("border", "2px solid " + BLUE)
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "36px").set("background-color", "#1c1c1e");
        avatar.add(new Span("👤"));

        Span username = new Span(user.getUsername());
        username.getStyle().set("font-size", "18px").set("font-weight", "bold")
                .set("color", WHITE).set("margin-top", "8px");

        String favTeamText = "No favourite team set";
        if (user.getFavTeamId() != null) {
            favTeamText = teamRepository.findById(user.getFavTeamId())
                    .map(t -> t.getTeamName() + " Fan").orElse("No favourite team set");
        }
        Span favTeam = new Span(favTeamText);
        favTeam.getStyle().set("font-size", "13px").set("color", BLUE)
                .set("cursor", "pointer");

        profileSection.add(avatar, username, favTeam);

        // ── Stats box ─────────────────────────────
        Div statsBox = new Div();
        statsBox.setWidthFull();
        statsBox.getStyle()
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "10px")
                .set("padding", "16px")
                .set("margin-bottom", "16px")
                .set("background-color", "#222226")
                .set("width", "95%%");

        Span totsLabel = new Span("Team of the season");
        totsLabel.getStyle().set("font-size", "12px").set("color", GREY_TEXT)
                .set("letter-spacing", "0.5px");

        String totsValue = "No team ratings yet";
        List<Long> topTeams = ratingRepository.findTopRatedTeamByUser(userId);
        if (!topTeams.isEmpty()) {
            totsValue = teamRepository.findById(topTeams.get(0))
                    .map(Team::getTeamName).orElse("Unknown");
        }

        Span totsName = new Span(totsValue);
        totsName.getStyle().set("font-size", "15px").set("font-weight", "bold")
                .set("color", WHITE).set("display", "block").set("margin", "4px 0 16px 0");

        Span motsLabel = new Span("Match of the season");
        motsLabel.getStyle().set("font-size", "12px").set("color", GREY_TEXT)
                .set("letter-spacing", "0.5px");

        String motsValue = "No fixture ratings yet";
        List<Long> topFixtures = ratingRepository.findTopRatedFixtureByUser(userId);
        if (!topFixtures.isEmpty()) {
            Optional<Fixture> topFixture = fixtureRepository.findById(topFixtures.get(0));
            if (topFixture.isPresent()) {
                Fixture f = topFixture.get();
                String home = teamRepository.findById(f.getHomeTeamId()).map(Team::getTeamName).orElse("?");
                String away = teamRepository.findById(f.getAwayTeamId()).map(Team::getTeamName).orElse("?");
                motsValue = home + " vs " + away;
            }
        }

        Span motsName = new Span(motsValue);
        motsName.getStyle().set("font-size", "15px").set("font-weight", "bold")
                .set("color", WHITE).set("display", "block").set("margin-top", "4px");

        statsBox.add(totsLabel, totsName, motsLabel, motsName);

        // ── Nav rows ──────────────────────────────
        card.add(profileSection, statsBox,
                buildNavRow("Diary", "diary"),
                buildNavRow("Reviews", "reviews"));

        add(nav, card);
    }

    private Div buildNavRow(String label, String route) {
        Div row = new Div();
        row.setWidthFull();
        row.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "14px 16px")
                .set("border", "1px solid #3a3a3e")
                .set("border-radius", "8px").set("margin-bottom", "12px")
                .set("cursor", "pointer").set("background-color", "#222226").set("width", "95%");

        row.getElement().addEventListener("mouseover",
                e -> row.getStyle().set("background-color", "#303036"));
        row.getElement().addEventListener("mouseout",
                e -> row.getStyle().set("background-color", "#222226"));

        Span text = new Span(label);
        text.getStyle().set("font-size", "14px").set("font-weight", "bold").set("color", "#ffffff");

        Span chevron = new Span("›");
        chevron.getStyle().set("font-size", "20px").set("color", "rgb(0,97,127)");

        row.add(text, chevron);
        row.addClickListener(e -> UI.getCurrent().navigate(route));
        return row;
    }
}