package com.projecthub.ui.card;

import com.projecthub.event.ProjectSelectedEvent;
import com.projecthub.model.Project;
import com.projecthub.service.GitService;
import com.projecthub.service.ToastService;
import com.projecthub.service.VSCodeLauncherService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interactive project card: progress, tech badges, git branch ComboBox, and Open (VS Code).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FxmlView("ProjectCard.fxml")
public class ProjectCardController {

    private final GitService gitService;
    private final VSCodeLauncherService vsCodeLauncherService;
    private final ToastService toastService;
    private final ApplicationEventPublisher eventPublisher;

    private Project project;
    private boolean suppressingBranchEvents;

    @FXML private VBox cardRoot;
    @FXML private Label projectNameLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private FlowPane techStackPane;
    @FXML private ComboBox<String> branchComboBox;
    @FXML private Button openButton;
    @FXML private FontIcon openIcon;

    public ProjectCardController(
            GitService gitService,
            VSCodeLauncherService vsCodeLauncherService,
            ToastService toastService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.gitService = gitService;
        this.vsCodeLauncherService = vsCodeLauncherService;
        this.toastService = toastService;
        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void initialize() {
        openIcon.setIconCode(Feather.EXTERNAL_LINK);
        openButton.setOnAction(e -> onOpenClicked());
        branchComboBox.setOnAction(e -> onBranchSelected());

        // Card body click selects the project; ignore clicks on branch ComboBox / Open button
        cardRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onCardClicked);
    }

    /**
     * Bind this card instance to a project model and load git branches.
     */
    public void bind(Project project) {
        this.project = project;
        projectNameLabel.setText(project.getName());
        progressLabel.setText(project.getProgressPercent() + "%");
        progressBar.setProgress(project.getProgressPercent() / 100.0);

        techStackPane.getChildren().clear();
        for (String tech : project.getTechStack()) {
            Label badge = new Label(tech);
            badge.getStyleClass().add("tech-badge");
            techStackPane.getChildren().add(badge);
        }

        loadBranches();
    }

    private void loadBranches() {
        suppressingBranchEvents = true;
        try {
            List<String> branches = gitService.listBranches(project.getWorkingDirectory());
            if (branches.isEmpty()) {
                // Demo fallback when the directory is not a git repo
                branches = List.of(
                        project.getCurrentBranch() != null ? project.getCurrentBranch() : "main",
                        "main",
                        "feature/platform-core",
                        "hotfix/auth-leak"
                );
                branches = branches.stream().distinct().toList();
            }
            branchComboBox.getItems().setAll(branches);

            String current = project.getCurrentBranch();
            if (current != null && branchComboBox.getItems().contains(current)) {
                branchComboBox.getSelectionModel().select(current);
            } else if (!branchComboBox.getItems().isEmpty()) {
                branchComboBox.getSelectionModel().selectFirst();
            }
        } finally {
            suppressingBranchEvents = false;
        }
    }

    private void onOpenClicked() {
        if (project == null) {
            return;
        }
        boolean opened = vsCodeLauncherService.openInVSCode(project.getWorkingDirectory());
        if (opened) {
            toastService.showSuccess("Opening " + project.getName() + " in VS Code");
        } else {
            toastService.showError("Could not launch VS Code for " + project.getName());
        }
    }

    private void onBranchSelected() {
        if (suppressingBranchEvents || project == null) {
            return;
        }
        String selected = branchComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            return;
        }
        if (selected.equals(project.getCurrentBranch())) {
            return;
        }

        toastService.showInfo("Checking out " + selected + "…");
        gitService.checkoutBranch(project.getWorkingDirectory(), selected)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        project = project.withCurrentBranch(selected);
                        toastService.showSuccess("Switched to branch " + selected);
                    } else {
                        toastService.showError("Failed to checkout " + selected);
                        suppressingBranchEvents = true;
                        try {
                            if (project.getCurrentBranch() != null) {
                                branchComboBox.getSelectionModel().select(project.getCurrentBranch());
                            }
                        } finally {
                            suppressingBranchEvents = false;
                        }
                    }
                }));
    }

    private void onCardClicked(MouseEvent event) {
        if (project == null) {
            return;
        }
        // Do not treat interactions with the branch dropdown or Open button as card selection
        if (isDescendantOf(event.getTarget(), branchComboBox) || isDescendantOf(event.getTarget(), openButton)) {
            return;
        }
        eventPublisher.publishEvent(new ProjectSelectedEvent(this, project));
    }

    private boolean isDescendantOf(Object target, javafx.scene.Node ancestor) {
        if (!(target instanceof javafx.scene.Node node) || ancestor == null) {
            return false;
        }
        javafx.scene.Node current = node;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
