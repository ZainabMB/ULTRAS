package com.example.views;

import com.example.model.Team;
import com.example.repository.TeamRepository;
import com.example.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("signup")
@PageTitle("Sign Up - Ultras")
public class SignUpView extends VerticalLayout {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public SignUpView(UserService userService, TeamRepository teamRepository) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(false);
        getStyle()
                .set("background-color", DARK)
                .set("min-height", "100vh");

        // ── Card ──────────────────────────────────
        Div card = new Div();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border-radius", "16px")
                .set("border", "1px solid " + BORDER)
                .set("padding", "40px 32px")
                .set("width", "90%").set("max-width", "380px")
                .set("display", "flex").set("flex-direction", "column")
                .set("align-items", "stretch").set("box-sizing", "border-box");

        // ── Brand ─────────────────────────────────
        Span brand = new Span("ULTRAS");
        brand.getStyle()
                .set("font-weight", "800").set("font-size", "22px")
                .set("letter-spacing", "4px").set("color", WHITE)
                .set("text-align", "center").set("display", "block")
                .set("margin-bottom", "4px");

        Div accent = new Div();
        accent.getStyle()
                .set("width", "40px").set("height", "3px")
                .set("background-color", BLUE).set("border-radius", "2px")
                .set("margin", "0 auto 24px auto");

        H2 heading = new H2("Create account");
        heading.getStyle()
                .set("color", WHITE).set("font-size", "20px")
                .set("font-weight", "600").set("margin", "0 0 24px 0")
                .set("text-align", "center");

        // ── Fields ────────────────────────────────
        TextField username = new TextField("Username");
        username.setWidthFull();
        username.setRequired(true);
        styleField(username.getStyle());
        username.getStyle().set("margin-bottom", "12px");

        TextField email = new TextField("Email");
        email.setWidthFull();
        email.setRequired(true);
        styleField(email.getStyle());
        email.getStyle().set("margin-bottom", "12px");

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        password.setRequired(true);
        styleField(password.getStyle());
        password.getStyle().set("margin-bottom", "12px");

        // ── Favourite team dropdown ───────────────
        ComboBox<Team> favouriteTeam = new ComboBox<>("Favourite Team (optional)");
        favouriteTeam.setWidthFull();
        favouriteTeam.setPlaceholder("Select your team");
        favouriteTeam.getStyle()
                .set("--vaadin-input-field-background", DARK)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-label-color", GREY_TEXT)
                .set("--vaadin-input-field-placeholder-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER)
                .set("--lumo-base-color", DARK_CARD)
                .set("--lumo-body-text-color", WHITE)
                .set("margin-bottom", "20px");

        List<Team> teams = teamRepository.findAll();
        favouriteTeam.setItems(teams);
        favouriteTeam.setItemLabelGenerator(Team::getTeamName);

        // ── Sign up button ────────────────────────
        Button signUpBtn = new Button("Create account");
        signUpBtn.setWidthFull();
        signUpBtn.getStyle()
                .set("background-color", BLUE).set("color", WHITE)
                .set("border", "none").set("border-radius", "8px")
                .set("padding", "12px").set("font-size", "15px")
                .set("font-weight", "600").set("cursor", "pointer")
                .set("margin-bottom", "16px");

        // ── Divider ───────────────────────────────
        Div dividerRow = new Div();
        dividerRow.getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("margin-bottom", "16px");
        Div l1 = new Div(); l1.getStyle().set("flex", "1").set("height", "1px").set("background-color", BORDER);
        Span or = new Span("or"); or.getStyle().set("color", GREY_TEXT).set("font-size", "12px").set("margin", "0 12px");
        Div l2 = new Div(); l2.getStyle().set("flex", "1").set("height", "1px").set("background-color", BORDER);
        dividerRow.add(l1, or, l2);

        // ── Sign in link ──────────────────────────
        Button signInLink = new Button("Already have an account? Sign in");
        signInLink.setWidthFull();
        signInLink.getStyle()
                .set("background-color", "transparent").set("color", WHITE)
                .set("border", "1px solid " + BORDER).set("border-radius", "8px")
                .set("padding", "12px").set("font-size", "14px").set("cursor", "pointer");
        signInLink.addClickListener(e -> UI.getCurrent().navigate("signin"));

        // ── Sign up logic ─────────────────────────
        signUpBtn.addClickListener(e -> {
            String usernameVal = username.getValue().trim();
            String emailVal    = email.getValue().trim();
            String passwordVal = password.getValue().trim();

            if (usernameVal.isEmpty()) {
                Notification.show("Please enter a username", 2000, Notification.Position.MIDDLE);
                return;
            }
            if (emailVal.isEmpty()) {
                Notification.show("Please enter an email", 2000, Notification.Position.MIDDLE);
                return;
            }
            if (passwordVal.isEmpty()) {
                Notification.show("Please enter a password", 2000, Notification.Position.MIDDLE);
                return;
            }

            Long teamId = favouriteTeam.getValue() != null
                    ? favouriteTeam.getValue().getTeamId() : null;

            boolean ok = userService.register(emailVal, passwordVal, usernameVal, teamId);

            if (!ok) {
                Notification.show("An account with this email already exists", 2000,
                        Notification.Position.MIDDLE);
            } else {
                Notification.show("Account created! Please sign in.", 2000,
                        Notification.Position.BOTTOM_START);
                UI.getCurrent().navigate("signin");
            }
        });

        card.add(brand, accent, heading, username, email, password,
                favouriteTeam, signUpBtn, dividerRow, signInLink);
        add(card);
    }

    private void styleField(com.vaadin.flow.dom.Style style) {
        style.set("--vaadin-input-field-background", DARK)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-label-color", GREY_TEXT)
                .set("--vaadin-input-field-placeholder-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER);
    }
}