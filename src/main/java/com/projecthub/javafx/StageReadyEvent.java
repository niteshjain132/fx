package com.projecthub.javafx;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
 * Published when the JavaFX primary stage is ready to be configured.
 */
public class StageReadyEvent extends ApplicationEvent {

    public StageReadyEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return (Stage) getSource();
    }
}
