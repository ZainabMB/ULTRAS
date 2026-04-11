package com.example.views;

import com.example.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
@Route("signin")
@PageTitle("Sign In - Ultras")
public class SignInView extends VerticalLayout {

    public SignInView(UserService userService) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
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

        H2 title2 = new H2("Welcome Back");

        TextField email = new TextField("Email");
        PasswordField password = new PasswordField("Password");

        Button signInBtn = new Button("Sign In");
        signInBtn.getStyle().set("background-color", "#1a1a1a").set("color", "white");

        Paragraph signUpLink = new Paragraph("Don't have an account? Sign up");
        signUpLink.getStyle().set("cursor", "pointer").set("color", "blue");
        signUpLink.addClickListener(e -> UI.getCurrent().navigate("signup"));

        signInBtn.addClickListener(e -> {
            var user = userService.login(email.getValue(), password.getValue());

            if (user == null) {
                Notification.show("Invalid email or password");
            } else {
                UI.getCurrent().navigate("matches");
            }
        });

        add(logoBox, title, title2, email, password, signInBtn, signUpLink);
    }
}
