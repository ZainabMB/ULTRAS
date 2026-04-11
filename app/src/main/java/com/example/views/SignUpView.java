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
@Route("signup")
@PageTitle("Sign Up - Ultras")
public class SignUpView extends VerticalLayout {

    public SignUpView(UserService userService) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Create Account");

        TextField fullName = new TextField("Username");
        TextField email = new TextField("Email");
        PasswordField password = new PasswordField("Password");

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.getStyle().set("background-color", "#1a1a1a").set("color", "white");

        Paragraph signInLink = new Paragraph("Already have an account? Sign in");
        signInLink.getStyle().set("cursor", "pointer").set("color", "blue");
        signInLink.addClickListener(e -> UI.getCurrent().navigate("signin"));

        signUpBtn.addClickListener(e -> {
            boolean ok = userService.register(
                    email.getValue(),
                    password.getValue(),
                    fullName.getValue()
            );

            if (!ok) {
                Notification.show("Email already exists");
            } else {
                Notification.show("Account created!");
                UI.getCurrent().navigate("signin");
            }
        });

        add(title, fullName, email, password, signUpBtn, signInLink);
    }
}
