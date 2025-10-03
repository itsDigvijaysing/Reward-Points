package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.utils.PreferencesManager;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText usernameInput, dailyLimitInput;
    private TextInputLayout usernameInputLayout, dailyLimitInputLayout;
    private MaterialSwitch dailyLimitSwitch, dailyRemindersSwitch, achievementNotificationsSwitch;
    private MaterialButton saveProfileBtn, resetPointsBtn, manageRewardsBtn, managePointsSourcesBtn;
    private MaterialButton exportDataBtn, resetAppBtn;

    // Managers
    private PreferencesManager preferencesManager;
    private CustomizationManager customizationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        initializeComponents();
        setupClickListeners();
        loadSettings();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeComponents() {
        // Text inputs
        usernameInput = findViewById(R.id.username_input);
        dailyLimitInput = findViewById(R.id.daily_limit_input);
        usernameInputLayout = findViewById(R.id.username_input_layout);
        dailyLimitInputLayout = findViewById(R.id.daily_limit_input_layout);

        // Switches
        dailyLimitSwitch = findViewById(R.id.daily_limit_switch);
        dailyRemindersSwitch = findViewById(R.id.daily_reminders_switch);
        achievementNotificationsSwitch = findViewById(R.id.achievement_notifications_switch);

        // Buttons
        saveProfileBtn = findViewById(R.id.save_profile_btn);
        resetPointsBtn = findViewById(R.id.reset_points_btn);
        manageRewardsBtn = findViewById(R.id.manage_rewards_btn);
        managePointsSourcesBtn = findViewById(R.id.manage_points_sources_btn);
        exportDataBtn = findViewById(R.id.export_data_btn);
        resetAppBtn = findViewById(R.id.reset_app_btn);

        // Initialize managers
        preferencesManager = new PreferencesManager(this);
        customizationManager = new CustomizationManager(this);

        // Set app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            findViewById(R.id.app_version_text).setTag("Version " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // Version not found, use default
        }
    }

    private void setupClickListeners() {
        // Daily limit switch toggle
        dailyLimitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dailyLimitInputLayout.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        // Profile save button
        saveProfileBtn.setOnClickListener(v -> saveProfile());

        // Reset points button
        resetPointsBtn.setOnClickListener(v -> showResetPointsDialog());

        // Management buttons
        manageRewardsBtn.setOnClickListener(v -> openManageRewards());
        managePointsSourcesBtn.setOnClickListener(v -> openManagePointsSources());

        // Data export and reset
        exportDataBtn.setOnClickListener(v -> exportAllData());
        resetAppBtn.setOnClickListener(v -> showResetAppDialog());

        // Save settings when switches change
        dailyRemindersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> saveNotificationSettings());
        achievementNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> saveNotificationSettings());
    }

    private void loadSettings() {
        // Load user profile
        String username = preferencesManager.getUserName();
        if (!TextUtils.isEmpty(username)) {
            usernameInput.setText(username);
        }

        // Load points system settings
        boolean isDailyLimitEnabled = preferencesManager.isDailyLimitEnabled();
        dailyLimitSwitch.setChecked(isDailyLimitEnabled);
        dailyLimitInputLayout.setVisibility(isDailyLimitEnabled ? android.view.View.VISIBLE : android.view.View.GONE);

        if (isDailyLimitEnabled) {
            int dailyLimit = preferencesManager.getDailyPointsLimit();
            dailyLimitInput.setText(String.valueOf(dailyLimit));
        }

        // Load notification settings
        dailyRemindersSwitch.setChecked(preferencesManager.isDailyRemindersEnabled());
        achievementNotificationsSwitch.setChecked(preferencesManager.isAchievementNotificationsEnabled());
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameInputLayout.setError("Name is required");
            return;
        }

        // Save username
        preferencesManager.setUserName(username);

        // Save daily limit settings
        preferencesManager.setDailyLimitEnabled(dailyLimitSwitch.isChecked());

        if (dailyLimitSwitch.isChecked()) {
            String limitStr = dailyLimitInput.getText().toString().trim();
            if (!TextUtils.isEmpty(limitStr)) {
                try {
                    int limit = Integer.parseInt(limitStr);
                    if (limit > 0) {
                        preferencesManager.setDailyPointsLimit(limit);
                    }
                } catch (NumberFormatException e) {
                    // Invalid number, ignore
                }
            }
        }

        Toast.makeText(this, getString(R.string.settings_saved_successfully), Toast.LENGTH_SHORT).show();
    }

    private void saveNotificationSettings() {
        preferencesManager.setDailyRemindersEnabled(dailyRemindersSwitch.isChecked());
        preferencesManager.setAchievementNotificationsEnabled(achievementNotificationsSwitch.isChecked());
    }

    private void showResetPointsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Reset Points")
            .setMessage("This will reset your current points to 0. This cannot be undone. Continue?")
            .setPositiveButton(getString(R.string.confirm), (dialog, which) -> resetPoints())
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private void resetPoints() {
        preferencesManager.saveCounter(0);
        Toast.makeText(this, "Points reset successfully", Toast.LENGTH_SHORT).show();
    }

    private void openManageRewards() {
        startActivity(new Intent(this, ManageRewardsActivity.class));
    }

    private void openManagePointsSources() {
        startActivity(new Intent(this, ManagePointsSourcesActivity.class));
    }

    private void exportAllData() {
        try {
            StringBuilder exportData = new StringBuilder();
            exportData.append("=== REWARD POINTS APP DATA EXPORT ===\n\n");

            // User info
            exportData.append("User: ").append(preferencesManager.getUserName()).append("\n");
            exportData.append("Current Points: ").append(preferencesManager.getCounter()).append("\n");
            exportData.append("Export Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date())).append("\n\n");

            // Settings
            exportData.append("=== SETTINGS ===\n");
            exportData.append("Daily Limit Enabled: ").append(preferencesManager.isDailyLimitEnabled()).append("\n");
            if (preferencesManager.isDailyLimitEnabled()) {
                exportData.append("Daily Points Limit: ").append(preferencesManager.getDailyPointsLimit()).append("\n");
            }
            exportData.append("Daily Reminders: ").append(preferencesManager.isDailyRemindersEnabled()).append("\n");
            exportData.append("Achievement Notifications: ").append(preferencesManager.isAchievementNotificationsEnabled()).append("\n\n");

            // Rewards
            exportData.append("=== REWARDS ===\n");
            java.util.List<com.rewardpoints.app.models.CustomReward> rewards = customizationManager.getCustomRewards();
            for (com.rewardpoints.app.models.CustomReward reward : rewards) {
                exportData.append("- ").append(reward.getName()).append(" (").append(reward.getPointsCost()).append(" points)\n");
                exportData.append("  Category: ").append(reward.getCategory()).append("\n");
                exportData.append("  Description: ").append(reward.getDescription()).append("\n\n");
            }

            // Points Sources
            exportData.append("=== POINTS SOURCES ===\n");
            java.util.List<com.rewardpoints.app.models.PointsSource> sources = customizationManager.getAllPointsSources();
            for (com.rewardpoints.app.models.PointsSource source : sources) {
                exportData.append("- ").append(source.getName()).append(" (+").append(source.getPointsValue()).append(" points)\n");
                exportData.append("  Category: ").append(source.getCategory()).append("\n");
                exportData.append("  Description: ").append(source.getDescription()).append("\n\n");
            }

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, exportData.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reward Points App Data Export");
            startActivity(Intent.createChooser(shareIntent, "Export App Data"));

        } catch (Exception e) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResetAppDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Reset All App Data")
            .setMessage("This will permanently delete ALL your data including:\n\n• Points and history\n• Custom rewards\n• Points sources\n• Settings\n\nThis cannot be undone. Continue?")
            .setPositiveButton("RESET ALL", (dialog, which) -> resetAllAppData())
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private void resetAllAppData() {
        try {
            // Clear all preferences
            preferencesManager.clearAllData();

            // Clear all customization data - use available methods
            customizationManager.resetAllData();

            Toast.makeText(this, "All app data has been reset", Toast.LENGTH_SHORT).show();

            // Restart the app
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Reset failed", Toast.LENGTH_SHORT).show();
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
}
