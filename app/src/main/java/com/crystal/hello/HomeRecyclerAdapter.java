package com.crystal.hello;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plaid.client.response.TransactionsGetResponse;

import java.util.List;

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
        String transactionName = transactionList.get(position).getDate();
        holder.transactionItemView.setText(transactionName);
        Log.d("Transaction date", String.valueOf(transactionName));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView transactionItemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionItemView = itemView.findViewById(R.id.text_transaction_name);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    transactionItemView.setText("Clicked! "+ transactionItemView.getText());
//                }
//            });
        }
    }
}
