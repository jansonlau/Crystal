package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        Button nameContinueButton = findViewById(R.id.buttonNameContinue);
        nameContinueButton.setOnClickListener(view -> {
            TextInputEditText firstNameEditText = findViewById(R.id.editTextFirstName);
            TextInputEditText lastNameEditText = findViewById(R.id.editTextLastName);

            Intent intent = new Intent(NameActivity.this, EmailActivity.class)
                    .putExtra("com.crystal.hello.FIRST_NAME", String.valueOf(firstNameEditText.getText()))
                    .putExtra("com.crystal.hello.LAST_NAME", String.valueOf(lastNameEditText.getText()));
            startActivity(intent);
        });
    }
}
