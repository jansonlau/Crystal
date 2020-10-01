package com.crystal.hello.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.crystal.hello.ViewHolder;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BankAccountsRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final List<DocumentSnapshot> bankAccountsList;
    private final LayoutInflater layoutInflater;

    public BankAccountsRecyclerAdapter(final FragmentActivity activity, final List<DocumentSnapshot> list) {
        bankAccountsList = list;
        layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = layoutInflater.inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Map<String, Object> account = bankAccountsList.get(position).getData();
        String name = String.valueOf(Objects.requireNonNull(account).get("officialName"));

        // Replace non-word characters
        name = name.replaceAll("[^\\p{L}\\p{Z}]", "");

        // Remove numbers if last 4 digits contains mask
        if (name.length() >= 4
                && name.substring(name.length() - 5, name.length()-1).matches(".*\\d.*")) {
            name = name.substring(0, name.length() - 5);
        }

        if (name.equals("null")) {
            name = String.valueOf(account.get("name"));
        }

        // Mask
        String mask = "";
        if (!String.valueOf(account.get("mask")).equals("null")) {
            mask = "\u2022\u2022\u2022\u2022 ".concat(String.valueOf(account.get("mask")));
        }

        // Current balance
        final Map<String, Object> balances = (HashMap<String, Object>) account.get("balances");
        final double currentBalance = (double) Objects.requireNonNull(balances).get("current");
        String amountString = String.format(Locale.US,"%.2f", currentBalance);
        if (currentBalance >= 0.0) {
            amountString = "$".concat(amountString);
        } else {
            amountString = new StringBuilder(amountString).insert(1, "$").toString();
        }

        holder.transactionTitleTextView.setText(name);
        holder.transactionAmountTextView.setText(amountString);
        holder.transactionSubtitleTextView.setText(mask);
        holder.transactionArrowImageView.setVisibility(View.GONE);
        holder.transactionLogoImageView.setImageResource(R.drawable.ic_baseline_account_balance_24);
        holder.transactionLogoImageView.setBackgroundResource(R.drawable.payments_background);
        holder.transactionConstraintLayout.removeView(holder.transactionDividerView);
    }

    @Override
    public int getItemCount() {
        return bankAccountsList.size();
    }
}
