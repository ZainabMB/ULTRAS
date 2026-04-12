package com.example.views.components;

import com.example.service.ReviewService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class ReviewDialog extends Dialog {

    public ReviewDialog(
            Long userId,
            Long fixtureId,
            String matchLabel,
            ReviewService reviewService,
            Runnable onSaved
    ) {
        setWidth("360px");
        setCloseOnOutsideClick(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3("Write a Review");
        title.getStyle().set("margin", "0 0 4px 0");

        Span subtitle = new Span(matchLabel);
        subtitle.getStyle().set("color", "#666").set("font-size", "13px");

        TextArea reviewArea = new TextArea();
        reviewArea.setPlaceholder("Write your thoughts about this match...");
        reviewArea.setWidthFull();
        reviewArea.setMinHeight("120px");

        // Pre-fill if user already has a review
        reviewService.getUserReview(userId, fixtureId)
                .ifPresent(r -> reviewArea.setValue(r.getBody()));

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.getStyle().set("gap", "8px");

        Button cancel = new Button("Cancel");
        cancel.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ccc")
                .set("cursor", "pointer");
        cancel.addClickListener(e -> close());

        Button save = new Button("Save");
        save.getStyle()
                .set("background-color", "#1a1a1a")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer");

        save.addClickListener(e -> {
            String reviewText = reviewArea.getValue().trim();
            if (reviewText.isEmpty()) {
                Notification.show("Please write something", 2000,
                        Notification.Position.MIDDLE);
                return;
            }
            reviewService.submitReview(userId, fixtureId, reviewText);
            Notification.show("Review saved!", 2000, Notification.Position.BOTTOM_START);
            close();
            if (onSaved != null) onSaved.run();
        });

        buttons.add(cancel, save);
        content.add(title, subtitle, reviewArea, buttons);
        add(content);
    }
}