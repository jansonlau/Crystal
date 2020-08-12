package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PasswordActivity extends AppCompatActivity {
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        passwordLayout = findViewById(R.id.inputLayoutPassword);
        passwordEditText = findViewById(R.id.editTextPassword);

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText())) {
                    passwordLayout.setError(null);
                }
                return false;
            }
        });

        Button button = findViewById(R.id.buttonPasswordContinue);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PasswordActivity.this.validateForm()) {
                    return;
                }

                TextInputEditText passwordEditText = PasswordActivity.this.findViewById(R.id.editTextPassword);
                Intent intent = new Intent(PasswordActivity.this, MobileNumberActivity.class)
                        .putExtra("com.crystal.hello.FIRST_NAME", PasswordActivity.this.getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"))
                        .putExtra("com.crystal.hello.LAST_NAME", PasswordActivity.this.getIntent().getStringExtra("com.crystal.hello.LAST_NAME"))
                        .putExtra("com.crystal.hello.EMAIL", PasswordActivity.this.getIntent().getStringExtra("com.crystal.hello.EMAIL"))
                        .putExtra("com.crystal.hello.PASSWORD", String.valueOf(passwordEditText));
                PasswordActivity.this.startActivity(intent);
            }
        });
    }

    private boolean isPasswordValid(Editable text) {
        return text != null && text.length() >= 6;
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!isPasswordValid(passwordEditText.getText())) {
            passwordLayout.setError("Must have at least 6 characters");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        return valid;
    }
}
