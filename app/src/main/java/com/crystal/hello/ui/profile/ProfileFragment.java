package com.crystal.hello.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.MainActivity;
import com.crystal.hello.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.plaid.link.Plaid;
import com.plaid.link.configuration.AccountSubtype;
import com.plaid.link.configuration.LinkConfiguration;
import com.plaid.link.configuration.PlaidEnvironment;
import com.plaid.link.configuration.PlaidProduct;
import com.plaid.link.result.PlaidLinkResultHandler;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;

public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private View root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.getBankAccountsFromDatabase();
        Plaid.initialize(Objects.requireNonNull(getActivity()).getApplication());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        final Button button = root.findViewById(R.id.addAccountButton);
        button.setOnClickListener(view -> openLink());

        final TextView logOutTextView = root.findViewById(R.id.logOutTextView);
        logOutTextView.setOnClickListener(view -> logOut());

        observeBankAccountsList();

        profileViewModel.getMutableTransactionsCompleteBoolean().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                root.findViewById(R.id.bankAccountsTextView).setVisibility(View.VISIBLE);
                root.findViewById(R.id.addAccountButton).setVisibility(View.VISIBLE);
                root.findViewById(R.id.profileFragmentProgressBar).setVisibility(View.GONE);
            }
        });
        return root;
    }

    private void openLink() {
        Plaid.openLink(this, new LinkConfiguration.Builder()
                .clientName("Crystal")
                .environment(PlaidEnvironment.DEVELOPMENT)
//                .products(Arrays.asList(PlaidProduct.TRANSACTIONS, PlaidProduct.LIABILITIES))
                .products(Collections.singletonList(PlaidProduct.TRANSACTIONS))
                .publicKey("bbf9cf93da45517aa5283841dfc534")
                .accountSubtypeFilter(Collections.singletonList(AccountSubtype.CREDIT.CREDIT_CARD.INSTANCE))
                .build());

        // TODO: Get linkToken with server
//        LinkTokenRequester.INSTANCE.getToken().subscribe(this::onLinkTokenSuccess, this::onLinkTokenError);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myPlaidResultHandler.onActivityResult(requestCode, resultCode, data);
    }

    private final PlaidLinkResultHandler myPlaidResultHandler = new PlaidLinkResultHandler(
            linkSuccess -> {
                root.findViewById(R.id.bankAccountsTextView).setVisibility(View.GONE);
                root.findViewById(R.id.addAccountButton).setVisibility(View.GONE);
                root.findViewById(R.id.profileFragmentProgressBar).setVisibility(View.VISIBLE);

                final String publicToken = linkSuccess.getPublicToken();
                profileViewModel.buildPlaidClient();
                profileViewModel.exchangeAccessToken(publicToken);
                return Unit.INSTANCE;
            },

            linkExit -> {
//                if (linkExit.error != null) {
//                    // Link error
//                } else {
//                    // Left link
//                }
                return Unit.INSTANCE;
            }
    );

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finishAffinity();
    }

    private void observeBankAccountsList() {
        final RecyclerView bankAccountsRecyclerView = root.findViewById(R.id.bankAccountsRecyclerView);
        bankAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        profileViewModel.getMutableBankAccountsList().observe(getViewLifecycleOwner(), new Observer<List<DocumentSnapshot>>() {
            @Override
            public void onChanged(List<DocumentSnapshot> documentSnapshotList) {
                final BankAccountsRecyclerAdapter recyclerAdapter = new BankAccountsRecyclerAdapter(getActivity(), documentSnapshotList);
                bankAccountsRecyclerView.setAdapter(recyclerAdapter);
            }
        });
    }
}