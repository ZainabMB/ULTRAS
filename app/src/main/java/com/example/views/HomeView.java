package com.example.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("/")
@PageTitle("Ultras")
public class HomeView extends VerticalLayout {

    // ── Theme constants ───────────────────────────
    private static final String BLUE       = "rgb(0,97,127)";
    private static final String BLUE_DARK  = "rgb(0,75,99)";
    private static final String DARK       = "#1c1c1e";
    private static final String DARK_CARD  = "#2a2a2e";
    private static final String GREY_TEXT  = "#a0a0a8";
    private static final String WHITE      = "#ffffff";

    public HomeView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(false);
        setSpacing(false);

        // Dark background fills full page
        getStyle()
                .set("background-color", DARK)
                .set("min-height", "100vh");

        // ── Card container ────────────────────────
        Div card = new Div();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border-radius", "16px")
                .set("border", "1px solid #3a3a3e")
                .set("padding", "48px 40px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("width", "90%")
                .set("max-width", "400px")
                .set("box-sizing", "border-box");

        // ── Logo ──────────────────────────────────
        // Replace Image src with your actual filename
        /*Image logo = new Image("logo.jpeg", "Ultras logo");
        logo.setWidth("100px");
        logo.setHeight("100px");
        logo.getStyle()
                .set("object-fit", "contain")
                .set("margin-bottom", "24px");*/

        // ── Title ─────────────────────────────────
        H1 title = new H1("ULTRAS");
        title.getStyle()
                .set("margin", "0 0 6px 0")
                .set("font-size", "32px")
                .set("font-weight", "800")
                .set("letter-spacing", "4px")
                .set("color", WHITE);

        // Blue accent underline
        Div accent = new Div();
        accent.getStyle()
                .set("width", "48px")
                .set("height", "3px")
                .set("background-color", BLUE)
                .set("border-radius", "2px")
                .set("margin", "0 auto 20px auto");

        // ── Subtitle ──────────────────────────────
        Paragraph subtitle = new Paragraph("Rate and review football matches");
        subtitle.getStyle()
                .set("color", GREY_TEXT)
                .set("margin", "0 0 36px 0")
                .set("font-size", "14px")
                .set("text-align", "center");

        // ── Buttons ───────────────────────────────
        // Sign In — primary blue button
        Button signInBtn = new Button("Sign In");
        signInBtn.setWidthFull();
        signInBtn.getStyle()
                .set("background-color", BLUE)
                .set("color", WHITE)
                .set("border", "none")
                .set("border-radius", "8px")
                .set("padding", "12px")
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("margin-bottom", "12px")
                .set("transition", "background-color 0.2s");
        signInBtn.addClickListener(e -> UI.getCurrent().navigate("signin"));

        // Sign Up — outline button
        Button signUpBtn = new Button("Create an account");
        signUpBtn.setWidthFull();
        signUpBtn.getStyle()
                .set("background-color", "transparent")
                .set("color", WHITE)
                .set("border", "1px solid #3a3a3e")
                .set("border-radius", "8px")
                .set("padding", "12px")
                .set("font-size", "15px")
                .set("font-weight", "500")
                .set("cursor", "pointer")
                .set("transition", "border-color 0.2s");
        signUpBtn.addClickListener(e -> UI.getCurrent().navigate("signup"));

        // ── Divider with "or" ─────────────────────
        HorizontalLayout dividerRow = new HorizontalLayout();
        dividerRow.setWidthFull();
        dividerRow.setAlignItems(Alignment.CENTER);
        dividerRow.getStyle().set("margin", "4px 0");

        Div line1 = new Div();
        line1.getStyle()
                .set("flex", "1").set("height", "1px")
                .set("background-color", "#3a3a3e");

        Span orText = new Span("or");
        orText.getStyle()
                .set("color", GREY_TEXT).set("font-size", "12px")
                .set("margin", "0 12px");

        Div line2 = new Div();
        line2.getStyle()
                .set("flex", "1").set("height", "1px")
                .set("background-color", "#3a3a3e");

        dividerRow.add(line1, orText, line2);

        // ── Continue as guest ─────────────────────
        Button guestBtn = new Button("Continue as guest");
        guestBtn.setWidthFull();
        guestBtn.getStyle()
                .set("background-color", "transparent")
                .set("color", GREY_TEXT)
                .set("border", "none")
                .set("font-size", "13px")
                .set("cursor", "pointer")
                .set("padding", "8px")
                .set("margin-top", "4px");
        guestBtn.addClickListener(e -> UI.getCurrent().navigate("matches"));

        card.add( title, accent, subtitle,
                signInBtn, dividerRow, signUpBtn, guestBtn);

        add(card);
    }
}