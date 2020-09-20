package com.crystal.hello;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TransactionItemDetailFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root;

    private Map<String, Object> transaction;
    private int transactionItemLogo;
    private int transactionItemLogoBackground;
    private String transactionItemName;
    private String transactionItemDate;
    private String transactionItemAmount;
    private String transactionItemCategory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel                   = new ViewModelProvider(this).get(HomeViewModel.class);
        transaction                     = (Map<String, Object>) Objects.requireNonNull(getArguments()).getSerializable("TRANSACTION_ITEM_MAP");
        transactionItemLogo             = getArguments().getInt("TRANSACTION_ITEM_LOGO");
        transactionItemLogoBackground   = getArguments().getInt("TRANSACTION_ITEM_LOGO_BACKGROUND");
        transactionItemName             = getArguments().getString("TRANSACTION_ITEM_NAME");
        transactionItemDate             = getArguments().getString("TRANSACTION_ITEM_DATE");
        transactionItemAmount           = getArguments().getString("TRANSACTION_ITEM_AMOUNT");
        transactionItemCategory         = getArguments().getString("TRANSACTION_ITEM_CATEGORY");
        homeViewModel.getTransactionHistoryFromDatabase(transaction);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);

        // Similar transactions
        root.findViewById(R.id.transactionDetailHistoryTextView).setVisibility(View.GONE);
        root.findViewById(R.id.transactionDetailHistoryCardView).setVisibility(View.GONE);

        homeViewModel.getMutableTransactionHistoryList().observe(getViewLifecycleOwner(), transactionHistoryList -> {
            DocumentSnapshot removeDuplicateTransactionDoc = null;
            for (final DocumentSnapshot doc : transactionHistoryList) {
                if (String.valueOf(doc.get("transactionId")).equals(transaction.get("transactionId"))) {
                    removeDuplicateTransactionDoc = doc;
                }
            }
            transactionHistoryList.remove(removeDuplicateTransactionDoc);

            root.findViewById(R.id.transactionItemDetailProgressBar).setVisibility(View.GONE);
            if (!transactionHistoryList.isEmpty()) {
                root.findViewById(R.id.transactionDetailHistoryTextView).setVisibility(View.VISIBLE);
                root.findViewById(R.id.transactionDetailHistoryCardView).setVisibility(View.VISIBLE);
            }

            final RecyclerView transactionHistoryRecyclerView = root.findViewById(R.id.transactionDetailTransactionHistoryRecyclerView);
            final TransactionRecyclerAdapter recyclerAdapter = new TransactionRecyclerAdapter(getActivity(), transactionHistoryList);
            transactionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            transactionHistoryRecyclerView.setAdapter(recyclerAdapter);
        });

        final ImageView logoImageView       = root.findViewById(R.id.imageViewTransactionDetailLogo);
        final ImageView saveImageView       = root.findViewById(R.id.transactionDetailSaveImageView);
        final TextView amountTextView       = root.findViewById(R.id.textViewTransactionDetailAmount);
        final TextView nameTextView         = root.findViewById(R.id.textViewTransactionDetailName);
        final TextView dateTextView         = root.findViewById(R.id.textViewTransactionDetailDate);
        final TextView statusTextView       = root.findViewById(R.id.textViewTransactionDetailStatus);
        final TextView bankNameTextView     = root.findViewById(R.id.textViewTransactionDetailBankName);
        final TextView accountMaskTextView  = root.findViewById(R.id.textViewTransactionDetailAccountMask);
        final TextView addressTextView      = root.findViewById(R.id.textViewTransactionDetailAddress);
        final TextView categoryTextView     = root.findViewById(R.id.textViewTransactionDetailCategory);

        // Bank account
        Map<String, Object> account = null;
        for (Map<String, Object> bankAccount : homeViewModel.getBankAccountsList()) {
            if (String.valueOf(transaction.get("accountId")).equals(String.valueOf(bankAccount.get("accountId")))) {
                account = bankAccount;
            }
        }

        String transactionItemAccountMask       = "";
        String locationString                   = "";
        String transactionItemAccountName       = String.valueOf(Objects.requireNonNull(account).get("officialName"));

        // Remove numbers if last 4 digits contains mask
        if (transactionItemAccountName.length() >= 4
                && transactionItemAccountName.substring(transactionItemAccountName.length() - 5, transactionItemAccountName.length()-1).matches(".*\\d.*")) {
            transactionItemAccountName = transactionItemAccountName.substring(0, transactionItemAccountName.length() - 5);
        }

        if (!String.valueOf(account.get("mask")).equals("null")) {
            transactionItemAccountMask = "\u2022\u2022\u2022\u2022 " + account.get("mask");
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

        // Saved transaction
        if ((boolean) transaction.get("saved")) {
            setImageViewToSavedResource(saveImageView);
        }
        setSaveTransactionClickListener(saveImageView, transaction);

        // Show location or map if available. Else, hide the views
        final Map<String, Object> locationMap = (HashMap<String, Object>) Objects.requireNonNull(transaction.get("location"));
        final String address                  = (String) locationMap.get("address");
        final String city                     = (String) locationMap.get("city");
        final String region                   = (String) locationMap.get("region");
        final String postalCode               = (String) locationMap.get("postalCode");
        final CardView locationCardView       = root.findViewById(R.id.transactionDetailMapAndLocationCardView);
        final SupportMapFragment mapFragment  = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.transactionDetailMapFragment);

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

            // Map
            if (mapFragment != null) {
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

    private void loadMap(final GoogleMap googleMap, String name, final String addressString, CardView locationCardView) throws IOException {
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        final List<Address> addressList = geocoder.getFromLocationName(addressString, 1);

        if (!addressList.isEmpty()) {
            locationCardView.setVisibility(View.VISIBLE);
            final Address address = addressList.get(0);
            final double latitude = address.getLatitude();
            final double longitude = address.getLongitude();
            final LatLng latLng = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(name));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.setMinZoomPreference(15);
            googleMap.getUiSettings().setAllGesturesEnabled(true);

            final NestedScrollView transactionDetailNestedScrollView = root.findViewById(R.id.transactionDetailNestedScrollView);
            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    transactionDetailNestedScrollView.requestDisallowInterceptTouchEvent(true);
                    googleMap.resetMinMaxZoomPreference();
                }
            });

            // Address listener
            final FrameLayout transactionDetailAddressFrameLayout = root.findViewById(R.id.transactionDetailAddressFrameLayout);
            transactionDetailAddressFrameLayout.setOnClickListener(view -> {
                final String uriString = "geo:".concat(String.valueOf(latitude)).concat(",").concat(String.valueOf(longitude)).concat("?q=").concat(addressString);
                final Uri gmmIntentUri = Uri.parse(uriString);
                final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            });
        } else {
            locationCardView.setVisibility(View.GONE);
        }
    }

    private void setSaveTransactionClickListener(@NotNull final ImageView saveImageView, final Map<String, Object> transaction) {
        saveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressBar saveProgressBar = root.findViewById(R.id.transactionDetailSaveProgressBar);
                saveImageView.setVisibility(View.GONE);
                saveProgressBar.setVisibility(View.VISIBLE);
                homeViewModel.setSaveTransactionToDatabase(transaction);

                homeViewModel.getMutableSavedTransactionBoolean().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        saveProgressBar.setVisibility(View.GONE);
                        saveImageView.setVisibility(View.VISIBLE);
                        setImageViewToSavedResource(saveImageView);
                    }
                });
            }
        });
    }

    private void setImageViewToSavedResource(@NotNull final ImageView saveImageView) {
        saveImageView.setImageResource(R.drawable.ic_outline_bookmark_border_24);
        saveImageView.setPadding(4, 4, 4, 4);
        saveImageView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        saveImageView.setBackgroundResource(R.drawable.round_corner);
    }
}