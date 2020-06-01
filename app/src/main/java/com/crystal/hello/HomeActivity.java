package com.crystal.hello;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    public static PlaidClient plaidClient;
    public static String accessToken; // We store the accessToken in memory - in production, store it in a secure persistent data store.
    private String publicToken;
//    private TransactionsResource transactionsResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        publicToken = intent.getStringExtra(Intent.EXTRA_TEXT);
        buildPlaidClient();
        getAccessToken();
    }

    private void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret("5e9e830fd1ed690012c3be3c", "74cf176067e0712cc2eabdf800829e")
                .publicKey("bbf9cf93da45517aa5283841dfc534") // optional. only needed to call endpoints that require a public key
                .sandboxBaseUrl() // or equivalent, depending on which environment you're calling into
                .build();
    }

    private void getAccessToken() {
        // Asynchronously do the same thing. Useful for potentially long-lived calls.
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {

                    /**
                     * Invoked for a received HTTP response.
                     *
                     * <p>Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
                     */
                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                accessToken = response.body().getAccessToken();
                                Log.i("access token", response.body().getAccessToken());
                                Log.i("item ID", response.body().getItemId());
                                getTransactions();
//                                transactionsResource = new TransactionsResource();
//                                transactionsResource.getTransactions();
                            }
                        }
                    }

                    /**
                     * Invoked when a network exception occurred talking to the server or when an unexpected exception
                     * occurred creating the request or processing the response.
                     */
                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                        // handle the failure as needed
                    }
                });
    }

    private void getTransactions() {
        /** Code for getting categories https://plaid.com/docs/#categories
         plaidClient.service().categoriesGet(new CategoriesGetRequest()).enqueue(new Callback<CategoriesGetResponse>() {
        @Override
        public void onResponse(Call<CategoriesGetResponse> call, Response<CategoriesGetResponse> response) {
        List<CategoriesGetResponse.Category> categories = response.body().getCategories();
        }

        @Override
        public void onFailure(Call<CategoriesGetResponse> call, Throwable t) {

        }
        });
         */

        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100);
        Date endDate = new Date();

        TransactionsGetRequest request =
                new TransactionsGetRequest(accessToken, startDate, endDate)
                        .withCount(100);

        plaidClient.service().transactionsGet(request).enqueue(new Callback<TransactionsGetResponse>() {
            @Override
            public void onResponse(@NotNull Call<TransactionsGetResponse> call,
                                   @NotNull Response<TransactionsGetResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("Transaction count", String.valueOf(response.body().getTransactions().size()));
                        for (TransactionsGetResponse.Transaction transaction : response.body().getTransactions()) {
                            Log.d("Transactions", transaction.getName());
                        }

                        final ListView listNotes = findViewById(R.id.list);
                        ArrayAdapter<TransactionsGetResponse.Transaction> arrayAdapter = new ArrayAdapter<>(HomeActivity.this,
                                android.R.layout.simple_list_item_1, response.body().getTransactions());

                        listNotes.setAdapter(arrayAdapter);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {

            }
        });
    }

    private void getAccounts() {
        plaidClient.service()
                .accountsGet(new AccountsGetRequest(accessToken))
                .enqueue(new Callback<AccountsGetResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<AccountsGetResponse> call,
                                           @NotNull Response<AccountsGetResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Log.i("AccountsResponse: ", String.valueOf(response.body().getAccounts()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<AccountsGetResponse> call, @NotNull Throwable t) {

                    }
                });
    }
}