package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.PointsSource;

public class CreatePointsSourceActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText nameInput, descriptionInput, pointsInput;
    private TextInputLayout nameInputLayout, pointsInputLayout;
    private ChipGroup categoryChipGroup;
    private RadioGroup frequencyGroup;
    private MaterialButton saveBtn, cancelBtn;

    // Manager
    private CustomizationManager customizationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_points_source);

        setupToolbar();
        initializeComponents();
        setupClickListeners();

        // Set default category selection
        categoryChipGroup.check(R.id.chip_health);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeComponents() {
        nameInput = findViewById(R.id.name_input);
        descriptionInput = findViewById(R.id.description_input);
        pointsInput = findViewById(R.id.points_input);
        nameInputLayout = findViewById(R.id.name_input_layout);
        pointsInputLayout = findViewById(R.id.points_input_layout);
        categoryChipGroup = findViewById(R.id.category_chip_group);
        frequencyGroup = findViewById(R.id.frequency_group);
        saveBtn = findViewById(R.id.save_btn);
        cancelBtn = findViewById(R.id.cancel_btn);

        customizationManager = new CustomizationManager(this);
    }

    private void setupClickListeners() {
        saveBtn.setOnClickListener(v -> savePointsSource());
        cancelBtn.setOnClickListener(v -> finish());
    }

    private void savePointsSource() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            int points = Integer.parseInt(pointsInput.getText().toString().trim());
            String category = getSelectedCategory();
            String frequency = getSelectedFrequency();

            // Create new points source
            PointsSource pointsSource = new PointsSource(
                name,
                description,
                points,
                category
            );

            // Set frequency limitation if needed
            if ("daily".equals(frequency)) {
                pointsSource.setDailyLimit(1);
            } else if ("weekly".equals(frequency)) {
                pointsSource.setWeeklyLimit(1);
            }

            // Save points source
            customizationManager.addPointsSource(pointsSource);

            Toast.makeText(this, getString(R.string.points_source_added), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        nameInputLayout.setError(null);
        pointsInputLayout.setError(null);

        // Validate name
        if (TextUtils.isEmpty(nameInput.getText())) {
            nameInputLayout.setError("Activity name is required");
            isValid = false;
        }

        // Validate points
        String pointsStr = pointsInput.getText().toString().trim();
        if (TextUtils.isEmpty(pointsStr)) {
            pointsInputLayout.setError(getString(R.string.error_points_value_required));
            isValid = false;
        } else {
            try {
                int points = Integer.parseInt(pointsStr);
                if (points <= 0) {
                    pointsInputLayout.setError(getString(R.string.error_points_must_be_positive));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                pointsInputLayout.setError("Please enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private String getSelectedCategory() {
        int selectedId = categoryChipGroup.getCheckedChipId();

        if (selectedId == R.id.chip_health) return "Health";
        if (selectedId == R.id.chip_work) return "Work";
        if (selectedId == R.id.chip_learning) return "Learning";
        if (selectedId == R.id.chip_social) return "Social";
        if (selectedId == R.id.chip_hobby) return "Hobby";
        if (selectedId == R.id.chip_mood) return "Mood";

        return "Health"; // Default
    }

    private String getSelectedFrequency() {
        int selectedId = frequencyGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.frequency_daily) return "daily";
        if (selectedId == R.id.frequency_weekly) return "weekly";

        return "unlimited"; // Default
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
