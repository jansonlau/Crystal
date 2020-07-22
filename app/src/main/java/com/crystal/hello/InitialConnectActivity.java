package com.crystal.hello;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plaid.link.Plaid;
import com.plaid.linkbase.models.configuration.LinkConfiguration;
import com.plaid.linkbase.models.configuration.PlaidEnvironment;
import com.plaid.linkbase.models.configuration.PlaidProduct;
import com.plaid.linkbase.models.connection.LinkAccount;
import com.plaid.linkbase.models.connection.LinkConnection;
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class InitialConnectActivity extends AppCompatActivity {
    private final int LINK_REQUEST_CODE = 1;
    private final String TAG = InitialConnectActivity.class.getSimpleName();
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_connect);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Button button = findViewById(R.id.buttonLinkBankContinue);
        button.setOnClickListener(view -> {
            createUserWithEmailAndPassword();
//            setOptionalEventListener();
//            openLink();
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
            Log.i(TAG, " Not handled");
        }
    }

    private PlaidLinkResultHandler myPlaidResultHandler = new PlaidLinkResultHandler(LINK_REQUEST_CODE,
            // Handle onSuccess
            // Metadata gives accountNumber if it's useful in the future
            linkConnection -> {
                LinkConnection.LinkConnectionMetadata metadata = linkConnection.getLinkConnectionMetadata();
                Log.i(TAG, getString(
                        R.string.content_success,
                        linkConnection.getPublicToken(),
                        metadata.getAccounts().get(0).getAccountId(),
                        metadata.getAccounts().get(0).getAccountName(),
                        metadata.getInstitutionId(),
                        metadata.getInstitutionName()));
                String publicToken = linkConnection.getPublicToken();

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
                    Intent intent = new Intent(InitialConnectActivity.this, HomeActivity.class)
                            .putExtra("com.crystal.hello.PUBLIC_TOKEN", publicToken)
                            .putExtra("com.crystal.hello.CREATE_USER", true);
                    startActivity(intent);
                    finishAffinity();
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
                Log.i(TAG, getString(
                        R.string.content_cancelled,
                        linkCancellation.getInstitutionId(),
                        linkCancellation.getInstitutionName(),
                        linkCancellation.getLinkSessionId(),
                        linkCancellation.getStatus()));
                return Unit.INSTANCE;
            },

            // Handle onExit (close button???)
            plaidApiError -> {
                Log.i(TAG, getString(
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

    // Add account to database only if account was successfully created in Firebase Auth
    private void createUserWithEmailAndPassword() {
        String email = String.valueOf(getIntent().getStringExtra("com.crystal.hello.EMAIL"));
        String password = String.valueOf(getIntent().getStringExtra("com.crystal.hello.PASSWORD"));
        String firstName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"));
        String lastName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.LAST_NAME"));
        String mobileNumber = String.valueOf(getIntent().getStringExtra("com.crystal.hello.MOBILE_NUMBER"));

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            setOptionalEventListener();
                            openLink();
//                            sendEmailVerification();
                            setUserToDatabase(email, firstName, lastName, mobileNumber);

                        } else { // Invalid email or password
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(InitialConnectActivity.this
                                        , task.getException().getMessage()
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendEmailVerification() {
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendEmailVerification:success");
                        Toast.makeText(InitialConnectActivity.this
                                , "Verification email sent to " + user.getEmail()
                                , Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification:failure", task.getException());
                        Toast.makeText(InitialConnectActivity.this
                                , "Failed to send verification email."
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Set user profile information to "users" collection with Firebase Auth Uid as document ID
    private void setUserToDatabase(String email, String firstName, String lastName, String mobileNumber) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("first", firstName);
        userData.put("last", lastName);
        userData.put("mobile", mobileNumber);

        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .set(userData)
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
}
