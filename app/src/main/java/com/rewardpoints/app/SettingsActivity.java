package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.utils.PreferencesManager;

public class SettingsActivity extends AppCompatActivity {

    // UI Components - fixed switch type
    private TextInputEditText usernameInput;
    private TextInputLayout usernameInputLayout;
    private SwitchMaterial notificationsSwitch;
    private MaterialButton saveUsernameButton, manageRewardsBtn, managePointsSourcesBtn;
    private MaterialButton viewPointsHistoryBtn, resetDataBtn;

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
        try {
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up toolbar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeComponents() {
        try {
            // Text inputs - with null checks
            usernameInputLayout = findViewById(R.id.username_input_layout);
            usernameInput = findViewById(R.id.username_edit_text);

            // Switches - with null checks
            notificationsSwitch = findViewById(R.id.notifications_switch);

            // Buttons - with null checks
            saveUsernameButton = findViewById(R.id.save_username_button);
            manageRewardsBtn = findViewById(R.id.manage_rewards_button);
            managePointsSourcesBtn = findViewById(R.id.manage_points_sources_button);
            viewPointsHistoryBtn = findViewById(R.id.view_points_history_button);
            resetDataBtn = findViewById(R.id.reset_data_button);

            // Initialize managers
            preferencesManager = new PreferencesManager(this);
            customizationManager = new CustomizationManager(this);

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        try {
            // Profile save button - with null check
            if (saveUsernameButton != null) {
                saveUsernameButton.setOnClickListener(v -> saveProfile());
            }

            // Management buttons - with null checks
            if (manageRewardsBtn != null) {
                manageRewardsBtn.setOnClickListener(v -> openManageRewards());
            }
            if (managePointsSourcesBtn != null) {
                managePointsSourcesBtn.setOnClickListener(v -> openManagePointsSources());
            }
            if (viewPointsHistoryBtn != null) {
                viewPointsHistoryBtn.setOnClickListener(v -> openPointsHistory());
            }

            // Reset data button - with null check
            if (resetDataBtn != null) {
                resetDataBtn.setOnClickListener(v -> showResetAppDialog());
            }

            // Save settings when switch changes - with null check
            if (notificationsSwitch != null) {
                notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> saveNotificationSettings());
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSettings() {
        try {
            // Load user profile - with null checks
            String username = preferencesManager.getUserName();
            if (!TextUtils.isEmpty(username) && usernameInput != null) {
                usernameInput.setText(username);
            }

            // Load notifications setting - with null check
            if (notificationsSwitch != null) {
                notificationsSwitch.setChecked(preferencesManager.isNotificationsEnabled());
            }

            // Display app version in Settings
            displayAppVersion();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayAppVersion() {
        try {
            // Get real app version from package manager
            String versionName = "1.5.0"; // Fallback version
            int versionCode = 5; // Fallback version code

            try {
                android.content.pm.PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
            } catch (Exception e) {
                // Use fallback versions if package info not available
            }

            // Since app_version_text doesn't exist in layout, we'll show version in title instead
            if (getSupportActionBar() != null) {
                String versionText = "Settings - v" + versionName;
                getSupportActionBar().setTitle(versionText);
            }

        } catch (Exception e) {
            // Silently fail if version display is not available
        }
    }

    private void saveProfile() {
        try {
            if (usernameInput == null || usernameInputLayout == null) {
                Toast.makeText(this, "Error: UI elements not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = usernameInput.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                usernameInputLayout.setError("Name is required");
                return;
            }

            // Save username
            preferencesManager.setUserName(username);
            usernameInputLayout.setError(null); // Clear any previous error

            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNotificationSettings() {
        try {
            if (notificationsSwitch != null) {
                preferencesManager.setNotificationsEnabled(notificationsSwitch.isChecked());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving notification settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

    private void openPointsHistory() {
        startActivity(new Intent(this, PointsHistoryActivity.class));
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
