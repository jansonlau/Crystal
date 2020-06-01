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

import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitialConnectActivity extends AppCompatActivity {
    private static final int LINK_REQUEST_CODE = 1;

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

                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, publicToken);
                startActivity(intent);
                finish();
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
}
