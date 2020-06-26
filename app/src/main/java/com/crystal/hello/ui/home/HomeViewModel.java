package com.crystal.hello.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.HomeActivity;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    // SAVING STATES https://developer.android.com/topic/libraries/architecture/saving-states
    private String accessToken = "access-development-74de9498-a64b-4c42-9d40-1a27aebbe9ca"; // In production, store it in a secure persistent data store.
    private String itemId = "wnXw7JoxEjIxZJzpzxdnfY3aKJJwROuLQxLO5";
    private String clientIdKey = "5e9e830fd1ed690012c3be3c";
    private String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private String sandboxSecretKey = "74cf176067e0712cc2eabdf800829e";
    private String publicKey = "bbf9cf93da45517aa5283841dfc534";

    private MutableLiveData<List<TransactionsGetResponse.Transaction>> mList;
    private MutableLiveData<Double> currentBalanceAmount;
    private static List<TransactionsGetResponse.Transaction> fullTransactionList;
    private static HashMap<String, Account> accountIdToAccountMap;
//    private HashMap<String, List<TransactionsGetResponse.Transaction>> accountIdToTransactionListMap;
    private PlaidClient plaidClient;
    private int transactionOffset;
    private final int count;

    public HomeViewModel() {
        mList = new MutableLiveData<>();
        currentBalanceAmount = new MutableLiveData<>();
        fullTransactionList = new LinkedList<>();
        accountIdToAccountMap = new HashMap<>(); // credit card accounts only
//        accountIdToTransactionListMap = new HashMap<>();
        transactionOffset = 0;
        count = 500;

        buildPlaidClient();
//        exchangeAccessToken();
        getPlaidTransactionsAndBalances(transactionOffset); // Temporary
    }

    public LiveData<List<TransactionsGetResponse.Transaction>> getTransactionList() { return mList; }
    public LiveData<Double> getCurrentBalanceAmount() {
        return currentBalanceAmount;
    }
    public static List<TransactionsGetResponse.Transaction> getFullTransactionList() { return fullTransactionList; }
    public static HashMap<String, Account> getAccountIdToAccountMap() { return accountIdToAccountMap; }

    private void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret(clientIdKey, developmentSecretKey)
                .publicKey(publicKey) // optional. only needed to call endpoints that require a public key
                .developmentBaseUrl()
                .build();
    }
//
//    // Asynchronously get token
//    private void exchangeAccessToken() {
//        plaidClient.service()
//                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(HomeActivity.publicToken))
//                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {
//
//                    @Override
//                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
//                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            accessToken = response.body().getAccessToken(); // Log item_id for retrieving item
//                            Log.i(HomeViewModel.class.getSimpleName() + " plaid_accessToken", response.body().getAccessToken());
//                            Log.i(HomeViewModel.class.getSimpleName() + " plaid_itemId", response.body().getItemId());
//                            getPlaidTransactionsAndBalances(transactionOffset);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
//                                          @NotNull Throwable t) {
//                        Log.w(HomeViewModel.class.getSimpleName() + "accessToken_failure", call.toString());
//                    }
//                });
//    }

    private void getPlaidTransactionsAndBalances(Integer offset) {
//        Date startDate = new Date(1511049600L); // 1970
        Date startDate = new Date(System.currentTimeMillis() - 1511049600L * 100); // 2017
//        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100); // 2020
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
                    }

//                    // Map each credit card transaction to an account id for easy reference
//                    for (HashMap.Entry<String, Account> entry : accountIdToAccountMap.entrySet()) {
//                        List<TransactionsGetResponse.Transaction> list = new ArrayList<>();
//
//                        for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
//                            if (entry.getKey().equals(transaction.getAccountId())) {
//                                if (accountIdToTransactionListMap.containsKey(entry.getKey())) {
//                                    accountIdToTransactionListMap.get(entry.getKey()).add(transaction);
//                                } else {
//                                    list.add(transaction);
//                                    accountIdToTransactionListMap.put(entry.getKey(), list);
//                                }
//                            }
//                        }
//                    }

                    // Call getTransactions first b/c transactions are sorted by date
                    List<TransactionsGetResponse.Transaction> transactionList = new ArrayList<>();
                    for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
                        for (String accountId : accountIdToAccountMap.keySet()) {
                            if (transaction.getAccountId().equals(accountId)) {
                                transactionList.add(transaction);
                            }
                        }
                    }

                    fullTransactionList.addAll(transactionList);
                    transactionOffset += count;

                    for (TransactionsGetResponse.Transaction transaction : transactionList) {
                        Log.d(HomeViewModel.class.getSimpleName() + " plaid_transaction",
                                transaction.getDate() + " "
                                        + String.format(Locale.US,"%.2f", transaction.getAmount()) + " "
                                        + transaction.getName());
                    }

                    // If there are more than 500 transactions, get more because they're paginated
                    int totalTransactions = responseBody.getTotalTransactions();
                    if (transactionOffset < totalTransactions) {
                        getPlaidTransactionsAndBalances(transactionOffset); // Get all transactions within the date period set
                    } else {
                        // Calculate balance
                        double currentBalance = 0.0;
                        for (Account account : accountIdToAccountMap.values()) {
                            currentBalance += account.getBalances().getCurrent();
                        }
                        currentBalanceAmount.setValue(currentBalance);
                        mList.setValue(fullTransactionList); // Post all transactions
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
                Log.w(HomeViewModel.class.getSimpleName() + "transaction_failure", call.toString());
            }
        });
    }

//    private void getCategories() {
//        // Code for getting categories https://plaid.com/docs/#categories
//        plaidClient.service().categoriesGet(new CategoriesGetRequest()).enqueue(new Callback<CategoriesGetResponse>() {
//            @Override
//            public void onResponse(Call<CategoriesGetResponse> call, Response<CategoriesGetResponse> response) {
//                List<CategoriesGetResponse.Category> categories = response.body().getCategories();
//            }
//
//            @Override
//            public void onFailure(Call<CategoriesGetResponse> call, Throwable t) {
//
//            }
//        });
//    }
}