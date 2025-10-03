package com.rewardpoints.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rewardpoints.app.R;
import com.rewardpoints.app.models.CustomReward;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.RewardViewHolder> {

    private final List<CustomReward> rewards;
    private final Context context;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(CustomReward reward);
        void onDeleteReward(CustomReward reward); // Keep only delete callback
    }

    public RewardsAdapter(Context context) {
        this.context = context;
        this.rewards = new ArrayList<>();
    }

    public void setOnRewardClickListener(OnRewardClickListener listener) {
        this.listener = listener;
    }

    public void updateRewards(List<CustomReward> newRewards) {
        this.rewards.clear();
        this.rewards.addAll(newRewards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        CustomReward reward = rewards.get(position);
        holder.bind(reward);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public class RewardViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText, descriptionText, pointsText, categoryText;
        private final MaterialButton redeemButton;
        private final MaterialCardView cardView;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.reward_name);
            descriptionText = itemView.findViewById(R.id.reward_description);
            pointsText = itemView.findViewById(R.id.reward_points);
            categoryText = itemView.findViewById(R.id.reward_category);
            redeemButton = itemView.findViewById(R.id.claim_reward_button);
            cardView = itemView.findViewById(R.id.reward_card);
        }

        public void bind(CustomReward reward) {
            if (reward == null) return;

            // Set basic information
            if (nameText != null) {
                nameText.setText(reward.getName());
            }
            if (descriptionText != null) {
                descriptionText.setText(reward.getDescription());
            }
            if (pointsText != null) {
                pointsText.setText(String.valueOf(reward.getPointsCost()));
            }
            if (categoryText != null) {
                categoryText.setText(reward.getCategory());
            }

            // Set up the Claim Reward button
            if (redeemButton != null) {
                redeemButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRewardClick(reward);
                    }
                });
            }

            // Set up long press for delete (remove the card click for claiming)
            if (cardView != null) {
                cardView.setOnLongClickListener(v -> {
                    if (listener != null) {
                        showDeleteConfirmation(reward);
                    }
                    return true;
                });

                // Remove the single click listener that was making the whole card clickable
                cardView.setOnClickListener(null);
            }
        }

        // Remove the showRewardOptionsDialog method since we don't need edit options
        private void showDeleteConfirmation(CustomReward reward) {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Delete Reward")
                .setMessage("Are you sure you want to delete '" + reward.getName() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (listener != null) {
                        listener.onDeleteReward(reward);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        }
    }
}
