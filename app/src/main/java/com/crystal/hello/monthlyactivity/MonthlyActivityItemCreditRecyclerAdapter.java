package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.Objects;

public class MonthlyActivityItemCreditRecyclerAdapter extends RecyclerView.Adapter<MonthlyActivityItemCreditRecyclerAdapter.ViewHolder> {
    private final FragmentActivity fragmentActivity;
    private final LayoutInflater layoutInflater;
    private final Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap; // Key: Category, Value: Documents
    private final List<Map<String, Double>> oneMonthNegativeAmountByCategoryList; // Key: Category, Value: Total transaction amount

    public MonthlyActivityItemCreditRecyclerAdapter(FragmentActivity activity
            , Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap
            , List<Map<String, Double>> oneMonthNegativeAmountByCategoryList) {

        fragmentActivity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.oneMonthNegativeAmountTransactionsByCategoryMap = oneMonthNegativeAmountTransactionsByCategoryMap;
        this.oneMonthNegativeAmountByCategoryList = oneMonthNegativeAmountByCategoryList;
    }

    @NonNull
    @Override
    public MonthlyActivityItemCreditRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_credits, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyActivityItemCreditRecyclerAdapter.ViewHolder holder, int position) {
        Map<String, Double> transaction = oneMonthNegativeAmountByCategoryList.get(position);
        String category = "";
        double amount = 0;
        for (Map.Entry<String, Double> entry : transaction.entrySet()) {
            category = entry.getKey();
            amount = entry.getValue();
        }

        // Total amount string
        String amountString = String.format(Locale.US,"%.2f", amount);
        amountString = new StringBuilder(amountString).insert(1, "$").toString();

        holder.creditNameTextView.setText(category);
        holder.creditAmountTextView.setText(amountString);

        // Number of transactions string
        int transactionCountInt = Objects.requireNonNull(oneMonthNegativeAmountTransactionsByCategoryMap.get(category)).size();
        String transactionCountString = String.valueOf(transactionCountInt);
        if (category.equals("Payments")) {
//            holder.creditLogoImageView.setImageResource();
            if (transactionCountInt == 1) {
                transactionCountString += " Payment";
            } else {
                transactionCountString += " Payments";
            }
            holder.creditCountTextView.setText(transactionCountString);
        } else if (category.equals("Refunds")) {
//            holder.creditLogoImageView.setImageResource();
            if (transactionCountInt == 1) {
                transactionCountString += " Refund";
            } else {
                transactionCountString += " Refunds";
            }
        }
        holder.creditCountTextView.setText(transactionCountString);

        if (position == getItemCount() - 1) { // Remove divider in last item of recycler view
            holder.creditConstraintLayout.removeView(holder.creditDividerView);
        }

        initializeMonthlyActivityItemDetailFragment(holder, category);
    }

    @Override
    public int getItemCount() {
        return oneMonthNegativeAmountTransactionsByCategoryMap.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View              creditDividerView;
        final TextView          creditNameTextView;
        final TextView          creditAmountTextView;
        final TextView          creditCountTextView;
        final ImageView         creditLogoImageView;
        final ConstraintLayout  creditConstraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            creditDividerView       = itemView.findViewById(R.id.creditDividerView);
            creditNameTextView      = itemView.findViewById(R.id.creditNameTextView);
            creditAmountTextView    = itemView.findViewById(R.id.creditAmountTextView);
            creditCountTextView     = itemView.findViewById(R.id.creditCountTextView);
            creditLogoImageView     = itemView.findViewById(R.id.creditLogoImageView);
            creditConstraintLayout  = itemView.findViewById(R.id.creditConstraintLayout);
        }
    }

    private void initializeMonthlyActivityItemDetailFragment(ViewHolder holder, String category) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment monthlyActivityItemDetailFragment = new MonthlyActivityItemDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putSerializable("com.crystal.hello.TRANSACTIONS_LIST", (Serializable) oneMonthNegativeAmountTransactionsByCategoryMap.get(category));
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
