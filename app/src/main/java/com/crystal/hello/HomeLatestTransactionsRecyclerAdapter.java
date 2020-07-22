package com.crystal.hello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.plaid.client.response.TransactionsGetResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
        Map<String, Object> transaction = transactionsList.get(position);
        String transactionName = (String) transaction.get("name");

        // Set date
        String transactionDate = (String) transaction.get("date");
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

        // Set location and amount
        Map<String, Object> location = (HashMap<String, Object>) transaction.get("location");
        String transactionLocation = "";
        String transactionAmount = String.format(Locale.US,"%.2f", (double) transaction.get("amount"));

        if ((double) transaction.get("amount") >= 0.00) {
            transactionAmount = "$" + transactionAmount;

            if ((boolean) transaction.get("pending")) {
                transactionLocation = "Pending - ";
            }

            if ((location != null && location.get("city") != null && location.get("region") != null)) {
                transactionLocation += location.get("city") + ", " + location.get("region");
            } else {
                switch ((String) transaction.get("paymentChannel")) {
                    case "online":
                        transactionLocation += "Online";
                        break;
                    case "in store":
                        transactionLocation += "In-store";
                        break;
                    case "other":
                        transactionLocation += "Other Channel";
                        break;
                }
            }
        } else { // Negative transactions
            transactionAmount = new StringBuilder(transactionAmount).insert(1, "$").toString();
            if (transactionName.toLowerCase().contains("pymnt")
                    || transactionName.toLowerCase().contains("payment")
                    || transactionName.toLowerCase().contains("pay")) {
                transactionLocation = "Payment";
            } else {
                transactionLocation = "Refund";
            }
        }

        holder.transactionNameTextView.setText(transactionName);
        holder.transactionLocationTextView.setText(transactionLocation);
        holder.transactionDateTextView.setText(transactionDate);
        holder.transactionAmountTextView.setText(transactionAmount);
        if (position == getItemCount() - 1) { // Remove divider in last item of recycler view
            holder.transactionConstraintLayout.removeView(holder.transactionDividerView);
        }

        // Initialize TransactionItemDetailFragment
        Bundle bundle = new Bundle();
        bundle.putInt("TRANSACTION_ITEM_POSITION", position);
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

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout transactionConstraintLayout;
        final TextView transactionNameTextView;
        final TextView transactionLocationTextView;
        final TextView transactionDateTextView;
        final TextView transactionAmountTextView;
        final View transactionDividerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionConstraintLayout = itemView.findViewById(R.id.constraintLayoutTransactionItem);
            transactionNameTextView     = itemView.findViewById(R.id.textViewTransactionName);
            transactionLocationTextView = itemView.findViewById(R.id.textViewTransactionLocation);
            transactionDateTextView     = itemView.findViewById(R.id.textViewTransactionDate);
            transactionAmountTextView   = itemView.findViewById(R.id.textViewTransactionAmount);
            transactionDividerView      = itemView.findViewById(R.id.viewTransactionDivider);
        }
    }
}