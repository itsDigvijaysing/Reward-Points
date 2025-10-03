package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.rewardpoints.app.adapters.RewardsAdapter;
import com.rewardpoints.app.managers.CustomizationManager;
import com.rewardpoints.app.models.CustomReward;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManageRewardsActivity extends AppCompatActivity implements RewardsAdapter.OnRewardClickListener {

    private RecyclerView rewardsRecyclerView;
    private FloatingActionButton fabAdd;
    private RewardsAdapter rewardsAdapter;
    private CustomizationManager customizationManager;
    private ActivityResultLauncher<Intent> createRewardLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rewards);

        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupActivityResultLauncher();
        loadRewards();
    }

    private void initializeComponents() {
        rewardsRecyclerView = findViewById(R.id.rewards_recycler_view);
        fabAdd = findViewById(R.id.fab_add_reward);
        customizationManager = new CustomizationManager(this);
    }

    private void setupActivityResultLauncher() {
        createRewardLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadRewards(); // Refresh the list
                }
            }
        );
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Rewards");
        }
    }

    private void setupRecyclerView() {
        rewardsAdapter = new RewardsAdapter(this);
        rewardsAdapter.setOnRewardClickListener(this);
        rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardsRecyclerView.setAdapter(rewardsAdapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateRewardActivity.class);
            createRewardLauncher.launch(intent);
        });
    }

    private void loadRewards() {
        try {
            List<CustomReward> rewards = customizationManager.getCustomRewards();
            rewardsAdapter.updateRewards(rewards);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading rewards", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRewardClick(CustomReward reward) {
        // For management, clicking shows options
        Toast.makeText(this, "Reward: " + reward.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteReward(CustomReward reward) {
        // Delete the reward using CustomizationManager
        customizationManager.deleteCustomReward(reward.getId());

        // Refresh the rewards list
        loadRewards();

        // Show confirmation
        Toast.makeText(this, "Reward '" + reward.getName() + "' deleted!", Toast.LENGTH_SHORT).show();
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
