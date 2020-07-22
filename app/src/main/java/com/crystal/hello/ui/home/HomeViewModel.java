package com.crystal.hello.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.HomeActivity;
import com.crystal.hello.InitialConnectActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.CategoriesGetResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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

    private String clientIdKey = "5e9e830fd1ed690012c3be3c";
    private String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private String sandboxSecretKey = "74cf176067e0712cc2eabdf800829e";
    private String publicKey = "bbf9cf93da45517aa5283841dfc534";
    private final String TAG = HomeViewModel.class.getSimpleName();

    private MutableLiveData<Double> currentTotalBalance;
    private static List<TransactionsGetResponse.Transaction> allTransactionsList;
    private static HashMap<String, Account> accountIdToAccountMap;
    private MutableLiveData<List<Map<String, Object>>> subsetTransactionsList;

    private PlaidClient plaidClient;
    private int transactionOffset;
    private final int count;
    private final FirebaseUser user;
    private final FirebaseFirestore db;
    private final DocumentReference docRef;

    public HomeViewModel() {
        currentTotalBalance = new MutableLiveData<>();
        subsetTransactionsList = new MutableLiveData<>();
        allTransactionsList = new ArrayList<>();

        accountIdToAccountMap = new HashMap<>(); // Credit card accounts only
        transactionOffset = 0;
        count = 500;
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users").document(user.getUid());
    }

    public LiveData<Double> getCurrentTotalBalance() {
        return currentTotalBalance;
    }

    public LiveData<List<Map<String, Object>>> getSubsetTransactionsList() {
        return subsetTransactionsList;
    }

    public static List<TransactionsGetResponse.Transaction> getAllTransactionsList() {
        return allTransactionsList;
    }

    public static HashMap<String, Account> getAccountIdToAccountMap() {
        return accountIdToAccountMap;
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
                            getPlaidTransactionsAndBalances(transactionOffset);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                        Log.w(TAG + "accessToken_failure", call.toString());
                    }
                });
    }

    private void getPlaidTransactionsAndBalances(Integer offset) {
//        Date startDate = new Date(1511049600L); // 1970
//        Date startDate = new Date(System.currentTimeMillis() - 1511049600L * 100); // 2017
        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100); // 2020
        Date endDate = new Date();
        TransactionsGetRequest request =
                new TransactionsGetRequest(accessToken, startDate, endDate)
                        .withCount(count)
                        .withOffset(offset);

        plaidClient.service().transactionsGet(request).enqueue(new Callback<TransactionsGetResponse>() {
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
                        setAccountsToDatabase(accountIdToAccountMap);
                    }

                    // Call getTransactions first b/c transactions are sorted by date
                    List<TransactionsGetResponse.Transaction> paginatedTransactionsList = new ArrayList<>();
                    for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
                        for (String accountId : accountIdToAccountMap.keySet()) {
                            if (transaction.getAccountId().equals(accountId)) {
                                paginatedTransactionsList.add(transaction);
                            }
                        }
                    }

                    allTransactionsList.addAll(paginatedTransactionsList);
                    transactionOffset += count;

                    for (TransactionsGetResponse.Transaction transaction : paginatedTransactionsList) {
                        Log.d(TAG + " plaid_transaction",
                                transaction.getDate() + " "
                                        + String.format(Locale.US,"%.2f", transaction.getAmount()) + " "
                                        + transaction.getName());
                    }

                    // If there are more than 500 transactions, get more because they're paginated
                    int totalTransactions = responseBody.getTotalTransactions();
                    if (transactionOffset < totalTransactions) {
                        getPlaidTransactionsAndBalances(transactionOffset); // Get all transactions within the date period set
                    } else {
//                        // Calculate balance
//                        double currentBalance = 0.0;
//                        for (Account account : accountIdToAccountMap.values()) {
//                            currentBalance += account.getBalances().getCurrent();
//                        }
//                        currentBalanceAmount.setValue(currentBalance);

                        setTransactionsToDatabase(allTransactionsList);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
                Log.w(TAG + "transaction_failure", call.toString());
            }
        });
    }

    // Set Transaction to "transactions" collection with Plaid transactionId as document ID
    private void setTransactionsToDatabase(List<TransactionsGetResponse.Transaction> fullTransactionList) {
        for (TransactionsGetResponse.Transaction transaction : fullTransactionList) {
            docRef.collection("transactions")
                    .document(transaction.getTransactionId())
                    .set(transaction, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            getTransactionsFromDatabase();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }

    // Set Account to "banks" collection with Plaid accountId as document ID
    private void setAccountsToDatabase(HashMap<String, Account> accountIdToAccountMap) {
        for (Account account : accountIdToAccountMap.values()) {
            docRef.collection("banks")
                    .document(account.getAccountId())
                    .set(account, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

            HashMap<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("itemId", itemId);

            db.collection("users")
                    .document(user.getUid())
                    .collection("banks")
                    .document(account.getAccountId())
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }

    protected void getTransactionsFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING).limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Map<String, Object>> transactionDocuments = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                transactionDocuments.add(document.getData());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            subsetTransactionsList.setValue(transactionDocuments);
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

                            for (QueryDocumentSnapshot document : task.getResult()) {
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