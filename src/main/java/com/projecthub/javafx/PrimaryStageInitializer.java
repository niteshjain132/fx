package com.projecthub.javafx;

import atlantafx.base.theme.PrimerLight;
import com.projecthub.ui.main.MainViewController;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Configures the primary stage with AtlantaFX PrimerLight theme and the main dashboard.
 */
@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;

    public PrimaryStageInitializer(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        javafx.application.Application.setUserAgentStylesheet(
                new PrimerLight().getUserAgentStylesheet()
        );

        Scene scene = new Scene(fxWeaver.loadView(MainViewController.class), 1280, 800);
        var css = getClass().getResource("/styles/projecthub.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("ProjectHub");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);

        var icon = getClass().getResource("/icons/projecthub-icon.png");
        if (icon != null) {
            stage.getIcons().add(new Image(icon.toExternalForm()));
        }

        stage.show();
    }
}
