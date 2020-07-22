package com.crystal.hello;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crystal.hello.ui.home.HomeViewModel;
import com.plaid.client.response.Account;
import com.plaid.client.response.TransactionsGetResponse;

public class TransactionItemDetailFragment extends Fragment {

    private TransactionItemDetailViewModel transactionItemDetailViewModel;

    public static TransactionItemDetailFragment newInstance() {
        return new TransactionItemDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        transactionItemDetailViewModel = new ViewModelProvider(this).get(TransactionItemDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);

        final TextView amount           = root.findViewById(R.id.textViewTransactionDetailAmount);
        final TextView nameAndLocation  = root.findViewById(R.id.textViewTransactionDetailName);
        final TextView date             = root.findViewById(R.id.textViewTransactionDetailDate);
        final TextView status           = root.findViewById(R.id.textViewTransactionDetailStatus);
        final TextView channel          = root.findViewById(R.id.textViewTransactionDetailChannel);
        final TextView accountMask      = root.findViewById(R.id.textViewTransactionDetailAccountMask);
        final TextView address          = root.findViewById(R.id.textViewTransactionDetailAddress);

        int transactionItemPosition = 0;
        String transactionItemName = "";
        String transactionItemDate = "";
        String transactionItemAmount = "";
        String transactionAccountMask = "\u2022\u2022\u2022\u2022 ";

        // TODO Get HomeViewModel instead
        if (getArguments() != null) {
            transactionItemPosition = getArguments().getInt("TRANSACTION_ITEM_POSITION");
            transactionItemName = getArguments().getString("TRANSACTION_ITEM_NAME");
            transactionItemDate = getArguments().getString("TRANSACTION_ITEM_DATE");
            transactionItemAmount = getArguments().getString("TRANSACTION_ITEM_AMOUNT");
        }

        TransactionsGetResponse.Transaction transaction = HomeViewModel.getAllTransactionsList().get(transactionItemPosition);
        TransactionsGetResponse.Transaction.Location location = transaction.getLocation();
        Account account = HomeViewModel.getAccountIdToAccountMap().get(transaction.getAccountId());

        if (account != null) {
            transactionAccountMask += account.getMask();
        }

        if (location.getCity() != null && location.getRegion() != null) {
            transactionItemName += ", " + location.getCity() + ", " + location.getRegion();

            if (location.getAddress() != null && location.getPostalCode() != null) {
                String addressString = location.getAddress() + ", "
                        + location.getCity() + ", "
                        + location.getRegion() + ", "
                        + location.getPostalCode();
                address.setText(addressString);
            } else {
                root.findViewById(R.id.cardViewMapAndAddress).setVisibility(View.GONE);
            }
        } else {
            root.findViewById(R.id.cardViewMapAndAddress).setVisibility(View.GONE);
        }

        String transactionStatus = "Status: ";
        if (transaction.getPending()) {
            transactionStatus += "Pending";

        } else {
            transactionStatus += "Completed";
        }

        String channelString = "Paid ";
        switch (transaction.getPaymentChannel()) {
            case "online":
                channelString += "Online";
                break;
            case "in store":
                channelString += "In-store";
                break;
            case "other":
                if (transactionItemName != null && transaction.getAmount() < 0.00) {
                    if (transactionItemName.toLowerCase().contains("pymnt")
                            || transactionItemName.toLowerCase().contains("payment")
                            || transactionItemName.toLowerCase().contains("pay")) {
                        channelString = "Payment";
                    } else {
                        channelString = "Refund";
                    }
                } else {
                    channelString = "Other Channel";
                }
                break;
        }

        amount.setText(transactionItemAmount);
        nameAndLocation.setText(transactionItemName);
        date.setText(transactionItemDate);
        channel.setText(channelString);
        accountMask.setText(transactionAccountMask);
        status.setText(transactionStatus);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}