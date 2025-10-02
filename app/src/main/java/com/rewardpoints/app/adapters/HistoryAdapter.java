package com.rewardpoints.app.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rewardpoints.app.R;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.TransactionViewHolder> {

    private List<EnhancedTransaction> transactions;
    private Context context;

    public HistoryAdapter(Context context) {
        this.context = context;
        this.transactions = new ArrayList<>();
    }

    public void updateTransactions(List<EnhancedTransaction> newTransactions) {
        this.transactions.clear();
        this.transactions.addAll(newTransactions);
        notifyDataSetChanged();
    }

    // Add this method to handle string-based history data
    public void updateHistory(List<String> historyStrings) {
        this.transactions.clear();

        // Convert string history to EnhancedTransaction objects
        for (String historyItem : historyStrings) {
            try {
                // Parse the string format: "Date: Action: Points"
                // Example: "2024-01-01 10:30: Added 50 points for Task completion"
                String[] parts = historyItem.split(":", 3);
                if (parts.length >= 2) {
                    String dateStr = parts[0].trim();
                    String action = parts.length > 2 ? parts[2].trim() : parts[1].trim();

                    // Extract points from action string
                    int points = extractPointsFromString(action);
                    boolean isEarned = action.toLowerCase().contains("added") ||
                                     action.toLowerCase().contains("earned") ||
                                     action.toLowerCase().contains("gained");

                    // Create transaction object with correct constructor: (type, source, description, points, category)
                    EnhancedTransaction transaction = new EnhancedTransaction(
                        isEarned ? "EARN" : "SPEND",
                        "History Import", // source
                        action, // description
                        Math.abs(points), // points
                        "general" // category
                    );

                    // Set the timestamp if we can parse the date
                    transaction.setTimestamp(parseTimestamp(dateStr));

                    this.transactions.add(transaction);
                }
            } catch (Exception e) {
                // If parsing fails, create a simple transaction with the raw string
                EnhancedTransaction transaction = new EnhancedTransaction(
                    "INFO",
                    "History Import", // source
                    historyItem, // description
                    0, // points
                    "general" // category
                );
                this.transactions.add(transaction);
            }
        }
        notifyDataSetChanged();
    }

    private long parseTimestamp(String dateStr) {
        try {
            // Try to parse common date formats, fallback to current time
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = formatter.parse(dateStr);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private int extractPointsFromString(String text) {
        try {
            // Look for numbers in the string
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.matches("\\d+")) {
                    return Integer.parseInt(word);
                }
            }
        } catch (Exception e) {
            // Return 0 if no points found
        }
        return 0;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        try {
            if (transactions != null && position >= 0 && position < transactions.size()) {
                EnhancedTransaction transaction = transactions.get(position);
                holder.bind(transaction);
            }
        } catch (Exception e) {
            // Prevent crashes from binding errors
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView descriptionText, pointsText, dateText, categoryText;
        private ImageView typeIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                cardView = itemView.findViewById(R.id.transaction_card);
                descriptionText = itemView.findViewById(R.id.transaction_description);
                pointsText = itemView.findViewById(R.id.transaction_points);
                dateText = itemView.findViewById(R.id.transaction_date);
                categoryText = itemView.findViewById(R.id.transaction_category);
                typeIcon = itemView.findViewById(R.id.type_icon);
            } catch (Exception e) {
                // Handle case where views might not be found
                e.printStackTrace();
            }
        }

        public void bind(EnhancedTransaction transaction) {
            if (transaction == null) return;

            try {
                // Set description with null check
                if (descriptionText != null) {
                    String description = transaction.getDescription();
                    descriptionText.setText(description != null ? description : "Transaction");
                }

                // Set category with null check and proper formatting
                if (categoryText != null) {
                    String category = transaction.getCategory();
                    categoryText.setText(category != null ? category.toUpperCase() : "GENERAL");
                }

                // Format points with sign and null check
                if (pointsText != null) {
                    String pointsText = (transaction.isEarning() ? "+" : "-") + transaction.getPoints();
                    this.pointsText.setText(pointsText);

                    // Set color based on transaction type - use ContextCompat for API compatibility
                    int pointsColor = transaction.isEarning()
                        ? androidx.core.content.ContextCompat.getColor(context, R.color.success)
                        : androidx.core.content.ContextCompat.getColor(context, R.color.error);
                    this.pointsText.setTextColor(pointsColor);
                }

                // Format date with null check
                if (dateText != null) {
                    try {
                        String dateStr = DateUtils.getRelativeTimeSpanString(
                            transaction.getTimestamp(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString();
                        dateText.setText(dateStr);
                    } catch (Exception e) {
                        dateText.setText("Recently");
                    }
                }

                // Set icon based on type with proper sizing and null check
                if (typeIcon != null) {
                    try {
                        int iconRes = transaction.isEarning() ? R.drawable.ic_add : R.drawable.ic_remove;
                        typeIcon.setImageResource(iconRes);

                        // Ensure proper image scaling
                        typeIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                        int pointsColor = transaction.isEarning()
                            ? androidx.core.content.ContextCompat.getColor(context, R.color.success)
                            : androidx.core.content.ContextCompat.getColor(context, R.color.error);
                        typeIcon.setColorFilter(pointsColor);

                        // Set proper image bounds to prevent stretching
                        typeIcon.setAdjustViewBounds(true);

                    } catch (Exception e) {
                        // Set default icon if there's an error
                        typeIcon.setImageResource(R.drawable.ic_category_default);
                        typeIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                }

                // Update the parent container background based on transaction type
                if (cardView != null) {
                    // Add subtle visual feedback
                    cardView.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(context, R.color.surface)
                    );
                }

            } catch (Exception e) {
                // Catch any other binding errors
                e.printStackTrace();
            }
        }
    }
}
