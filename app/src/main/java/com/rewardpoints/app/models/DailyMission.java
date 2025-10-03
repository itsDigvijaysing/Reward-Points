package com.rewardpoints.app.models;

public class DailyMission {
    private String id;
    private String title;
    private String description;
    private int rewardPoints;
    private String category;
    private boolean completed;
    private long createdAt;

    public DailyMission() {
        this.createdAt = System.currentTimeMillis();
    }

    public DailyMission(String id, String title, String description, int rewardPoints, String category, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rewardPoints = rewardPoints;
        this.category = category;
        this.completed = completed;
        this.createdAt = System.currentTimeMillis();
    }

    public DailyMission(String id, String name, String description, int points, boolean completed) {
        this.id = id;
        this.title = name;
        this.description = description;
        this.rewardPoints = points;
        this.category = "Daily";
        this.completed = completed;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Additional methods for compatibility
    public String getName() {
        return title;
    }

    public void setName(String name) {
        this.title = name;
    }

    public int getPoints() {
        return rewardPoints;
    }

    public void setPoints(int points) {
        this.rewardPoints = points;
    }

    public String getCategoryEmoji() {
        switch (category) {
            case "checkin": return "✅";
            case "points": return "⭐";
            case "reward": return "🎁";
            case "special": return "🏆";
            default: return "📋";
        }
    }

    @Override
    public String toString() {
        return "DailyMission{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", rewardPoints=" + rewardPoints +
                ", completed=" + completed +
                '}';
    }
}
