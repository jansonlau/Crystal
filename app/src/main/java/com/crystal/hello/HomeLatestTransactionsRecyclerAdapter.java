package com.crystal.hello;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeLatestTransactionsRecyclerAdapter extends RecyclerView.Adapter<HomeLatestTransactionsRecyclerAdapter.ViewHolder> {
    private final List<Map<String, Object>> transactionsList;
    private final LayoutInflater layoutInflater;
    private final FragmentActivity fragmentActivity;

    public HomeLatestTransactionsRecyclerAdapter(FragmentActivity activity, List<Map<String, Object>> list) {
        transactionsList = list;
        layoutInflater = LayoutInflater.from(activity);
        fragmentActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get transaction fields
        Map<String, Object> transaction = transactionsList.get(position);
        String transactionName      = String.valueOf(transaction.get("name"));
        String transactionDate      = String.valueOf(transaction.get("date"));
        double transactionAmount    = (double) transaction.get("amount");
        List<String> categoriesList = (List<String>) transaction.get("category");
        boolean transactionPending  = (boolean) transaction.get("pending");

        // Parse transaction fields
        transactionDate = parseTransactionDate(transactionDate);
        String parsedTransactionAmount = parseTransactionAmount(transactionAmount);
        int drawableId = parseTransactionLogo(holder, categoriesList);
        transactionName = parsePopularNames(transactionName);
        initializeTransactionItemDetailFragment(holder,
                position,
                drawableId,
                transactionName,
                transactionDate,
                parsedTransactionAmount);

        // Set parsed transaction fields to view
//        holder.transactionLocationTextView.setText(transactionLocation);
        if (transactionPending) {
            transactionDate += " - Pending";
        }
        // Remove divider in last item of recycler view
        if (position == getItemCount() - 1) {
            holder.transactionConstraintLayout.removeView(holder.transactionDividerView);
        }
        holder.transactionLogoImageView.setImageResource(drawableId);
        holder.transactionNameTextView.setText(transactionName);
        holder.transactionDateTextView.setText(transactionDate);
        holder.transactionAmountTextView.setText(parsedTransactionAmount);
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout transactionConstraintLayout;
        final ImageView transactionLogoImageView;
        final TextView transactionNameTextView;
//        final TextView transactionLocationTextView;
        final TextView transactionDateTextView;
        final TextView transactionAmountTextView;
        final View transactionDividerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionConstraintLayout = itemView.findViewById(R.id.transactionConstraintLayout);
            transactionLogoImageView    = itemView.findViewById(R.id.transactionLogoImageView);
            transactionNameTextView     = itemView.findViewById(R.id.transactionNameTextView);
//            transactionLocationTextView = itemView.findViewById(R.id.transactionLocationTextView);
            transactionDateTextView     = itemView.findViewById(R.id.transactionDateTextView);
            transactionAmountTextView   = itemView.findViewById(R.id.transactionAmountTextView);
            transactionDividerView      = itemView.findViewById(R.id.transactionDividerView);
        }
    }

    private String parseTransactionAmount(double transactionAmount) {
        String amountString = String.format(Locale.US,"%.2f", transactionAmount);
        if (transactionAmount >= 0.0) {
            amountString = "$" + amountString;
        } else {
            amountString = new StringBuilder(amountString).insert(1, "$").toString();
        }
        return amountString;
    }

    private String parseTransactionDate(String transactionDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = dateFormat.parse(transactionDate);
            dateFormat = new SimpleDateFormat("M/d/yy", Locale.US);
            if (date != null) {
                transactionDate = dateFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return transactionDate;
    }

    private int parseTransactionLogo(ViewHolder holder, List<String> categoriesList) {
        String category = "";
        if (categoriesList != null) {
            category = categoriesList.get(0);
        }

        int drawableInt = R.drawable.services;
        if (categoriesList != null) {
            switch (category) {
                case "Food and Drink":
                    drawableInt = R.drawable.food;
                    holder.transactionLogoImageView.setBackgroundResource(R.drawable.food_background);
                    break;
                case "Shops":
                    drawableInt = R.drawable.shopping;
                    holder.transactionLogoImageView.setBackgroundResource(R.drawable.shopping_background);
                    break;
                case "Travel":
                    drawableInt = R.drawable.travel;
                    holder.transactionLogoImageView.setBackgroundResource(R.drawable.travel_background);
                    break;
                case "Recreation":
                    drawableInt = R.drawable.entertainment;
                    holder.transactionLogoImageView.setBackgroundResource(R.drawable.entertainment_background);
                    break;
                case "Healthcare":
                    drawableInt = R.drawable.health;
                    holder.transactionLogoImageView.setBackgroundResource(R.drawable.health_background);
                    break;
                default:
                    holder.transactionLogoImageView.setBackgroundResource(R.drawable.services_background);
            }
        }
        return drawableInt;
    }

    private void initializeTransactionItemDetailFragment(ViewHolder holder,
                                                         int position,
                                                         int drawableId,
                                                         String transactionName,
                                                         String transactionDate,
                                                         String transactionAmount) {
        final Bundle bundle = new Bundle();
        bundle.putInt("TRANSACTION_ITEM_POSITION", position);
        bundle.putInt(("TRANSACTION_ITEM_LOGO"), drawableId);
        bundle.putString("TRANSACTION_ITEM_NAME", transactionName);
        bundle.putString("TRANSACTION_ITEM_DATE", transactionDate);
        bundle.putString("TRANSACTION_ITEM_AMOUNT", transactionAmount);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment transactionItemDetailFragment = new TransactionItemDetailFragment();
                transactionItemDetailFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutFragmentContainer, transactionItemDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private String parsePopularNames(String transactionName) {
        if (transactionName.toLowerCase().contains("amazon.com")) {
            transactionName = "Amazon.com";
        }
        return transactionName;
    }
}
