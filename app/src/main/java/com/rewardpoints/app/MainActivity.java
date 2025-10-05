package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.adapters.RewardsAdapter;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.managers.AchievementManager;
import com.rewardpoints.app.models.CustomReward;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.models.Achievement;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements RewardsAdapter.OnRewardClickListener {

    private static final String TAG = "MainActivity";

    // UI Components
    private TextView rewardCounter, welcomeText, congratsText;
    private MaterialCardView dailyFeedbackCard;
    private MaterialButton dayInfoBtn, customizeRewardsBtn;
    private LinearLayout historyBtn, settingsBtn; // Changed from MaterialButton to LinearLayout
    private LinearLayout fabAdd; // FIXED: Changed from FloatingActionButton to LinearLayout to match layout
    private RecyclerView rewardsRecyclerView;
    private CollapsingToolbarLayout collapsingToolbar;

    // Adapters
    private RewardsAdapter rewardsAdapter;

    // Managers
    private CustomizationManager customizationManager;
    private PreferencesManager preferencesManager;
    private AchievementManager achievementManager;

    // Data
    private int currentPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainActivity");

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: Layout set successfully");

            initializeComponents();
            Log.d(TAG, "onCreate: Components initialized");

            checkFirstLaunch();
            Log.d(TAG, "onCreate: First launch check completed");

            setupToolbar();
            Log.d(TAG, "onCreate: Toolbar setup completed");

            setupRecyclerView();
            Log.d(TAG, "onCreate: RecyclerView setup completed");

            setupClickListeners();
            Log.d(TAG, "onCreate: Click listeners setup completed");

            if (savedInstanceState == null) {
                handleDailyFeedback();
                Log.d(TAG, "onCreate: Daily feedback handled");
            }

            loadData();
            Log.d(TAG, "onCreate: Data loaded successfully");

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Fatal error", e);
            Toast.makeText(this, "App initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeComponents() {
        Log.d(TAG, "initializeComponents: Starting component initialization");

        try {
            // Initialize UI components (only the ones that exist in layout)
            rewardCounter = findViewById(R.id.RewardCounter);
            Log.d(TAG, "initializeComponents: rewardCounter = " + (rewardCounter != null ? "found" : "null"));

            welcomeText = findViewById(R.id.welcome_text);
            Log.d(TAG, "initializeComponents: welcomeText = " + (welcomeText != null ? "found" : "null"));

            congratsText = findViewById(R.id.txtCongrats);
            Log.d(TAG, "initializeComponents: congratsText = " + (congratsText != null ? "found" : "null"));

            dailyFeedbackCard = findViewById(R.id.daily_feedback_card);
            Log.d(TAG, "initializeComponents: dailyFeedbackCard = " + (dailyFeedbackCard != null ? "found" : "null"));

            dayInfoBtn = findViewById(R.id.btndayinfo);
            Log.d(TAG, "initializeComponents: dayInfoBtn = " + (dayInfoBtn != null ? "found" : "null"));

            customizeRewardsBtn = findViewById(R.id.customize_rewards_btn);
            Log.d(TAG, "initializeComponents: customizeRewardsBtn = " + (customizeRewardsBtn != null ? "found" : "null"));

            historyBtn = findViewById(R.id.history_btn);
            Log.d(TAG, "initializeComponents: historyBtn = " + (historyBtn != null ? "found" : "null"));

            settingsBtn = findViewById(R.id.settings_btn);
            Log.d(TAG, "initializeComponents: settingsBtn = " + (settingsBtn != null ? "found" : "null"));

            fabAdd = findViewById(R.id.fab_add);
            Log.d(TAG, "initializeComponents: fabAdd = " + (fabAdd != null ? "found" : "null"));

            rewardsRecyclerView = findViewById(R.id.rewards_recycler_view);
            Log.d(TAG, "initializeComponents: rewardsRecyclerView = " + (rewardsRecyclerView != null ? "found" : "null"));

            collapsingToolbar = findViewById(R.id.collapsing_toolbar);
            Log.d(TAG, "initializeComponents: collapsingToolbar = " + (collapsingToolbar != null ? "found" : "null"));

            // Initialize managers
            Log.d(TAG, "initializeComponents: Initializing managers");
            customizationManager = new CustomizationManager(this);
            Log.d(TAG, "initializeComponents: CustomizationManager initialized");

            preferencesManager = new PreferencesManager(this);
            Log.d(TAG, "initializeComponents: PreferencesManager initialized");

            achievementManager = new AchievementManager(this);
            Log.d(TAG, "initializeComponents: AchievementManager initialized");

        } catch (Exception e) {
            Log.e(TAG, "initializeComponents: Error during initialization", e);
            throw e;
        }
    }

    private void checkFirstLaunch() {
        if (preferencesManager.isFirstLaunch()) {
            showWelcomeDialog();
            preferencesManager.setFirstLaunch(false);
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Update welcome message with user name only (removed version to prevent text overlap)
        String userName = preferencesManager.getUserName();
        if (welcomeText != null) {
            String welcomeMessage = getString(R.string.welcome_title) + ", " + userName + "!";
            welcomeText.setText(welcomeMessage);
        }
    }

    private void setupRecyclerView() {
        if (rewardsRecyclerView != null) {
            rewardsAdapter = new RewardsAdapter(this);
            rewardsAdapter.setOnRewardClickListener(this);
            rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            rewardsRecyclerView.setAdapter(rewardsAdapter);
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Starting click listener setup");

        try {
            if (dayInfoBtn != null) {
                dayInfoBtn.setOnClickListener(v -> {
                    Log.d(TAG, "dayInfoBtn clicked");
                    showMoodSelectionDialog();
                });
                Log.d(TAG, "setupClickListeners: dayInfoBtn listener set");
            } else {
                Log.w(TAG, "setupClickListeners: dayInfoBtn is null");
            }

            if (customizeRewardsBtn != null) {
                customizeRewardsBtn.setOnClickListener(v -> {
                    Log.d(TAG, "customizeRewardsBtn clicked");
                    openCustomizationActivity();
                });
                Log.d(TAG, "setupClickListeners: customizeRewardsBtn listener set");
            } else {
                Log.w(TAG, "setupClickListeners: customizeRewardsBtn is null");
            }

            if (historyBtn != null) {
                historyBtn.setOnClickListener(v -> {
                    Log.d(TAG, "historyBtn clicked");
                    openHistoryActivity();
                });
                Log.d(TAG, "setupClickListeners: historyBtn listener set");
            } else {
                Log.w(TAG, "setupClickListeners: historyBtn is null");
            }

            if (settingsBtn != null) {
                settingsBtn.setOnClickListener(v -> {
                    Log.d(TAG, "settingsBtn clicked");
                    openSettingsActivity();
                });
                Log.d(TAG, "setupClickListeners: settingsBtn listener set");
            } else {
                Log.w(TAG, "setupClickListeners: settingsBtn is null");
            }

            if (fabAdd != null) {
                fabAdd.setOnClickListener(v -> {
                    Log.d(TAG, "fabAdd clicked");
                    showAddOptionsDialog();
                });
                Log.d(TAG, "setupClickListeners: fabAdd listener set");
            } else {
                Log.w(TAG, "setupClickListeners: fabAdd is null");
            }

            // Add click listeners for achievements and missions cards
            MaterialCardView achievementsCard = findViewById(R.id.achievements_card);
            MaterialCardView missionsCard = findViewById(R.id.missions_card);

            Log.d(TAG, "setupClickListeners: achievementsCard = " + (achievementsCard != null ? "found" : "null"));
            Log.d(TAG, "setupClickListeners: missionsCard = " + (missionsCard != null ? "found" : "null"));

            if (achievementsCard != null) {
                achievementsCard.setOnClickListener(v -> {
                    Log.d(TAG, "achievementsCard clicked - opening achievements activity");
                    openAchievementsActivity();
                });
                Log.d(TAG, "setupClickListeners: achievementsCard listener set");
            } else {
                Log.w(TAG, "setupClickListeners: achievementsCard is null - cannot set listener");
            }

            if (missionsCard != null) {
                missionsCard.setOnClickListener(v -> {
                    Log.d(TAG, "missionsCard clicked - opening daily missions activity");
                    openDailyMissionsActivity();
                });
                Log.d(TAG, "setupClickListeners: missionsCard listener set");
            } else {
                Log.w(TAG, "setupClickListeners: missionsCard is null - cannot set listener");
            }

        } catch (Exception e) {
            Log.e(TAG, "setupClickListeners: Error setting up click listeners", e);
            throw e;
        }
    }

    private void loadData() {
        try {
            // Load current points
            currentPoints = preferencesManager.getCounter();
            if (rewardCounter != null) {
                rewardCounter.setText(String.valueOf(currentPoints));
            }

            // Load and display rewards with API level 21 compatibility
            List<CustomReward> activeRewards = customizationManager.getCustomRewards();
            Iterator<CustomReward> iterator = activeRewards.iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().isActive()) {
                    iterator.remove();
                }
            }

            // Handle empty rewards with better messaging
            if (rewardsAdapter != null) {
                rewardsAdapter.updateRewards(activeRewards);
                if (activeRewards.isEmpty()) {
                    showEmptyRewardsState();
                }
            }

            // Update welcome message based on points
            updateWelcomeMessage();

        } catch (Exception e) {
            showError(getString(R.string.error_data_load));
        }
    }

    private void showEmptyRewardsState() {
        // Show a helpful message for empty rewards
        if (congratsText != null) {
            congratsText.setText(getString(R.string.no_rewards_available));
            congratsText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_bright));
            congratsText.setVisibility(View.VISIBLE);
            congratsText.setAlpha(1f);

            // Use Handler with WeakReference to prevent memory leaks
            android.os.Handler handler = new android.os.Handler();
            java.lang.ref.WeakReference<TextView> textRef = new java.lang.ref.WeakReference<>(congratsText);
            String targetText = getString(R.string.no_rewards_available);

            handler.postDelayed(() -> {
                TextView textView = textRef.get();
                if (textView != null && targetText.equals(textView.getText().toString())) {
                    textView.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> {
                            TextView tv = textRef.get();
                            if (tv != null) {
                                tv.setVisibility(View.GONE);
                            }
                        })
                        .start();
                }
            }, 5000);
        }
    }

    private void updateWelcomeMessage() {
        if (collapsingToolbar == null) return;

        String message;
        if (currentPoints < 100) {
            message = "Keep going! You're doing great!";
        } else if (currentPoints < 500) {
            message = "Awesome progress! Keep it up!";
        } else if (currentPoints < 1000) {
            message = "You're on fire! Amazing work!";
        } else {
            message = "Incredible! You're a points master!";
        }
        collapsingToolbar.setTitle(message);
    }

    private void handleDailyFeedback() {
        if (!preferencesManager.hasFeedbackToday()) {
            if (dailyFeedbackCard != null) {
                dailyFeedbackCard.setVisibility(View.VISIBLE);
            }
        } else {
            if (dailyFeedbackCard != null) {
                dailyFeedbackCard.setVisibility(View.GONE);
            }
        }
    }

    private void showWelcomeDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Welcome to Reward Points!")
                .setMessage("Start earning points by completing daily check-ins, creating custom rewards, and achieving your goals!")
                .setPositiveButton("Get Started", null)
                .show();
    }

    private void showMoodSelectionDialog() {
        // Fixed order: Best to worst moods with appropriate points
        String[] moods = {"🚀 Amazing", "😊 Great", "😐 Okay", "😴 Tired", "😔 Not Great"};
        int[] moodPoints = {75, 50, 25, 15, 10};

        new MaterialAlertDialogBuilder(this)
                .setTitle("How are you feeling today?")
                .setItems(moods, (dialog, which) -> {
                    String selectedMood = moods[which];
                    int pointsEarned = moodPoints[which];

                    // Add points and record transaction
                    addPoints(pointsEarned, "Daily Check-in", "Mood: " + selectedMood);

                    // Mark feedback as completed for today
                    preferencesManager.setFeedbackCompletedToday();

                    // Hide the daily feedback card
                    if (dailyFeedbackCard != null) {
                        dailyFeedbackCard.setVisibility(View.GONE);
                    }

                    // Show success message
                    showCongratsMessage("Great job checking in! +" + pointsEarned + " points");

                    dialog.dismiss();
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void showAddOptionsDialog() {
        String[] options = {
            "➕ Add Custom Points",
            "🎁 Create New Reward",
            "⭐ Create Daily Mission",
            "🎯 Create Long-term Goal"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("What would you like to add?")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showAddCustomPointsDialog();
                            break;
                        case 1:
                            openCreateRewardActivity();
                            break;
                        case 2:
                            showCreateDailyMissionDialog();
                            break;
                        case 3:
                            showCreateLongTermGoalDialog();
                            break;
                    }
                })
                .show();
    }

    private void showAddCustomPointsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_points, null);
        TextView pointsInput = dialogView.findViewById(R.id.points_input);
        TextView descriptionInput = dialogView.findViewById(R.id.description_input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Custom Points")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        int points = Integer.parseInt(pointsInput.getText().toString());
                        String description = descriptionInput.getText().toString();
                        if (description.isEmpty()) description = "Custom points";

                        addPoints(points, "Manual Entry", description);
                        showCongratsMessage("Added " + points + " points!");
                    } catch (NumberFormatException e) {
                        showError("Please enter a valid number");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addPoints(int points, String source, String description) {
        currentPoints += points;
        preferencesManager.setCounter(currentPoints);

        if (rewardCounter != null) {
            rewardCounter.setText(String.valueOf(currentPoints));
        }

        // Record transaction
        EnhancedTransaction transaction = new EnhancedTransaction(
                "EARN", source, description, points, "Daily"
        );
        preferencesManager.addTransaction(transaction);

        // Check for achievements
        achievementManager.checkAchievements(currentPoints);

        // Update welcome message
        updateWelcomeMessage();
    }

    private void showCongratsMessage(String message) {
        if (congratsText != null) {
            congratsText.setText(message);
            congratsText.setVisibility(View.VISIBLE);
            congratsText.setAlpha(0f);
            congratsText.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();

            // Use Handler with WeakReference to prevent memory leaks
            android.os.Handler handler = new android.os.Handler();
            java.lang.ref.WeakReference<TextView> textRef = new java.lang.ref.WeakReference<>(congratsText);

            handler.postDelayed(() -> {
                TextView textView = textRef.get();
                if (textView != null) {
                    textView.animate()
                            .alpha(0f)
                            .setDuration(500)
                            .withEndAction(() -> {
                                TextView tv = textRef.get();
                                if (tv != null) {
                                    tv.setVisibility(View.GONE);
                                }
                            })
                            .start();
                }
            }, 3000);
        }
    }

    // RewardsAdapter.OnRewardClickListener implementation
    @Override
    public void onRewardClick(CustomReward reward) {
        if (currentPoints >= reward.getPointsCost()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Redeem Reward")
                    .setMessage("Redeem '" + reward.getName() + "' for " + reward.getPointsCost() + " points?")
                    .setPositiveButton("Redeem", (dialog, which) -> {
                        redeemReward(reward);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            int needed = reward.getPointsCost() - currentPoints;
            showError("You need " + needed + " more points to redeem this reward");
        }
    }

    @Override
    public void onDeleteReward(CustomReward reward) {
        // Delete the reward using CustomizationManager
        customizationManager.deleteCustomReward(reward.getId());

        // Refresh the rewards list
        loadData();

        // Show confirmation
        showCongratsMessage("Reward '" + reward.getName() + "' deleted!");
    }

    private void redeemReward(CustomReward reward) {
        currentPoints -= reward.getPointsCost();
        preferencesManager.setCounter(currentPoints);

        if (rewardCounter != null) {
            rewardCounter.setText(String.valueOf(currentPoints));
        }

        // Record transaction
        EnhancedTransaction transaction = new EnhancedTransaction(
                "REDEEM", "Reward Redemption", reward.getName(), -reward.getPointsCost(), "Daily"
        );
        preferencesManager.addTransaction(transaction);

        // Show success message
        showCongratsMessage("Reward redeemed! -" + reward.getPointsCost() + " points");

        // Update welcome message
        updateWelcomeMessage();
    }

    // Missing navigation methods that are referenced in the code
    private void openCustomizationActivity() {
        Log.d(TAG, "openCustomizationActivity: Attempting to open customization activity");

        try {
            Intent intent = new Intent(this, ManageRewardsActivity.class);
            startActivity(intent);
            Log.d(TAG, "openCustomizationActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openCustomizationActivity: Error opening customization activity", e);
            showError("Unable to open customization activity");
        }
    }

    private void openHistoryActivity() {
        Log.d(TAG, "openHistoryActivity: Attempting to open history activity");

        try {
            Intent intent = new Intent(this, ModernHistoryActivity.class);
            startActivity(intent);
            Log.d(TAG, "openHistoryActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openHistoryActivity: Error opening history activity", e);
            showError("Unable to open history activity");
        }
    }

    private void openSettingsActivity() {
        Log.d(TAG, "openSettingsActivity: Attempting to open settings activity");

        try {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            Log.d(TAG, "openSettingsActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openSettingsActivity: Error opening settings activity", e);
            showError("Unable to open settings activity");
        }
    }

    private void openCreateRewardActivity() {
        Log.d(TAG, "openCreateRewardActivity: Attempting to open create reward activity");

        try {
            Intent intent = new Intent(this, CreateRewardActivity.class);
            startActivity(intent);
            Log.d(TAG, "openCreateRewardActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openCreateRewardActivity: Error opening create reward activity", e);
            showError("Unable to open create reward activity");
        }
    }

    private void openCreatePointsSourceActivity() {
        Log.d(TAG, "openCreatePointsSourceActivity: Attempting to open create points source activity");

        try {
            Intent intent = new Intent(this, CreatePointsSourceActivity.class);
            startActivity(intent);
            Log.d(TAG, "openCreatePointsSourceActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openCreatePointsSourceActivity: Error opening create points source activity", e);
            showError("Unable to open create points source activity");
        }
    }

    private void openDailyMissionsActivity() {
        Log.d(TAG, "openDailyMissionsActivity: Attempting to open daily missions activity");

        try {
            Intent intent = new Intent(this, DailyMissionsActivity.class);
            startActivity(intent);
            Log.d(TAG, "openDailyMissionsActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openDailyMissionsActivity: Error opening daily missions activity", e);
            showError("Unable to open daily missions activity");
        }
    }

    private void openAchievementsActivity() {
        Log.d(TAG, "openAchievementsActivity: Attempting to open achievements activity");

        try {
            Intent intent = new Intent(this, AchievementsActivity.class);
            startActivity(intent);
            Log.d(TAG, "openAchievementsActivity: Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "openAchievementsActivity: Error opening achievements activity", e);
            showError("Unable to open achievements activity: " + e.getMessage());
        }
    }

    // Missing error handling method
    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            message = "An error occurred";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        loadData();
        handleDailyFeedback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (rewardsAdapter != null) {
            rewardsAdapter = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes
    }

    // Add the missing methods that XML layouts are calling from MainActivity context
    public void ShowmissionPopup(View view) {
        Log.d(TAG, "ShowmissionPopup: Method called with view = " + view);

        try {
            Log.d(TAG, "ShowmissionPopup: Creating mission dialog");

            // Simple implementation - show available missions
            String[] missionOptions = {
                "📋 Daily Check-in (+25 pts)",
                "⭐ Earn 50 Points (+30 pts)",
                "🎁 Create New Reward (+40 pts)",
                "🏆 Weekend Bonus (+75 pts)"
            };

            Log.d(TAG, "ShowmissionPopup: Mission options created, showing dialog");

            new android.app.AlertDialog.Builder(this)
                .setTitle("Quick Missions")
                .setItems(missionOptions, (dialog, which) -> {
                    Log.d(TAG, "ShowmissionPopup: Mission option " + which + " selected");

                    try {
                        switch (which) {
                            case 0:
                                Log.d(TAG, "ShowmissionPopup: Daily check-in selected");
                                if (!preferencesManager.hasFeedbackToday()) {
                                    showMoodSelectionDialog();
                                } else {
                                    Toast.makeText(this, "Daily check-in already completed today!", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 1:
                                Log.d(TAG, "ShowmissionPopup: Earn 50 points selected");
                                addPoints(50, "Quick Mission", "Earned 50 points mission");
                                break;
                            case 2:
                                Log.d(TAG, "ShowmissionPopup: Create reward selected");
                                openCreateRewardActivity();
                                break;
                            case 3:
                                Log.d(TAG, "ShowmissionPopup: Weekend bonus selected");
                                addPoints(75, "Weekend Bonus", "Weekend warrior bonus");
                                break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "ShowmissionPopup: Error in mission selection", e);
                        Toast.makeText(this, "Mission error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "ShowmissionPopup: Dialog cancelled");
                })
                .show();

            Log.d(TAG, "ShowmissionPopup: Dialog shown successfully");

        } catch (Exception e) {
            Log.e(TAG, "ShowmissionPopup: Fatal error", e);
            Toast.makeText(this, "Mission popup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void Showachievementpopup(View view) {
        Log.d(TAG, "Showachievementpopup: Method called with view = " + view);

        try {
            Log.d(TAG, "Showachievementpopup: Opening achievements activity");
            openAchievementsActivity();
        } catch (Exception e) {
            Log.e(TAG, "Showachievementpopup: Error opening achievements", e);
            Toast.makeText(this, "Achievement popup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Missing onClick methods that are causing crashes
    public void ShowTodayPopup(View view) {
        Log.d(TAG, "ShowTodayPopup: Method called with view = " + view);

        try {
            Log.d(TAG, "ShowTodayPopup: Opening mood selection dialog");
            showMoodSelectionDialog();
        } catch (Exception e) {
            Log.e(TAG, "ShowTodayPopup: Error showing mood dialog", e);
            Toast.makeText(this, "Today popup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateDailyMissionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_mission, null);
        android.widget.EditText nameInput = dialogView.findViewById(R.id.mission_name_input);
        android.widget.EditText descriptionInput = dialogView.findViewById(R.id.mission_description_input);
        android.widget.EditText pointsInput = dialogView.findViewById(R.id.mission_points_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("⭐ Create Daily Mission")
            .setMessage("Create a task that will be available every day")
            .setView(dialogView)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String pointsText = pointsInput.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a mission name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    description = "Complete this daily task";
                }

                int points = 25; // Default points
                try {
                    if (!pointsText.isEmpty()) {
                        points = Integer.parseInt(pointsText);
                        if (points <= 0) {
                            Toast.makeText(this, "Points must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid points value", Toast.LENGTH_SHORT).show();
                    return;
                }

                createNewDailyMission(name, description, points);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createNewDailyMission(String name, String description, int points) {
        try {
            // Use DailyMission model and save via PreferencesManager
            com.rewardpoints.app.models.DailyMission mission = new com.rewardpoints.app.models.DailyMission(
                "mission_" + System.currentTimeMillis(),
                name,
                description,
                points,
                false
            );

            java.util.List<com.rewardpoints.app.models.DailyMission> missions = preferencesManager.getUserDailyMissions();
            if (missions == null) {
                missions = new java.util.ArrayList<>();
            }

            missions.add(mission);
            preferencesManager.saveUserDailyMissions(missions);

            Toast.makeText(this, "Daily mission '" + name + "' created successfully!", Toast.LENGTH_SHORT).show();
            showCongratsMessage("New daily mission created! +" + points + " points available daily");

        } catch (Exception e) {
            Toast.makeText(this, "Error creating mission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateLongTermGoalDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_achievement, null);
        android.widget.EditText nameInput = dialogView.findViewById(R.id.achievement_name_input);
        android.widget.EditText descriptionInput = dialogView.findViewById(R.id.achievement_description_input);
        android.widget.EditText targetInput = dialogView.findViewById(R.id.achievement_target_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Create Long-term Goal")
            .setMessage("Set a points target to work towards")
            .setView(dialogView)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String targetText = targetInput.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    description = "Reach your points target";
                }

                int target = 1000; // Default target
                try {
                    if (!targetText.isEmpty()) {
                        target = Integer.parseInt(targetText);
                        if (target <= currentPoints) {
                            Toast.makeText(this, "Target must be higher than current points (" + currentPoints + ")", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid target value", Toast.LENGTH_SHORT).show();
                    return;
                }

                createNewLongTermGoal(name, description, target);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createNewLongTermGoal(String name, String description, int target) {
        try {
            // Create achievement with points target
            Achievement achievement = new Achievement(
                "goal_" + System.currentTimeMillis(),
                name,
                description,
                target,
                "🎯",
                false
            );

            achievementManager.addUserAchievement(achievement);
            Toast.makeText(this, "Long-term goal '" + name + "' created successfully!", Toast.LENGTH_SHORT).show();
            showCongratsMessage("New goal set! Target: " + target + " points");

        } catch (Exception e) {
            Toast.makeText(this, "Error creating goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
