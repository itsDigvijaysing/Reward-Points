package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.adapters.MissionsAdapter;
import com.rewardpoints.app.models.DailyMission;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyMissionsActivity extends AppCompatActivity implements MissionsAdapter.OnMissionClickListener {

    private RecyclerView missionsRecyclerView;
    private MissionsAdapter missionsAdapter;
    private TextView dailyStreakText, completionText, emptyStateText;
    private ProgressBar dailyProgressBar;
    private MaterialCardView streakCard;
    private FloatingActionButton fabAddMission;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_missions);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        checkDailyReset();
        loadMissions();
    }

    private void initializeViews() {
        missionsRecyclerView = findViewById(R.id.missions_recycler_view);
        dailyStreakText = findViewById(R.id.daily_streak_text);
        completionText = findViewById(R.id.completion_text);
        dailyProgressBar = findViewById(R.id.daily_progress_bar);
        streakCard = findViewById(R.id.streak_card);
        emptyStateText = findViewById(R.id.empty_state_layout) != null ?
            findViewById(R.id.empty_state_layout).findViewById(android.R.id.text1) : null; // Use system text view if custom doesn't exist

        fabAddMission = findViewById(R.id.fab_add_achievement); // Reuse existing FAB ID

        preferencesManager = new PreferencesManager(this);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("⭐ Daily Missions");
        }

        // Show current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        toolbar.setSubtitle("Today: " + currentDate);
    }

    private void setupRecyclerView() {
        try {
            if (missionsRecyclerView != null) {
                missionsAdapter = new MissionsAdapter(this);
                if (missionsAdapter != null) {
                    missionsAdapter.setOnMissionClickListener(this);
                    missionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    missionsRecyclerView.setAdapter(missionsAdapter);
                }
            } else {
                android.util.Log.w("DailyMissionsActivity", "missionsRecyclerView is null - cannot setup");
            }
        } catch (Exception e) {
            android.util.Log.e("DailyMissionsActivity", "Error setting up RecyclerView", e);
            Toast.makeText(this, "RecyclerView setup failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        if (fabAddMission != null) {
            fabAddMission.setOnClickListener(v -> showCreateMissionDialog());
        }
    }

    private void checkDailyReset() {
        // Check if it's a new day and reset missions if needed
        String lastDate = preferencesManager.getLastMissionDate();
        String currentDate = getCurrentDateString();

        if (!currentDate.equals(lastDate)) {
            resetDailyMissions();
            preferencesManager.setLastMissionDate(currentDate);
        }
    }

    private String getCurrentDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void resetDailyMissions() {
        // Reset all user missions for the new day
        List<DailyMission> missions = preferencesManager.getUserDailyMissions();
        if (missions != null) {
            for (DailyMission mission : missions) {
                mission.setCompleted(false);
            }
            preferencesManager.saveUserDailyMissions(missions);
        }
    }

    private void loadMissions() {
        try {
            // Ensure preferencesManager is not null
            if (preferencesManager == null) {
                preferencesManager = new PreferencesManager(this);
            }

            List<DailyMission> missions = preferencesManager.getUserDailyMissions();
            if (missions == null) {
                missions = new ArrayList<>();
            }

            // Calculate progress with null checks
            int totalMissions = missions.size();
            int completedMissions = 0;

            for (DailyMission mission : missions) {
                if (mission != null && mission.isCompleted()) {
                    completedMissions++;
                }
            }

            // Update UI with comprehensive null checks
            if (completionText != null) {
                completionText.setText(completedMissions + " / " + totalMissions + " Completed Today");
            }

            if (dailyProgressBar != null) {
                int progress = totalMissions > 0 ? (completedMissions * 100) / totalMissions : 0;
                dailyProgressBar.setProgress(progress);
            }

            // Update streak
            updateDailyStreak(completedMissions == totalMissions && totalMissions > 0);

            // Show missions or empty state with proper null checks
            if (!missions.isEmpty() && missionsAdapter != null && missionsRecyclerView != null) {
                missionsAdapter.updateMissions(missions);
                missionsRecyclerView.setVisibility(View.VISIBLE);

                // Safe empty state handling
                View emptyLayout = findViewById(R.id.empty_state_layout);
                if (emptyLayout != null) {
                    emptyLayout.setVisibility(View.GONE);
                }
            } else {
                // Handle empty missions case
                if (missionsRecyclerView != null) {
                    missionsRecyclerView.setVisibility(View.GONE);
                }

                View emptyLayout = findViewById(R.id.empty_state_layout);
                if (emptyLayout != null) {
                    emptyLayout.setVisibility(View.VISIBLE);
                }

                if (emptyStateText != null) {
                    emptyStateText.setText("⭐ No daily missions yet\n\nTap + to create your first daily mission!\n\nMissions reset every day at midnight.");
                }
            }

        } catch (Exception e) {
            // Comprehensive error handling
            android.util.Log.e("DailyMissionsActivity", "Error loading missions", e);
            Toast.makeText(this, "Error loading missions. Please try again.", Toast.LENGTH_SHORT).show();

            // Safe fallback UI state
            try {
                if (missionsRecyclerView != null) {
                    missionsRecyclerView.setVisibility(View.GONE);
                }
                View emptyLayout = findViewById(R.id.empty_state_layout);
                if (emptyLayout != null) {
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            } catch (Exception fallbackError) {
                // Ultimate fallback - just log the error
                android.util.Log.e("DailyMissionsActivity", "Fallback UI update failed", fallbackError);
            }
        }
    }

    private void updateDailyStreak(boolean allCompleted) {
        int currentStreak = preferencesManager.getDailyStreak();

        if (dailyStreakText != null) {
            dailyStreakText.setText(String.valueOf(currentStreak));
        }

        // If all missions completed today, potentially increment streak
        if (allCompleted) {
            String lastStreakDate = preferencesManager.getLastStreakDate();
            String currentDate = getCurrentDateString();

            if (!currentDate.equals(lastStreakDate)) {
                preferencesManager.setDailyStreak(currentStreak + 1);
                preferencesManager.setLastStreakDate(currentDate);

                // Show streak bonus
                showStreakBonus(currentStreak + 1);
            }
        }
    }

    private void showStreakBonus(int streak) {
        if (streak > 1) {
            int bonusPoints = streak * 5; // 5 points per streak day

            // Award bonus points
            int currentPoints = preferencesManager.getCounter();
            currentPoints += bonusPoints;
            preferencesManager.setCounter(currentPoints);

            // Record transaction
            preferencesManager.addTransaction(new EnhancedTransaction(
                "EARN", "Daily Streak Bonus", "Day " + streak + " streak", bonusPoints, "Daily"
            ));

            Toast.makeText(this, "🔥 " + streak + "-day streak! Bonus: +" + bonusPoints + " points", Toast.LENGTH_LONG).show();
        }
    }

    private void showCreateMissionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_mission, null);
        EditText nameInput = dialogView.findViewById(R.id.mission_name_input);
        EditText descriptionInput = dialogView.findViewById(R.id.mission_description_input);
        EditText pointsInput = dialogView.findViewById(R.id.mission_points_input);

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

                createNewMission(name, description, points);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createNewMission(String name, String description, int points) {
        try {
            DailyMission mission = new DailyMission(
                "mission_" + System.currentTimeMillis(),
                name,
                description,
                points,
                false
            );

            List<DailyMission> missions = preferencesManager.getUserDailyMissions();
            if (missions == null) {
                missions = new ArrayList<>();
            }

            missions.add(mission);
            preferencesManager.saveUserDailyMissions(missions);

            Toast.makeText(this, "Mission '" + name + "' created!", Toast.LENGTH_SHORT).show();
            loadMissions();

        } catch (Exception e) {
            Toast.makeText(this, "Error creating mission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMissionClick(DailyMission mission) {
        if (!mission.isCompleted()) {
            showCompleteMissionDialog(mission);
        } else {
            Toast.makeText(this, "Mission already completed today!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteMission(DailyMission mission) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Mission")
            .setMessage("Are you sure you want to delete '" + mission.getName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                deleteMission(mission);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteMission(DailyMission mission) {
        try {
            List<DailyMission> missions = preferencesManager.getUserDailyMissions();
            if (missions != null) {
                missions.removeIf(m -> m.getId().equals(mission.getId()));
                preferencesManager.saveUserDailyMissions(missions);
                Toast.makeText(this, "Mission deleted", Toast.LENGTH_SHORT).show();
                loadMissions();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting mission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCompleteMissionDialog(DailyMission mission) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("✅ Complete Mission")
            .setMessage("Mark '" + mission.getName() + "' as completed?\n\nYou'll earn " + mission.getPoints() + " points!")
            .setPositiveButton("Complete", (dialog, which) -> {
                completeMission(mission);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void completeMission(DailyMission mission) {
        try {
            // Mark as completed
            mission.setCompleted(true);

            // Update in storage
            List<DailyMission> missions = preferencesManager.getUserDailyMissions();
            if (missions != null) {
                for (int i = 0; i < missions.size(); i++) {
                    if (missions.get(i).getId().equals(mission.getId())) {
                        missions.set(i, mission);
                        break;
                    }
                }
                preferencesManager.saveUserDailyMissions(missions);
            }

            // Award points
            int currentPoints = preferencesManager.getCounter();
            currentPoints += mission.getPoints();
            preferencesManager.setCounter(currentPoints);

            // Record transaction
            preferencesManager.addTransaction(new EnhancedTransaction(
                "EARN", "Daily Mission", mission.getName(), mission.getPoints(), "Daily"
            ));

            Toast.makeText(this, "✅ Mission completed! +" + mission.getPoints() + " points", Toast.LENGTH_SHORT).show();
            loadMissions();

        } catch (Exception e) {
            Toast.makeText(this, "Error completing mission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        checkDailyReset();
        loadMissions();
    }
}