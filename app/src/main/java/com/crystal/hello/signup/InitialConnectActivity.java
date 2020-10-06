package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.HomeActivity;
import com.crystal.hello.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.plaid.link.Plaid;
import com.plaid.link.PlaidHandler;
import com.plaid.link.configuration.LinkPublicKeyConfiguration;
import com.plaid.link.configuration.PlaidEnvironment;
import com.plaid.link.configuration.PlaidProduct;
import com.plaid.link.result.LinkAccountSubtype;
import com.plaid.link.result.LinkResultHandler;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;

public class InitialConnectActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private PlaidHandler plaidHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_connect);
        createPlaidHandler();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final Button button = findViewById(R.id.buttonLinkBankContinue);
        button.setOnClickListener(view -> openPlaidLink());
    }

    private void createPlaidHandler() {
        plaidHandler = Plaid.create(getApplication(), new LinkPublicKeyConfiguration.Builder()
                .clientName("Crystal")
                .environment(PlaidEnvironment.DEVELOPMENT)
//                .products(Arrays.asList(PlaidProduct.TRANSACTIONS, PlaidProduct.LIABILITIES))
                .products(Collections.singletonList(PlaidProduct.TRANSACTIONS))
                .publicKey("bbf9cf93da45517aa5283841dfc534")
                .accountSubtypes(Collections.singletonList(LinkAccountSubtype.CREDIT.CREDIT_CARD.INSTANCE))
                .build());
    }

    private void openPlaidLink() {
        plaidHandler.open(this);
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

    private final LinkResultHandler myPlaidResultHandler = new LinkResultHandler(
            linkSuccess -> {
                findViewById(R.id.text_finish_sign_up).setVisibility(View.GONE);
                findViewById(R.id.text_link_bank_caption).setVisibility(View.GONE);
                findViewById(R.id.buttonLinkBankContinue).setVisibility(View.GONE);
                findViewById(R.id.initialConnectProgressBar).setVisibility(View.VISIBLE);

                final String publicToken = linkSuccess.getPublicToken();
                createUserWithEmailAndPassword(publicToken);
                return Unit.INSTANCE;
            },

            linkExit -> {
                createUserWithEmailAndPassword(null);
                return Unit.INSTANCE;
            }
    );

    // Move to HomeActivity
    private void createUserWithEmailAndPassword(final String publicToken) {
        final String email = String.valueOf(getIntent().getStringExtra("com.crystal.hello.EMAIL"));
        final String password = String.valueOf(getIntent().getStringExtra("com.crystal.hello.PASSWORD"));
        final String firstName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"));
        final String lastName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.LAST_NAME"));
        final String mobileNumber = String.valueOf(getIntent().getStringExtra("com.crystal.hello.MOBILE_NUMBER"));
        final Intent intent = new Intent(this, HomeActivity.class);

        if (publicToken != null) {
            intent.putExtra("com.crystal.hello.PUBLIC_TOKEN_STRING", publicToken);
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(intent);
                        finishAffinity();

                        final FirebaseUser user = auth.getCurrentUser();
                        sendEmailVerification(Objects.requireNonNull(user));
                        setUserToDatabase(user, email, firstName, lastName, mobileNumber);
                    } else { // Invalid email or password
                        Toast.makeText(InitialConnectActivity.this
                                , Objects.requireNonNull(task.getException()).getMessage()
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification(@NotNull FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(InitialConnectActivity.this
                                , "Verification email sent to ".concat(Objects.requireNonNull(user.getEmail()))
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Set user profile information to "users" collection with Firebase Auth Uid as document ID
    private void setUserToDatabase(@NotNull FirebaseUser user, String email, String firstName, String lastName, String mobileNumber) {
        // Set default budget values
        final Map<String, Integer> budgets = new HashMap<>();
        budgets.put("travel"        , 100);
        budgets.put("health"        , 100);
        budgets.put("shopping"      , 100);
        budgets.put("services"      , 100);
        budgets.put("foodDrinks"    , 100);
        budgets.put("entertainment" , 100);

        final Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("first", firstName);
        userData.put("last", lastName);
        userData.put("mobile", mobileNumber);

        final DocumentReference docRef = db.collection("users").document(user.getUid());
        final DocumentReference budgetRef = docRef.collection("profile").document("budgets");
        final DocumentReference userRef = docRef.collection("profile").document("user");
        final WriteBatch batch = db.batch();

        batch.set(budgetRef, budgets, SetOptions.merge())
                .set(userRef, userData, SetOptions.merge())
                .commit();
    }
}
