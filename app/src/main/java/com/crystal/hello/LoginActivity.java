package com.crystal.hello;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextView forgotPasswordButton;
    private Button loginButton;
    private ProgressBar loginProgressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameLayout = findViewById(R.id.layoutLoginUsername);
        passwordLayout = findViewById(R.id.layoutLoginPassword);
        usernameEditText = findViewById(R.id.editTextLoginUsername);
        passwordEditText = findViewById(R.id.editTextLoginPassword);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        loginButton = findViewById(R.id.buttonLogin);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        auth = FirebaseAuth.getInstance();
        setListeners();
    }

    private void setListeners() {
        usernameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isUsernameValid(usernameEditText.getText())) {
                    usernameLayout.setError(null);
                }
                return false;
            }
        });

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText())) {
                    passwordLayout.setError(null);
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                fetchSignInMethodsForEmail(String.valueOf(usernameEditText.getText()).trim(), String.valueOf(passwordEditText.getText()));
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                final Editable emailAddress = usernameEditText.getText();
                passwordLayout.setError(null);

                if (!isUsernameValid(emailAddress)) {
                    usernameLayout.setError("Please enter a valid email.");
                    return;
                }

                showProgressBar(true);
                usernameLayout.setError(null);
                auth.fetchSignInMethodsForEmail(emailAddress.toString())
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (task.isSuccessful()) {
                                    final SignInMethodQueryResult result = task.getResult();
                                    final List<String> signInMethods = Objects.requireNonNull(result).getSignInMethods();

                                    // Check if email exists in Firebase
                                    if (Objects.requireNonNull(signInMethods).contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                        auth.sendPasswordResetEmail(emailAddress.toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        showProgressBar(false);
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(LoginActivity.this
                                                                    , "Password reset email sent."
                                                                    , Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        showProgressBar(false);
                                        usernameLayout.setError("The email you entered doesn't belong to an account.");
                                    }
                                }
                            }
                        });
            }
        });
    }

    private boolean isPasswordValid(Editable text) {
        return text != null && text.length() >= 6;
    }

    private boolean isUsernameValid(Editable text) {
        return text != null && text.length() >= 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!isPasswordValid(passwordEditText.getText())) {
            passwordLayout.setError("Must have at least 6 characters.");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!isUsernameValid(usernameEditText.getText())) {
            usernameLayout.setError("Please enter a valid email.");
            valid = false;
        } else {
            usernameLayout.setError(null);
        }

        return valid;
    }

    // Sign in user and start HomeActivity
    private void fetchSignInMethodsForEmail(String email, String password) {
        if (!validateForm()) {
            return;
        }
        showProgressBar(true);

        // Check if email exists
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            final SignInMethodQueryResult result = task.getResult();
                            final List<String> signInMethods = Objects.requireNonNull(result).getSignInMethods();

                            if (Objects.requireNonNull(signInMethods).contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                signIn(email, password);
                            } else {
                                showProgressBar(false);
                                usernameLayout.setError("The email you entered doesn't belong to an account.");
                            }
                        }
                    }
                });
    }

    // Check if password is correct
    private void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    final Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finishAffinity();
                } else {
                    showProgressBar(false);
                    passwordLayout.setError("Sorry, your password was incorrect.");
                }
            }
        });
    }

    private void showProgressBar(boolean progressBarVisible) {
        if (progressBarVisible) {
            usernameLayout.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.GONE);
            loginProgressBar.setVisibility(View.VISIBLE);
        } else {
            usernameLayout.setVisibility(View.VISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            loginProgressBar.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard(@NotNull final View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}