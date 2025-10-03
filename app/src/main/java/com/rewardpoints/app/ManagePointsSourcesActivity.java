package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.rewardpoints.app.adapters.PointsSourcesAdapter;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.PointsSource;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManagePointsSourcesActivity extends AppCompatActivity implements PointsSourcesAdapter.OnPointsSourceClickListener {

    private RecyclerView sourcesRecyclerView;
    private FloatingActionButton fabAdd;
    private CustomizationManager customizationManager;
    private PointsSourcesAdapter sourcesAdapter;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_points_sources);

        initializeComponents();
        setupToolbar();
        setupClickListeners();
        setupRecyclerView();
        loadPointsSources();
    }

    private void initializeComponents() {
        sourcesRecyclerView = findViewById(R.id.sources_recycler_view);
        fabAdd = findViewById(R.id.fab_add_source);
        customizationManager = new CustomizationManager(this);
        preferencesManager = new PreferencesManager(this);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Points Sources");
        }
    }

    private void setupClickListeners() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, CreatePointsSourceActivity.class);
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open create points source activity", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupRecyclerView() {
        if (sourcesRecyclerView != null) {
            sourcesAdapter = new PointsSourcesAdapter(this);
            sourcesAdapter.setOnPointsSourceClickListener(this);
            sourcesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            sourcesRecyclerView.setAdapter(sourcesAdapter);
        }
    }

    private void loadPointsSources() {
        try {
            List<PointsSource> sources = customizationManager.getPointsSources();

            if (sourcesAdapter != null) {
                sourcesAdapter.updatePointsSources(sources);
            }

            if (sources.isEmpty()) {
                Toast.makeText(this, "No points sources found. Add some using the + button!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Loaded " + sources.size() + " points sources", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading points sources: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // PointsSourcesAdapter.OnPointsSourceClickListener implementation
    @Override
    public void onPointsSourceClick(PointsSource pointsSource) {
        // Use the points source - add points to user
        try {
            int currentPoints = preferencesManager.getCounter();
            currentPoints += pointsSource.getPointsValue();
            preferencesManager.setCounter(currentPoints);

            // Record transaction
            EnhancedTransaction transaction = new EnhancedTransaction(
                "EARN",
                pointsSource.getName(),
                pointsSource.getDescription(),
                pointsSource.getPointsValue(),
                pointsSource.getCategory()
            );
            preferencesManager.addTransaction(transaction);

            Toast.makeText(this, "Earned " + pointsSource.getPointsValue() + " points from " + pointsSource.getName() + "!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error using points source: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditPointsSource(PointsSource pointsSource) {
        // Edit functionality - for now show a message
        Toast.makeText(this, "Edit functionality for '" + pointsSource.getName() + "' coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePointsSource(PointsSource pointsSource) {
        try {
            customizationManager.deletePointsSource(pointsSource.getId());
            loadPointsSources(); // Refresh the list
            Toast.makeText(this, "Points source '" + pointsSource.getName() + "' deleted!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting points source: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadPointsSources(); // Refresh the list
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
