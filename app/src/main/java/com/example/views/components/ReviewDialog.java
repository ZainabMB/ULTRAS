package com.example.views.components;

import com.example.service.ReviewService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class ReviewDialog extends Dialog {

    private static final String BLUE      = "rgb(0,97,127)";
    private static final String DARK_CARD = "#2a2a2e";
    private static final String BORDER    = "#3a3a3e";
    private static final String GREY_TEXT = "#a0a0a8";
    private static final String WHITE     = "#ffffff";

    public ReviewDialog(Long userId, Long fixtureId, String matchLabel,
                        ReviewService reviewService, Runnable onSave) {

        // Style the dialog itself dark
        getElement().getStyle()
                .set("background-color", DARK_CARD)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "12px")
                .set("padding", "0");

        setWidth("90%");
        setMaxWidth("420px");
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle()
                .set("background-color", DARK_CARD)
                .set("border-radius", "12px")
                .set("gap", "12px");

        // Header
        Span title = new Span("Write a review");
        title.getStyle().set("font-weight", "bold").set("font-size", "16px").set("color", WHITE);

        Span subtitle = new Span(matchLabel);
        subtitle.getStyle().set("font-size", "12px").set("color", GREY_TEXT);

        // Text area
        TextArea reviewArea = new TextArea();
        reviewArea.setPlaceholder("Share your thoughts on this match...");
        reviewArea.setWidthFull();
        reviewArea.setMinHeight("120px");
        reviewArea.getStyle()
                .set("--vaadin-input-field-background", "#1c1c1e")
                .set("--vaadin-input-field-value-color", WHITE)
                .set("--vaadin-input-field-placeholder-color", GREY_TEXT)
                .set("--vaadin-input-field-border-color", BORDER)
                .set("--lumo-base-color", "#1c1c1e")
                .set("--lumo-body-text-color", WHITE);

        // Pre-fill if user already has a review
        reviewService.getUserReview(userId, fixtureId)
                .ifPresent(r -> reviewArea.setValue(r.getBody()));

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.getStyle().set("gap", "8px");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyle()
                .set("background-color", "transparent").set("color", GREY_TEXT)
                .set("border", "1px solid " + BORDER).set("border-radius", "8px")
                .set("cursor", "pointer").set("flex", "1");
        cancelBtn.addClickListener(e -> close());

        Button saveBtn = new Button("Save review");
        saveBtn.getStyle()
                .set("background-color", BLUE).set("color", WHITE)
                .set("border", "none").set("border-radius", "8px")
                .set("cursor", "pointer").set("flex", "1").set("font-weight", "600");

        saveBtn.addClickListener(e -> {
            String body = reviewArea.getValue().trim();
            if (body.isEmpty()) {
                Notification.show("Review cannot be empty", 2000, Notification.Position.MIDDLE);
                return;
            }
            reviewService.submitReview(userId, fixtureId, body);
            Notification.show("Review saved!", 2000, Notification.Position.BOTTOM_START);
            close();
            onSave.run();
        });

        buttons.add(cancelBtn, saveBtn);
        content.add(title, subtitle, reviewArea, buttons);
        add(content);
    }
}