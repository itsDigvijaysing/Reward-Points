package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PointsHistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "reward_points_prefs";
    private static final String KEY_HISTORY = "fulldate";

    private TextView historyTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointshistory);

        initializeComponents();
        loadHistory();
        setupClickListeners();
    }

    private void initializeComponents() {
        historyTextView = findViewById(R.id.txtviewofhistory);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadHistory() {
        String history = sharedPreferences.getString(KEY_HISTORY, "No History Available");
        historyTextView.setText(history);
    }

    private void setupClickListeners() {
        ImageButton clearHistoryButton = findViewById(R.id.imgbtnclearhistory);
        clearHistoryButton.setOnClickListener(v -> clearHistory());
    }

    private void clearHistory() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_HISTORY, "");
        editor.apply();

        Toast.makeText(this, "History cleared successfully!", Toast.LENGTH_SHORT).show();

        // Immediately update the display
        historyTextView.setText("No History Available");
    }
}