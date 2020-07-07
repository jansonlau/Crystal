package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class MobileNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number);

        Button button = findViewById(R.id.buttonMobileNumberContinue);
        button.setOnClickListener(view -> {
            TextInputEditText mobileNumberEditText = findViewById(R.id.editTextMobileNumber);

            Intent intent = new Intent(MobileNumberActivity.this, InitialConnectActivity.class)
                    .putExtra("com.crystal.hello.FIRST_NAME", getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"))
                    .putExtra("com.crystal.hello.LAST_NAME", getIntent().getStringExtra("com.crystal.hello.LAST_NAME"))
                    .putExtra("com.crystal.hello.EMAIL", getIntent().getStringExtra("com.crystal.hello.EMAIL"))
                    .putExtra("com.crystal.hello.PASSWORD", getIntent().getStringExtra("com.crystal.hello.PASSWORD"))
                    .putExtra("com.crystal.hello.MOBILE_NUMBER", String.valueOf(mobileNumberEditText.getText()));
            startActivity(intent);
            finish();
        });
    }
}
