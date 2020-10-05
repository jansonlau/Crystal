package com.crystal.hello.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.MainActivity;
import com.crystal.hello.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.plaid.link.Plaid;
import com.plaid.link.configuration.LinkPublicKeyConfiguration;
import com.plaid.link.configuration.PlaidEnvironment;
import com.plaid.link.configuration.PlaidProduct;
import com.plaid.link.result.LinkAccountSubtype;
import com.plaid.link.result.LinkResultHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;

public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private View root;
    private RecyclerView budgetAmountsRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        final Button button = root.findViewById(R.id.addAccountButton);
        button.setOnClickListener(view -> openLink());

        final TextView logOutTextView = root.findViewById(R.id.logOutTextView);
        logOutTextView.setOnClickListener(view -> logOut());

        budgetAmountsRecyclerView = root.findViewById(R.id.budgetAmountsRecyclerView);

        observeBankAccountsList();
        observeBudgetAmountsList();
//        observeAddAccountCompleteBoolean();
        setBudgetAmountSaveButtonListener();
        return root;
    }

    private void openLink() {
        Plaid.create(requireActivity().getApplication(), new LinkPublicKeyConfiguration.Builder()
                .clientName("Crystal")
                .environment(PlaidEnvironment.DEVELOPMENT)
//                .products(Arrays.asList(PlaidProduct.TRANSACTIONS, PlaidProduct.LIABILITIES))
                .products(Collections.singletonList(PlaidProduct.TRANSACTIONS))
                .publicKey("bbf9cf93da45517aa5283841dfc534")
                .accountSubtypes(Collections.singletonList(LinkAccountSubtype.CREDIT.CREDIT_CARD.INSTANCE))
                .build())
                .open(this);

        // TODO: Get linkToken with server
//        LinkTokenRequester.INSTANCE.getToken().subscribe(this::onLinkTokenSuccess, this::onLinkTokenError);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myPlaidResultHandler.onActivityResult(requestCode, resultCode, data);
    }

    private final LinkResultHandler myPlaidResultHandler = new LinkResultHandler(
            linkSuccess -> {
//                root.findViewById(R.id.bankAccountsTextView).setVisibility(View.GONE);
//                root.findViewById(R.id.addAccountButton).setVisibility(View.GONE);
//                root.findViewById(R.id.profileFragmentProgressBar).setVisibility(View.VISIBLE);

                final String publicToken = linkSuccess.getPublicToken();
                profileViewModel.buildPlaidClient();
                profileViewModel.exchangeAccessToken(publicToken);
                return Unit.INSTANCE;
            },

            linkExit -> {
//                if (linkExit.getError() != null) {
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
        requireActivity().finishAffinity();
    }

//    private void observeAddAccountCompleteBoolean() {
//        profileViewModel.getMutableTransactionsCompleteBoolean().observe(getViewLifecycleOwner(), aBoolean -> {
//            if (aBoolean) {
//                root.findViewById(R.id.bankAccountsTextView).setVisibility(View.VISIBLE);
//                root.findViewById(R.id.addAccountButton).setVisibility(View.VISIBLE);
//                root.findViewById(R.id.profileFragmentProgressBar).setVisibility(View.GONE);
//            }
//        });
//    }

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

    private void observeBudgetAmountsList() {
        budgetAmountsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        profileViewModel.getMutableBudgetsMap().observe(getViewLifecycleOwner(), new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> categoryBudgetAmountMap) {
                final BudgetAmountsRecyclerAdapter recyclerAdapter = new BudgetAmountsRecyclerAdapter(getActivity(), categoryBudgetAmountMap);
                budgetAmountsRecyclerView.setAdapter(recyclerAdapter);
            }
        });
    }

    // Check each recycler view item / category for new text
    private void setBudgetAmountSaveButtonListener() {
        final Button budgetAmountSaveButton = root.findViewById(R.id.budgetAmountSaveButton);
        budgetAmountSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide keyboard
                final InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Toast.makeText(getContext(), "Saved budget amounts", Toast.LENGTH_LONG).show();
                final Map<String, Integer> budgetData = new HashMap<>();

                for (int position = 0; position < 6; position++) {
                    final View itemView = Objects.requireNonNull(budgetAmountsRecyclerView.findViewHolderForLayoutPosition(position)).itemView;
                    final TextInputEditText budgetAmountEditText = itemView.findViewById(R.id.budgetAmountEditText);
                    final String budgetAmount = Objects.requireNonNull(budgetAmountEditText.getText()).toString();

                    if (budgetAmountEditText.isFocused()) {
                        budgetAmountEditText.clearFocus();
                    }

                    if (!budgetAmount.isEmpty()) {
                        String category;
                        switch (position) {
                            case 0:
                                category = "shopping";
                                break;
                            case 1:
                                category = "foodDrinks";
                                break;
                            case 2:
                                category = "travel";
                                break;
                            case 3:
                                category = "entertainment";
                                break;
                            case 4:
                                category = "health";
                                break;
                            default:
                                category = "services";
                        }
                        budgetData.put(category, Integer.decode(budgetAmount));
                        budgetAmountEditText.setHint("$".concat(budgetAmount));
                        budgetAmountEditText.setText("");
                    }
                }
                if (!budgetData.isEmpty()) {
                    profileViewModel.setBudgetAmountsToDatabase(budgetData);
                }
            }
        });
    }
}