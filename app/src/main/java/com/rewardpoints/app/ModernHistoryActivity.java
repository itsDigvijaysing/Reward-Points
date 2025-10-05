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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ModernHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView totalEarnedText, totalSpentText;

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
        loadData();
    }

    private void initializeComponents() {
        historyRecyclerView = findViewById(R.id.transactions_recycler_view);
        totalEarnedText = findViewById(R.id.total_earned_text);
        totalSpentText = findViewById(R.id.total_spent_text);

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
    }

    private void loadData() {
        try {
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
            } else if ("SPEND".equals(transaction.getType()) || "REDEEM".equals(transaction.getType())) {
                // Count both SPEND and REDEEM transactions as spent points
                totalSpent += Math.abs(transaction.getAmount()); // Use absolute value in case amount is negative
            }
        }

        if (totalEarnedText != null) {
            totalEarnedText.setText(String.valueOf(totalEarned));
        }
        if (totalSpentText != null) {
            totalSpentText.setText(String.valueOf(totalSpent));
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
