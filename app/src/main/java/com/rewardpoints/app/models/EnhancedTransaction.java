package com.rewardpoints.app.models;

/**
 * Enhanced transaction model with detailed analytics support
 */
public class EnhancedTransaction {
    private String id;
    private String type; // EARN, SPEND
    private String source; // What generated the transaction
    private String description;
    private int points;
    private long timestamp;
    private String category;
    private String subcategory;
    private String mood; // For mood-based earnings
    private double latitude;
    private double longitude;
    private String notes;

    public EnhancedTransaction() {
        // Default constructor
    }

    public EnhancedTransaction(String type, String source, String description, int points, String category) {
        this.id = generateId();
        this.type = type;
        this.source = source;
        this.description = description;
        this.points = points;
        this.timestamp = System.currentTimeMillis();
        this.category = category;
    }

    private String generateId() {
        return "txn_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isEarning() {
        return "EARN".equals(type);
    }

    public boolean isSpending() {
        return "SPEND".equals(type);
    }

    @Override
    public String toString() {
        String sign = isEarning() ? "+" : "-";
        return description + " " + sign + points + " points";
    }
}
