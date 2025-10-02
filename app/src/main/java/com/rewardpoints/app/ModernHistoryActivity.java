package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.rewardpoints.app.adapters.HistoryAdapter;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class ModernHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView totalEarnedText, totalSpentText;
    private MaterialButton clearHistoryBtn;
    private FloatingActionButton exportFab;
    private TabLayout filterTabs;

    private CustomizationManager customizationManager;
    private PreferencesManager preferencesManager;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointshistory);

        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupFilterTabs();
        loadData();
    }

    private void initializeComponents() {
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        totalEarnedText = findViewById(R.id.total_earned_text);
        totalSpentText = findViewById(R.id.total_spent_text);
        clearHistoryBtn = findViewById(R.id.clear_history_btn);
        exportFab = findViewById(R.id.export_fab);
        filterTabs = findViewById(R.id.filter_tabs);

        customizationManager = new CustomizationManager(this);
        preferencesManager = new PreferencesManager(this);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.transaction_history));
        }
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(this);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void setupClickListeners() {
        clearHistoryBtn.setOnClickListener(v -> showClearHistoryDialog());
        exportFab.setOnClickListener(v -> exportHistory());
    }

    private void setupFilterTabs() {
        filterTabs.addTab(filterTabs.newTab().setText(getString(R.string.all_time)));
        filterTabs.addTab(filterTabs.newTab().setText(getString(R.string.this_month)));
        filterTabs.addTab(filterTabs.newTab().setText(getString(R.string.this_week)));

        filterTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterTransactions(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadData() {
        try {
            // Load statistics
            int totalEarned = customizationManager.getTotalPointsEarned();
            int totalSpent = customizationManager.getTotalPointsSpent();

            totalEarnedText.setText(String.valueOf(totalEarned));
            totalSpentText.setText(String.valueOf(totalSpent));

            // Load transactions
            List<EnhancedTransaction> transactions = customizationManager.getEnhancedTransactions();
            historyAdapter.updateTransactions(transactions);

            // Show empty state if no transactions
            findViewById(R.id.empty_state_layout).setVisibility(
                transactions.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterTransactions(int filterType) {
        // Implement filtering logic based on time period
        List<EnhancedTransaction> allTransactions = customizationManager.getEnhancedTransactions();
        // Filter logic would go here based on filterType
        historyAdapter.updateTransactions(allTransactions);
    }

    private void showClearHistoryDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.confirmation_title))
            .setMessage(getString(R.string.clear_history_confirmation))
            .setPositiveButton(getString(R.string.confirm), (dialog, which) -> clearHistory())
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private void clearHistory() {
        try {
            customizationManager.clearAllTransactions();
            preferencesManager.clearHistory();

            Toast.makeText(this, getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
            loadData();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportHistory() {
        // Implement export functionality
        Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
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
