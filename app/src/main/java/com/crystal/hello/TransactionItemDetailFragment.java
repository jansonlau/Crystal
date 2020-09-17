package com.crystal.hello;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.ui.home.HomeViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TransactionItemDetailFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root                                = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);
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
        root.findViewById(R.id.transactionDetailNestedScrollView).setVisibility(View.INVISIBLE);

        // Bank account
        Map<String, Object> account = null;
        for (Map<String, Object> bankAccount : homeViewModel.getBankAccountsList()) {
            if (String.valueOf(Objects.requireNonNull(transaction).get("accountId")).equals(String.valueOf(bankAccount.get("accountId")))) {
                account = bankAccount;
            }
        }

        final int transactionItemLogo           = getArguments().getInt("TRANSACTION_ITEM_LOGO");
        final int transactionItemLogoBackground = getArguments().getInt("TRANSACTION_ITEM_LOGO_BACKGROUND");
        final String transactionItemName        = Objects.requireNonNull(getArguments().getString("TRANSACTION_ITEM_NAME"));
        final String transactionItemDate        = getArguments().getString("TRANSACTION_ITEM_DATE");
        final String transactionItemAmount      = getArguments().getString("TRANSACTION_ITEM_AMOUNT");
        final String transactionItemCategory    = getArguments().getString("TRANSACTION_ITEM_CATEGORY");
        String transactionItemAccountMask       = "";
        String locationString                   = "";
        String transactionItemAccountName       = String.valueOf(Objects.requireNonNull(account).get("name"));
        transactionItemAccountName              = transactionItemAccountName.substring(0, transactionItemAccountName.length() - 5);

        if (!String.valueOf(account.get("mask")).equals("null")) {
            transactionItemAccountMask = "\u2022\u2022\u2022\u2022 " + account.get("mask");
        }

        // Show location or map if available. Else, hide the views
        final Map<String, Object> locationMap = (HashMap<String, Object>) Objects.requireNonNull(transaction.get("location"));
        final String address                  = (String) locationMap.get("address");
        final String city                     = (String) locationMap.get("city");
        final String region                   = (String) locationMap.get("region");
        final String postalCode               = (String) locationMap.get("postalCode");
        final CardView locationCardView       = root.findViewById(R.id.transactionDetailMapAndLocationCardView);

        if ((address == null && city == null && region == null && postalCode == null)
                || String.valueOf(transaction.get("paymentChannel")).equals("online")) {
            locationCardView.setVisibility(View.GONE);
        } else {
            if (address != null) {
                locationString = address;
            } else {
                locationString = transactionItemName;
            }

            if (city != null) {
                locationString = locationString.concat(", ").concat(city);
            }

            if (region != null) {
                locationString = locationString.concat(", ").concat(region);
            }

            if (postalCode != null) {
                locationString = locationString.concat(", ").concat(postalCode);
            }
        }

        // Map
        final SupportMapFragment mapFragment = (SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()).findFragmentById(R.id.transactionDetailMapFragment);

        if (!locationString.isEmpty() && mapFragment != null) {
            final String finalLocationString = locationString;
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    try {
                        loadMap(googleMap, transactionItemName, finalLocationString, locationCardView);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Transaction status
        final List<String> categoriesList = (List<String>) transaction.get("category");
        String transactionStatus = "Status: ";
        if ((boolean) transaction.get("pending")) {
            transactionStatus = transactionStatus.concat("Pending");
        } else if (!Objects.requireNonNull(categoriesList).get(0).equals("Transfer") && (double) transaction.get("amount") < 0) {
            transactionStatus = transactionStatus.concat("Refunded");
        } else {
            transactionStatus = transactionStatus.concat("Posted");
        }

        // Similar transactions
        homeViewModel.getTransactionHistoryFromDatabase(transaction);
        homeViewModel.getMutableTransactionHistoryList().observe(getViewLifecycleOwner(), transactionHistoryList -> {
            root.findViewById(R.id.transactionItemDetailProgressBar).setVisibility(View.GONE);
            root.findViewById(R.id.transactionDetailNestedScrollView).setVisibility(View.VISIBLE);

            DocumentSnapshot removeDuplicateTransactionDoc = null;
            for (DocumentSnapshot doc : transactionHistoryList) {
                if (String.valueOf(doc.get("transactionId")).equals(transaction.get("transactionId"))) {
                    removeDuplicateTransactionDoc = doc;
                }
            }
            transactionHistoryList.remove(removeDuplicateTransactionDoc);

            if (transactionHistoryList.isEmpty()) {
                root.findViewById(R.id.transactionDetailHistoryTextView).setVisibility(View.GONE);
            }

            final RecyclerView transactionHistoryRecyclerView = root.findViewById(R.id.transactionDetailTransactionHistoryRecyclerView);
            transactionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            final TransactionRecyclerAdapter recyclerAdapter = new TransactionRecyclerAdapter(getActivity(), transactionHistoryList);
            transactionHistoryRecyclerView.setAdapter(recyclerAdapter);
        });

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

    private void loadMap(final GoogleMap googleMap, String name, final String addressString, CardView locationCardView) throws IOException {
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        final List<Address> addressList = geocoder.getFromLocationName(addressString, 1);

        if (!addressList.isEmpty()) {
            final Address address = addressList.get(0);
            final LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            if (googleMap != null) {
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.setMinZoomPreference(15);
            }
        } else {
            locationCardView.setVisibility(View.GONE);
        }
    }
}