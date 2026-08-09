package com.projecthub.javafx;

import com.projecthub.ProjectHubApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX Application that initializes the Spring Boot context and publishes
 * a {@link StageReadyEvent} once the primary stage is available.
 */
public class ProjectHubJavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder()
                .sources(ProjectHubApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) {
        applicationContext.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}
