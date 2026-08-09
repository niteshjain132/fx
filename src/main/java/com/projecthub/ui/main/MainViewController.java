package com.projecthub.ui.main;

import com.projecthub.event.ProjectSelectedEvent;
import com.projecthub.model.ActivityItem;
import com.projecthub.model.Milestone;
import com.projecthub.model.Project;
import com.projecthub.service.ProjectDataService;
import com.projecthub.service.ToastService;
import com.projecthub.ui.card.ProjectCardController;
import com.projecthub.ui.detail.ProjectDetailController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Main application shell: dark slate sidebar + dashboard canvas.
 */
@Component
@FxmlView("MainView.fxml")
public class MainViewController {

    private final ProjectDataService projectDataService;
    private final ToastService toastService;
    private final FxWeaver fxWeaver;

    @FXML private StackPane rootStack;
    @FXML private VBox sidebar;
    @FXML private Label brandTitle;
    @FXML private FontIcon brandIcon;
    @FXML private Button navDashboard;
    @FXML private Button navProjects;
    @FXML private Button navTasks;
    @FXML private Button navReleases;
    @FXML private Button navAnalytics;
    @FXML private Button settingsButton;
    @FXML private Label springStatusLabel;
    @FXML private StackPane contentHost;
    @FXML private VBox dashboardView;
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;
    @FXML private ComboBox<String> workspaceSelector;
    @FXML private Label metricProjectsValue;
    @FXML private Label metricProjectsTrend;
    @FXML private Label metricTasksValue;
    @FXML private Label metricTasksBadge;
    @FXML private Label metricReleasesValue;
    @FXML private Label metricVelocityValue;
    @FXML private VBox projectListContainer;
    @FXML private VBox milestonesContainer;
    @FXML private VBox activityContainer;
    @FXML private FontIcon settingsIcon;

    public MainViewController(
            ProjectDataService projectDataService,
            ToastService toastService,
            FxWeaver fxWeaver
    ) {
        this.projectDataService = projectDataService;
        this.toastService = toastService;
        this.fxWeaver = fxWeaver;
    }

    @FXML
    public void initialize() {
        toastService.attach(rootStack);
        brandIcon.setIconCode(Feather.BOX);
        settingsIcon.setIconCode(Feather.SETTINGS);
        springStatusLabel.setText("Spring Engine: OK");

        workspaceSelector.getItems().setAll("Engineering", "Platform", "Mobile", "Data");
        workspaceSelector.getSelectionModel().selectFirst();

        setActiveNav(navDashboard);
        wireNav(navDashboard, "Dashboard");
        wireNav(navProjects, "Active Projects");
        wireNav(navTasks, "Task Board");
        wireNav(navReleases, "Releases");
        wireNav(navAnalytics, "Team Analytics");

        settingsButton.setOnAction(e -> toastService.showInfo("Settings panel coming soon"));

        populateMetrics();
        populateProjects();
        populateMilestones();
        populateActivity();
    }

    @EventListener
    public void onProjectSelected(ProjectSelectedEvent event) {
        showProjectDetail(event.getProject());
    }

    public void showDashboard() {
        contentHost.getChildren().setAll(dashboardView);
        setActiveNav(navDashboard);
    }

    private void showProjectDetail(Project project) {
        var controllerAndView = fxWeaver.load(ProjectDetailController.class);
        ProjectDetailController detailController = controllerAndView.getController();
        detailController.bind(project, this::showDashboard);
        controllerAndView.getView().ifPresent(view -> contentHost.getChildren().setAll(view));
        setActiveNav(navProjects);
    }

    private void populateMetrics() {
        var metrics = projectDataService.getDashboardMetrics();
        metricProjectsValue.setText(String.valueOf(metrics.activeProjects()));
        metricProjectsTrend.setText(metrics.projectsTrend());
        metricTasksValue.setText(String.valueOf(metrics.openSprintTasks()));
        metricTasksBadge.setText(metrics.tasksBadge());
        metricReleasesValue.setText(String.valueOf(metrics.releasesThisWeek()));
        metricVelocityValue.setText(metrics.velocityPoints() + " pts/week");
    }

    private void populateProjects() {
        projectListContainer.getChildren().clear();
        for (Project project : projectDataService.getActiveProjects()) {
            var pair = fxWeaver.load(ProjectCardController.class);
            pair.getController().bind(project);
            pair.getView().ifPresent(view -> {
                VBox.setMargin(view, new Insets(0, 0, 12, 0));
                projectListContainer.getChildren().add(view);
            });
        }
    }

    private void populateMilestones() {
        milestonesContainer.getChildren().clear();
        for (Milestone milestone : projectDataService.getUpcomingMilestones()) {
            HBox row = new HBox(10);
            row.getStyleClass().add("milestone-row");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            FontIcon icon = new FontIcon(Feather.FLAG);
            icon.getStyleClass().add("milestone-icon");

            VBox text = new VBox(2);
            Label title = new Label(milestone.title());
            title.getStyleClass().add("milestone-title");
            Label days = new Label(milestone.daysRemaining() + " days");
            days.getStyleClass().add("milestone-days");
            text.getChildren().addAll(title, days);

            Label status = new Label(milestone.status().replace('-', ' '));
            status.getStyleClass().addAll("milestone-status", "status-" + milestone.status());
            HBox.setHgrow(text, Priority.ALWAYS);

            row.getChildren().addAll(icon, text, status);
            milestonesContainer.getChildren().add(row);
        }
    }

    private void populateActivity() {
        activityContainer.getChildren().clear();
        for (ActivityItem item : projectDataService.getTeamActivity()) {
            HBox row = new HBox(10);
            row.getStyleClass().add("activity-row");
            row.setAlignment(javafx.geometry.Pos.TOP_LEFT);

            FontIcon icon = new FontIcon(iconForActivity(item.kind()));
            icon.getStyleClass().add("activity-icon");

            VBox text = new VBox(2);
            Label actor = new Label(item.actor());
            actor.getStyleClass().add("activity-actor");
            Label action = new Label(item.action());
            action.getStyleClass().add("activity-action");
            action.setWrapText(true);
            Label time = new Label(item.timestamp());
            time.getStyleClass().add("activity-time");
            text.getChildren().addAll(actor, action, time);
            HBox.setHgrow(text, Priority.ALWAYS);

            row.getChildren().addAll(icon, text);
            activityContainer.getChildren().add(row);
        }
    }

    private Feather iconForActivity(String kind) {
        return switch (kind) {
            case "commit" -> Feather.GIT_COMMIT;
            case "task" -> Feather.CHECK_SQUARE;
            case "pr" -> Feather.GIT_PULL_REQUEST;
            case "release" -> Feather.PACKAGE;
            default -> Feather.MESSAGE_CIRCLE;
        };
    }

    private void wireNav(Button button, String label) {
        button.setOnAction(e -> {
            setActiveNav(button);
            if (button == navDashboard) {
                showDashboard();
            } else {
                toastService.showInfo(label + " — navigation stub");
            }
        });
    }

    private void setActiveNav(Button active) {
        for (Button button : new Button[]{navDashboard, navProjects, navTasks, navReleases, navAnalytics}) {
            button.getStyleClass().remove("nav-active");
        }
        if (!active.getStyleClass().contains("nav-active")) {
            active.getStyleClass().add("nav-active");
        }
    }
}
