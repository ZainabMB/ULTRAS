package com.example.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Ultras")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle().set("background-color", "#f5f5f5");

        // Logo placeholder
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

        // Title
        H1 title = new H1("'ULTRAS'");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "36px");

        // Subtitle
        Paragraph subtitle = new Paragraph("A football rating and reviewing platform");
        subtitle.getStyle()
                .set("color", "#666")
                .set("margin", "0 0 32px 0")
                .set("font-size", "14px");

        // Guest button
        Button signUp = new Button("SignUp");
        signUp.getStyle()
                .set("margin-right", "12px")
                .set("background-color", "white")
                .set("border", "1px solid #ccc")
                .set("cursor", "pointer");
        signUp.addClickListener(e -> UI.getCurrent().navigate("signup"));

        // Sign in button
        Button signInBtn = new Button("Sign In");
        signInBtn.getStyle()
                .set("background-color", "#1a1a1a")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer");
        signInBtn.addClickListener(e -> UI.getCurrent().navigate("signin"));

        add(logoBox, title, subtitle, signUp, signInBtn);
    }
}