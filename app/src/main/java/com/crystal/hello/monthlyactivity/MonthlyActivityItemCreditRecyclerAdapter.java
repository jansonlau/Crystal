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

import org.jetbrains.annotations.NotNull;

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

    public MonthlyActivityItemCreditRecyclerAdapter(final FragmentActivity activity,
                                                    final Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap,
                                                    final List<Map<String, Double>> oneMonthNegativeAmountByCategoryList) {
        fragmentActivity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.oneMonthNegativeAmountTransactionsByCategoryMap = oneMonthNegativeAmountTransactionsByCategoryMap;
        this.oneMonthNegativeAmountByCategoryList = oneMonthNegativeAmountByCategoryList;
    }

    @NonNull
    @Override
    public MonthlyActivityItemCreditRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View itemView = layoutInflater.inflate(R.layout.item_monthly_activity_credits, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MonthlyActivityItemCreditRecyclerAdapter.ViewHolder holder, final int position) {
        final Map.Entry<String, Double> transaction = oneMonthNegativeAmountByCategoryList.get(position).entrySet().iterator().next();
        final String category = transaction.getKey();
        final double amount = transaction.getValue();

        // Total amount string
        String amountString = String.format(Locale.US,"%.2f", amount);
        amountString = new StringBuilder(amountString).insert(1, "$").toString();

        // Number of transactions string
        int transactionCountInt = Objects.requireNonNull(oneMonthNegativeAmountTransactionsByCategoryMap.get(category)).size();
        String transactionCountString = String.valueOf(transactionCountInt).concat(" ").concat(category);
        if (transactionCountInt > 1) {
            transactionCountString = transactionCountString.concat("s");
        }

        if (category.equals("Payment")) {
            holder.creditLogoImageView.setImageResource(R.drawable.payments);
            holder.creditLogoImageView.setBackgroundResource(R.drawable.payments_background);
        } else if (category.equals("Refund")) {
            holder.creditLogoImageView.setImageResource(R.drawable.refunds);
            holder.creditLogoImageView.setBackgroundResource(R.drawable.refunds_background);
        }

        holder.creditNameTextView.setText(category.concat("s"));
        holder.creditAmountTextView.setText(amountString);
        holder.creditCountTextView.setText(transactionCountString);
        initializeMonthlyActivityItemDetailFragment(holder, category);

        if (position == getItemCount() - 1) { // Remove divider in last item of recycler view
            holder.creditConstraintLayout.removeView(holder.creditDividerView);
        }
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

    private void initializeMonthlyActivityItemDetailFragment(@NotNull final ViewHolder holder, final String category) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment monthlyActivityItemDetailFragment = new MonthlyActivityItemDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putSerializable("com.crystal.hello.TRANSACTIONS_LIST", (Serializable) oneMonthNegativeAmountTransactionsByCategoryMap.get(category));
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
