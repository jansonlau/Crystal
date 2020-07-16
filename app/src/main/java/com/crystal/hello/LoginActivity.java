package com.crystal.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameLayout = findViewById(R.id.layoutLoginUsername);
        passwordLayout = findViewById(R.id.layoutLoginPassword);
        usernameEditText = findViewById(R.id.editTextLoginUsername);
        passwordEditText = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.buttonLogin);
        mAuth = FirebaseAuth.getInstance();
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

        // Clear the error once more than 8 characters are typed.
        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText())) {
                    passwordLayout.setError(null);
                }
                return false;
            }
        });

        // Set an error if the password is less than 8 characters.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(String.valueOf(usernameEditText.getText()), String.valueOf(passwordEditText.getText()));
            }
        });
    }

    private boolean isPasswordValid(Editable text) {
        return text != null && text.length() >= 8;
    }

    private boolean isUsernameValid(Editable text) {
        return text != null && text.length() >= 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!isPasswordValid(passwordEditText.getText())) {
            passwordLayout.setError("Must have at least 8 characters");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!isUsernameValid(usernameEditText.getText())) {
            usernameLayout.setError("Invalid email address");
            valid = false;
        } else {
            usernameLayout.setError(null);
        }

        return valid;
    }

    private void signIn(String email, String password) {
        Log.d(LoginActivity.class.getSimpleName(), "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LoginActivity.class.getSimpleName(), "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser(); // TODO: Start HomeActivity when logged in

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LoginActivity.class.getSimpleName(), "signInWithEmail:failure", task.getException());
                    if (task.getException() != null) {
                        Toast.makeText(LoginActivity.this, "Your username or password was incorrect.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}