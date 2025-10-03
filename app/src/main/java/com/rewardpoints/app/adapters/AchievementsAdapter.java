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
import com.rewardpoints.app.models.Achievement;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private List<Achievement> achievements;
    private Context context;
    private OnAchievementClickListener listener;

    // Interface for handling achievement clicks
    public interface OnAchievementClickListener {
        void onAchievementClick(Achievement achievement);
        void onDeleteAchievement(Achievement achievement);
    }

    public AchievementsAdapter(Context context) {
        this.context = context;
        this.achievements = new ArrayList<>();
    }

    public void setOnAchievementClickListener(OnAchievementClickListener listener) {
        this.listener = listener;
    }

    public void updateAchievements(List<Achievement> newAchievements) {
        this.achievements.clear();
        this.achievements.addAll(newAchievements);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public class AchievementViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText, descriptionText, bonusPointsText, progressText;
        private ImageView achievementIcon;
        private MaterialCardView cardView;
        private LinearProgressIndicator progressBar;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.achievement_name);
            descriptionText = itemView.findViewById(R.id.achievement_description);
            bonusPointsText = itemView.findViewById(R.id.bonus_points_text);
            progressText = itemView.findViewById(R.id.progress_text);
            achievementIcon = itemView.findViewById(R.id.achievement_icon);
            cardView = itemView.findViewById(R.id.achievement_card);
            progressBar = itemView.findViewById(R.id.achievement_progress_bar);

            // Handle click events
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onAchievementClick(achievements.get(position));
                    }
                }
            });

            // Handle long-press for delete (safer than looking for non-existent delete button)
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteAchievement(achievements.get(position));
                        return true;
                    }
                    return false;
                }
            });
        }

        public void bind(Achievement achievement) {
            if (achievement == null) return;

            // Set basic information
            if (nameText != null) {
                nameText.setText(achievement.getName());
            }
            if (descriptionText != null) {
                descriptionText.setText(achievement.getDescription());
            }
            if (bonusPointsText != null) {
                bonusPointsText.setText("+" + achievement.getBonusPoints() + " pts");
            }

            // Set achievement state
            boolean isUnlocked = achievement.isUnlocked();

            if (achievementIcon != null) {
                if (isUnlocked) {
                    achievementIcon.setImageResource(android.R.drawable.star_big_on);
                    achievementIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_light));
                } else {
                    achievementIcon.setImageResource(android.R.drawable.star_big_off);
                    achievementIcon.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
                }
            }

            // Set card appearance based on unlock status
            if (cardView != null) {
                if (isUnlocked) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                    cardView.setAlpha(1.0f);
                } else {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.background_light));
                    cardView.setAlpha(0.7f);
                }
            }

            // Set text colors based on unlock status
            int textColor = isUnlocked ?
                context.getResources().getColor(android.R.color.black) :
                context.getResources().getColor(android.R.color.darker_gray);

            if (nameText != null) {
                nameText.setTextColor(textColor);
            }
            if (descriptionText != null) {
                descriptionText.setTextColor(textColor);
            }

            // Set progress (for now, simple unlocked/locked state)
            if (progressBar != null && progressText != null) {
                if (isUnlocked) {
                    progressBar.setProgress(100);
                    progressText.setText("Completed!");
                    progressText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    progressBar.setProgress(0);
                    progressText.setText("In Progress");
                    progressText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                }
            }
        }
    }
}
