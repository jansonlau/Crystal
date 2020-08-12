package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NameActivity extends AppCompatActivity {
    private TextInputEditText firstNameEditText;
    private TextInputLayout firstNameInputLayout;
    private TextInputEditText lastNameEditText;
    private TextInputLayout lastNameInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        firstNameInputLayout = findViewById(R.id.firstNameInputLayout);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        lastNameInputLayout = findViewById(R.id.lastNameInputLayout);

        Button nameContinueButton = findViewById(R.id.buttonNameContinue);
        nameContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText firstNameEditText = NameActivity.this.findViewById(R.id.firstNameEditText);
                TextInputEditText lastNameEditText = NameActivity.this.findViewById(R.id.lastNameEditText);

                Intent intent = new Intent(NameActivity.this, EmailActivity.class)
                        .putExtra("com.crystal.hello.FIRST_NAME", String.valueOf(firstNameEditText.getText()))
                        .putExtra("com.crystal.hello.LAST_NAME", String.valueOf(lastNameEditText.getText()));
                NameActivity.this.startActivity(intent);
            }
        });
    }
}
