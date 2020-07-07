package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class EmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        Button emailContinueButton = findViewById(R.id.buttonEmailContinue);
        emailContinueButton.setOnClickListener(view -> {
            TextInputEditText emailEditText = findViewById(R.id.editTextEmail);

            Intent intent = new Intent(EmailActivity.this, PasswordActivity.class)
                    .putExtra("com.crystal.hello.FIRST_NAME", getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"))
                    .putExtra("com.crystal.hello.LAST_NAME", getIntent().getStringExtra("com.crystal.hello.LAST_NAME"))
                    .putExtra("com.crystal.hello.EMAIL", String.valueOf(emailEditText.getText()));
            startActivity(intent);
        });
    }

//    public static boolean isValidEmail(CharSequence target) {
//        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
//    }
}
