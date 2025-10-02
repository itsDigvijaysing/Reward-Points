package com.rewardpoints.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private List<CustomReward> rewards;
    private Context context;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(CustomReward reward);
        void onEditReward(CustomReward reward);
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

    class RewardViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView nameText, descriptionText, pointsText, categoryText;
        private MaterialButton claimButton, editButton;
        private ImageView categoryIcon;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.reward_card);
            nameText = itemView.findViewById(R.id.reward_name);
            descriptionText = itemView.findViewById(R.id.reward_description);
            pointsText = itemView.findViewById(R.id.reward_points);
            categoryText = itemView.findViewById(R.id.reward_category);
            claimButton = itemView.findViewById(R.id.claim_button);
            editButton = itemView.findViewById(R.id.edit_button);
            categoryIcon = itemView.findViewById(R.id.category_icon);
        }

        public void bind(CustomReward reward) {
            nameText.setText(reward.getName());
            descriptionText.setText(reward.getDescription());
            pointsText.setText(String.valueOf(reward.getPointsCost()));
            categoryText.setText(reward.getCategory());

            // Set category icon and color
            setCategoryIcon(reward.getCategory());

            claimButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRewardClick(reward);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditReward(reward);
                }
            });

            // Show usage statistics if available
            if (reward.getTimesUsed() > 0) {
                descriptionText.append("\n• Used " + reward.getTimesUsed() + " times");
            }
        }

        private void setCategoryIcon(String category) {
            int iconRes = R.drawable.ic_category_default;
            int colorRes = R.color.primary;

            switch (category) {
                case "Entertainment":
                    iconRes = R.drawable.ic_entertainment;
                    colorRes = R.color.entertainment;
                    break;
                case "Health & Fitness":
                    iconRes = R.drawable.ic_health;
                    colorRes = R.color.health;
                    break;
                case "Shopping":
                    iconRes = R.drawable.ic_shopping;
                    colorRes = R.color.shopping;
                    break;
                case "Travel":
                    iconRes = R.drawable.ic_travel;
                    colorRes = R.color.travel;
                    break;
                case "Food & Dining":
                    iconRes = R.drawable.ic_food;
                    colorRes = R.color.food;
                    break;
                case "Education":
                    iconRes = R.drawable.ic_education;
                    colorRes = R.color.education;
                    break;
            }

            categoryIcon.setImageResource(iconRes);
            categoryIcon.setColorFilter(context.getColor(colorRes));
        }
    }
}
