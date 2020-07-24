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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plaid.client.response.Account;
import com.plaid.client.response.TransactionsGetResponse;

import java.util.HashMap;
import java.util.Map;

public class TransactionItemDetailFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FirebaseFirestore db;
    private DocumentReference docRef;

    public static TransactionItemDetailFragment newInstance() {
        return new TransactionItemDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root       = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);
        homeViewModel   = new ViewModelProvider(this).get(HomeViewModel.class);
        db              = FirebaseFirestore.getInstance();
        docRef          = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final TextView amount       = root.findViewById(R.id.textViewTransactionDetailAmount);
        final TextView name         = root.findViewById(R.id.textViewTransactionDetailName);
        final TextView location     = root.findViewById(R.id.textViewTransactionDetailLocation);
        final TextView date         = root.findViewById(R.id.textViewTransactionDetailDate);
        final TextView status       = root.findViewById(R.id.textViewTransactionDetailStatus);
        final TextView channel      = root.findViewById(R.id.textViewTransactionDetailChannel);
        final TextView accountMask  = root.findViewById(R.id.textViewTransactionDetailAccountMask);
        final TextView address      = root.findViewById(R.id.textViewTransactionDetailAddress);

        int transactionItemPosition     = 0;
        String transactionItemName      = "";
        String transactionItemDate      = "";
        String transactionItemLocation  = "";
        String transactionItemAmount    = "";
        String transactionAccountMask   = "\u2022\u2022\u2022\u2022 ";

        // TODO Get HomeViewModel instead
        if (getArguments() != null) {
            transactionItemPosition = getArguments().getInt("TRANSACTION_ITEM_POSITION");
            transactionItemName     = getArguments().getString("TRANSACTION_ITEM_NAME");
            transactionItemDate     = getArguments().getString("TRANSACTION_ITEM_DATE");
            transactionItemAmount   = getArguments().getString("TRANSACTION_ITEM_AMOUNT");
        }

        Map<String, Object> transaction = homeViewModel.getSubsetTransactionsList().get(transactionItemPosition);
//        String accountId = (String) transaction.get("accountId");


//        if (account != null) {
//            transactionAccountMask += account.getMask();
//        }


        Map<String, Object> locationMap = (HashMap<String, Object>) transaction.get("location");
        if (locationMap != null && locationMap.get("city") != null && locationMap.get("region") != null) {
            transactionItemLocation += locationMap.get("city") + ", " + locationMap.get("region");

            if (locationMap.get("address") != null && locationMap.get("postalCode") != null) {
                String addressString = locationMap.get("address") + ", "
                        + locationMap.get("city") + ", "
                        + locationMap.get("region") + ", "
                        + locationMap.get("postalCode");
                address.setText(addressString);
            } else {
                root.findViewById(R.id.cardViewMapAndAddress).setVisibility(View.GONE);
            }
        } else {
            root.findViewById(R.id.cardViewMapAndAddress).setVisibility(View.GONE);
            root.findViewById(R.id.textViewTransactionDetailLocation).setVisibility(View.GONE);
        }

        String transactionStatus = "Status: ";
        if ((boolean) transaction.get("pending")) {
            transactionStatus += "Pending";
        } else {
            transactionStatus += "Completed";
        }

//        String channelString = "Paid ";
//        switch (transaction.getPaymentChannel()) {
//            case "online":
//                channelString += "Online";
//                break;
//            case "in store":
//                channelString += "In-store";
//                break;
//            case "other":
//                if (transactionItemName != null && transaction.getAmount() < 0.00) {
//                    if (transactionItemName.toLowerCase().contains("pymnt")
//                            || transactionItemName.toLowerCase().contains("payment")
//                            || transactionItemName.toLowerCase().contains("pay")) {
//                        channelString = "Payment";
//                    } else {
//                        channelString = "Refund";
//                    }
//                } else {
//                    channelString = "Other Channel";
//                }
//                break;
//        }

        amount.setText(transactionItemAmount);
        name.setText(transactionItemName);
        location.setText(transactionItemLocation);
        date.setText(transactionItemDate);
//        channel.setText(channelString);
        accountMask.setText(transactionAccountMask);
        status.setText(transactionStatus);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}