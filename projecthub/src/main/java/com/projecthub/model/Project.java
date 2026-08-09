package com.projecthub.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing an engineering project tracked in ProjectHub.
 */
public class Project {

    private final String id;
    private final String name;
    private final int progressPercent;
    private final List<String> techStack;
    private final Path workingDirectory;
    private final String currentBranch;
    private final String description;

    public Project(
            String id,
            String name,
            int progressPercent,
            List<String> techStack,
            Path workingDirectory,
            String currentBranch,
            String description
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.progressPercent = progressPercent;
        this.techStack = List.copyOf(techStack);
        this.workingDirectory = workingDirectory;
        this.currentBranch = currentBranch;
        this.description = description != null ? description : "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    public String getCurrentBranch() {
        return currentBranch;
    }

    public String getDescription() {
        return description;
    }

    public Project withCurrentBranch(String branch) {
        return new Project(id, name, progressPercent, techStack, workingDirectory, branch, description);
    }
}
