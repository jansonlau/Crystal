package com.crystal.hello;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        Map.Entry<String, Double> transaction = sortedPositiveAmountByCategoryList.get(position);
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
        final ImageView         budgetLogoImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetDividerView       = itemView.findViewById(R.id.budgetDividerView);
            budgetNameTextView      = itemView.findViewById(R.id.budgetNameTextView);
            budgetAmountTextView    = itemView.findViewById(R.id.budgetAmountTextView);
            budgetProgressBar       = itemView.findViewById(R.id.budgetProgressBar);
            budgetConstraintLayout  = itemView.findViewById(R.id.budgetConstraintLayout);
            budgetLogoImageView     = itemView.findViewById(R.id.budgetLogoImageView);
        }
    }
}
