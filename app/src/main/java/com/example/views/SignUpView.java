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

    public SignUpView(UserService userService, TeamRepository teamRepository) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Create Account");

        TextField username = new TextField("Username");
        TextField email = new TextField("Email");
        PasswordField password = new PasswordField("Password");

        // Favourite team dropdown
        ComboBox<Team> favouriteTeam = new ComboBox<>("Favourite Team");
        favouriteTeam.setWidth("280px");
        favouriteTeam.setPlaceholder("Select your team");

        // Load all teams from DB
        List<Team> teams = teamRepository.findAll();
        favouriteTeam.setItems(teams);

        // Show team name in dropdown
        favouriteTeam.setItemLabelGenerator(Team::getTeamName);

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.getStyle().set("background-color", "#1a1a1a").set("color", "white");

        Paragraph signInLink = new Paragraph("Already have an account? Sign in");
        signInLink.getStyle().set("cursor", "pointer").set("color", "blue");
        signInLink.addClickListener(e -> UI.getCurrent().navigate("signin"));

        signUpBtn.addClickListener(e -> {
            String usernameVal = username.getValue().trim();
            String emailVal = email.getValue().trim();
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
                    ? favouriteTeam.getValue().getTeamId()
                    : null;

            boolean ok = userService.register(emailVal, passwordVal, usernameVal, teamId);

            if (!ok) {
                Notification.show("Email already exists");
            } else {
                Notification.show("Account created!");
                UI.getCurrent().navigate("signin");
            }
        });
        username.setRequired(true);
        email.setRequired(true);
        password.setRequired(true);

        add(title, username, email, password, favouriteTeam, signUpBtn, signInLink);
    }
}