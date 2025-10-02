package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.adapters.RewardsAdapter;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.CustomReward;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.models.PointsSource;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements RewardsAdapter.OnRewardClickListener {

    // UI Components
    private TextView rewardCounter, welcomeText, congratsText;
    private MaterialCardView dailyFeedbackCard;
    private MaterialButton dayInfoBtn, customizeRewardsBtn;
    private LinearLayout historyBtn, settingsBtn; // Changed from MaterialButton to LinearLayout
    private FloatingActionButton fabAdd;
    private RecyclerView rewardsRecyclerView;
    private CollapsingToolbarLayout collapsingToolbar;

    // Adapters
    private RewardsAdapter rewardsAdapter;

    // Managers
    private CustomizationManager customizationManager;
    private PreferencesManager preferencesManager;

    // Data
    private int currentPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        checkFirstLaunch();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        if (savedInstanceState == null) {
            handleDailyFeedback();
        }

        loadData();
    }

    private void initializeComponents() {
        // Initialize UI components (only the ones that exist in layout)
        rewardCounter = findViewById(R.id.RewardCounter);
        welcomeText = findViewById(R.id.welcome_text);
        congratsText = findViewById(R.id.txtCongrats);
        dailyFeedbackCard = findViewById(R.id.daily_feedback_card);
        dayInfoBtn = findViewById(R.id.btndayinfo);
        customizeRewardsBtn = findViewById(R.id.customize_rewards_btn);
        historyBtn = findViewById(R.id.history_btn);
        settingsBtn = findViewById(R.id.settings_btn);
        fabAdd = findViewById(R.id.fab_add);
        rewardsRecyclerView = findViewById(R.id.rewards_recycler_view);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);

        // Initialize managers
        customizationManager = new CustomizationManager(this);
        preferencesManager = new PreferencesManager(this);
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

        // Update welcome message with user name
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
        if (dayInfoBtn != null) {
            dayInfoBtn.setOnClickListener(v -> showMoodSelectionDialog());
        }
        if (customizeRewardsBtn != null) {
            customizeRewardsBtn.setOnClickListener(v -> openCustomizationActivity());
        }
        if (historyBtn != null) {
            historyBtn.setOnClickListener(v -> openHistoryActivity());
        }
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> openSettingsActivity());
        }
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddOptionsDialog());
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

            if (rewardsAdapter != null) {
                rewardsAdapter.updateRewards(activeRewards);
            }

            // Update welcome message based on points
            updateWelcomeMessage();

        } catch (Exception e) {
            showError(getString(R.string.error_data_load));
        }
    }

    private void updateWelcomeMessage() {
        if (collapsingToolbar == null) return;

        String message;
        if (currentPoints >= 1000) {
            message = "You're a Points Master! 🏆";
        } else if (currentPoints >= 500) {
            message = "Great job collecting points! 🌟";
        } else if (currentPoints >= 100) {
            message = "Keep up the good work! 💪";
        } else {
            message = "Start your journey today! 🚀";
        }
        collapsingToolbar.setTitle(message);
    }

    private void handleDailyFeedback() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        int lastDay = preferencesManager.getLastDay();
        boolean todayFeedbackGiven = preferencesManager.isTodayFeedbackGiven();

        if (lastDay != currentDay && !todayFeedbackGiven && dailyFeedbackCard != null) {
            dailyFeedbackCard.setVisibility(View.VISIBLE);
            animateCardIn(dailyFeedbackCard);
        } else if (dailyFeedbackCard != null) {
            dailyFeedbackCard.setVisibility(View.GONE);
        }
    }

    private void showWelcomeDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.welcome_title))
            .setMessage(getString(R.string.welcome_subtitle) + "\n\n" +
                       "✨ " + getString(R.string.onboarding_points_desc) + "\n" +
                       "🎁 " + getString(R.string.onboarding_rewards_desc) + "\n" +
                       "⚙️ " + getString(R.string.onboarding_customize_desc))
            .setPositiveButton(getString(R.string.get_started), (dialog, which) -> showNameInputDialog())
            .setCancelable(false)
            .show();
    }

    private void showNameInputDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter your name");

        new MaterialAlertDialogBuilder(this)
            .setTitle("What should we call you?")
            .setView(input)
            .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    preferencesManager.setUserName(name);
                    if (welcomeText != null) {
                        String welcomeMessage = getString(R.string.welcome_title) + ", " + name + "!";
                        welcomeText.setText(welcomeMessage);
                    }
                }
            })
            .setNegativeButton(getString(R.string.skip), null)
            .show();
    }

    private void showMoodSelectionDialog() {
        List<PointsSource> moodSources = customizationManager.getPointsSourcesByCategory("Mood");

        if (moodSources.isEmpty()) {
            showError("No mood options configured. Please add some in settings.");
            return;
        }

        String[] moodNames = new String[moodSources.size()];
        for (int i = 0; i < moodSources.size(); i++) {
            moodNames[i] = moodSources.get(i).getName() + " (+" + moodSources.get(i).getPointsValue() + " points)";
        }

        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.mood_feedback_title))
            .setItems(moodNames, (dialog, which) -> {
                PointsSource selectedMood = moodSources.get(which);
                addPointsFromSource(selectedMood, "Mood");
                handleDailyFeedbackComplete();
            })
            .show();
    }

    private void showAddOptionsDialog() {
        String[] options = {
            getString(R.string.add_new_reward),
            getString(R.string.add_custom_points_source),
            "Add Custom Activity"
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle("Add New Item")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        openRewardCreator();
                        break;
                    case 1:
                        openPointsSourceCreator();
                        break;
                    case 2:
                        openActivityCreator();
                        break;
                }
            })
            .show();
    }

    private void addPointsFromSource(PointsSource source, String mood) {
        try {
            currentPoints += source.getPointsValue();
            preferencesManager.saveCounter(currentPoints);
            if (rewardCounter != null) {
                rewardCounter.setText(String.valueOf(currentPoints));
            }

            // Create enhanced transaction
            EnhancedTransaction transaction = new EnhancedTransaction(
                "EARN",
                source.getName(),
                source.getDescription(),
                source.getPointsValue(),
                source.getCategory()
            );
            transaction.setMood(mood);
            customizationManager.addTransaction(transaction);

            // Update source usage
            source.incrementUsage();
            customizationManager.updatePointsSource(source);

            updateWelcomeMessage();

        } catch (Exception e) {
            showError(getString(R.string.error_generic));
        }
    }

    private void handleDailyFeedbackComplete() {
        preferencesManager.setTodayFeedbackGiven(true);
        preferencesManager.saveDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        if (dailyFeedbackCard != null) {
            dailyFeedbackCard.setVisibility(View.GONE);
            animateCardOut(dailyFeedbackCard);
        }

        showSuccessAnimation("Daily check-in complete! 🎉");
    }

    // RewardsAdapter.OnRewardClickListener implementation
    @Override
    public void onRewardClick(CustomReward reward) {
        if (currentPoints >= reward.getPointsCost()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Claim " + reward.getName())
                .setMessage("This will cost " + reward.getPointsCost() + " points. Continue?")
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> claimReward(reward))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
        } else {
            int needed = reward.getPointsCost() - currentPoints;
            showError("You need " + needed + " more points to claim this reward.");
        }
    }

    @Override
    public void onEditReward(CustomReward reward) {
        Intent intent = new Intent(this, EditRewardActivity.class);
        intent.putExtra("reward_id", reward.getId());
        startActivity(intent);
    }

    private void claimReward(CustomReward reward) {
        try {
            currentPoints -= reward.getPointsCost();
            preferencesManager.saveCounter(currentPoints);
            if (rewardCounter != null) {
                rewardCounter.setText(String.valueOf(currentPoints));
            }

            // Create transaction
            EnhancedTransaction transaction = new EnhancedTransaction(
                "SPEND",
                reward.getName(),
                reward.getDescription(),
                reward.getPointsCost(),
                reward.getCategory()
            );
            customizationManager.addTransaction(transaction);

            // Update reward usage
            reward.incrementUsage();
            customizationManager.updateCustomReward(reward);

            updateWelcomeMessage();
            showSuccessAnimation(getString(R.string.reward_claimed) + " 🎉");

        } catch (Exception e) {
            showError(getString(R.string.error_generic));
        }
    }

    // Navigation methods
    private void openCustomizationActivity() {
        startActivity(new Intent(this, ManageRewardsActivity.class));
    }

    private void openHistoryActivity() {
        startActivity(new Intent(this, ModernHistoryActivity.class));
    }

    private void openSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void openRewardCreator() {
        startActivity(new Intent(this, CreateRewardActivity.class));
    }

    private void openPointsSourceCreator() {
        startActivity(new Intent(this, CreatePointsSourceActivity.class));
    }

    private void openActivityCreator() {
        Toast.makeText(this, "Custom activity creator coming soon!", Toast.LENGTH_SHORT).show();
    }

    // Animation and UI methods
    private void animateCardIn(View card) {
        if (card == null) return;
        card.setAlpha(0f);
        card.setTranslationY(-100f);
        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start();
    }

    private void animateCardOut(View card) {
        if (card == null) return;
        card.animate()
            .alpha(0f)
            .translationY(-100f)
            .setDuration(300)
            .start();
    }

    private void showSuccessAnimation(String message) {
        if (congratsText == null) return;
        congratsText.setText(message);
        congratsText.setVisibility(View.VISIBLE);
        congratsText.setAlpha(0f);
        congratsText.animate()
            .alpha(1f)
            .setDuration(500)
            .start();

        // Hide after 3 seconds
        congratsText.postDelayed(() -> {
            if (congratsText != null) {
                congratsText.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        if (congratsText != null) {
                            congratsText.setVisibility(View.GONE);
                        }
                    })
                    .start();
            }
        }, 3000);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes gracefully
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_points", currentPoints);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPoints = savedInstanceState.getInt("current_points", 0);
        if (rewardCounter != null) {
            rewardCounter.setText(String.valueOf(currentPoints));
        }
    }
}
