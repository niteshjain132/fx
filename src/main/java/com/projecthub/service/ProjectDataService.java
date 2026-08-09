package com.projecthub.service;

import com.projecthub.model.ActivityItem;
import com.projecthub.model.Milestone;
import com.projecthub.model.Project;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * Provides sample / workspace project data for the dashboard.
 */
@Service
public class ProjectDataService {

    public List<Project> getActiveProjects() {
        Path home = Path.of(System.getProperty("user.home"));
        Path cwd = Path.of("").toAbsolutePath();

        return List.of(
                new Project(
                        "spring-microservices-v2",
                        "Spring Microservices V2",
                        58,
                        List.of("Kotlin", "Docker", "Java 21", "Spring Boot"),
                        cwd,
                        "feature/platform-core",
                        "Platform modernization of the microservice mesh with Java 21 and Spring Boot 3."
                ),
                new Project(
                        "projecthub-desktop",
                        "ProjectHub Desktop",
                        72,
                        List.of("Java 21", "JavaFX", "AtlantaFX", "Spring Boot"),
                        cwd,
                        "main",
                        "macOS-native engineering dashboard with Git and VS Code integration."
                ),
                new Project(
                        "auth-gateway",
                        "Auth Gateway",
                        34,
                        List.of("Java 21", "Spring Security", "OAuth2"),
                        home.resolve("projects/auth-gateway"),
                        "hotfix/auth-leak",
                        "Centralized authentication gateway with token rotation and audit trails."
                ),
                new Project(
                        "release-pipeline",
                        "Release Pipeline",
                        91,
                        List.of("GitHub Actions", "Docker", "Kotlin"),
                        home.resolve("projects/release-pipeline"),
                        "main",
                        "Automated release orchestration with canary deployments."
                )
        );
    }

    public List<Milestone> getUpcomingMilestones() {
        return List.of(
                new Milestone("Q3 Milestone", 12, "on-track"),
                new Milestone("API Freeze", 5, "urgent"),
                new Milestone("Beta Ship", 21, "planned"),
                new Milestone("Security Audit", 8, "on-track")
        );
    }

    public List<ActivityItem> getTeamActivity() {
        return List.of(
                new ActivityItem("maya.chen", "pushed 4 commits to feature/platform-core", "2m ago", "commit"),
                new ActivityItem("jordan.lee", "moved task PH-142 to In Review", "18m ago", "task"),
                new ActivityItem("sam.okafor", "opened PR #88 — auth token rotation", "41m ago", "pr"),
                new ActivityItem("alex.rivera", "deployed release-pipeline@1.4.0", "1h ago", "release"),
                new ActivityItem("priya.nair", "commented on Sprint Board", "2h ago", "comment")
        );
    }

    public DashboardMetrics getDashboardMetrics() {
        return new DashboardMetrics(12, "+38%", 45, "12 high priority", 3, 41);
    }

    public record DashboardMetrics(
            int activeProjects,
            String projectsTrend,
            int openSprintTasks,
            String tasksBadge,
            int releasesThisWeek,
            int velocityPoints
    ) {
    }
}
