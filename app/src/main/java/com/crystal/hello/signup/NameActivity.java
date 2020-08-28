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

public class NameActivity extends AppCompatActivity {
    private TextInputEditText firstNameEditText;
    private TextInputLayout firstNameInputLayout;
    private TextInputEditText lastNameEditText;
    private TextInputLayout lastNameInputLayout;
    private Button nameContinueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        firstNameInputLayout = findViewById(R.id.firstNameInputLayout);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        lastNameInputLayout = findViewById(R.id.lastNameInputLayout);
        nameContinueButton = findViewById(R.id.buttonNameContinue);
        setListeners();
    }

    private void setListeners() {
        firstNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isFirstNameValid(firstNameEditText.getText())) {
                    firstNameInputLayout.setError(null);
                }
                return false;
            }
        });

        lastNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isLastNameValid(lastNameEditText.getText())) {
                    lastNameInputLayout.setError(null);
                }
                return false;
            }
        });

        nameContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()) {
                    return;
                }

                TextInputEditText firstNameEditText = NameActivity.this.findViewById(R.id.firstNameEditText);
                TextInputEditText lastNameEditText = NameActivity.this.findViewById(R.id.lastNameEditText);

                Intent intent = new Intent(NameActivity.this, EmailActivity.class)
                        .putExtra("com.crystal.hello.FIRST_NAME", String.valueOf(firstNameEditText.getText()))
                        .putExtra("com.crystal.hello.LAST_NAME", String.valueOf(lastNameEditText.getText()));
                NameActivity.this.startActivity(intent);
            }
        });
    }

    private boolean isFirstNameValid(Editable text) {
        return text != null && text.length() >= 1;
    }

    private boolean isLastNameValid(Editable text) {
        return text != null && text.length() >= 1;
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!isFirstNameValid(firstNameEditText.getText())) {
            firstNameInputLayout.setError("Must have at least 1 character.");
            valid = false;
        } else {
            firstNameInputLayout.setError(null);
        }

        if (!isLastNameValid(lastNameEditText.getText())) {
            lastNameInputLayout.setError("Must have at least 1 character.");
            valid = false;
        } else {
            lastNameInputLayout.setError(null);
        }

        return valid;
    }
}
