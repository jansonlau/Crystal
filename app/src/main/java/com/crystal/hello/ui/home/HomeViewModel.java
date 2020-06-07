package com.crystal.hello.ui.home;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.HomeActivity;
import com.crystal.hello.R;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List> mList;
    private PlaidClient plaidClient;
    private String accessToken; // We store the accessToken in memory - in production, store it in a secure persistent data store.

    public HomeViewModel() {
        mList = new MutableLiveData<>();
        buildPlaidClient();
        exchangeAccessToken();
    }

    public LiveData<List> getList() {
        return mList;
    }

    private void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret("5e9e830fd1ed690012c3be3c", "74cf176067e0712cc2eabdf800829e")
                .publicKey("bbf9cf93da45517aa5283841dfc534") // optional. only needed to call endpoints that require a public key
                .sandboxBaseUrl() // or equivalent, depending on which environment you're calling into
                .build();
    }

    private void exchangeAccessToken() {
        // Asynchronously do the same thing. Useful for potentially long-lived calls.
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(HomeActivity.publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {

                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            accessToken = response.body().getAccessToken();
                            Log.i("Access token", response.body().getAccessToken());
                            Log.i("Item ID", response.body().getItemId());
                            getTransactions();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                    }
                });
    }

    private void getTransactions() {
        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100);
        Date endDate = new Date();

        TransactionsGetRequest request =
                new TransactionsGetRequest(accessToken, startDate, endDate).withCount(100);

        plaidClient.service().transactionsGet(request).enqueue(new Callback<TransactionsGetResponse>() {
            @Override
            public void onResponse(@NotNull Call<TransactionsGetResponse> call,
                                   @NotNull Response<TransactionsGetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("Transaction count", String.valueOf(response.body().getTransactions().size()));

                    List<String> transactionNames = new ArrayList<>();
                    for (TransactionsGetResponse.Transaction transaction : response.body().getTransactions()) {
                        Log.d("Transactions", transaction.getName());
                        transactionNames.add(transaction.getName());
                    }
                    mList.postValue(transactionNames);
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {

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
}