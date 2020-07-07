package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class PasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        Button button = findViewById(R.id.buttonPasswordContinue);
        button.setOnClickListener(view -> {
            TextInputEditText passwordEditText = findViewById(R.id.editTextPassword);

            Intent intent = new Intent(PasswordActivity.this, MobileNumberActivity.class)
                    .putExtra("com.crystal.hello.FIRST_NAME", getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"))
                    .putExtra("com.crystal.hello.LAST_NAME", getIntent().getStringExtra("com.crystal.hello.LAST_NAME"))
                    .putExtra("com.crystal.hello.EMAIL", getIntent().getStringExtra("com.crystal.hello.EMAIL"))
                    .putExtra("com.crystal.hello.PASSWORD", String.valueOf(passwordEditText.getText()));
            startActivity(intent);
        });
    }
}
