package com.rewardpoints.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.rewardpoints.app.R;
import com.rewardpoints.app.models.DailyMission;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class MissionsAdapter extends RecyclerView.Adapter<MissionsAdapter.MissionViewHolder> {

    private Context context;
    private List<DailyMission> missions;
    private OnMissionClickListener listener;

    public interface OnMissionClickListener {
        void onMissionClick(DailyMission mission);
        void onDeleteMission(DailyMission mission);
    }

    public MissionsAdapter(Context context) {
        this.context = context;
        this.missions = new ArrayList<>();
    }

    public void setOnMissionClickListener(OnMissionClickListener listener) {
        this.listener = listener;
    }

    public void updateMissions(List<DailyMission> newMissions) {
        this.missions.clear();
        this.missions.addAll(newMissions);
        notifyDataSetChanged();
    }

    public List<DailyMission> getMissions() {
        return missions;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        DailyMission mission = missions.get(position);
        holder.bind(mission);
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    public class MissionViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView titleText, descriptionText, pointsText, statusText;
        private ImageView missionIcon, statusIcon;

        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Safe findViewById - handle missing IDs gracefully
            cardView = itemView.findViewById(R.id.mission_card);
            titleText = itemView.findViewById(R.id.mission_title);
            descriptionText = itemView.findViewById(R.id.mission_description);
            pointsText = itemView.findViewById(R.id.mission_points);
            // Set these to null for missing IDs - they'll be handled safely
            statusText = null; // itemView.findViewById(R.id.mission_status);
            missionIcon = null; // itemView.findViewById(R.id.mission_icon);
            statusIcon = null; // itemView.findViewById(R.id.status_icon);

            // Set up click listeners
            if (cardView != null) {
                // Regular click for completing mission
                cardView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onMissionClick(missions.get(position));
                    }
                });

                // Long press for deleting mission
                cardView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteMission(missions.get(position));
                        return true;
                    }
                    return false;
                });
            } else {
                // Fallback to itemView if cardView is null
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onMissionClick(missions.get(position));
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteMission(missions.get(position));
                        return true;
                    }
                    return false;
                });
            }
        }

        public void bind(DailyMission mission) {
            if (mission == null) return;

            // Set basic information
            if (titleText != null) {
                titleText.setText(mission.getTitle());
            }
            if (descriptionText != null) {
                descriptionText.setText(mission.getDescription());
            }
            if (pointsText != null) {
                pointsText.setText("+" + mission.getRewardPoints() + " points");
            }

            // Set mission status
            if (statusText != null) {
                if (mission.isCompleted()) {
                    statusText.setText("✅ Completed");
                    statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                } else {
                    statusText.setText("📋 Available");
                    statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_bright));
                }
            }

            // Set mission icon based on category
            if (missionIcon != null) {
                setMissionIcon(mission.getCategory());
            }

            // Set status icon
            if (statusIcon != null) {
                if (mission.isCompleted()) {
                    statusIcon.setImageResource(android.R.drawable.checkbox_on_background);
                } else {
                    statusIcon.setImageResource(android.R.drawable.checkbox_off_background);
                }
            }

            // Set card background based on completion status
            if (cardView != null) {
                if (mission.isCompleted()) {
                    // Medium gray background using app's color template
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.text_hint));
                    cardView.setAlpha(1.0f);
                } else {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                    cardView.setAlpha(1.0f);
                }
            }
        }

        private void setMissionIcon(String category) {
            if (missionIcon == null || category == null) return;

            int iconResource;
            switch (category.toLowerCase()) {
                case "checkin":
                    iconResource = android.R.drawable.ic_menu_today;
                    break;
                case "points":
                    iconResource = android.R.drawable.btn_star_big_on;
                    break;
                case "reward":
                    iconResource = android.R.drawable.ic_menu_gallery;
                    break;
                case "special":
                    iconResource = android.R.drawable.ic_menu_preferences;
                    break;
                default:
                    iconResource = android.R.drawable.ic_menu_agenda;
                    break;
            }

            try {
                missionIcon.setImageResource(iconResource);
            } catch (Exception e) {
                missionIcon.setImageResource(android.R.drawable.ic_menu_agenda);
            }
        }
    }
}
