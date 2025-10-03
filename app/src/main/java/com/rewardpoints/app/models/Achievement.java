package com.rewardpoints.app.models;

/**
 * Model class for user achievements in the reward points system
 */
public class Achievement {
    private String id;
    private String name;
    private String description;
    private int bonusPoints;
    private String category;
    private boolean isUnlocked;
    private long unlockedDate;
    private String iconName;
    private int progress;
    private int targetValue;

    public Achievement() {
        // Default constructor
    }

    public Achievement(String id, String name, String description, int bonusPoints, String category, boolean isUnlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bonusPoints = bonusPoints;
        this.category = category;
        this.isUnlocked = isUnlocked;
        this.unlockedDate = isUnlocked ? System.currentTimeMillis() : 0;
        this.progress = 0;
        this.targetValue = 1;
    }

    public Achievement(String id, String name, String description, int bonusPoints, String category, boolean isUnlocked, int targetValue) {
        this(id, name, description, bonusPoints, category, isUnlocked);
        this.targetValue = targetValue;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(int bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
        if (unlocked && this.unlockedDate == 0) {
            this.unlockedDate = System.currentTimeMillis();
        }
    }

    public long getUnlockedDate() {
        return unlockedDate;
    }

    public void setUnlockedDate(long unlockedDate) {
        this.unlockedDate = unlockedDate;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        // Auto-unlock if progress reaches target
        if (progress >= targetValue && !isUnlocked) {
            setUnlocked(true);
        }
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public float getProgressPercentage() {
        if (targetValue <= 0) return 0f;
        return Math.min(100f, (progress * 100f) / targetValue);
    }

    public boolean isCompleted() {
        return isUnlocked; // For user goals, completed = unlocked
    }

    public void setCompleted(boolean completed) {
        setUnlocked(completed);
    }

    public int getRewardPoints() {
        return bonusPoints; // Alias for bonusPoints to match the interface used in activities
    }

    public void setRewardPoints(int rewardPoints) {
        setBonusPoints(rewardPoints);
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", bonusPoints=" + bonusPoints +
                ", category='" + category + '\'' +
                ", isUnlocked=" + isUnlocked +
                ", progress=" + progress +
                ", targetValue=" + targetValue +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Achievement that = (Achievement) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
