package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.PointsSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManagePointsSourcesActivity extends AppCompatActivity {

    private RecyclerView sourcesRecyclerView;
    private FloatingActionButton fabAdd;
    private CustomizationManager customizationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_points_sources);

        initializeComponents();
        setupToolbar();
        setupClickListeners();
        loadPointsSources();
    }

    private void initializeComponents() {
        sourcesRecyclerView = findViewById(R.id.sources_recycler_view);
        fabAdd = findViewById(R.id.fab_add_source);
        customizationManager = new CustomizationManager(this);
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
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePointsSourceActivity.class);
            startActivityForResult(intent, 100);
        });
    }

    private void loadPointsSources() {
        try {
            List<PointsSource> sources = customizationManager.getPointsSources();
            // TODO: Create adapter for points sources
            Toast.makeText(this, "Loaded " + sources.size() + " points sources", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading points sources", Toast.LENGTH_SHORT).show();
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
