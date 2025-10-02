package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private MaterialButton themeSelector, backupDataBtn, restoreDataBtn, resetAppBtn;
    private MaterialSwitch notificationsSwitch;
    private TextView versionText;

    private CustomizationManager customizationManager;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeComponents();
        setupToolbar();
        setupClickListeners();
        loadSettings();
    }

    private void initializeComponents() {
        themeSelector = findViewById(R.id.theme_selector);
        backupDataBtn = findViewById(R.id.backup_data_btn);
        restoreDataBtn = findViewById(R.id.restore_data_btn);
        resetAppBtn = findViewById(R.id.reset_app_btn);
        notificationsSwitch = findViewById(R.id.notifications_switch);
        versionText = findViewById(R.id.version_text);

        customizationManager = new CustomizationManager(this);
        preferencesManager = new PreferencesManager(this);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.settings));
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.manage_points_sources).setOnClickListener(v ->
            startActivity(new Intent(this, ManagePointsSourcesActivity.class)));

        findViewById(R.id.manage_rewards).setOnClickListener(v ->
            startActivity(new Intent(this, ManageRewardsActivity.class)));

        themeSelector.setOnClickListener(v -> showThemeSelector());

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationsEnabled(isChecked);
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"),
                Toast.LENGTH_SHORT).show();
        });

        backupDataBtn.setOnClickListener(v -> backupData());
        restoreDataBtn.setOnClickListener(v -> restoreData());
        resetAppBtn.setOnClickListener(v -> showResetDialog());
    }

    private void loadSettings() {
        // Load current theme
        String currentTheme = preferencesManager.getThemeMode();
        themeSelector.setText(currentTheme);

        // Load notifications setting
        notificationsSwitch.setChecked(preferencesManager.areNotificationsEnabled());

        // Show version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(getString(R.string.app_version, versionName));
        } catch (Exception e) {
            versionText.setText("Version 2.0.0");
        }
    }

    private void showThemeSelector() {
        String[] themes = {"System", "Light", "Dark"};
        String currentTheme = preferencesManager.getThemeMode();
        int selectedIndex = 0;

        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(currentTheme)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
                String selectedTheme = themes[which];
                preferencesManager.setThemeMode(selectedTheme);
                themeSelector.setText(selectedTheme);
                dialog.dismiss();

                // Restart activity to apply theme
                recreate();
            })
            .show();
    }

    private void backupData() {
        // Implement backup functionality
        Toast.makeText(this, "Backup feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void restoreData() {
        // Implement restore functionality
        Toast.makeText(this, "Restore feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showResetDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.confirmation_title))
            .setMessage(getString(R.string.reset_settings_confirmation))
            .setPositiveButton(getString(R.string.confirm), (dialog, which) -> resetApp())
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private void resetApp() {
        try {
            preferencesManager.clearAll();
            customizationManager.clearAllTransactions();

            Toast.makeText(this, "App reset successfully!", Toast.LENGTH_SHORT).show();

            // Return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
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
