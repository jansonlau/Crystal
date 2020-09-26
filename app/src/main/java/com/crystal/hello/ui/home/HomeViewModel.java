package com.crystal.hello.ui.home;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
    // Key Initialization vector, Value encrypted data
    private String accessToken;
    private String itemId;

    private final String clientIdKey          = "5e9e830fd1ed690012c3be3c";
    private final String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private final String sandboxSecretKey     = "74cf176067e0712cc2eabdf800829e";

    private final MutableLiveData<Double> currentTotalBalance;
    private final MutableLiveData<List<DocumentSnapshot>> mutableSubsetTransactionsList;
    private final MutableLiveData<List<DocumentSnapshot>> mutableTransactionHistoryList;
    private final MutableLiveData<Map<String, Object>> mutableLatestTransactionMap;
    private static List<Map<String, Object>> bankAccountsList;
    private final Map<String, Account> accountIdToAccountMap;
    private static MonthlyActivityViewModel monthlyActivityViewModel;
    private final MutableLiveData<Boolean> mutableSavedTransactionBoolean;

    private PlaidClient plaidClient;
    private int transactionOffset;
    private final FirebaseFirestore db;
    private final DocumentReference docRef;


    public HomeViewModel() {
        currentTotalBalance             = new MutableLiveData<>();
        mutableSubsetTransactionsList   = new MutableLiveData<>();
        mutableTransactionHistoryList   = new MutableLiveData<>();
        mutableSavedTransactionBoolean  = new MutableLiveData<>();
        mutableLatestTransactionMap     = new MutableLiveData<>();
        accountIdToAccountMap           = new HashMap<>(); // Credit card accounts only
        transactionOffset               = 0;
        db                              = FirebaseFirestore.getInstance();
        docRef                          = db.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        monthlyActivityViewModel        = new MonthlyActivityViewModel();
    }

    public MutableLiveData<Double> getCurrentTotalBalance() {
        return currentTotalBalance;
    }

    public MutableLiveData<List<DocumentSnapshot>> getMutableSubsetTransactionsList() {
        return mutableSubsetTransactionsList;
    }

    public MutableLiveData<List<DocumentSnapshot>> getMutableTransactionHistoryList() {
        return mutableTransactionHistoryList;
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
    protected void exchangeAccessToken(String publicToken) {
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ItemPublicTokenExchangeResponse responseBody = response.body();
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
    private void getPlaidAccountsAndTransactions(final Integer offset) {
        final int count = 500;
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

                    transactionOffset += count;
                    int totalTransactions = responseBody.getTotalTransactions();
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
            final DocumentReference transactionsRef = docRef.collection("transactions")
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
                        getSubsetTransactionsFromDatabase();
                        monthlyActivityViewModel = new MonthlyActivityViewModel();
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

        // Set default budget values
        final Map<String, Integer> budgets = new HashMap<>();
        budgets.put("travel"        , 100);
        budgets.put("health"        , 100);
        budgets.put("shopping"      , 500);
        budgets.put("services"      , 100);
        budgets.put("foodDrinks"    , 100);
        budgets.put("entertainment" , 100);

        final DocumentReference budgetRef = docRef.collection("profile")
                .document("budgets");

        batch.set(budgetRef, budgets, SetOptions.merge());

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
                    }
                });
    }

    protected void getSubsetTransactionsFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<DocumentSnapshot> subsetTransactionsList = Objects.requireNonNull(task.getResult()).getDocuments();
                        mutableSubsetTransactionsList.setValue(subsetTransactionsList);
                    }
                });
    }

    protected void getBalancesAndBankAccountsFromDatabase() {
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

    public void getTransactionHistoryFromDatabase(@NotNull final Map<String, Object> transaction) {
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
                        mutableTransactionHistoryList.setValue(Objects.requireNonNull(task.getResult()).getDocuments());
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
}