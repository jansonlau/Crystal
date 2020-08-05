package com.crystal.hello.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    // SAVING STATES https://developer.android.com/topic/libraries/architecture/saving-states
    private String accessToken; // In production, store it in a secure persistent data store.
    private String itemId;

    private String clientIdKey          = "5e9e830fd1ed690012c3be3c";
    private String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private String sandboxSecretKey     = "74cf176067e0712cc2eabdf800829e";
    private String publicKey            = "bbf9cf93da45517aa5283841dfc534";
    private final String TAG            = HomeViewModel.class.getSimpleName();

    private MutableLiveData<Double> currentTotalBalance;
    private MutableLiveData<List<Map<String, Object>>> mutableSubsetTransactionsList;
    private static List<Map<String, Object>> subsetTransactionsList; // Use in TransactionItemDetailFragment requires static declaration
    private static List<Map<String, Object>> bankAccountsList;
    private Map<String, Account> accountIdToAccountMap;

    private PlaidClient plaidClient;
    private int transactionOffset;
    private final FirebaseFirestore db;
    private final DocumentReference docRef;

    public HomeViewModel() {
        currentTotalBalance             = new MutableLiveData<>();
        mutableSubsetTransactionsList   = new MutableLiveData<>();
        accountIdToAccountMap           = new HashMap<>(); // Credit card accounts only
        transactionOffset               = 0;
        db                              = FirebaseFirestore.getInstance();
        docRef                          = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public LiveData<Double> getCurrentTotalBalance() {
        return currentTotalBalance;
    }

    public LiveData<List<Map<String, Object>>> getMutableSubsetTransactionsList() {
        return mutableSubsetTransactionsList;
    }

    public List<Map<String, Object>> getSubsetTransactionsList() {
        return subsetTransactionsList;
    }

    public List<Map<String, Object>> getBankAccountsList() {
        return bankAccountsList;
    }

    protected void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret(clientIdKey, sandboxSecretKey)
                .publicKey(publicKey)
                .sandboxBaseUrl()
                .build();
    }

    // Asynchronously get token for a bank account
    protected void exchangeAccessToken() {
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(HomeActivity.publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            accessToken = response.body().getAccessToken();
                            itemId = response.body().getItemId();
                            Log.i(TAG + ":plaid_accessToken:", response.body().getAccessToken());
                            Log.i(TAG + ":plaid_itemId:", response.body().getItemId());
                            getPlaidAccountsAndTransactions(transactionOffset);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                        Log.w(TAG + "accessToken_failure", call.toString());
                    }
                });
    }

    // TODO: Plaid Liabilities for upcoming bill amount
//    private void getPlaidLiabilities() {
//        LiabilitiesGetRequest request = new LiabilitiesGetRequest(accessToken);
//        plaidClient.service().liabilitiesGet(request).enqueue(new Callback<LiabilitiesGetResponse>() {
//            @Override
//            public void onResponse(@NotNull Call<LiabilitiesGetResponse> call,
//                                   @NotNull Response<LiabilitiesGetResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<LiabilitiesGetResponse.CreditCardLiability> creditCardList = response.body().getLiabilities().getCredit();
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<LiabilitiesGetResponse> call,
//                                  @NotNull Throwable t) {
//
//            }
//        });
//    }

    // Plaid Transactions for Accounts and Transactions
    private void getPlaidAccountsAndTransactions(Integer offset) {
        final int count = 500;
//        Date startDate = new Date(1511049600L); // 1970
        Date startDate = new Date(System.currentTimeMillis() - 1511049600L * 100); // 2017
//        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100); // 2020
        Date endDate = new Date();
        TransactionsGetRequest request = new TransactionsGetRequest(accessToken, startDate, endDate)
                .withCount(count)
                .withOffset(offset);

        plaidClient.service()
                .transactionsGet(request)
                .enqueue(new Callback<TransactionsGetResponse>() {
            @Override
            public void onResponse(@NotNull Call<TransactionsGetResponse> call,
                                   @NotNull Response<TransactionsGetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TransactionsGetResponse responseBody = response.body();

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
                    List<TransactionsGetResponse.Transaction> paginatedTransactionsList = new ArrayList<>();
                    for (String accountId : accountIdToAccountMap.keySet()) {
                        for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
                            if (transaction.getAccountId().equals(accountId)) {
                                paginatedTransactionsList.add(transaction);
                            }
                        }
                    }

                    for (TransactionsGetResponse.Transaction transaction : paginatedTransactionsList) {
                        Log.d(TAG + " plaid_transaction", transaction.getDate() + " "
                                + String.format(Locale.US,"%.2f", transaction.getAmount()) + " "
                                + transaction.getName());
                    }

                    transactionOffset += count;
                    int totalTransactions = responseBody.getTotalTransactions();
                    setPaginatedPlaidTransactionsToDatabase(paginatedTransactionsList, totalTransactions);
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
                Log.w(TAG + "transaction_failure", call.toString());
            }
        });
    }

    // Write to Firestore with paginated list because of Plaid's 500 transaction limit and
    // Firestore's WriteBatch has limit of 500 documents
    // Set Plaid Transaction to "transactions" collection with Plaid transactionId as document ID
    private void setPaginatedPlaidTransactionsToDatabase(List<TransactionsGetResponse.Transaction> paginatedTransactionsList,
                                                         int totalTransactions) {
        final WriteBatch batch = db.batch();
        for (TransactionsGetResponse.Transaction transaction : paginatedTransactionsList) {
            DocumentReference transactionsRef = docRef.collection("transactions")
                    .document(transaction.getTransactionId());

            batch.set(transactionsRef, transaction, SetOptions.merge());
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");

                        // If there are more than 500 transactions, get more because they're paginated
                        if (transactionOffset < totalTransactions) {
                            getPlaidAccountsAndTransactions(transactionOffset); // Get all transactions within the date period set
                        } else {
                            getSubsetTransactionsFromDatabase();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    // Set Plaid Account to "banks" collection with Plaid accountId as document ID
    private void setPlaidAccountsToDatabase() {
        final WriteBatch batch = db.batch();
        for (Account account : accountIdToAccountMap.values()) {
            Map<String, Object> identifiers = new HashMap<>();
            identifiers.put("accessToken", accessToken);
            identifiers.put("itemId", itemId);

            DocumentReference identifiersRef = docRef.collection("identifiers")
                    .document(account.getAccountId());

            DocumentReference banksRef = docRef.collection("banks")
                    .document(account.getAccountId());

            batch.set(identifiersRef, identifiers, SetOptions.merge());
            batch.set(banksRef, account, SetOptions.merge());
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        getBalancesFromDatabase();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    protected void getSubsetTransactionsFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            subsetTransactionsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                subsetTransactionsList.add(document.getData());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            mutableSubsetTransactionsList.setValue(subsetTransactionsList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    protected void getBalancesFromDatabase() {
        docRef.collection("banks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double totalBalance = 0.0;
                            bankAccountsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                bankAccountsList.add((HashMap<String, Object>) document.getData());

                                Map<String, Object> balances = (HashMap<String, Object>) document.getData().get("balances");
                                totalBalance += (double) balances.get("current");
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            currentTotalBalance.setValue(totalBalance);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}