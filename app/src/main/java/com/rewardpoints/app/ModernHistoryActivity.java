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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ModernHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView totalEarnedText, totalSpentText; // , emptyStateText;
    private MaterialButton clearHistoryBtn;
    private FloatingActionButton exportFab;
    private TabLayout filterTabs;

    private CustomizationManager customizationManager;
    private PreferencesManager preferencesManager;
    private HistoryAdapter historyAdapter;

    private List<EnhancedTransaction> allTransactions;
    private String currentFilter = "all";

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
        // emptyStateText = findViewById(R.id.empty_state_text); // Comment out until layout is updated

        customizationManager = new CustomizationManager(this);
        preferencesManager = new PreferencesManager(this);
        allTransactions = new ArrayList<>();
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
        if (clearHistoryBtn != null) {
            clearHistoryBtn.setOnClickListener(v -> showClearHistoryDialog());
        }

        if (exportFab != null) {
            exportFab.setOnClickListener(v -> exportHistory());
        }
    }

    private void setupFilterTabs() {
        if (filterTabs != null) {
            filterTabs.addTab(filterTabs.newTab().setText(getString(R.string.all_time)));
            filterTabs.addTab(filterTabs.newTab().setText(getString(R.string.this_month)));
            filterTabs.addTab(filterTabs.newTab().setText(getString(R.string.this_week)));

            filterTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            currentFilter = "all";
                            break;
                        case 1:
                            currentFilter = "month";
                            break;
                        case 2:
                            currentFilter = "week";
                            break;
                    }
                    applyFilter();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void loadData() {
        try {
            // Get transactions from PreferencesManager instead of CustomizationManager
            allTransactions = preferencesManager.getTransactions();
            applyFilter();
            updateSummary();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilter() {
        List<EnhancedTransaction> filteredTransactions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        switch (currentFilter) {
            case "all":
                filteredTransactions = new ArrayList<>(allTransactions);
                break;
            case "month":
                filteredTransactions = filterTransactionsByMonth(calendar);
                break;
            case "week":
                filteredTransactions = filterTransactionsByWeek(calendar);
                break;
        }

        if (historyAdapter != null) {
            historyAdapter.updateTransactions(filteredTransactions);
        }

        // updateEmptyState(filteredTransactions.isEmpty());
    }

    private List<EnhancedTransaction> filterTransactionsByMonth(Calendar calendar) {
        List<EnhancedTransaction> filtered = new ArrayList<>();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        for (EnhancedTransaction transaction : allTransactions) {
            Calendar transactionCal = Calendar.getInstance();
            transactionCal.setTime(new Date(transaction.getTimestamp()));

            if (transactionCal.get(Calendar.MONTH) == currentMonth &&
                transactionCal.get(Calendar.YEAR) == currentYear) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    private List<EnhancedTransaction> filterTransactionsByWeek(Calendar calendar) {
        List<EnhancedTransaction> filtered = new ArrayList<>();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        long weekStart = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        long weekEnd = calendar.getTimeInMillis();

        for (EnhancedTransaction transaction : allTransactions) {
            long transactionTime = transaction.getTimestamp();
            if (transactionTime >= weekStart && transactionTime <= weekEnd) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    private void updateSummary() {
        int totalEarned = 0;
        int totalSpent = 0;

        for (EnhancedTransaction transaction : allTransactions) {
            if ("EARN".equals(transaction.getType())) {
                totalEarned += transaction.getAmount();
            } else if ("SPEND".equals(transaction.getType())) {
                totalSpent += transaction.getAmount();
            }
        }

        if (totalEarnedText != null) {
            totalEarnedText.setText(String.valueOf(totalEarned));
        }
        if (totalSpentText != null) {
            totalSpentText.setText(String.valueOf(totalSpent));
        }
    }

    // private void updateEmptyState(boolean isEmpty) {
    //     if (emptyStateText != null) {
    //         if (isEmpty) {
    //             emptyStateText.setVisibility(android.view.View.VISIBLE);
    //             emptyStateText.setText(getString(R.string.history_empty_message));
    //         } else {
    //             emptyStateText.setVisibility(android.view.View.GONE);
    //         }
    //     }
    // }

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
            preferencesManager.clearTransactions();
            allTransactions.clear();
            applyFilter();
            updateSummary();
            Toast.makeText(this, getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportHistory() {
        try {
            StringBuilder exportData = new StringBuilder();
            exportData.append("Transaction History Export\n");
            exportData.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");

            for (EnhancedTransaction transaction : allTransactions) {
                exportData.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(transaction.getTimestamp()))).append("\n");
                exportData.append("Type: ").append(transaction.getType()).append("\n");
                exportData.append("Title: ").append(transaction.getTitle()).append("\n");
                exportData.append("Amount: ").append(transaction.getAmount()).append(" points\n");
                exportData.append("Category: ").append(transaction.getCategory()).append("\n");
                if (transaction.getMood() != null) {
                    exportData.append("Mood: ").append(transaction.getMood()).append("\n");
                }
                exportData.append("---\n");
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, exportData.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reward Points History");
            startActivity(Intent.createChooser(shareIntent, "Export History"));

        } catch (Exception e) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
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
