package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.crystal.hello.ViewHolder;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MonthlyActivityItemMerchantRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final FragmentActivity fragmentActivity;
    private final LayoutInflater layoutInflater;
    private final Map<String, List<DocumentSnapshot>> oneMonthMerchantTransactionsMap; // Key: Category, Value: Documents
    private final List<Map<String, Double>> oneMonthAmountByMerchantNameList; // Key: Category, Value: Total transaction amount
    private int logoBackgroundDrawableInt;

    public MonthlyActivityItemMerchantRecyclerAdapter(final FragmentActivity activity,
                                                      final Map<String, List<DocumentSnapshot>> oneMonthMerchantTransactionsMap,
                                                      final List<Map<String, Double>> oneMonthAmountByMerchantNameList) {
        fragmentActivity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.oneMonthMerchantTransactionsMap = oneMonthMerchantTransactionsMap;
        this.oneMonthAmountByMerchantNameList = oneMonthAmountByMerchantNameList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View itemView = layoutInflater.inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Map.Entry<String, Double> transaction = oneMonthAmountByMerchantNameList.get(position).entrySet().iterator().next();
        final String merchantName = transaction.getKey();
        final double amount = transaction.getValue();

        // Number of transactions string
        final int transactionCountInt = Objects.requireNonNull(oneMonthMerchantTransactionsMap.get(merchantName)).size();
        String transactionCountString = String.valueOf(transactionCountInt).concat(" Transaction");
        if (transactionCountInt > 1) {
            transactionCountString = transactionCountString.concat("s");
        }

        // Logo
        final List<String> categoriesList = (List<String>) Objects.requireNonNull(Objects.requireNonNull(oneMonthMerchantTransactionsMap.get(merchantName))).get(0).get("category");
        final String parsedTransactionAmountString = parseTransactionAmount(amount);
        final int drawableInt = parseTransactionLogo(Objects.requireNonNull(categoriesList));

        holder.transactionLogoImageView.setBackgroundResource(logoBackgroundDrawableInt);
        holder.transactionLogoImageView.setImageResource(drawableInt);
        holder.transactionTitleTextView.setText(merchantName);
        holder.transactionAmountTextView.setText(parsedTransactionAmountString);
        holder.transactionSubtitleTextView.setText(transactionCountString);
        initializeMonthlyActivityItemDetailFragment(holder, merchantName);
    }

    @Override
    public int getItemCount() {
        return Math.min(oneMonthMerchantTransactionsMap.size(), 5);
    }

    private void initializeMonthlyActivityItemDetailFragment(@NotNull final ViewHolder holder, final String merchantName) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment monthlyActivityItemDetailFragment = new MonthlyActivityItemDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putSerializable("com.crystal.hello.TRANSACTIONS_LIST", (Serializable) oneMonthMerchantTransactionsMap.get(merchantName));
                monthlyActivityItemDetailFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentFrameLayout, monthlyActivityItemDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @NotNull
    private String parseTransactionAmount(final double transactionAmount) {
        String amountString = String.format(Locale.US,"%.2f", transactionAmount);
        if (transactionAmount >= 0) {
            amountString = "$".concat(amountString);
        } else {
            amountString = new StringBuilder(amountString).insert(1, "$").toString();
        }
        return amountString;
    }

    // Categories from Plaid are stored in a list
    private int parseTransactionLogo(@NotNull final List<String> categoriesList) {
        final String category = categoriesList.get(0);
        int logoDrawableInt = R.drawable.services;

        switch (category) {
            case "Food and Drink":
                logoDrawableInt = R.drawable.food;
                logoBackgroundDrawableInt = R.drawable.food_background;
                break;
            case "Shops":
                logoDrawableInt = R.drawable.shopping;
                logoBackgroundDrawableInt = R.drawable.shopping_background;
                break;
            case "Travel":
                logoDrawableInt = R.drawable.travel;
                logoBackgroundDrawableInt = R.drawable.travel_background;
                break;
            case "Recreation":
                logoDrawableInt = R.drawable.entertainment;
                logoBackgroundDrawableInt = R.drawable.entertainment_background;
                break;
            case "Healthcare":
                logoDrawableInt = R.drawable.health;
                logoBackgroundDrawableInt = R.drawable.health_background;
                break;
            default:
                logoBackgroundDrawableInt = R.drawable.services_background;
        }
        return logoDrawableInt;
    }
}
