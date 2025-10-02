package com.rewardpoints.app.constants;

/**
 * Constants used throughout the application
 */
public class AppConstants {

    // Reward costs
    public static final int MOVIE_COST = 200;
    public static final int GAME_COST = 300;
    public static final int TRAVEL_COST = 200;
    public static final int PRODUCT_COST = 500;

    // Reward points for mood feedback
    public static final int VERY_HAPPY_POINTS = 120;
    public static final int HAPPY_POINTS = 60;
    public static final int NEUTRAL_POINTS = 20;
    public static final int SAD_POINTS = 10;
    public static final int VERY_SAD_POINTS = 0;

    // Achievement points
    public static final int VERY_IMP_ACHIEVEMENT_POINTS = 200;
    public static final int IMP_ACHIEVEMENT_POINTS = 120;
    public static final int GOOD_ACHIEVEMENT_POINTS = 60;

    // Mission points
    public static final int MISSION_POINTS = 25;

    // Date format
    public static final String DATE_FORMAT = "dd-MM-yyyy";

    // Messages
    public static final String INSUFFICIENT_POINTS_MSG = "You do not have sufficient points";
    public static final String GIVE_FEEDBACK_MSG = "Give Feedback of Today";
    public static final String FEEDBACK_RECEIVED_MSG = "Feedback is received for Today";

    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}
