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
    private final Map<String, List<DocumentSnapshot>> oneMonthTransactionsByCategoryMap; // Key: Category, Value: Documents
    private final List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList; // Key: Category, Value: Total transaction amount

    public MonthlyActivityItemBudgetRecyclerAdapter(FragmentActivity activity
            , Map<String, List<DocumentSnapshot>> oneMonthTransactionsByCategoryMap
            , List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList) {

        layoutInflater = LayoutInflater.from(activity);
        this.oneMonthTransactionsByCategoryMap = oneMonthTransactionsByCategoryMap;
        this.sortedPositiveAmountByCategoryList = sortedPositiveAmountByCategoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_categories, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
        return sortedPositiveAmountByCategoryList.size();
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
}
