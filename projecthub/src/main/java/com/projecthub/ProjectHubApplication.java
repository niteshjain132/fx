package com.projecthub;

import com.projecthub.javafx.ProjectHubJavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ProjectHub entry point. Launches JavaFX via a custom Application subclass
 * that bootstraps the Spring context (FxWeaver pattern).
 */
@SpringBootApplication
public class ProjectHubApplication {

    public static void main(String[] args) {
        Application.launch(ProjectHubJavaFxApplication.class, args);
    }
}
