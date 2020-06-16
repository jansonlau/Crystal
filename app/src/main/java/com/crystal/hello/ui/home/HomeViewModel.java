package com.crystal.hello.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.HomeActivity;
import com.plaid.client.PlaidClient;
import com.plaid.client.model.paymentinitiation.Amount;
import com.plaid.client.request.AccountsBalanceGetRequest;
import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.request.AssetReportCreateRequest;
import com.plaid.client.request.AssetReportGetRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.AssetReportCreateResponse;
import com.plaid.client.response.AssetReportGetResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;
import com.robinhood.spark.SparkAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    // SAVING STATES https://developer.android.com/topic/libraries/architecture/saving-states
    private String accessToken; // In production, store it in a secure persistent data store.
    private String assetReportToken; // In production, store it in a secure persistent data store.
    private String clientIdKey = "5e9e830fd1ed690012c3be3c";
    private String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private String sandboxSecretKey = "74cf176067e0712cc2eabdf800829e";
    private String publicKey = "bbf9cf93da45517aa5283841dfc534";

    private MutableLiveData<List<TransactionsGetResponse.Transaction>> mList;
    private MutableLiveData<Double> currentBalanceAmount;
    private static List<TransactionsGetResponse.Transaction> transactionList;
    private PlaidClient plaidClient;
    private int transactionOffset;
    private final int count;

    public HomeViewModel() {
        mList = new MutableLiveData<>();
        currentBalanceAmount = new MutableLiveData<>();
        transactionList = new LinkedList<>();
        transactionOffset = 0;
        count = 500;

        buildPlaidClient();
        exchangeAccessToken();
    }

    public LiveData<List<TransactionsGetResponse.Transaction>> getTransactionList() {
        return mList;
    }

    public LiveData<Double> getCurrentBalanceAmount() {
        return currentBalanceAmount;
    }

    private void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret(clientIdKey, sandboxSecretKey)
                .publicKey(publicKey) // optional. only needed to call endpoints that require a public key
                .sandboxBaseUrl()
                .build();
    }

    private void exchangeAccessToken() {
        // Asynchronously get token
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(HomeActivity.publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {

                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            accessToken = response.body().getAccessToken(); // Log item_id for retrieving item
                            Log.i(HomeViewModel.class.getSimpleName() + " accessToken", response.body().getAccessToken());
                            Log.i(HomeViewModel.class.getSimpleName() + " itemId", response.body().getItemId());
                            getPlaidTransactions(transactionOffset);
                            getPlaidBalances();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                        Log.w(HomeViewModel.class.getSimpleName() + "accessToken_failure", call.toString());
                    }
                });
    }

    private void getPlaidTransactions(Integer offset) {
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
                    Integer totalTransactions = responseBody.getTotalTransactions();
                    transactionList.addAll(responseBody.getTransactions());
                    transactionOffset += count;

                    Log.d(HomeViewModel.class.getSimpleName() + " totalTransactions", String.valueOf(totalTransactions));
                    for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
                        Log.d(HomeViewModel.class.getSimpleName() + " transaction",
                                transaction.getDate() + " "
                                        + String.format(Locale.US,"%.2f", transaction.getAmount()) + " "
                                        + transaction.getName());
                    }

                    if (transactionOffset < totalTransactions) {
                        getPlaidTransactions(transactionOffset); // Get all transactions within the date period set
                    } else {
                        mList.postValue(transactionList); // Post all transactions
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
                Log.w(HomeViewModel.class.getSimpleName() + "transaction_failure", call.toString());
            }
        });
    }

    private void getPlaidBalances() {
        AccountsBalanceGetRequest request = new AccountsBalanceGetRequest(accessToken);
        plaidClient.service().accountsBalanceGet(request).enqueue(new Callback<AccountsBalanceGetResponse>() {
            @Override
            public void onResponse(@NotNull Call<AccountsBalanceGetResponse> call,
                                   @NotNull Response<AccountsBalanceGetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double currentBalance = 0.0;
                    for (Account account : response.body().getAccounts()) {
                        currentBalance += account.getBalances().getCurrent();
                    }
                    currentBalanceAmount.postValue(currentBalance);
                }
            }

            @Override
            public void onFailure(@NotNull Call<AccountsBalanceGetResponse> call, @NotNull Throwable t) {
                Log.w(HomeViewModel.class.getSimpleName() + "balance_failure", call.toString());
            }
        });
    }

//    private void getAccounts() {
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
//
//        plaidClient.service()
//                .accountsGet(new AccountsGetRequest(accessToken))
//                .enqueue(new Callback<AccountsGetResponse>() {
//                    @Override
//                    public void onResponse(@NotNull Call<AccountsGetResponse> call,
//                                           @NotNull Response<AccountsGetResponse> response) {
//                        if (response.isSuccessful()) {
//                            if (response.body() != null) {
//                                Log.i("AccountsResponse: ", String.valueOf(response.body().getAccounts()));
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NotNull Call<AccountsGetResponse> call, @NotNull Throwable t) {
//
//                    }
//                });
//    }

//    private void getPlaidAssetReportToken() { // https://plaid.com/docs/#assets
//        plaidClient.service()
//                .assetReportCreate(new AssetReportCreateRequest(Collections.singletonList(accessToken), 730))
//                .enqueue(new Callback<AssetReportCreateResponse>() {
//            @Override
//            public void onResponse(@NotNull Call<AssetReportCreateResponse> call, @NotNull Response<AssetReportCreateResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    String assetReportId = response.body().getAssetReportId(); // Used for identifying webhooks
//                    assetReportToken = response.body().getAssetReportToken();
//                    getPlaidAssetReport();
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<AssetReportCreateResponse> call, @NotNull Throwable t) {
//
//            }
//        });
//    }
//
//    private void getPlaidAssetReport() {
//        plaidClient.service().assetReportGet(new AssetReportGetRequest(assetReportToken)
//                .withIncludeInsights(true))
//                .enqueue(new Callback<AssetReportGetResponse>() {
//            @Override
//            public void onResponse(@NotNull Call<AssetReportGetResponse> call, @NotNull Response<AssetReportGetResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    AssetReportGetResponse.AssetReport assetReport = response.body().getReport().getItems().get(0).getAccounts().get(0).get
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<AssetReportGetResponse> call, @NotNull Throwable t) {
//
//            }
//        });
//    }
}