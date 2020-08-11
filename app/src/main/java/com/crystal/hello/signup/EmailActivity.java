package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;
import java.util.Objects;

public class EmailActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.editTextEmail);
        emailInputLayout = findViewById(R.id.inputLayoutEmail);
        setListeners();
    }

    private void setListeners() {
        emailEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isEmailValid(emailEditText.getText())) {
                    emailInputLayout.setError(null);
                }
                return false;
            }
        });

        Button emailContinueButton = findViewById(R.id.buttonEmailContinue);
        emailContinueButton.setOnClickListener(view -> {
            if (!validateForm()) {
                return;
            }
            fetchSignInMethodsForEmail();
        });
    }

    private void fetchSignInMethodsForEmail() {
        String email = String.valueOf(emailEditText.getText());
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = Objects.requireNonNull(result).getSignInMethods();

                            if (Objects.requireNonNull(signInMethods).contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                emailInputLayout.setError("Email address already exists");
                            } else {
                                Intent intent = new Intent(EmailActivity.this, PasswordActivity.class)
                                        .putExtra("com.crystal.hello.FIRST_NAME", getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"))
                                        .putExtra("com.crystal.hello.LAST_NAME", getIntent().getStringExtra("com.crystal.hello.LAST_NAME"))
                                        .putExtra("com.crystal.hello.EMAIL", email);
                                startActivity(intent);
                            }
                        } else {
                            Log.e(EmailActivity.class.getSimpleName(), "Error getting sign in methods for user", task.getException());
                        }
                    }
                });
    }

    private boolean isEmailValid(Editable text) {
        return text != null && text.length() >= 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!isEmailValid(emailEditText.getText())) {
            emailInputLayout.setError("Please enter a valid email.");
            valid = false;
        } else {
            emailInputLayout.setError(null);
        }

        return valid;
    }
}
