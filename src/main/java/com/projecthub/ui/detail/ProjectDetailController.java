package com.projecthub.ui.detail;

import com.projecthub.model.Project;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * In-app project detail view with Kanban board and repository file listing.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("ProjectDetailView.fxml")
public class ProjectDetailController {

    @FXML private VBox detailRoot;
    @FXML private Button backButton;
    @FXML private FontIcon backIcon;
    @FXML private Label projectTitle;
    @FXML private Label projectDescription;
    @FXML private Label branchLabel;
    @FXML private FlowPane techStackPane;
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private ListView<String> filesList;

    private Runnable onBack;

    @FXML
    public void initialize() {
        backIcon.setIconCode(Feather.ARROW_LEFT);
        backButton.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });
    }

    public void bind(Project project, Runnable onBack) {
        this.onBack = onBack;
        projectTitle.setText(project.getName());
        projectDescription.setText(project.getDescription());
        branchLabel.setText(project.getCurrentBranch() != null ? project.getCurrentBranch() : "—");

        techStackPane.getChildren().clear();
        for (String tech : project.getTechStack()) {
            Label badge = new Label(tech);
            badge.getStyleClass().add("tech-badge");
            techStackPane.getChildren().add(badge);
        }

        populateKanban(project);
        populateFiles(project.getWorkingDirectory());
    }

    private void populateKanban(Project project) {
        todoColumn.getChildren().setAll(
                kanbanCard("PH-201 Spec review", "Todo"),
                kanbanCard("PH-208 Update README", "Todo")
        );
        inProgressColumn.getChildren().setAll(
                kanbanCard("PH-142 Branch sync UI", "In Progress"),
                kanbanCard("PH-155 VS Code launcher", "In Progress")
        );
        doneColumn.getChildren().setAll(
                kanbanCard("PH-101 Dashboard shell", "Done"),
                kanbanCard("PH-110 AtlantaFX theme", "Done"),
                kanbanCard("PH-118 Metric cards", "Done")
        );
    }

    private VBox kanbanCard(String title, String status) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kanban-card");
        card.setPadding(new Insets(10, 12, 10, 12));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kanban-card-title");
        titleLabel.setWrapText(true);

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("kanban-card-status");

        card.getChildren().addAll(titleLabel, statusLabel);
        VBox.setMargin(card, new Insets(0, 0, 8, 0));
        return card;
    }

    private void populateFiles(Path workingDirectory) {
        filesList.getItems().clear();
        if (workingDirectory == null || !Files.isDirectory(workingDirectory)) {
            filesList.getItems().add("(directory unavailable)");
            return;
        }
        try (Stream<Path> stream = Files.list(workingDirectory)) {
            List<String> names = stream
                    .sorted(Comparator
                            .<Path, Boolean>comparing(p -> !Files.isDirectory(p))
                            .thenComparing(p -> p.getFileName().toString().toLowerCase()))
                    .map(p -> (Files.isDirectory(p) ? "[dir] " : "[file] ") + p.getFileName())
                    .limit(40)
                    .toList();
            if (names.isEmpty()) {
                filesList.getItems().add("(empty directory)");
            } else {
                filesList.getItems().addAll(names);
            }
        } catch (IOException ex) {
            filesList.getItems().add("(unable to list files)");
        }
    }
}
