package com.rewardpoints.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rewardpoints.app.R;
import com.rewardpoints.app.models.PointsSource;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class PointsSourcesAdapter extends RecyclerView.Adapter<PointsSourcesAdapter.PointsSourceViewHolder> {

    private List<PointsSource> pointsSources;
    private Context context;
    private OnPointsSourceClickListener listener;

    public interface OnPointsSourceClickListener {
        void onPointsSourceClick(PointsSource pointsSource);
        void onEditPointsSource(PointsSource pointsSource);
        void onDeletePointsSource(PointsSource pointsSource);
    }

    public PointsSourcesAdapter(Context context) {
        this.context = context;
        this.pointsSources = new ArrayList<>();
    }

    public void setOnPointsSourceClickListener(OnPointsSourceClickListener listener) {
        this.listener = listener;
    }

    public void updatePointsSources(List<PointsSource> newPointsSources) {
        this.pointsSources.clear();
        if (newPointsSources != null) {
            this.pointsSources.addAll(newPointsSources);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PointsSourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new PointsSourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointsSourceViewHolder holder, int position) {
        PointsSource pointsSource = pointsSources.get(position);
        holder.bind(pointsSource);
    }

    @Override
    public int getItemCount() {
        return pointsSources.size();
    }

    public class PointsSourceViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText, descriptionText;

        public PointsSourceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(android.R.id.text1);
            descriptionText = itemView.findViewById(android.R.id.text2);
        }

        public void bind(PointsSource pointsSource) {
            if (pointsSource == null) return;

            // Set basic information
            if (nameText != null) {
                String nameWithPoints = pointsSource.getName() + " (+" + pointsSource.getPointsValue() + " pts)";
                nameText.setText(nameWithPoints);
            }
            if (descriptionText != null) {
                String categoryAndDesc = "[" + pointsSource.getCategory() + "] " + pointsSource.getDescription();
                descriptionText.setText(categoryAndDesc);
            }

            // Set up click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    showOptionsDialog(pointsSource);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    showOptionsDialog(pointsSource);
                }
                return true;
            });
        }

        private void showOptionsDialog(PointsSource pointsSource) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Points Source: " + pointsSource.getName())
                   .setItems(new String[]{"Use Points Source", "Edit", "Delete"}, (dialog, which) -> {
                       if (which == 0) {
                           // Use option
                           if (listener != null) {
                               listener.onPointsSourceClick(pointsSource);
                           }
                       } else if (which == 1) {
                           // Edit option
                           if (listener != null) {
                               listener.onEditPointsSource(pointsSource);
                           }
                       } else if (which == 2) {
                           // Delete option
                           showDeleteConfirmation(pointsSource);
                       }
                   })
                   .setNegativeButton("Cancel", null)
                   .show();
        }

        private void showDeleteConfirmation(PointsSource pointsSource) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Delete Points Source")
                   .setMessage("Are you sure you want to delete '" + pointsSource.getName() + "'?")
                   .setPositiveButton("Delete", (dialog, which) -> {
                       if (listener != null) {
                           listener.onDeletePointsSource(pointsSource);
                       }
                   })
                   .setNegativeButton("Cancel", null)
                   .show();
        }
    }
}
