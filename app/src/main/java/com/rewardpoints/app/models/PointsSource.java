package com.rewardpoints.app.models;

/**
 * Model for customizable points sources
 */
public class PointsSource {
    private String id;
    private String name;
    private String description;
    private int pointsValue;
    private String category;
    private String iconName;
    private boolean isActive;
    private long createdDate;
    private long lastUsedDate;
    private int timesUsed;
    private int dailyLimit;
    private int weeklyLimit;

    public PointsSource() {
        // Default constructor
    }

    public PointsSource(String name, String description, int pointsValue, String category) {
        this.id = generateId();
        this.name = name;
        this.description = description;
        this.pointsValue = pointsValue;
        this.category = category;
        this.isActive = true;
        this.createdDate = System.currentTimeMillis();
        this.lastUsedDate = 0;
        this.timesUsed = 0;
    }

    private String generateId() {
        return "points_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointsValue() { return pointsValue; }
    public void setPointsValue(int pointsValue) { this.pointsValue = pointsValue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }

    public long getLastUsedDate() { return lastUsedDate; }
    public void setLastUsedDate(long lastUsedDate) { this.lastUsedDate = lastUsedDate; }

    public int getTimesUsed() { return timesUsed; }
    public void setTimesUsed(int timesUsed) { this.timesUsed = timesUsed; }

    public int getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(int dailyLimit) { this.dailyLimit = dailyLimit; }

    public int getWeeklyLimit() { return weeklyLimit; }
    public void setWeeklyLimit(int weeklyLimit) { this.weeklyLimit = weeklyLimit; }

    public void incrementUsage() {
        this.timesUsed++;
        this.lastUsedDate = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return name + " (+" + pointsValue + " points)";
    }
}
