package com.projecthub.event;

import com.projecthub.model.Project;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a project card body is clicked (excluding branch ComboBox / Open button).
 */
public class ProjectSelectedEvent extends ApplicationEvent {

    private final Project project;

    public ProjectSelectedEvent(Object source, Project project) {
        super(source);
        this.project = project;
    }

    public Project getProject() {
        return project;
    }
}
