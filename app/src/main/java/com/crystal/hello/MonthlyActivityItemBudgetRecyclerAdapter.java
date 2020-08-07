package com.crystal.hello;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MonthlyActivityItemBudgetRecyclerAdapter extends RecyclerView.Adapter<MonthlyActivityItemBudgetRecyclerAdapter.ViewHolder> {
    private final LayoutInflater layoutInflater;
    private final Map<String, List<DocumentSnapshot>> positiveAndNegativeAmountTransactionsByCategoryMap;
    private Map<String, List<DocumentSnapshot>> positiveAmountTransactionsByCategoryMap;
    private Map<String, Double> positiveAmountByCategoryMap;

    public MonthlyActivityItemBudgetRecyclerAdapter(FragmentActivity activity
            , Map<String, List<DocumentSnapshot>> positiveAndNegativeAmountTransactionsByCategoryMap) {
        layoutInflater = LayoutInflater.from(activity);

        this.positiveAndNegativeAmountTransactionsByCategoryMap = positiveAndNegativeAmountTransactionsByCategoryMap;
        positiveAmountTransactionsByCategoryMap = new HashMap<>();
        positiveAmountByCategoryMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_categories, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get only positive amount transactions
        for (Map.Entry<String, List<DocumentSnapshot>> entry : positiveAndNegativeAmountTransactionsByCategoryMap.entrySet()) {
            String category = entry.getKey();
            List<DocumentSnapshot> documents = entry.getValue();
            positiveAmountByCategoryMap.put(category, getTotalTransactionAmount(category, documents));
        }

        // Sort positive amounts then add to list to keep order
        List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList = positiveAmountByCategoryMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(new Comparator<Double>() {
                    @Override
                    public int compare(Double K, Double V) {
                        return V.compareTo(K);
                    }
                }))
                .collect(Collectors.toList());

        // Set view
        Map.Entry<String, Double> transaction = sortedPositiveAmountByCategoryList.get(position);
        holder.budgetNameTextView.setText(transaction.getKey());
        holder.budgetProgressBar.setProgress(transaction.getValue().intValue(), true);

        String amountString = String.format(Locale.US,"%.2f", transaction.getValue());
        amountString = "$" + amountString;
        holder.budgetAmountTextView.setText(amountString);

        if (position == getItemCount() - 1) { // Remove divider in last item of recycler view
            holder.budgetConstraintLayout.removeView(holder.budgetDividerView);
        }
    }

    @Override
    public int getItemCount() {
        return positiveAndNegativeAmountTransactionsByCategoryMap.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View              budgetDividerView;
        final TextView          budgetNameTextView;
        final TextView          budgetAmountTextView;
        final ProgressBar       budgetProgressBar;
        final ConstraintLayout  budgetConstraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetDividerView       = itemView.findViewById(R.id.budgetDividerView);
            budgetNameTextView      = itemView.findViewById(R.id.budgetNameTextView);
            budgetAmountTextView    = itemView.findViewById(R.id.budgetAmountTextView);
            budgetProgressBar       = itemView.findViewById(R.id.budgetProgressBar);
            budgetConstraintLayout  = itemView.findViewById(R.id.budgetConstraintLayout);
        }
    }

    private double getTotalTransactionAmount(String category, List<DocumentSnapshot> documents) {
        double total = 0;
        List<DocumentSnapshot> positiveAmountTransactionsList = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            double amount = (double) Objects.requireNonNull(document.getData()).get("amount");
            if (amount >= 0.0) {
                total += amount;
                positiveAmountTransactionsList.add(document);
            }
        }
        positiveAmountTransactionsByCategoryMap.put(category, positiveAmountTransactionsList);
        return total;
    }
}
