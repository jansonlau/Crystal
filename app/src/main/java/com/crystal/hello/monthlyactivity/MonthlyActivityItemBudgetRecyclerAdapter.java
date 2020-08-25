package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyActivityItemBudgetRecyclerAdapter extends RecyclerView.Adapter<MonthlyActivityItemBudgetRecyclerAdapter.ViewHolder> {
    private final FragmentActivity fragmentActivity;
    private final LayoutInflater layoutInflater;
    private final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap; // Key: Category, Value: Documents
    private final List<Map.Entry<String, Double>> oneMonthSortedPositiveAmountByCategoryList; // Key: Category, Value: Total transaction amount

    public MonthlyActivityItemBudgetRecyclerAdapter(FragmentActivity activity
            , Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap
            , List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList) {

        fragmentActivity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.oneMonthPositiveAmountTransactionsByCategoryMap = oneMonthPositiveAmountTransactionsByCategoryMap;
        this.oneMonthSortedPositiveAmountByCategoryList = sortedPositiveAmountByCategoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_categories, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> transaction = oneMonthSortedPositiveAmountByCategoryList.get(position);
        String category = transaction.getKey();

        String amountString = String.format(Locale.US,"%.2f", transaction.getValue());
        amountString = "$" + amountString;

        holder.budgetNameTextView.setText(category);
        holder.budgetProgressBar.setProgress(transaction.getValue().intValue());
        holder.budgetAmountTextView.setText(amountString);

        switch (category) {
            case "Food & Drinks":
                holder.budgetLogoImageView.setImageResource(R.drawable.food);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.food_background);
                break;
            case "Shopping":
                holder.budgetLogoImageView.setImageResource(R.drawable.shopping);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.shopping_background);
                break;
            case "Travel":
                holder.budgetLogoImageView.setImageResource(R.drawable.travel);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.travel_background);
                break;
            case "Entertainment":
                holder.budgetLogoImageView.setImageResource(R.drawable.entertainment);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.entertainment_background);
                break;
            case "Health":
                holder.budgetLogoImageView.setImageResource(R.drawable.health);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.health_background);
                break;
            default:
                holder.budgetLogoImageView.setImageResource(R.drawable.services);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.services_background);
        }

        if (position == getItemCount() - 1) { // Remove divider in last item of recycler view
            holder.budgetConstraintLayout.removeView(holder.budgetDividerView);
        }

        initializeMonthlyActivityItemDetailFragment(holder, category);
    }

    @Override
    public int getItemCount() {
        return oneMonthSortedPositiveAmountByCategoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View              budgetDividerView;
        final TextView          budgetNameTextView;
        final TextView          budgetAmountTextView;
        final ImageView         budgetLogoImageView;
        final ProgressBar       budgetProgressBar;
        final ConstraintLayout  budgetConstraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetDividerView       = itemView.findViewById(R.id.budgetDividerView);
            budgetNameTextView      = itemView.findViewById(R.id.budgetNameTextView);
            budgetAmountTextView    = itemView.findViewById(R.id.budgetAmountTextView);
            budgetLogoImageView     = itemView.findViewById(R.id.budgetLogoImageView);
            budgetProgressBar       = itemView.findViewById(R.id.budgetProgressBar);
            budgetConstraintLayout  = itemView.findViewById(R.id.budgetConstraintLayout);
        }
    }

    private void initializeMonthlyActivityItemDetailFragment(ViewHolder holder, String category) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment monthlyActivityItemDetailFragment = new MonthlyActivityItemDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putSerializable("com.crystal.hello.TRANSACTIONS_LIST", (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap.get(category));
                monthlyActivityItemDetailFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutFragmentContainer, monthlyActivityItemDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}
