package com.crystal.hello.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MobileNumberActivity extends AppCompatActivity {
    private TextInputEditText mobileEditText;
    private TextInputLayout mobileInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number);

        mobileEditText = findViewById(R.id.editTextMobileNumber);
        mobileInputLayout = findViewById(R.id.inputLayoutMobileNumber);

        mobileEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isMobileNumberValid(mobileEditText.getText())) {
                    mobileInputLayout.setError(null);
                }
                return false;
            }
        });

        Button button = findViewById(R.id.buttonMobileNumberContinue);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MobileNumberActivity.this.validateForm()) {
                    return;
                }

                Intent intent = new Intent(MobileNumberActivity.this, InitialConnectActivity.class)
                        .putExtra("com.crystal.hello.FIRST_NAME", MobileNumberActivity.this.getIntent().getStringExtra("com.crystal.hello.FIRST_NAME"))
                        .putExtra("com.crystal.hello.LAST_NAME", MobileNumberActivity.this.getIntent().getStringExtra("com.crystal.hello.LAST_NAME"))
                        .putExtra("com.crystal.hello.EMAIL", MobileNumberActivity.this.getIntent().getStringExtra("com.crystal.hello.EMAIL"))
                        .putExtra("com.crystal.hello.PASSWORD", MobileNumberActivity.this.getIntent().getStringExtra("com.crystal.hello.PASSWORD"))
                        .putExtra("com.crystal.hello.MOBILE_NUMBER", String.valueOf(mobileEditText.getText()));
                MobileNumberActivity.this.startActivity(intent);
            }
        });
    }

    private boolean isMobileNumberValid(Editable text) {
        return text != null && text.length() >= 10 && Patterns.PHONE.matcher(text).matches();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!isMobileNumberValid(mobileEditText.getText())) {
            mobileInputLayout.setError("Please enter a valid number.");
            valid = false;
        } else {
            mobileInputLayout.setError(null);
        }

        return valid;
    }
}
