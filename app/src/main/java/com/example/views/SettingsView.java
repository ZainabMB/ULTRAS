package com.example.views;

import com.example.model.Team;
import com.example.model.User;
import com.example.repository.TeamRepository;
import com.example.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Route("settings")
@PageTitle("Settings - Ultras")
public class SettingsView extends VerticalLayout {

    public SettingsView(UserService userService, TeamRepository teamRepository) {

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

        Span title = new Span("Settings");
        title.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("flex", "1").set("text-align", "center");

        header.add(back, title);

        if (userId == null) {
            add(header, new Paragraph("Please sign in to view settings."));
            return;
        }

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            add(header, new Paragraph("User not found."));
            return;
        }

        User user = userOpt.get();

        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle().set("gap", "16px");

        // ── Username section ──────────────────────
        content.add(buildSectionCard(
                "Username",
                buildUsernameSection(user, userId, userService)
        ));

        // ── Password section ──────────────────────
        content.add(buildSectionCard(
                "Change Password",
                buildPasswordSection(userId, userService)
        ));

        // ── Favourite team section ────────────────
        content.add(buildSectionCard(
                "Favourite Team",
                buildFavTeamSection(user, userId, userService, teamRepository)
        ));

        // ── Delete account section ────────────────
        content.add(buildDeleteSection(userId, userService));

        add(header, content);
    }

    // ── Username ──────────────────────────────────
    private VerticalLayout buildUsernameSection(User user, Long userId, UserService userService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "8px");

        TextField usernameField = new TextField();
        usernameField.setValue(user.getUsername());
        usernameField.setWidthFull();

        Button saveBtn = buildSaveButton("Save");
        saveBtn.addClickListener(e -> {
            String val = usernameField.getValue().trim();
            if (val.isEmpty()) {
                Notification.show("Username cannot be empty", 2000, Notification.Position.MIDDLE);
                return;
            }
            boolean ok = userService.updateUsername(userId, val);
            if (ok) {
                Notification.show("Username updated!", 2000, Notification.Position.BOTTOM_START);
            } else {
                Notification.show("Username already taken", 2000, Notification.Position.MIDDLE);
            }
        });

        layout.add(usernameField, saveBtn);
        return layout;
    }

    // ── Password ──────────────────────────────────
    private VerticalLayout buildPasswordSection(Long userId, UserService userService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "8px");

        PasswordField currentPassword = new PasswordField("Current password");
        currentPassword.setWidthFull();

        PasswordField newPassword = new PasswordField("New password");
        newPassword.setWidthFull();

        PasswordField confirmPassword = new PasswordField("Confirm new password");
        confirmPassword.setWidthFull();

        Button saveBtn = buildSaveButton("Change Password");
        saveBtn.addClickListener(e -> {
            String current = currentPassword.getValue();
            String newPwd = newPassword.getValue();
            String confirm = confirmPassword.getValue();

            if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                Notification.show("Please fill in all fields", 2000, Notification.Position.MIDDLE);
                return;
            }
            if (!newPwd.equals(confirm)) {
                Notification.show("New passwords do not match", 2000, Notification.Position.MIDDLE);
                return;
            }
            if (newPwd.length() < 6) {
                Notification.show("Password must be at least 6 characters", 2000,
                        Notification.Position.MIDDLE);
                return;
            }

            boolean ok = userService.changePassword(userId, current, newPwd);
            if (ok) {
                Notification.show("Password changed!", 2000, Notification.Position.BOTTOM_START);
                currentPassword.clear();
                newPassword.clear();
                confirmPassword.clear();
            } else {
                Notification.show("Current password is incorrect", 2000,
                        Notification.Position.MIDDLE);
            }
        });

        layout.add(currentPassword, newPassword, confirmPassword, saveBtn);
        return layout;
    }

    // ── Favourite team ────────────────────────────
    private VerticalLayout buildFavTeamSection(User user, Long userId,
                                               UserService userService,
                                               TeamRepository teamRepository) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "8px");

        List<Team> teams = teamRepository.findAll();

        ComboBox<Team> teamPicker = new ComboBox<>();
        teamPicker.setItems(teams);
        teamPicker.setItemLabelGenerator(Team::getTeamName);
        teamPicker.setWidthFull();
        teamPicker.setPlaceholder("Select your team");

        // Pre-select current favourite
        if (user.getFavTeamId() != null) {
            teams.stream()
                    .filter(t -> t.getTeamId().equals(user.getFavTeamId()))
                    .findFirst()
                    .ifPresent(teamPicker::setValue);
        }

        Button saveBtn = buildSaveButton("Save");
        saveBtn.addClickListener(e -> {
            Team selected = teamPicker.getValue();
            userService.updateFavouriteTeam(userId,
                    selected != null ? selected.getTeamId() : null);
            Notification.show("Favourite team updated!", 2000,
                    Notification.Position.BOTTOM_START);
        });

        layout.add(teamPicker, saveBtn);
        return layout;
    }

    // ── Delete account ────────────────────────────
    private Div buildDeleteSection(Long userId, UserService userService) {
        Div section = new Div();
        section.setWidthFull();
        section.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #fca5a5")
                .set("border-radius", "8px")
                .set("padding", "16px");

        Span label = new Span("Delete Account");
        label.getStyle().set("font-weight", "bold").set("font-size", "14px")
                .set("color", "#dc2626").set("display", "block")
                .set("margin-bottom", "4px");

        Span warning = new Span("This will permanently delete your account, ratings and reviews.");
        warning.getStyle().set("font-size", "12px").set("color", "#999")
                .set("display", "block").set("margin-bottom", "12px");

        Button deleteBtn = new Button("Delete my account");
        deleteBtn.getStyle()
                .set("background-color", "#dc2626")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("width", "100%");

        deleteBtn.addClickListener(e -> {
            // Confirm dialog before deleting
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete account?");
            dialog.setText("Are you sure? This cannot be undone.");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirm -> {
                userService.deleteAccount(userId);

                // Clear session and security context
                VaadinSession.getCurrent().getSession().invalidate();
                SecurityContextHolder.clearContext();

                UI.getCurrent().navigate("");
                Notification.show("Account deleted.", 3000, Notification.Position.MIDDLE);
            });

            dialog.open();
        });

        section.add(label, warning, deleteBtn);
        return section;
    }

    // ── Helpers ───────────────────────────────────
    private Div buildSectionCard(String sectionTitle, VerticalLayout inner) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "16px");

        Span title = new Span(sectionTitle);
        title.getStyle().set("font-weight", "bold").set("font-size", "14px")
                .set("display", "block").set("margin-bottom", "12px");

        card.add(title, inner);
        return card;
    }

    private Button buildSaveButton(String label) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("background-color", "#1a1a1a")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("width", "100%")
                .set("margin-top", "4px");
        return btn;
    }
}