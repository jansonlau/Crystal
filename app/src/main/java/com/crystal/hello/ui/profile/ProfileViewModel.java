package com.crystal.hello.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {
    private final String clientIdKey = "5e9e830fd1ed690012c3be3c";
    private final String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private final String sandboxSecretKey = "74cf176067e0712cc2eabdf800829e";

    private PlaidClient plaidClient;
    private String accessToken;
    private String itemId;
    private final FirebaseFirestore db;
    private final DocumentReference docRef;
    private int transactionOffset;
    private final Map<String, Account> accountIdToAccountMap;
    private final MutableLiveData<Boolean> mutableTransactionsCompleteBoolean;
    private final MutableLiveData<List<DocumentSnapshot>> mutableBankAccountsList;

    public ProfileViewModel() {
        transactionOffset = 0;
        accountIdToAccountMap = new HashMap<>(); // Credit card accounts only
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        mutableTransactionsCompleteBoolean = new MutableLiveData<>();
        mutableBankAccountsList = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getMutableTransactionsCompleteBoolean() {
        return mutableTransactionsCompleteBoolean;
    }

    public MutableLiveData<List<DocumentSnapshot>> getMutableBankAccountsList() {
        return mutableBankAccountsList;
    }

    protected void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret(clientIdKey, developmentSecretKey)
                .developmentBaseUrl()
                .build();
    }

    // Asynchronously get access token for a bank account
    protected void exchangeAccessToken(String publicToken) {
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {

                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            final ItemPublicTokenExchangeResponse responseBody = response.body();
                            accessToken = responseBody.getAccessToken();
                            itemId = responseBody.getItemId();
                            getPlaidAccountsAndTransactions(transactionOffset);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                    }
                });
    }

    // Plaid Transactions for Accounts and Transactions
    private void getPlaidAccountsAndTransactions(final Integer offset) {
        final int count = 500;
        final Date startDate = new Date(0);
        final Date endDate = new Date();

        final TransactionsGetRequest request = new TransactionsGetRequest(Objects.requireNonNull(accessToken), startDate, endDate)
                .withCount(count)
                .withOffset(offset);

        plaidClient.service()
                .transactionsGet(request)
                .enqueue(new Callback<TransactionsGetResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<TransactionsGetResponse> call,
                                           @NotNull Response<TransactionsGetResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            final TransactionsGetResponse responseBody = response.body();

                            // Get credit card accounts once
                            // Accounts include account name and current balance
                            if (transactionOffset == 0) {
                                for (Account account : responseBody.getAccounts()) {
                                    if (account.getSubtype().equals("credit card")) {
                                        accountIdToAccountMap.put(account.getAccountId(), account);
                                    }
                                }
                                setPlaidAccountsToDatabase();
                            }

                            // Get transactions
                            final List<TransactionsGetResponse.Transaction> paginatedTransactionsList = new ArrayList<>();
                            for (String accountId : accountIdToAccountMap.keySet()) {
                                for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
                                    if (transaction.getAccountId().equals(accountId)) {
                                        paginatedTransactionsList.add(transaction);
                                    }
                                }
                            }

                            transactionOffset += count;
                            final int totalTransactions = responseBody.getTotalTransactions();
                            setPaginatedPlaidTransactionsToDatabase(paginatedTransactionsList, totalTransactions);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
                    }
                });
    }

    // Write to Firestore with paginated list because of Plaid's 500 transaction limit and
    // Firestore's WriteBatch has limit of 500 documents
    // Set Plaid Transaction to Firestore's "transactions" collection with Plaid transactionId as document ID
    private void setPaginatedPlaidTransactionsToDatabase(@NotNull final List<TransactionsGetResponse.Transaction> paginatedTransactionsList,
                                                         final int totalTransactions) {
        final WriteBatch batch = db.batch();
        for (TransactionsGetResponse.Transaction transaction : paginatedTransactionsList) {
            DocumentReference transactionsRef = docRef.collection("transactions")
                    .document(transaction.getTransactionId());

            batch.set(transactionsRef, transaction, SetOptions.merge());
            batch.set(transactionsRef, Collections.singletonMap("saved", false), SetOptions.merge());
        }

        // If there are more than 500 transactions, get more because they're paginated
        if (transactionOffset < totalTransactions) {
            getPlaidAccountsAndTransactions(transactionOffset); // Get all transactions within the date period set
            batch.commit();
            return;
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getBankAccountsFromDatabase();
                        mutableTransactionsCompleteBoolean.setValue(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    // Set Plaid Account to "banks" collection with Plaid accountId as document ID
    private void setPlaidAccountsToDatabase() {
        final WriteBatch batch = db.batch();
        for (Account account : accountIdToAccountMap.values()) {
            final Map<String, Object> identifiers = new HashMap<>();
            identifiers.put("accessToken", accessToken);
            identifiers.put("itemId", itemId);

            final DocumentReference identifiersRef = docRef.collection("identifiers")
                    .document(account.getAccountId());

            final DocumentReference banksRef = docRef.collection("banks")
                    .document(account.getAccountId());

            batch.set(identifiersRef, identifiers, SetOptions.merge());
            batch.set(banksRef, account, SetOptions.merge());
        }

        batch.commit();
    }

    protected void getBankAccountsFromDatabase() {
        docRef.collection("banks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getMutableBankAccountsList().setValue(Objects.requireNonNull(task.getResult()).getDocuments());
                    }
                });
    }
}