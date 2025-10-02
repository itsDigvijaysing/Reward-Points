package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.utils.PreferencesManager;

public class PointsHistoryActivity extends AppCompatActivity {

    private TextView historyTextView;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointshistory);

        initializeComponents();
        loadHistory();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh history when returning to activity
        loadHistory();
    }

    private void initializeComponents() {
        try {
            historyTextView = findViewById(R.id.txtviewofhistory);
            preferencesManager = new PreferencesManager(this);
        } catch (Exception e) {
            showToast(getString(R.string.error_generic));
            finish();
        }
    }

    private void loadHistory() {
        try {
            String history = preferencesManager.getHistory();
            if (history == null || history.trim().isEmpty() || "No Previous History".equals(history)) {
                historyTextView.setText(getString(R.string.no_history_available));
            } else {
                historyTextView.setText(history);
            }
        } catch (Exception e) {
            showToast(getString(R.string.error_data_load));
            historyTextView.setText(getString(R.string.no_history_available));
        }
    }

    private void setupClickListeners() {
        try {
            ImageButton clearHistoryButton = findViewById(R.id.imgbtnclearhistory);
            clearHistoryButton.setOnClickListener(v -> clearHistory());
            clearHistoryButton.setContentDescription("Clear history button");
        } catch (Exception e) {
            showToast(getString(R.string.error_generic));
        }
    }

    private void clearHistory() {
        try {
            preferencesManager.clearHistory();
            showToast(getString(R.string.history_cleared));

            // Immediately update the display
            historyTextView.setText(getString(R.string.no_history_available));
        } catch (Exception e) {
            showToast(getString(R.string.error_generic));
        }
    }

    private void showToast(String message) {
        if (message != null && !isFinishing()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
