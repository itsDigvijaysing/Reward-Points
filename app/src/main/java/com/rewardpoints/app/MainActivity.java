package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.constants.AppConstants;
import com.rewardpoints.app.models.RewardTransaction;
import com.rewardpoints.app.utils.PreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int counter;
    private TextView rewardCount;
    private TextView congratsText;
    private Dialog myDialog;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        if (savedInstanceState == null) {
            // Only handle daily feedback on fresh start, not on configuration changes
            handleDailyFeedback();
        }
        setupClickListeners();
        loadCounterFromPreferences();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current state to prevent data loss during configuration changes
        outState.putInt("counter", counter);
        outState.putBoolean("congrats_visible", congratsText.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state after configuration changes
        if (savedInstanceState != null) {
            counter = savedInstanceState.getInt("counter", 0);
            boolean congratsVisible = savedInstanceState.getBoolean("congrats_visible", false);
            rewardCount.setText(String.valueOf(counter));
            congratsText.setVisibility(congratsVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes (like split screen, rotation, UI mode changes) gracefully
        // No need to recreate activity, just update UI if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh counter display when returning to activity
        loadCounterFromPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dismiss any open dialogs to prevent window leaks
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (myDialog != null) {
            myDialog.dismiss();
            myDialog = null;
        }
    }

    private void initializeComponents() {
        rewardCount = findViewById(R.id.RewardCounter);
        congratsText = findViewById(R.id.txtCongrats);
        myDialog = new Dialog(this);
        preferencesManager = new PreferencesManager(this);
    }

    private void handleDailyFeedback() {
        try {
            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            Button btntodayP = findViewById(R.id.btndayinfo);
            int lastDay = preferencesManager.getLastDay();
            boolean todayFeedbackGiven = preferencesManager.isTodayFeedbackGiven();

            if (lastDay != currentDay) {
                btntodayP.setVisibility(View.VISIBLE);
                btntodayP.setClickable(true);

                if (todayFeedbackGiven) {
                    preferencesManager.saveDay(currentDay);
                    refreshUI();
                } else {
                    showToast(AppConstants.GIVE_FEEDBACK_MSG);
                }
            } else {
                btntodayP.setVisibility(View.GONE);
                btntodayP.setClickable(false);
                preferencesManager.setTodayFeedbackGiven(false);
                showToast(AppConstants.FEEDBACK_RECEIVED_MSG);
            }
        } catch (Exception e) {
            // Handle any calendar-related errors gracefully
            showToast(getString(R.string.error_generic));
        }
    }

    private void setupClickListeners() {
        // Reward buttons
        findViewById(R.id.btnmovielinear).setOnClickListener(v ->
            claimReward(AppConstants.MOVIE_COST, "1 Full Movie Reward Claimed", "1 Full Movie Reward Claimed..."));

        findViewById(R.id.btngamelinear).setOnClickListener(v ->
            claimReward(AppConstants.GAME_COST, "2Hr Game Reward Claimed", "2Hr Game Reward Claimed..."));

        findViewById(R.id.btntravellinear).setOnClickListener(v ->
            claimReward(AppConstants.TRAVEL_COST, "1Hr Travel Reward Claimed", "1Hr Travel Reward Claimed..."));

        findViewById(R.id.btnproductlinear).setOnClickListener(v ->
            claimReward(AppConstants.PRODUCT_COST, "Product Purchase (500rs)", "Product Purchase (500rs) Claimed..."));

        // History button
        findViewById(R.id.imgbuttonhistory).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PointsHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void claimReward(int cost, String historyDescription, String toastMessage) {
        try {
            if (counter >= cost) {
                counter -= cost;
                updateCounter();

                RewardTransaction transaction = new RewardTransaction(
                    getCurrentDate(), historyDescription, cost, false);
                preferencesManager.addToHistory(transaction.toString());

                showToast(toastMessage);
                congratsText.setVisibility(View.VISIBLE);
            } else {
                showToast(getString(R.string.insufficient_points));
            }
        } catch (Exception e) {
            showToast(getString(R.string.error_generic));
        }
    }

    private void addPoints(int points, String description) {
        try {
            counter += points;
            updateCounter();

            RewardTransaction transaction = new RewardTransaction(
                getCurrentDate(), description, points, true);
            preferencesManager.addToHistory(transaction.toString());
            preferencesManager.setTodayFeedbackGiven(true);
        } catch (Exception e) {
            showToast(getString(R.string.error_generic));
        }
    }

    private void updateCounter() {
        rewardCount.setText(String.valueOf(counter));
        preferencesManager.saveCounter(counter);
    }

    private void loadCounterFromPreferences() {
        try {
            counter = preferencesManager.getCounter();
            rewardCount.setText(String.valueOf(counter));
        } catch (Exception e) {
            showToast(getString(R.string.error_data_load));
        }
    }

    private void refreshUI() {
        // Refresh UI without recreating activity
        loadCounterFromPreferences();
        handleDailyFeedback();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat(AppConstants.DATE_FORMAT, Locale.getDefault()).format(new Date());
    }

    private void showToast(String message) {
        if (message != null && !isFinishing()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void ShowTodayPopup(View v) {
        if (isFinishing() || isDestroyed()) return;

        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }

        myDialog.setContentView(R.layout.todaypopup);

        // Close button with content description for accessibility
        myDialog.findViewById(R.id.btnclose).setOnClickListener(view -> myDialog.dismiss());
        myDialog.findViewById(R.id.btnclose).setContentDescription(getString(R.string.content_description_close_button));

        // Mood buttons with lambda expressions
        myDialog.findViewById(R.id.imggreat).setOnClickListener(view -> {
            addPoints(AppConstants.VERY_HAPPY_POINTS, "Very Happy Day");
            showToast("Congratulations 120 Points Added...");
            myDialog.dismiss();
            refreshUI();
        });

        myDialog.findViewById(R.id.imggood).setOnClickListener(view -> {
            addPoints(AppConstants.HAPPY_POINTS, "Happy Day");
            showToast("Congrats 60 Points Added");
            myDialog.dismiss();
            refreshUI();
        });

        myDialog.findViewById(R.id.imgneutral).setOnClickListener(view -> {
            addPoints(AppConstants.NEUTRAL_POINTS, "Okay Day");
            showToast("Nice 20 Points Added");
            myDialog.dismiss();
            refreshUI();
        });

        myDialog.findViewById(R.id.imgsad).setOnClickListener(view -> {
            addPoints(AppConstants.SAD_POINTS, "Sad Day");
            showToast("It's Okay Yaar.. 10 Points Added");
            myDialog.dismiss();
            refreshUI();
        });

        myDialog.findViewById(R.id.imgverysad).setOnClickListener(view -> {
            addPoints(AppConstants.VERY_SAD_POINTS, "Very Sad Day");
            showToast("Stay +ve Better days ahead...");
            myDialog.dismiss();
            refreshUI();
        });

        myDialog.show();
    }

    public void Showachievementpopup(View v) {
        if (isFinishing() || isDestroyed()) return;

        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }

        myDialog.setContentView(R.layout.achievementpopup);

        // Close button
        myDialog.findViewById(R.id.btncloseachievement).setOnClickListener(view -> myDialog.dismiss());
        myDialog.findViewById(R.id.btncloseachievement).setContentDescription(getString(R.string.content_description_close_button));

        // Achievement buttons
        myDialog.findViewById(R.id.imgvimpachievement).setOnClickListener(view -> {
            addPoints(AppConstants.VERY_IMP_ACHIEVEMENT_POINTS, "Very IMP Achievement");
            showToast("Congrats Yaar.. Kadak.... +200 Points");
            myDialog.dismiss();
        });

        myDialog.findViewById(R.id.imgimpachievement).setOnClickListener(view -> {
            addPoints(AppConstants.IMP_ACHIEVEMENT_POINTS, "Important Achievement");
            showToast("You are doing great +120 Points");
            myDialog.dismiss();
        });

        myDialog.findViewById(R.id.imgniceachievement).setOnClickListener(view -> {
            addPoints(AppConstants.GOOD_ACHIEVEMENT_POINTS, "Good Achievement");
            showToast("Nice... Keep it up +60 Points");
            myDialog.dismiss();
        });

        myDialog.show();
    }

    public void ShowmissionPopup(View v) {
        if (isFinishing() || isDestroyed()) return;

        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }

        myDialog.setContentView(R.layout.missionpopup);

        // Close button
        myDialog.findViewById(R.id.btnclosemission).setOnClickListener(view -> myDialog.dismiss());
        myDialog.findViewById(R.id.btnclosemission).setContentDescription(getString(R.string.content_description_close_button));

        // Mission buttons
        myDialog.findViewById(R.id.imghealthmission).setOnClickListener(view -> {
            addPoints(AppConstants.MISSION_POINTS, "Health Mission Complete");
            showToast("Congratulations 25 Points Added...");
            myDialog.dismiss();
        });

        myDialog.findViewById(R.id.imgcworkmission).setOnClickListener(view -> {
            addPoints(AppConstants.MISSION_POINTS, "Completed Computer Dev Mission");
            showToast("Congrats 25 Points Added");
            myDialog.dismiss();
        });

        myDialog.findViewById(R.id.imgsdmission).setOnClickListener(view -> {
            addPoints(AppConstants.MISSION_POINTS, "Completed Self Development Mission");
            showToast("Nice 25 Points Added");
            myDialog.dismiss();
        });

        myDialog.show();
    }
}

