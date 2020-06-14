package com.crystal.hello;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.ui.home.HomeFragment;
import com.plaid.client.response.TransactionsGetResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> {
    private final List<TransactionsGetResponse.Transaction> transactionList;
    private final LayoutInflater layoutInflater;

    public HomeRecyclerAdapter(Context context, List<TransactionsGetResponse.Transaction> list) {
        transactionList = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionsGetResponse.Transaction transaction = transactionList.get(position);
        String transactionName = transaction.getName();

        String transactionDate = transaction.getDate();
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

        String transactionLocation;
        String transactionAmount = String.format(Locale.US,"%.2f", transaction.getAmount());
        if (transaction.getAmount() >= 0.0) {
            transactionAmount = "$" + transactionAmount;
            transactionLocation = transaction.getLocation().getCity() + ", " + transaction.getLocation().getRegion();
        } else { // Negative transactions
            transactionAmount = new StringBuilder(transactionAmount).insert(1, "$").toString();
            if (transactionName.toLowerCase().contains("pymnt") || transactionName.toLowerCase().contains("payment")) {
                transactionLocation = "Payment";
            } else {
                transactionLocation = "Refund";
            }
        }

        holder.transactionNameTextView.setText(transactionName);
        holder.transactionLocationTextView.setText(transactionLocation);
        holder.transactionDateTextView.setText(transactionDate);
        holder.transactionAmountTextView.setText(transactionAmount);
        if (position == getItemCount() - 1) {
            holder.transactionConstraintLayout.removeView(holder.transactionDividerView);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
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
            transactionNameTextView = itemView.findViewById(R.id.textViewTransactionName);
            transactionLocationTextView = itemView.findViewById(R.id.textViewTransactionLocation);
            transactionDateTextView = itemView.findViewById(R.id.textViewTransactionDate);
            transactionAmountTextView = itemView.findViewById(R.id.textViewTransactionAmount);
            transactionDividerView = itemView.findViewById(R.id.viewTransactionDivider);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    transactionNameText.setText("Clicked! "+ transactionNameText.getText());
                    HomeFragment.sparkAdapter.initializeTransactionAmount();
                }
            });
        }
    }
}
