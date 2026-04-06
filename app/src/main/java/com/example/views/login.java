package com.example.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Ultras - Login")
public class login extends VerticalLayout {

    public login() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        getStyle().set("background-color", "#f5f5f5");

        // Logo placeholder (same as HomeView)
        Div logoBox = new Div();
        logoBox.getStyle()
                .set("width", "120px")
                .set("height", "120px")
                .set("background-color", "#e0e0e0")
                .set("border", "1px solid #ccc")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "24px");

        Span logoText = new Span("[ logo ]");
        logoText.getStyle().set("color", "#999").set("font-size", "13px");
        logoBox.add(logoText);

        // Title (same as HomeView)
        H1 title = new H1("'ULTRAS'");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "36px");

        // Subtitle
        Paragraph subtitle = new Paragraph("Sign in to your account");
        subtitle.getStyle()
                .set("color", "#666")
                .set("margin", "0 0 24px 0")
                .set("font-size", "14px");

        // Username field
        TextField username = new TextField("Username");
        username.setWidth("280px");

        // Password field — PasswordField masks input as ***
        PasswordField password = new PasswordField("Password");
        password.setWidth("280px");

        // Sign in button (same style as HomeView)
        Button signInBtn = new Button("Sign In");
        signInBtn.getStyle()
                .set("background-color", "#1a1a1a")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("width", "280px")
                .set("margin-top", "8px");
        signInBtn.addClickListener(e -> {
            // TODO: add authentication logic here
            UI.getCurrent().navigate("matches");
        });

        // Back button (same style as guest button in HomeView)
        Button backBtn = new Button("Back");
        backBtn.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ccc")
                .set("cursor", "pointer")
                .set("width", "280px")
                .set("margin-top", "8px");
        backBtn.addClickListener(e -> UI.getCurrent().navigate(""));

        add(logoBox, title, subtitle, username, password, signInBtn, backBtn);
    }
}