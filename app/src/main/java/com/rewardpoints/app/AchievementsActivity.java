package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.adapters.AchievementsAdapter;
import com.rewardpoints.app.managers.AchievementManager;
import com.rewardpoints.app.models.Achievement;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity implements AchievementsAdapter.OnAchievementClickListener {

    private RecyclerView achievementsRecyclerView;
    private TextView achievementProgressText, currentPointsText, emptyStateText;
    private LinearProgressIndicator achievementProgressBar;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddAchievement;

    private AchievementManager achievementManager;
    private PreferencesManager preferencesManager;
    private AchievementsAdapter achievementsAdapter;

    private boolean showingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadAchievements();
    }

    private void initializeComponents() {
        achievementsRecyclerView = findViewById(R.id.achievements_recycler_view);
        achievementProgressText = findViewById(R.id.achievement_progress_text);
        currentPointsText = findViewById(R.id.current_points_text);
        achievementProgressBar = findViewById(R.id.achievement_progress_bar);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        emptyStateText = emptyStateLayout != null ? emptyStateLayout.findViewById(android.R.id.text1) : null; // Use system text view if custom doesn't exist
        fabAddAchievement = findViewById(R.id.fab_add_achievement);

        achievementManager = new AchievementManager(this);
        preferencesManager = new PreferencesManager(this);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🎯 Long-term Goals");
        }
    }

    private void setupRecyclerView() {
        achievementsAdapter = new AchievementsAdapter(this);
        achievementsAdapter.setOnAchievementClickListener(this);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        achievementsRecyclerView.setAdapter(achievementsAdapter);
    }

    private void setupClickListeners() {
        if (fabAddAchievement != null) {
            fabAddAchievement.setOnClickListener(v -> showCreateAchievementDialog());
        }
    }

    private void loadAchievements() {
        try {
            // Update current points
            int currentPoints = preferencesManager.getCounter();
            if (currentPointsText != null) {
                currentPointsText.setText(String.valueOf(currentPoints));
            }

            // Load achievements based on current tab
            List<Achievement> allAchievements = achievementManager.getUserAchievements();
            List<Achievement> filteredAchievements = new ArrayList<>();

            if (allAchievements != null) {
                for (Achievement achievement : allAchievements) {
                    if (showingCompleted && achievement.isCompleted()) {
                        filteredAchievements.add(achievement);
                    } else if (!showingCompleted && !achievement.isCompleted()) {
                        filteredAchievements.add(achievement);
                    }
                }
            }

            // Update progress
            int totalAchievements = allAchievements != null ? allAchievements.size() : 0;
            int completedCount = 0;

            if (allAchievements != null) {
                for (Achievement achievement : allAchievements) {
                    if (achievement.isCompleted()) {
                        completedCount++;
                    }
                }
            }

            if (achievementProgressText != null) {
                achievementProgressText.setText(completedCount + " / " + totalAchievements + " Completed");
            }

            if (achievementProgressBar != null) {
                int progress = totalAchievements > 0 ? (completedCount * 100) / totalAchievements : 0;
                achievementProgressBar.setProgress(progress);
            }

            // Show achievements or empty state
            if (!filteredAchievements.isEmpty()) {
                achievementsAdapter.updateAchievements(filteredAchievements);
                achievementsRecyclerView.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
            } else {
                achievementsRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);

                if (emptyStateText != null) {
                    if (showingCompleted) {
                        emptyStateText.setText("🎯 No completed goals yet\n\nComplete some goals to see them here!");
                    } else {
                        emptyStateText.setText("🎯 No goals set yet\n\nTap + to create your first long-term goal!");
                    }
                }
            }

        } catch (Exception e) {
            // Handle error - show empty state
            achievementsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Error loading goals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateAchievementDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_achievement, null);
        EditText nameInput = dialogView.findViewById(R.id.achievement_name_input);
        EditText descriptionInput = dialogView.findViewById(R.id.achievement_description_input);
        EditText pointsInput = dialogView.findViewById(R.id.achievement_points_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Create Long-term Goal")
            .setView(dialogView)
            .setPositiveButton("Create", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String pointsText = pointsInput.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                    return;
                }

                int points = 50; // Default points
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

                createNewAchievement(name, description, points);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createNewAchievement(String name, String description, int rewardPoints) {
        try {
            Achievement achievement = new Achievement(
                "custom_" + System.currentTimeMillis(),
                name,
                description,
                rewardPoints,
                "Custom",
                false
            );

            achievementManager.addUserAchievement(achievement);
            Toast.makeText(this, "Goal '" + name + "' created!", Toast.LENGTH_SHORT).show();
            loadAchievements();

        } catch (Exception e) {
            Toast.makeText(this, "Error creating goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAchievementClick(Achievement achievement) {
        if (!achievement.isCompleted()) {
            showCompleteAchievementDialog(achievement);
        }
    }

    @Override
    public void onDeleteAchievement(Achievement achievement) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '" + achievement.getName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                achievementManager.deleteUserAchievement(achievement.getId());
                Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show();
                loadAchievements();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCompleteAchievementDialog(Achievement achievement) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Complete Goal")
            .setMessage("Mark '" + achievement.getName() + "' as completed?\n\nYou'll earn " + achievement.getRewardPoints() + " points!")
            .setPositiveButton("Complete", (dialog, which) -> {
                completeAchievement(achievement);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void completeAchievement(Achievement achievement) {
        try {
            // Mark as completed
            achievement.setCompleted(true);
            achievementManager.updateUserAchievement(achievement);

            // Award points
            int currentPoints = preferencesManager.getCounter();
            currentPoints += achievement.getRewardPoints();
            preferencesManager.setCounter(currentPoints);

            // Record transaction
            preferencesManager.addTransaction(new com.rewardpoints.app.models.EnhancedTransaction(
                "EARN", "Goal Completed", achievement.getName(),
                achievement.getRewardPoints(), "Achievement"
            ));

            Toast.makeText(this, "🎉 Goal completed! +" + achievement.getRewardPoints() + " points", Toast.LENGTH_LONG).show();
            loadAchievements();

        } catch (Exception e) {
            Toast.makeText(this, "Error completing goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAchievements();
    }
}
