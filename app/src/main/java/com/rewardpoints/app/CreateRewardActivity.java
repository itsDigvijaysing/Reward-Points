package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.CustomReward;

public class CreateRewardActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText nameInput, descriptionInput, pointsInput;
    private TextInputLayout nameInputLayout, pointsInputLayout;
    private ChipGroup categoryChipGroup;
    private MaterialButton saveBtn, cancelBtn;

    // Manager
    private CustomizationManager customizationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reward);

        setupToolbar();
        initializeComponents();
        setupClickListeners();

        // Set default category selection
        categoryChipGroup.check(R.id.chip_entertainment);
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
        saveBtn = findViewById(R.id.save_btn);
        cancelBtn = findViewById(R.id.cancel_btn);

        customizationManager = new CustomizationManager(this);
    }

    private void setupClickListeners() {
        saveBtn.setOnClickListener(v -> saveReward());
        cancelBtn.setOnClickListener(v -> finish());
    }

    private void saveReward() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            int points = Integer.parseInt(pointsInput.getText().toString().trim());
            String category = getSelectedCategory();

            // Create new reward
            CustomReward reward = new CustomReward(
                name,
                description,
                points,
                category
            );

            // Save reward
            customizationManager.addCustomReward(reward);

            Toast.makeText(this, getString(R.string.reward_added_successfully), Toast.LENGTH_SHORT).show();
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
            nameInputLayout.setError(getString(R.string.error_reward_name_required));
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

        if (selectedId == R.id.chip_entertainment) return getString(R.string.entertainment);
        if (selectedId == R.id.chip_health) return getString(R.string.health_fitness);
        if (selectedId == R.id.chip_shopping) return getString(R.string.shopping);
        if (selectedId == R.id.chip_travel) return getString(R.string.travel);
        if (selectedId == R.id.chip_food) return getString(R.string.food_dining);

        return getString(R.string.entertainment); // Default
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
