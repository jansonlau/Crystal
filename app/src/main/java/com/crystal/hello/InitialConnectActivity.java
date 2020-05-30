package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.TransactionsGetResponse;
import com.plaid.link.Plaid;
import com.plaid.linkbase.models.configuration.LinkConfiguration;
import com.plaid.linkbase.models.configuration.PlaidProduct;
import com.plaid.linkbase.models.connection.LinkConnection;
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitialConnectActivity extends AppCompatActivity {
    private static final int LINK_REQUEST_CODE = 1;
    public static String accessToken; // We store the accessToken in memory - in production, store it in a secure persistent data store.
    private PlaidClient plaidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_connect);

        View button = findViewById(R.id.button_link_bank_continue);
        button.setOnClickListener(view -> {
            setOptionalEventListener();
            openLink();
        });
    }

    /**
     * Optional, set an <a href="https://plaid.com/docs/link/android/#handling-onevent">event listener</a>.
     */
    private void setOptionalEventListener() {
        Plaid.setLinkEventListener(linkEvent -> {
            Log.i("Event", linkEvent.toString());
            return Unit.INSTANCE;
        });
    }

    /**
     * For all Link configuration options, have a look at the
     * <a href="https://plaid.com/docs/link/android/#parameter-reference">parameter reference</>
     */
    private void openLink() {
        ArrayList<PlaidProduct> products = new ArrayList<>();
        products.add(PlaidProduct.TRANSACTIONS);
        Plaid.openLink(
                this,
                new LinkConfiguration.Builder("Crystal", products).build(), // Defaults to plaid options value if set or SANDBOX
                LINK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!myPlaidResultHandler.onActivityResult(requestCode, resultCode, data)) {
            Log.i(InitialConnectActivity.class.getSimpleName(), "Not handled");
        }
    }

    private PlaidLinkResultHandler myPlaidResultHandler = new PlaidLinkResultHandler(LINK_REQUEST_CODE,
            // Handle onSuccess
            linkConnection -> {
                LinkConnection.LinkConnectionMetadata metadata = linkConnection.getLinkConnectionMetadata();

                Log.i(InitialConnectActivity.class.getSimpleName(), getString(
                        R.string.content_success,
                        linkConnection.getPublicToken(),
                        metadata.getAccounts().get(0).getAccountId(),
                        metadata.getAccounts().get(0).getAccountName(),
                        metadata.getInstitutionId(),
                        metadata.getInstitutionName()));

                String publicToken = linkConnection.getPublicToken();

                /*
                String accountId = metadata.getAccounts().get(0).getAccountId();
                String accountName = metadata.getAccounts().get(0).getAccountName();
                String accountNumber = metadata.getAccounts().get(0).getAccountNumber();
                String accountType = metadata.getAccounts().get(0).getAccountType();
                String accountSubType = metadata.getAccounts().get(0).getAccountSubType();
                String institutionId = metadata.getInstitutionId();
                String institutionName = metadata.getInstitutionName();
                 */

                getAccessToken(publicToken);
                return Unit.INSTANCE;
            },

            // Handle onCancelled (close button / Android back button)
            linkCancellation -> {
                Log.i(InitialConnectActivity.class.getSimpleName(), getString(
                        R.string.content_cancelled,
                        linkCancellation.getInstitutionId(),
                        linkCancellation.getInstitutionName(),
                        linkCancellation.getLinkSessionId(),
                        linkCancellation.getStatus()));
                return Unit.INSTANCE;
            },

            // Handle onExit (close button???)
            plaidApiError -> {
                Log.i(InitialConnectActivity.class.getSimpleName(), getString(
                        R.string.content_exit,
                        plaidApiError.getDisplayMessage(),
                        plaidApiError.getErrorCode(),
                        plaidApiError.getErrorMessage(),
                        plaidApiError.getLinkExitMetadata().getInstitutionId(),
                        plaidApiError.getLinkExitMetadata().getInstitutionName(),
                        plaidApiError.getLinkExitMetadata().getStatus()));
                return Unit.INSTANCE;
            }
    );

    private void getAccessToken(String publicToken) {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret("5e9e830fd1ed690012c3be3c", "74cf176067e0712cc2eabdf800829e")
                .publicKey("bbf9cf93da45517aa5283841dfc534") // optional. only needed to call endpoints that require a public key
                .sandboxBaseUrl() // or equivalent, depending on which environment you're calling into
                .build();

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
                                Log.i("access token: ", response.body().getAccessToken());
                                Log.i("item ID: ", response.body().getItemId());
                                getTransactions();
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

//    private void getAccounts() {
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

    private void getTransactions() {
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
                        for (TransactionsGetResponse.Transaction transaction : response.body().getTransactions()) {
                            Log.d("Transactions", transaction.getName());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {

            }
        });
    }
}
