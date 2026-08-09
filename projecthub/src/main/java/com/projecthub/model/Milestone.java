package com.projecthub.model;

/**
 * Upcoming milestone shown in the dashboard sidebar.
 */
public record Milestone(String title, int daysRemaining, String status) {
}
