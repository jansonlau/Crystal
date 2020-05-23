package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        Button button = findViewById(R.id.button_password_continue);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(PasswordActivity.this, MobileNumberActivity.class);
            startActivity(intent);
        });
    }
}
