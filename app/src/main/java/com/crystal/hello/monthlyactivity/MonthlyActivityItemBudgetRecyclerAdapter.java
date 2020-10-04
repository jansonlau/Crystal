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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyActivityItemBudgetRecyclerAdapter extends RecyclerView.Adapter<MonthlyActivityItemBudgetRecyclerAdapter.ViewHolder> {
    private final FragmentActivity fragmentActivity;
    private final LayoutInflater layoutInflater;
    private final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap; // Key: Category, Value: Documents
    private final List<Map<String, Double>> oneMonthSortedPositiveAmountByCategoryList; // Key: Category, Value: Total transaction amount
    private final Map<String, Object> budgetsMap;

    public MonthlyActivityItemBudgetRecyclerAdapter(final FragmentActivity activity,
                                                    final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap,
                                                    final List<Map<String, Double>> sortedPositiveAmountByCategoryList,
                                                    final Map<String, Object> budgetsMap) {

        fragmentActivity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.oneMonthPositiveAmountTransactionsByCategoryMap = oneMonthPositiveAmountTransactionsByCategoryMap;
        this.oneMonthSortedPositiveAmountByCategoryList = sortedPositiveAmountByCategoryList;
        this.budgetsMap = budgetsMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_categories, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Map.Entry<String, Double> categoryAndAmountMap = oneMonthSortedPositiveAmountByCategoryList.get(position)
                .entrySet()
                .iterator()
                .next();

        final String category = categoryAndAmountMap.getKey();
        final Double amount = categoryAndAmountMap.getValue();
        final String amountString = "$".concat(String.format(Locale.US,"%.2f", amount));
        final long budgetInt;

        switch (category) {
            case "Food & Drinks":
                holder.budgetLogoImageView.setImageResource(R.drawable.food);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.food_background);
                budgetInt = (long) budgetsMap.get("foodDrinks");
                break;
            case "Shopping":
                holder.budgetLogoImageView.setImageResource(R.drawable.shopping);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.shopping_background);
                budgetInt = (long) budgetsMap.get("shopping");
                break;
            case "Travel":
                holder.budgetLogoImageView.setImageResource(R.drawable.travel);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.travel_background);
                budgetInt = (long) budgetsMap.get("travel");
                break;
            case "Entertainment":
                holder.budgetLogoImageView.setImageResource(R.drawable.entertainment);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.entertainment_background);
                budgetInt = (long) budgetsMap.get("entertainment");
                break;
            case "Health":
                holder.budgetLogoImageView.setImageResource(R.drawable.health);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.health_background);
                budgetInt = (long) budgetsMap.get("health");
                break;
            default:
                holder.budgetLogoImageView.setImageResource(R.drawable.services);
                holder.budgetLogoImageView.setBackgroundResource(R.drawable.services_background);
                budgetInt = (long) budgetsMap.get("services");
        }

        holder.budgetNameTextView.setText(category);
        holder.budgetAmountTextView.setText(amountString);
        holder.budgetSubtitleTextView.setText("of $".concat(String.valueOf(budgetInt)));
        holder.budgetProgressBar.setMax((int) budgetInt);
        holder.budgetProgressBar.setProgress(amount.intValue());

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
        final TextView          budgetSubtitleTextView;
        final ImageView         budgetLogoImageView;
        final ProgressBar       budgetProgressBar;
        final ConstraintLayout  budgetConstraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetDividerView       = itemView.findViewById(R.id.budgetDividerView);
            budgetNameTextView      = itemView.findViewById(R.id.budgetNameTextView);
            budgetAmountTextView    = itemView.findViewById(R.id.budgetAmountTextView);
            budgetSubtitleTextView  = itemView.findViewById(R.id.budgetSubtitleTextView);
            budgetLogoImageView     = itemView.findViewById(R.id.budgetLogoImageView);
            budgetProgressBar       = itemView.findViewById(R.id.budgetProgressBar);
            budgetConstraintLayout  = itemView.findViewById(R.id.budgetConstraintLayout);
        }
    }

    private void initializeMonthlyActivityItemDetailFragment(@NotNull final ViewHolder holder, final String category) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment monthlyActivityItemDetailFragment = new MonthlyActivityItemDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putSerializable("com.crystal.hello.TRANSACTIONS_LIST", (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap.get(category));
                monthlyActivityItemDetailFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentFrameLayout, monthlyActivityItemDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}
