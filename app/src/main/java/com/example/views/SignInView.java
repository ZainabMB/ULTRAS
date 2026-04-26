package com.example.views;

import com.example.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;

@Route("signin")
@PageTitle("Sign In - Ultras")
public class SignInView extends VerticalLayout {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK      = "#1c1c1e";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public SignInView(UserService userService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(false);
        getStyle().set("background-color", DARK).set("min-height", "100vh");

        Div card = new Div();
        card.getStyle()
                .set("background-color", DARK_CARD)
                .set("border-radius", "16px")
                .set("border", "1px solid " + BORDER)
                .set("padding", "40px 32px")
                .set("width", "90%").set("max-width", "380px")
                .set("display", "flex").set("flex-direction", "column")
                .set("align-items", "stretch").set("box-sizing", "border-box");

        Span brand = new Span("ULTRAS");
        brand.getStyle().set("font-weight", "800").set("font-size", "22px")
                .set("letter-spacing", "4px").set("color", WHITE)
                .set("text-align", "center").set("display", "block")
                .set("margin-bottom", "4px");

        Div accent = new Div();
        accent.getStyle().set("width", "40px").set("height", "3px")
                .set("background-color", BLUE).set("border-radius", "2px")
                .set("margin", "0 auto 24px auto");

        H2 heading = new H2("Welcome back");
        heading.getStyle().set("color", WHITE).set("font-size", "20px")
                .set("font-weight", "600").set("margin", "0 0 24px 0")
                .set("text-align", "center");

        TextField email = new TextField("Email");
        email.setWidthFull();
        email.getStyle()
                .set("--vaadin-input-field-background", "#1c1c1e")
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-label-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER)
                .set("margin-bottom", "12px");

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        password.getStyle()
                .set("--vaadin-input-field-background", "#1c1c1e")
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-label-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER)
                .set("margin-bottom", "20px");

        Button signInBtn = new Button("Sign In");
        signInBtn.setWidthFull();
        signInBtn.getStyle()
                .set("background-color", BLUE).set("color", WHITE)
                .set("border", "none").set("border-radius", "8px")
                .set("padding", "12px").set("font-size", "15px")
                .set("font-weight", "600").set("cursor", "pointer")
                .set("margin-bottom", "16px");

        Div dividerRow = new Div();
        dividerRow.getStyle().set("display", "flex").set("align-items", "center")
                .set("margin-bottom", "16px");
        Div l1 = new Div(); l1.getStyle().set("flex", "1").set("height", "1px").set("background-color", BORDER);
        Span or = new Span("or"); or.getStyle().set("color", GREY_TEXT).set("font-size", "12px").set("margin", "0 12px");
        Div l2 = new Div(); l2.getStyle().set("flex", "1").set("height", "1px").set("background-color", BORDER);
        dividerRow.add(l1, or, l2);

        Button signUpLink = new Button("Create an account");
        signUpLink.setWidthFull();
        signUpLink.getStyle()
                .set("background-color", "transparent").set("color", WHITE)
                .set("border", "1px solid " + BORDER).set("border-radius", "8px")
                .set("padding", "12px").set("font-size", "14px").set("cursor", "pointer");
        signUpLink.addClickListener(e -> UI.getCurrent().navigate("signup"));

        signInBtn.addClickListener(e -> {
            var user = userService.login(email.getValue(), password.getValue());
            if (user == null) {
                Notification.show("Invalid email or password", 2000, Notification.Position.MIDDLE);
            } else {
                VaadinSession.getCurrent().setAttribute("userId", user.getUserId());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
                VaadinSession.getCurrent().getSession().setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext());
                UI.getCurrent().navigate("matches");
            }
        });

        card.add(brand, accent, heading, email, password, signInBtn, dividerRow, signUpLink);
        add(card);
    }
}