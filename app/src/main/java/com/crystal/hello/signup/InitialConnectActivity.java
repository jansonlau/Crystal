package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.HomeActivity;
import com.crystal.hello.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.plaid.link.Plaid;
import com.plaid.link.configuration.LinkTokenConfiguration;
import com.plaid.link.result.LinkResultHandler;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

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

        final Button button = findViewById(R.id.buttonLinkBankContinue);
        button.setOnClickListener(view -> {
            final String randomString = RandomStringUtils.randomAlphanumeric(28);
            getLinkToken(randomString);
        });
    }

    private void getLinkToken(final String userId) {
        final Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("getLinkToken")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {
                        final Map<String, Object> result = (Map<String, Object>) Objects.requireNonNull(task.getResult()).getData();
                        final String linkToken = (String) Objects.requireNonNull(result).get("linkToken");

                        // Open Plaid link
                        Plaid.create(getApplication(), new LinkTokenConfiguration.Builder()
                                .token(Objects.requireNonNull(linkToken))
                                .build())
                                .open(InitialConnectActivity.this);

                        return linkToken;
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myPlaidResultHandler.onActivityResult(requestCode, resultCode, data);
    }

    private final LinkResultHandler myPlaidResultHandler = new LinkResultHandler(
            linkSuccess -> {
                final String publicToken = linkSuccess.getPublicToken();
                createUserWithEmailAndPassword(publicToken);
                showProgressBar();
                return Unit.INSTANCE;
            },

            linkExit -> {
                createUserWithEmailAndPassword(null);
                showProgressBar();
                return Unit.INSTANCE;
            }
    );

    // Move to HomeActivity
    private void createUserWithEmailAndPassword(final String publicToken) {
        final String email = String.valueOf(getIntent().getStringExtra("com.crystal.hello.EMAIL")).trim();
        final String password = String.valueOf(getIntent().getStringExtra("com.crystal.hello.PASSWORD"));
        final String firstName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.FIRST_NAME")).trim();
        final String lastName = String.valueOf(getIntent().getStringExtra("com.crystal.hello.LAST_NAME")).trim();
        final String mobileNumber = String.valueOf(getIntent().getStringExtra("com.crystal.hello.MOBILE_NUMBER")).trim();
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

    private void showProgressBar() {
        findViewById(R.id.text_finish_sign_up).setVisibility(View.GONE);
        findViewById(R.id.text_link_bank_caption).setVisibility(View.GONE);
        findViewById(R.id.buttonLinkBankContinue).setVisibility(View.GONE);
        findViewById(R.id.initialConnectProgressBar).setVisibility(View.VISIBLE);
    }
}
