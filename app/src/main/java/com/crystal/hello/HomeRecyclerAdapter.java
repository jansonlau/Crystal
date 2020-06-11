package com.crystal.hello;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plaid.client.response.TransactionsGetResponse;

import java.util.List;
import java.util.Locale;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> {
    private final Context context; // Used for click listener in each transaction
    private final List<TransactionsGetResponse.Transaction> transactionList;
    private final LayoutInflater layoutInflater;

    public HomeRecyclerAdapter(Context activityContext, List<TransactionsGetResponse.Transaction> list) {
        context = activityContext;
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
        String transactionLocation = transaction.getLocation().getCity() + ", " + transaction.getLocation().getRegion();
        String transactionDate = transaction.getDate();
        String transactionAmount = "$" + String.format(Locale.US,"%.2f", transaction.getAmount());

        holder.transactionNameText.setText(transactionName);
        holder.transactionLocationText.setText(transactionLocation);
        holder.transactionDateText.setText(transactionDate);
        holder.transactionAmountText.setText(transactionAmount);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView transactionNameText;
        final TextView transactionDateText;
        final TextView transactionAmountText;
        final TextView transactionLocationText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionNameText = itemView.findViewById(R.id.text_transaction_name);
            transactionDateText = itemView.findViewById(R.id.text_transaction_date);
            transactionAmountText = itemView.findViewById(R.id.text_transaction_amount);
            transactionLocationText = itemView.findViewById(R.id.text_transaction_location);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    transactionItemView.setText("Clicked! "+ transactionItemView.getText());
//                }
//            });
        }
    }
}