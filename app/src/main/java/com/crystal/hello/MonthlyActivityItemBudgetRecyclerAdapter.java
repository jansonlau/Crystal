package com.crystal.hello;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MonthlyActivityItemBudgetRecyclerAdapter extends RecyclerView.Adapter<MonthlyActivityItemBudgetRecyclerAdapter.ViewHolder> {
    private final LayoutInflater layoutInflater;
    private List<String> budgetNameList;

    public MonthlyActivityItemBudgetRecyclerAdapter(FragmentActivity activity) {
        layoutInflater = LayoutInflater.from(activity);

        budgetNameList = new ArrayList<>();
        budgetNameList.add("Shopping");
        budgetNameList.add("Food & Drinks");
        budgetNameList.add("Travel");
        budgetNameList.add("Entertainment");
        budgetNameList.add("Health");
        budgetNameList.add("Services");
        budgetNameList.add("Transportation");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_budget, parent, false);
        return new MonthlyActivityItemBudgetRecyclerAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String budgetName = budgetNameList.get(position);
        holder.budgetNameTextView.setText(budgetName);

        if (position == getItemCount() - 1) { // Remove divider in last item of recycler view
            holder.budgetConstraintLayout.removeView(holder.budgetDividerView);
        }
    }

    @Override
    public int getItemCount() {
        return budgetNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout budgetConstraintLayout;
        final TextView budgetNameTextView;
        final View budgetDividerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetConstraintLayout  = itemView.findViewById(R.id.budgetConstraintLayout);
            budgetNameTextView      = itemView.findViewById(R.id.budgetNameTextView);
            budgetDividerView       = itemView.findViewById(R.id.budgetDividerView);
        }
    }
}
