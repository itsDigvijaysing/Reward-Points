package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.adapters.HistoryAdapter;
import com.rewardpoints.app.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class PointsHistoryActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;
    private RecyclerView historyRecyclerView;
    private LinearLayout emptyStateLayout;
    private TextView totalEarnedText;
    private TextView totalSpentText;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force light mode to prevent automatic dark mode switching
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointshistory);

        initializeComponents();
        setupToolbar();
        loadHistory();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void initializeComponents() {
        try {
            preferencesManager = new PreferencesManager(this);
            historyRecyclerView = findViewById(R.id.transactions_recycler_view);
            emptyStateLayout = findViewById(R.id.empty_state_layout);
            totalEarnedText = findViewById(R.id.total_earned_text);
            totalSpentText = findViewById(R.id.total_spent_text);

            // Setup RecyclerView
            historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            historyAdapter = new HistoryAdapter(this);
            historyRecyclerView.setAdapter(historyAdapter);

        } catch (Exception e) {
            showToast("Error initializing components");
            finish();
        }
    }

    private void setupToolbar() {
        try {
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        } catch (Exception e) {
            // Toolbar setup failed, continue without it
        }
    }

    private void loadHistory() {
        try {
            // Load statistics
            int totalEarned = preferencesManager.getTotalPointsEarned();
            int totalSpent = preferencesManager.getTotalPointsSpent();

            if (totalEarnedText != null) {
                totalEarnedText.setText(String.valueOf(totalEarned));
            }
            if (totalSpentText != null) {
                totalSpentText.setText(String.valueOf(totalSpent));
            }

            // Load modern transaction history from EnhancedTransactions
            List<com.rewardpoints.app.models.EnhancedTransaction> modernTransactions = preferencesManager.getTransactions();

            if (modernTransactions.isEmpty()) {
                // Fallback to legacy history if modern transactions are empty
                List<String> legacyHistory = getHistoryFromPreferences();
                if (legacyHistory.isEmpty()) {
                    showEmptyState();
                } else {
                    showLegacyHistoryData(legacyHistory);
                }
            } else {
                showModernHistoryData(modernTransactions);
            }
        } catch (Exception e) {
            showToast("Error loading history");
            showEmptyState();
        }
    }

    private List<String> getHistoryFromPreferences() {
        List<String> historyList = new ArrayList<>();
        try {
            String history = preferencesManager.getHistory();
            if (history != null && !history.trim().isEmpty() && !"No Previous History".equals(history)) {
                // Split history into individual transactions
                String[] transactions = history.split("\n");
                for (String transaction : transactions) {
                    if (!transaction.trim().isEmpty()) {
                        historyList.add(transaction.trim());
                    }
                }
            }
        } catch (Exception e) {
            // Return empty list on error
        }
        return historyList;
    }

    private void showEmptyState() {
        if (historyRecyclerView != null) {
            historyRecyclerView.setVisibility(View.GONE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showModernHistoryData(List<com.rewardpoints.app.models.EnhancedTransaction> transactions) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (historyRecyclerView != null) {
            historyRecyclerView.setVisibility(View.VISIBLE);
            historyAdapter.updateTransactions(transactions);
        }
    }

    private void showLegacyHistoryData(List<String> historyList) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (historyRecyclerView != null) {
            historyRecyclerView.setVisibility(View.VISIBLE);
            historyAdapter.updateHistory(historyList);
        }
    }

    private void setupClickListeners() {
        // Removed reference to clear_history_btn as it doesn't exist in our new layout
        // Clear history functionality can be added back later if needed
    }

    private void clearHistory() {
        try {
            preferencesManager.clearHistory();
            showToast("History cleared successfully");
            loadHistory(); // Refresh the display
        } catch (Exception e) {
            showToast("Error clearing history");
        }
    }

    private void showToast(String message) {
        if (message != null && !isFinishing()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
