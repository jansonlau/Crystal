package com.crystal.hello.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class BudgetAmountsRecyclerAdapter extends RecyclerView.Adapter<BudgetAmountsRecyclerAdapter.ViewHolder> {
    private final Map<String, Object> categoryBudgetAmountMap;
    private final LayoutInflater layoutInflater;
    public ViewHolder holder;

    public BudgetAmountsRecyclerAdapter(final FragmentActivity activity, final Map<String, Object> map) {
        categoryBudgetAmountMap = map;
        layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public BudgetAmountsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = layoutInflater.inflate(R.layout.item_budget_amount, parent, false);
        holder = new ViewHolder(itemView);
        return holder;
//        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetAmountsRecyclerAdapter.ViewHolder holder, int position) {
        final String title;
        final long budgetAmount;
        final int logoDrawableInt;
        final int logoBackgroundDrawableInt;

        switch (position) {
            case 0:
                title = "Shopping";
                logoDrawableInt = R.drawable.shopping;
                logoBackgroundDrawableInt = R.drawable.shopping_background;
                budgetAmount = (long) categoryBudgetAmountMap.get("shopping");
                break;
            case 1:
                title = "Food & Drinks";
                logoDrawableInt = R.drawable.food;
                logoBackgroundDrawableInt = R.drawable.food_background;
                budgetAmount = (long) categoryBudgetAmountMap.get("foodDrinks");
                break;
            case 2:
                title = "Travel";
                logoDrawableInt = R.drawable.travel;
                logoBackgroundDrawableInt = R.drawable.travel_background;
                budgetAmount = (long) categoryBudgetAmountMap.get("travel");
                break;
            case 3:
                title = "Entertainment";
                logoDrawableInt = R.drawable.entertainment;
                logoBackgroundDrawableInt = R.drawable.entertainment_background;
                budgetAmount = (long) categoryBudgetAmountMap.get("entertainment");
                break;
            case 4:
                title = "Health";
                logoDrawableInt = R.drawable.health;
                logoBackgroundDrawableInt = R.drawable.health_background;
                budgetAmount = (long) categoryBudgetAmountMap.get("health");
                break;
            default:
                title = "Services";
                logoDrawableInt = R.drawable.services;
                logoBackgroundDrawableInt = R.drawable.services_background;
                budgetAmount = (long) categoryBudgetAmountMap.get("services");
        }

        holder.budgetAmountTitleTextView.setText(title);
        holder.budgetAmountEditText.setHint("$".concat(String.valueOf(budgetAmount)));
        holder.budgetAmountImageView.setBackgroundResource(logoBackgroundDrawableInt);
        holder.budgetAmountImageView.setImageResource(logoDrawableInt);
    }

    @Override
    public int getItemCount() {
        return categoryBudgetAmountMap.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView          budgetAmountImageView;
        public final TextView           budgetAmountTitleTextView;
        public final TextInputLayout    budgetAmountInputLayout;
        public final TextInputEditText  budgetAmountEditText;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            budgetAmountImageView       = itemView.findViewById(R.id.budgetAmountImageView);
            budgetAmountTitleTextView   = itemView.findViewById(R.id.budgetAmountTitleTextView);
            budgetAmountInputLayout     = itemView.findViewById(R.id.budgetAmountInputLayout);
            budgetAmountEditText        = itemView.findViewById(R.id.budgetAmountEditText);
        }
    }
}