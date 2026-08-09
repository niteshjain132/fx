package com.projecthub.model;

/**
 * Team activity feed entry.
 */
public record ActivityItem(String actor, String action, String timestamp, String kind) {
}
