package com.rewardpoints.app.models;

/**
 * Data model for reward transactions
 */
public class RewardTransaction {
    private final String date;
    private final String description;
    private final int points;
    private final boolean isCredit; // true for earning points, false for spending

    public RewardTransaction(String date, String description, int points, boolean isCredit) {
        this.date = date;
        this.description = description;
        this.points = points;
        this.isCredit = isCredit;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCredit() {
        return isCredit;
    }

    @Override
    public String toString() {
        String sign = isCredit ? "+" : "-";
        return date + " : " + description + " " + sign + points + "P";
    }
}
