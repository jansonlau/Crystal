package com.crystal.hello.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.monthlyactivity.MonthlyActivityViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

public class HomeViewModel extends ViewModel {
    private String accessToken;
    private String itemId;

    private final String clientIdKey          = "5e9e830fd1ed690012c3be3c";
    private final String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private final String sandboxSecretKey     = "74cf176067e0712cc2eabdf800829e";

    private static MutableLiveData<Double> currentTotalBalance;
    private final MutableLiveData<List<DocumentSnapshot>> mutableLatestTransactionsList;
    private final MutableLiveData<List<DocumentSnapshot>> mutableSimilarTransactionsList;
    private final MutableLiveData<Map<String, Object>> mutableLatestTransactionMap;
    private static List<Map<String, Object>> bankAccountsList;
    private Map<String, Account> accountIdToAccountMap;
    public static MonthlyActivityViewModel monthlyActivityViewModel;
    private final MutableLiveData<Boolean> mutableSavedTransactionBoolean;

    private PlaidClient plaidClient;
    private int transactionOffset;
    private final FirebaseFirestore db;
    private static DocumentReference docRef;


    public HomeViewModel() {
        currentTotalBalance             = new MutableLiveData<>();
        mutableLatestTransactionsList   = new MutableLiveData<>();
        mutableSimilarTransactionsList  = new MutableLiveData<>();
        mutableSavedTransactionBoolean  = new MutableLiveData<>();
        mutableLatestTransactionMap     = new MutableLiveData<>();
        transactionOffset               = 0;
        db                              = FirebaseFirestore.getInstance();
        docRef                          = db.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        monthlyActivityViewModel        = new MonthlyActivityViewModel();
    }

    public MutableLiveData<Double> getCurrentTotalBalance() {
        return currentTotalBalance;
    }

    public MutableLiveData<List<DocumentSnapshot>> getMutableLatestTransactionsList() {
        return mutableLatestTransactionsList;
    }

    public MutableLiveData<List<DocumentSnapshot>> getMutableSimilarTransactionsList() {
        return mutableSimilarTransactionsList;
    }

    public MutableLiveData<Map<String, Object>> getMutableLatestTransactionMap() {
        return mutableLatestTransactionMap;
    }

    public List<Map<String, Object>> getBankAccountsList() {
        return bankAccountsList;
    }

    public static MonthlyActivityViewModel getMonthlyActivityViewModel() {
        return monthlyActivityViewModel;
    }

    protected void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret(clientIdKey, developmentSecretKey)
                .developmentBaseUrl()
                .build();
    }

    // Asynchronously get access token for a bank account
    protected void exchangeAccessToken(final String publicToken) {
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
                        t.getLocalizedMessage();
                    }
                });
    }

    // Plaid Transactions for Accounts and Transactions
    private void getPlaidAccountsAndTransactions(final Integer offset) {
        final int count = 250;
        final Date startDate = new Date(0); // Wed 31 December 1969 16:00:00
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
                                accountIdToAccountMap = new HashMap<>();
                                for (final Account account : responseBody.getAccounts()) {
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
                        } else {
                            getLatestTransactionsFromDatabase();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
                        t.getLocalizedMessage();
                    }
                });
    }

    // Write to Firestore with paginated list because of Plaid's 500 transaction limit and
    // Firestore's WriteBatch has limit of 500 documents
    // Set Plaid Transaction to Firestore's "transactions" collection with Plaid transactionId as document ID
    private void setPaginatedPlaidTransactionsToDatabase(@NotNull final List<TransactionsGetResponse.Transaction> paginatedTransactionsList,
                                                         final int totalTransactions) {
        final WriteBatch batch = db.batch();
        for (final TransactionsGetResponse.Transaction transaction : paginatedTransactionsList) {
            final DocumentReference transactionsRef = docRef.collection("transactions")
                    .document(transaction.getTransactionId());

            batch.set(transactionsRef, transaction, SetOptions.merge());
            batch.set(transactionsRef, Collections.singletonMap("saved", false), SetOptions.merge());
        }

        // Get more transactions because they're paginated
        if (transactionOffset < totalTransactions) {
            getPlaidAccountsAndTransactions(transactionOffset); // Get all transactions within the date period set
            batch.commit();
        } else {
            batch.commit()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            getLatestTransactionsFromDatabase();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.getLocalizedMessage();
                        }
                    });
        }
    }

    // Set Plaid Account to "banks" collection with Plaid accountId as document ID
    private void setPlaidAccountsToDatabase() {
        final WriteBatch batch = db.batch();
        for (final Account account : accountIdToAccountMap.values()) {
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

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getBalancesAndBankAccountsFromDatabase();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.getMessage();
                    }
                });
    }

    protected void getLatestTransactionsFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<DocumentSnapshot> latestTransactionsList = Objects.requireNonNull(task.getResult()).getDocuments();
                        mutableLatestTransactionsList.setValue(latestTransactionsList);
                        monthlyActivityViewModel = new MonthlyActivityViewModel();
                    }
                });
    }

    public static void getBalancesAndBankAccountsFromDatabase() {
        docRef.collection("banks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double totalBalance = 0;
                        Map<String, Object> bankAccountMap;
                        Map<String, Object> balances;
                        bankAccountsList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            bankAccountMap = document.getData();
                            bankAccountsList.add(bankAccountMap);
                            balances = (HashMap<String, Object>) bankAccountMap.get("balances");
                            totalBalance += (double) Objects.requireNonNull(balances).get("current");
                        }
                        currentTotalBalance.setValue(totalBalance);
                    }
                });
    }

    public void getSimilarTransactionsFromDatabase(@NotNull final Map<String, Object> transaction) {
        final String transactionDate = String.valueOf(transaction.get("date"));
        String queryNameField = "merchantName";
        String transactionName = String.valueOf(Objects.requireNonNull(transaction).get("merchantName"));

        if (transactionName.equals("null")) {
            transactionName = String.valueOf(Objects.requireNonNull(transaction).get("name"));
            queryNameField = "name";
        }

        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereLessThanOrEqualTo("date", transactionDate)
                .whereEqualTo(queryNameField, transactionName)
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mutableSimilarTransactionsList.setValue(Objects.requireNonNull(task.getResult()).getDocuments());
                    }
                });
    }

    public void setSaveTransactionToDatabase(@NotNull final String transactionId, boolean saveBoolean) {
        docRef.collection("transactions")
                .document(transactionId)
                .set(Collections.singletonMap("saved", saveBoolean), SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mutableSavedTransactionBoolean.setValue(true);
                        }
                    }
                });
    }

    public void getLatestTransactionFromDatabase(@NotNull final String transactionId) {
        docRef.collection("transactions")
                .document(transactionId)
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       mutableLatestTransactionMap.setValue(Objects.requireNonNull(task.getResult()).getData());
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
}