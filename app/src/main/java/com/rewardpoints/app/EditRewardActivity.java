package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public class EditRewardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar();

        // Get reward ID from intent
        String rewardId = getIntent().getStringExtra("reward_id");

        // TODO: Implement full reward editing UI
        Toast.makeText(this, "Reward editing feature coming soon!", Toast.LENGTH_LONG).show();

        // Return success for now
        setResult(RESULT_OK);
        finish();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Reward");
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
