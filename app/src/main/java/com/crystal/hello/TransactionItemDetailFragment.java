package com.crystal.hello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crystal.hello.ui.home.HomeViewModel;

import java.util.HashMap;
import java.util.Map;

public class TransactionItemDetailFragment extends Fragment {
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root       = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);
        homeViewModel   = new ViewModelProvider(this).get(HomeViewModel.class);

        final ImageView logo        = root.findViewById(R.id.imageViewTransactionDetailLogo);
        final TextView amount       = root.findViewById(R.id.textViewTransactionDetailAmount);
        final TextView name         = root.findViewById(R.id.textViewTransactionDetailName);
//        final TextView location     = root.findViewById(R.id.textViewTransactionDetailLocation);
        final TextView date         = root.findViewById(R.id.textViewTransactionDetailDate);
        final TextView status       = root.findViewById(R.id.textViewTransactionDetailStatus);
        final TextView bankName     = root.findViewById(R.id.textViewTransactionDetailBankName);
        final TextView accountMask  = root.findViewById(R.id.textViewTransactionDetailAccountMask);
        final TextView address      = root.findViewById(R.id.textViewTransactionDetailAddress);
        final TextView category     = root.findViewById(R.id.textViewTransactionDetailCategory);

        int transactionItemPosition         = 0;
        int transactionItemLogo             = 0;
        String transactionItemCategory;
        String transactionItemName          = "";
//        String transactionItemLocation      = "";
        String transactionItemDate          = "";
        String transactionItemAmount        = "";
        String transactionItemAccountName   = "";
        String transactionItemAccountMask   = "\u2022\u2022\u2022\u2022 ";

        if (getArguments() != null) {
            transactionItemPosition = getArguments().getInt("TRANSACTION_ITEM_POSITION");
            transactionItemLogo     = getArguments().getInt("TRANSACTION_ITEM_LOGO");
            transactionItemName     = getArguments().getString("TRANSACTION_ITEM_NAME");
            transactionItemDate     = getArguments().getString("TRANSACTION_ITEM_DATE");
            transactionItemAmount   = getArguments().getString("TRANSACTION_ITEM_AMOUNT");
        }

        Map<String, Object> transaction = homeViewModel.getSubsetTransactionsList().get(transactionItemPosition);
        Map<String, Object> account = null;

        // Bank account
        for (Map<String, Object> bankAccount : homeViewModel.getBankAccountsList()) {
            if (String.valueOf(transaction.get("accountId")).equals(String.valueOf(bankAccount.get("accountId")))) {
                account = bankAccount;
            }
        }

        if (account != null) {
            transactionItemAccountMask += String.valueOf(account.get("mask"));
            transactionItemAccountName = String.valueOf(account.get("name"));
            transactionItemAccountName = transactionItemAccountName.substring(0, transactionItemAccountName.length() - 5);
        }

        // Category
        switch (transactionItemLogo) {
            case R.drawable.ic_outline_shopping_cart_24:
                transactionItemCategory = "Shopping";
                break;
            case R.drawable.ic_outline_fastfood_24:
                transactionItemCategory = "Food & Drinks";
                break;
            case R.drawable.ic_outline_healing_24:
                transactionItemCategory = "Health";
                break;
            case R.drawable.ic_outline_local_movies_24:
                transactionItemCategory = "Entertainment";
                break;
            case R.drawable.ic_outline_airplanemode_active_24:
                transactionItemCategory = "Travel";
                break;
            default:
                transactionItemCategory = "Services";
        }

        // Show location or map if available. Else, hide the views
        Map<String, Object> locationMap = (HashMap<String, Object>) transaction.get("location");
        if (locationMap != null && locationMap.get("city") != null && locationMap.get("region") != null) {
            String locationString = transactionItemName + ", "
                    + locationMap.get("city") + ", "
                    + locationMap.get("region");

            if (locationMap.get("address") != null && locationMap.get("postalCode") != null) {
                locationString = locationMap.get("address") + ", "
                        + locationMap.get("city") + ", "
                        + locationMap.get("region") + ", "
                        + locationMap.get("postalCode");
            }
            address.setText(locationString);
        } else {
            root.findViewById(R.id.cardViewMapAndLocation).setVisibility(View.GONE);
            root.findViewById(R.id.textViewTransactionDetailLocation).setVisibility(View.GONE);
        }

        // Pending
        String transactionStatus = "Status: ";
        if ((boolean) transaction.get("pending")) {
            transactionStatus += "Pending";
        } else {
            transactionStatus += "Completed";
        }

        logo        .setImageResource(transactionItemLogo);
        amount      .setText(transactionItemAmount);
        name        .setText(transactionItemName);
//        location    .setText(transactionItemLocation);
        date        .setText(transactionItemDate);
        status      .setText(transactionStatus);
        category    .setText(transactionItemCategory);
        bankName    .setText(transactionItemAccountName);
        accountMask .setText(transactionItemAccountMask);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}