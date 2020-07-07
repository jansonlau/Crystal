package com.crystal.hello;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.plaid.link.Plaid;
import com.plaid.linkbase.models.configuration.LinkConfiguration;
import com.plaid.linkbase.models.configuration.PlaidEnvironment;
import com.plaid.linkbase.models.configuration.PlaidProduct;
import com.plaid.linkbase.models.connection.LinkAccount;
import com.plaid.linkbase.models.connection.LinkConnection;
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class InitialConnectActivity extends AppCompatActivity {
    private static final int LINK_REQUEST_CODE = 1;
    private String publicToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_connect);
        mAuth = FirebaseAuth.getInstance();

        Button button = findViewById(R.id.buttonLinkBankContinue);
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
        Plaid.openLink(this, new LinkConfiguration.Builder("Crystal", products)
                        .environment(PlaidEnvironment.SANDBOX)
                        .build(), LINK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!myPlaidResultHandler.onActivityResult(requestCode, resultCode, data)) {
            Log.i(InitialConnectActivity.class.getSimpleName(), " Not handled");
        }
    }

    private PlaidLinkResultHandler myPlaidResultHandler = new PlaidLinkResultHandler(LINK_REQUEST_CODE,
            // Handle onSuccess
            // Metadata gives accountNumber if it's useful in the future
            linkConnection -> {
                LinkConnection.LinkConnectionMetadata metadata = linkConnection.getLinkConnectionMetadata();
                Log.i(InitialConnectActivity.class.getSimpleName(), getString(
                        R.string.content_success,
                        linkConnection.getPublicToken(),
                        metadata.getAccounts().get(0).getAccountId(),
                        metadata.getAccounts().get(0).getAccountName(),
                        metadata.getInstitutionId(),
                        metadata.getInstitutionName()));
                publicToken = linkConnection.getPublicToken();

                // Look for credit card accounts
                // Might want to give option to select account to add in future
                List<LinkAccount> linkAccountList = metadata.getAccounts();
                boolean hasCreditCardAccount = false;
                for (LinkAccount linkAccount : linkAccountList) {
                    if (linkAccount.getAccountSubType() != null && linkAccount.getAccountSubType().equals("credit card")) {
                        hasCreditCardAccount = true;
                        break;
                    }
                }
                // Show alert dialog if credit card account is missing
                if (hasCreditCardAccount) {
                    createAccount();
                } else {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Missing Credit Card")
                            .setMessage("Crystal requires a bank account with a credit card")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
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

    private void createAccount() {
        String email = String.valueOf(getIntent().getStringExtra("com.crystal.hello.EMAIL"));
        String password = String.valueOf(getIntent().getStringExtra("com.crystal.hello.PASSWORD"));
        Log.d(InitialConnectActivity.class.getSimpleName(), "createAccount:" + email);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(InitialConnectActivity.class.getSimpleName(), "createUserWithEmail:success");
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class)
                            .putExtra("com.crystal.hello.PUBLIC_TOKEN", publicToken);
                    startActivity(intent);
                    finish();
                } else { // Invalid email or password
                    Log.w(InitialConnectActivity.class.getSimpleName(), "createUserWithEmail:failure", task.getException());
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
