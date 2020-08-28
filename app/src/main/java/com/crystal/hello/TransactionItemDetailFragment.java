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
import java.util.Objects;

public class TransactionItemDetailFragment extends Fragment {
    private HomeViewModel homeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root                     = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);
        final ImageView logoImageView       = root.findViewById(R.id.imageViewTransactionDetailLogo);
        final TextView amountTextView       = root.findViewById(R.id.textViewTransactionDetailAmount);
        final TextView nameTextView         = root.findViewById(R.id.textViewTransactionDetailName);
        final TextView dateTextView         = root.findViewById(R.id.textViewTransactionDetailDate);
        final TextView statusTextView       = root.findViewById(R.id.textViewTransactionDetailStatus);
        final TextView bankNameTextView     = root.findViewById(R.id.textViewTransactionDetailBankName);
        final TextView accountMaskTextView  = root.findViewById(R.id.textViewTransactionDetailAccountMask);
        final TextView addressTextView      = root.findViewById(R.id.textViewTransactionDetailAddress);
        final TextView categoryTextView     = root.findViewById(R.id.textViewTransactionDetailCategory);
        final Map<String, Object> transaction = (Map<String, Object>) Objects.requireNonNull(getArguments()).getSerializable("TRANSACTION_ITEM_MAP");

        // Bank account
        Map<String, Object> account = null;
        for (Map<String, Object> bankAccount : homeViewModel.getBankAccountsList()) {
            if (String.valueOf(Objects.requireNonNull(transaction).get("accountId")).equals(String.valueOf(bankAccount.get("accountId")))) {
                account = bankAccount;
            }
        }

        final int transactionItemLogo           = getArguments().getInt("TRANSACTION_ITEM_LOGO");
        final int transactionItemLogoBackground = getArguments().getInt("TRANSACTION_ITEM_LOGO_BACKGROUND");
        final String transactionItemName        = getArguments().getString("TRANSACTION_ITEM_NAME");
        final String transactionItemDate        = getArguments().getString("TRANSACTION_ITEM_DATE");
        final String transactionItemAmount      = getArguments().getString("TRANSACTION_ITEM_AMOUNT");
        final String transactionItemCategory    = getArguments().getString("TRANSACTION_ITEM_CATEGORY");
        String transactionItemAccountMask       = "";
        String locationString                   = "";
        String transactionItemAccountName       = String.valueOf(Objects.requireNonNull(account).get("name"));
        transactionItemAccountName              = transactionItemAccountName.substring(0, transactionItemAccountName.length() - 5);

        if (account.get("mask") != null) {
            transactionItemAccountMask = "\u2022\u2022\u2022\u2022 " + account.get("mask");
        }

        // Show location or map if available. Else, hide the views
        final Map<String, Object> locationMap = (HashMap<String, Object>) transaction.get("location");
        final String address                  = (String) Objects.requireNonNull(locationMap).get("address");
        final String city                     = (String) locationMap.get("city");
        final String region                   = (String) locationMap.get("region");
        final String postalCode               = (String) locationMap.get("postalCode");

        if (address != null && city != null && region != null && postalCode != null) {
            locationString = address + ", " + city + ", " + region + " " + postalCode;
        } else if (city != null && region != null && postalCode != null) {
            locationString = transactionItemName + ", " + city + ", " + region + " " + postalCode;
        } else if (city != null && region != null) {
            locationString = transactionItemName + ", " + city + ", " + region;
        } else {
            root.findViewById(R.id.cardViewMapAndLocation).setVisibility(View.GONE);
        }

        // Pending
        String transactionStatus = "Status: ";
        if ((boolean) transaction.get("pending")) {
            transactionStatus += "Pending";
        } else {
            transactionStatus += "Completed";
        }

        logoImageView       .setImageResource(transactionItemLogo);
        logoImageView       .setBackgroundResource(transactionItemLogoBackground);
        amountTextView      .setText(transactionItemAmount);
        nameTextView        .setText(transactionItemName);
        dateTextView        .setText(transactionItemDate);
        statusTextView      .setText(transactionStatus);
        categoryTextView    .setText(transactionItemCategory);
        bankNameTextView    .setText(transactionItemAccountName);
        accountMaskTextView .setText(transactionItemAccountMask);
        addressTextView     .setText(locationString);
        return root;
    }
}