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

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String DARK_NAV  = "#232326";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public SettingsView(UserService userService, TeamRepository teamRepository) {
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

        Span title = new Span("Settings");
        title.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("color", WHITE).set("flex", "1").set("text-align", "center");

        Button signOutBtn = new Button("Sign out");
        signOutBtn.getStyle()
                .set("background", "none").set("border", "none")
                .set("cursor", "pointer").set("color", "#ef4444")
                .set("font-size", "13px").set("padding", "0");
        signOutBtn.addClickListener(e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            SecurityContextHolder.clearContext();
            UI.getCurrent().navigate("");
        });

        header.add(back, title, signOutBtn);

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
        content.getStyle().set("gap", "16px").set("width", "95%");

        content.add(buildSectionCard("Username", buildUsernameSection(user, userId, userService)));
        content.add(buildSectionCard("Change Password", buildPasswordSection(userId, userService)));
        content.add(buildSectionCard("Favourite Team", buildFavTeamSection(user, userId, userService, teamRepository)));
        content.add(buildDeleteSection(userId, userService));

        add(header, content);
    }

    private VerticalLayout buildUsernameSection(User user, Long userId, UserService userService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "8px");

        TextField usernameField = new TextField();
        usernameField.setValue(user.getUsername());
        usernameField.setWidthFull();
        styleField(usernameField.getStyle());

        Button saveBtn = buildSaveButton("Save");
        saveBtn.addClickListener(e -> {
            String val = usernameField.getValue().trim();
            if (val.isEmpty()) { Notification.show("Username cannot be empty", 2000, Notification.Position.MIDDLE); return; }
            boolean ok = userService.updateUsername(userId, val);
            Notification.show(ok ? "Username updated!" : "Username already taken", 2000,
                    ok ? Notification.Position.BOTTOM_START : Notification.Position.MIDDLE);
        });

        layout.add(usernameField, saveBtn);
        return layout;
    }

    private VerticalLayout buildPasswordSection(Long userId, UserService userService) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "8px");

        PasswordField current = new PasswordField("Current password");
        PasswordField newPwd = new PasswordField("New password");
        PasswordField confirm = new PasswordField("Confirm new password");

        for (PasswordField f : new PasswordField[]{current, newPwd, confirm}) {
            f.setWidthFull();
            styleField(f.getStyle());
        }

        Button saveBtn = buildSaveButton("Change Password");
        saveBtn.addClickListener(e -> {
            if (current.getValue().isEmpty() || newPwd.getValue().isEmpty() || confirm.getValue().isEmpty()) {
                Notification.show("Please fill in all fields", 2000, Notification.Position.MIDDLE); return;
            }
            if (!newPwd.getValue().equals(confirm.getValue())) {
                Notification.show("Passwords do not match", 2000, Notification.Position.MIDDLE); return;
            }
            if (newPwd.getValue().length() < 6) {
                Notification.show("Password must be at least 6 characters", 2000, Notification.Position.MIDDLE); return;
            }
            boolean ok = userService.changePassword(userId, current.getValue(), newPwd.getValue());
            if (ok) {
                Notification.show("Password changed!", 2000, Notification.Position.BOTTOM_START);
                current.clear(); newPwd.clear(); confirm.clear();
            } else {
                Notification.show("Current password is incorrect", 2000, Notification.Position.MIDDLE);
            }
        });

        layout.add(current, newPwd, confirm, saveBtn);
        return layout;
    }

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
        teamPicker.getStyle()
                .set("--vaadin-input-field-background", DARK)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-border-color", BORDER);

        if (user.getFavTeamId() != null) {
            teams.stream().filter(t -> t.getTeamId().equals(user.getFavTeamId()))
                    .findFirst().ifPresent(teamPicker::setValue);
        }

        Button saveBtn = buildSaveButton("Save");
        saveBtn.addClickListener(e -> {
            Team selected = teamPicker.getValue();
            userService.updateFavouriteTeam(userId, selected != null ? selected.getTeamId() : null);
            Notification.show("Favourite team updated!", 2000, Notification.Position.BOTTOM_START);
        });

        layout.add(teamPicker, saveBtn);
        return layout;
    }

    private Div buildDeleteSection(Long userId, UserService userService) {
        Div section = new Div();
        section.setWidthFull();
        section.getStyle()
                .set("background-color", "#2a1a1a")
                .set("border", "1px solid #5a2a2a")
                .set("border-radius", "10px").set("padding", "16px");

        Span label = new Span("Delete Account");
        label.getStyle().set("font-weight", "bold").set("font-size", "14px")
                .set("color", "#ef4444").set("display", "block").set("margin-bottom", "4px");

        Span warning = new Span("This will permanently delete your account, ratings and reviews.");
        warning.getStyle().set("font-size", "12px").set("color", GREY_TEXT)
                .set("display", "block").set("margin-bottom", "12px");

        Button deleteBtn = new Button("Delete my account");
        deleteBtn.getStyle()
                .set("background-color", "#ef4444").set("color", WHITE)
                .set("border", "none").set("border-radius", "8px")
                .set("cursor", "pointer").set("width", "100%").set("padding", "10px");

        deleteBtn.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete account?");
            dialog.setText("Are you sure? This cannot be undone.");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(confirm -> {
                userService.deleteAccount(userId);
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

    private Div buildSectionCard(String sectionTitle, VerticalLayout inner) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "10px").set("padding", "16px");

        Span title = new Span(sectionTitle);
        title.getStyle().set("font-weight", "bold").set("font-size", "13px")
                .set("color", GREY_TEXT).set("letter-spacing", "0.5px")
                .set("display", "block").set("margin-bottom", "12px");

        card.add(title, inner);
        return card;
    }

    private Button buildSaveButton(String label) {
        Button btn = new Button(label);
        btn.getStyle()
                .set("background-color", BLUE).set("color", WHITE)
                .set("border", "none").set("border-radius", "8px")
                .set("cursor", "pointer").set("width", "100%")
                .set("padding", "10px").set("margin-top", "4px")
                .set("font-weight", "600");
        return btn;
    }

    private void styleField(com.vaadin.flow.dom.Style style) {
        style.set("--vaadin-input-field-background", DARK)
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-label-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER);
    }
}