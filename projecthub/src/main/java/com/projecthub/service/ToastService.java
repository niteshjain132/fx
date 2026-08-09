package com.projecthub.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.springframework.stereotype.Service;

/**
 * Non-blocking toast notifications overlaid on the main content area.
 */
@Service
public class ToastService {

    private StackPane host;

    public void attach(StackPane host) {
        this.host = host;
    }

    public void showSuccess(String message) {
        show(message, "toast-success");
    }

    public void showError(String message) {
        show(message, "toast-error");
    }

    public void showInfo(String message) {
        show(message, "toast-info");
    }

    private void show(String message, String styleClass) {
        if (host == null) {
            return;
        }
        Platform.runLater(() -> {
            Label toast = new Label(message);
            toast.getStyleClass().addAll("toast", styleClass);
            toast.setMaxWidth(420);
            toast.setWrapText(true);
            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            toast.setTranslateY(-24);
            toast.setOpacity(0);

            host.getChildren().add(toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            PauseTransition pause = new PauseTransition(Duration.seconds(2.6));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> host.getChildren().remove(toast));
                fadeOut.play();
            });
            pause.play();
        });
    }
}
