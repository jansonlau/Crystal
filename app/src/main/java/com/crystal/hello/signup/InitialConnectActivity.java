package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.HomeActivity;
import com.crystal.hello.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plaid.link.Plaid;
import com.plaid.link.configuration.AccountSubtype;
import com.plaid.link.configuration.LinkConfiguration;
import com.plaid.link.configuration.PlaidEnvironment;
import com.plaid.link.configuration.PlaidProduct;
import com.plaid.link.result.PlaidLinkResultHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;

public class InitialConnectActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_connect);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Plaid.initialize(getApplication());

        Button button = findViewById(R.id.buttonLinkBankContinue);
        button.setOnClickListener(view -> InitialConnectActivity.this.createUserWithEmailAndPassword());
    }

    private void openLink() {
        Plaid.openLink(this, new LinkConfiguration.Builder()
                .clientName("Crystal")
                .environment(PlaidEnvironment.DEVELOPMENT)
//                .products(Arrays.asList(PlaidProduct.TRANSACTIONS, PlaidProduct.LIABILITIES))
                .products(Collections.singletonList(PlaidProduct.TRANSACTIONS))
                .publicKey("bbf9cf93da45517aa5283841dfc534")
                .accountSubtypeFilter(Collections.singletonList(AccountSubtype.CREDIT.CREDIT_CARD.INSTANCE))
                .build());

        // TODO: Get linkToken with server
//        LinkTokenRequester.INSTANCE.getToken().subscribe(this::onLinkTokenSuccess, this::onLinkTokenError);
    }

//    private void onLinkTokenSuccess(String token) {
//        Plaid.openLink(this, new LinkConfiguration.Builder()
//                .token(token)
//                .clientName("Crystal")
//                .environment(PlaidEnvironment.DEVELOPMENT)
//                .products(Collections.singletonList(PlaidProduct.TRANSACTIONS))
////                .accountSubtypeFilter(Collections.singletonList(AccountSubtype.CREDIT.CREDIT_CARD))
//                .build());
//    }

//    private void onLinkTokenError(Throwable error) {
//        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myPlaidResultHandler.onActivityResult(requestCode, resultCode, data);
    }

    private PlaidLinkResultHandler myPlaidResultHandler = new PlaidLinkResultHandler(
            linkSuccess -> {
                String publicToken = linkSuccess.getPublicToken();

                Intent intent = new Intent(InitialConnectActivity.this, HomeActivity.class)
                        .putExtra("com.crystal.hello.PUBLIC_TOKEN_STRING", publicToken)
                        .putExtra("com.crystal.hello.CREATE_USER_BOOLEAN", true);
                InitialConnectActivity.this.startActivity(intent);
                InitialConnectActivity.this.finishAffinity();
                return Unit.INSTANCE;
            },

            linkExit -> {
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
                        final FirebaseUser user = auth.getCurrentUser();
                        sendEmailVerification(Objects.requireNonNull(user));
                        setUserToDatabase(user, email, firstName, lastName, mobileNumber);
                        openLink();
                    } else { // Invalid email or password
                        Toast.makeText(InitialConnectActivity.this
                                , Objects.requireNonNull(task.getException()).getMessage()
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(InitialConnectActivity.this
                                , "Verification email sent to " + user.getEmail()
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Set user profile information to "users" collection with Firebase Auth Uid as document ID
    private void setUserToDatabase(FirebaseUser user, String email, String firstName, String lastName, String mobileNumber) {
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
