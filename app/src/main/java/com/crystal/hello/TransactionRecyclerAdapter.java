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

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.ViewHolder> {
    private final List<DocumentSnapshot> transactionsList;
    private final LayoutInflater layoutInflater;
    private final FragmentActivity fragmentActivity;

    public TransactionRecyclerAdapter(FragmentActivity activity, List<DocumentSnapshot> list) {
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
        final Map<String, Object> transaction   = transactionsList.get(position).getData();
        final String transactionName            = String.valueOf(Objects.requireNonNull(transaction).get("name"));
        final double transactionAmount          = (double) transaction.get("amount");
        final List<String> categoriesList       = (List<String>) transaction.get("category");
        final boolean transactionPending        = (boolean) transaction.get("pending");
        String transactionDate                  = String.valueOf(transaction.get("date"));

        // Parse transaction fields
        transactionDate                         = parseTransactionDate(transactionDate);
        final String parsedTransactionAmount    = parseTransactionAmount(transactionAmount);
        final int drawableId                    = parseTransactionLogo(holder, Objects.requireNonNull(categoriesList));

        initializeTransactionItemDetailFragment(holder,
                transaction,
                drawableId,
                transactionName,
                transactionDate,
                parsedTransactionAmount);

        // Set parsed transaction fields to view
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
        final TextView transactionDateTextView;
        final TextView transactionAmountTextView;
        final View transactionDividerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionConstraintLayout = itemView.findViewById(R.id.transactionConstraintLayout);
            transactionLogoImageView    = itemView.findViewById(R.id.transactionLogoImageView);
            transactionNameTextView     = itemView.findViewById(R.id.transactionNameTextView);
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
            final Date date = dateFormat.parse(transactionDate);
            dateFormat = new SimpleDateFormat("M/d/yy", Locale.US);
            if (date != null) {
                transactionDate = dateFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return transactionDate;
    }

    // Categories from Plaid are stored in a list
    private int parseTransactionLogo(ViewHolder holder, List<String> categoriesList) {
        final String category = categoriesList.get(0);
        int drawableInt = R.drawable.services;

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
        return drawableInt;
    }

    private void initializeTransactionItemDetailFragment(ViewHolder holder,
                                                         Map<String, Object> transaction,
                                                         int drawableId,
                                                         String transactionName,
                                                         String transactionDate,
                                                         String transactionAmount) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Bundle bundle = new Bundle();
                bundle.putSerializable("TRANSACTION_ITEM_MAP", (Serializable) transaction);
                bundle.putInt(("TRANSACTION_ITEM_LOGO"), drawableId);
                bundle.putString("TRANSACTION_ITEM_NAME", transactionName);
                bundle.putString("TRANSACTION_ITEM_DATE", transactionDate);
                bundle.putString("TRANSACTION_ITEM_AMOUNT", transactionAmount);

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
}
