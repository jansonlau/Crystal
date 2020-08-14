package com.crystal.hello.signup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.HomeActivity;
import com.crystal.hello.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Objects;

import kotlin.Unit;

public class InitialConnectActivity extends AppCompatActivity {
    private final int LINK_REQUEST_CODE = 1;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_connect);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Button button = findViewById(R.id.buttonLinkBankContinue);
        button.setOnClickListener(view -> InitialConnectActivity.this.createUserWithEmailAndPassword());
    }

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
    }

    private PlaidLinkResultHandler myPlaidResultHandler = new PlaidLinkResultHandler(LINK_REQUEST_CODE,
            // Handle onSuccess
            // Metadata gives accountNumber if it's useful in the future
            linkConnection -> {
                LinkConnection.LinkConnectionMetadata metadata = linkConnection.getLinkConnectionMetadata();
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
                            .putExtra("com.crystal.hello.PUBLIC_TOKEN_STRING", publicToken)
                            .putExtra("com.crystal.hello.CREATE_USER_BOOLEAN", true);
                    InitialConnectActivity.this.startActivity(intent);
                    InitialConnectActivity.this.finishAffinity();
                } else {
                    new MaterialAlertDialogBuilder(InitialConnectActivity.this)
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
                Intent intent = new Intent(InitialConnectActivity.this, HomeActivity.class);
                InitialConnectActivity.this.startActivity(intent);
                InitialConnectActivity.this.finishAffinity();
                return Unit.INSTANCE;
            },

            // Handle onExit (close button???)
            plaidApiError -> {
                Intent intent = new Intent(InitialConnectActivity.this, HomeActivity.class);
                InitialConnectActivity.this.startActivity(intent);
                InitialConnectActivity.this.finishAffinity();
                return Unit.INSTANCE;
            }
    );

    // Add account to database only if account was successfully created in Firebase Auth
    private void createUserWithEmailAndPassword() {
        final String email = String.valueOf(getIntent().getStringExtra("com.crystal.hello.EMAIL"));
        final String password = String.valueOf(getIntent().getStringExtra("com.crystal.hello.PASSWORD"));
        final String firstName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"));
        final String lastName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.LAST_NAME"));
        final String mobileNumber = String.valueOf(getIntent().getStringExtra("com.crystal.hello.MOBILE_NUMBER"));

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        openLink();
                        sendEmailVerification();
                        setUserToDatabase(email, firstName, lastName, mobileNumber);

                    } else { // Invalid email or password
                        Toast.makeText(InitialConnectActivity.this
                                , Objects.requireNonNull(task.getException()).getMessage()
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification() {
        Objects.requireNonNull(user).sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(InitialConnectActivity.this
                                , "Verification email sent to " + user.getEmail()
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Set user profile information to "users" collection with Firebase Auth Uid as document ID
    private void setUserToDatabase(String email, String firstName, String lastName, String mobileNumber) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("first", firstName);
        userData.put("last", lastName);
        userData.put("mobile", mobileNumber);

        db.collection("users")
                .document(user.getUid())
                .set(userData);
    }
}
