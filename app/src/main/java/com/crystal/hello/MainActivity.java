package com.crystal.hello;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.signup.NameActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Move to Home Fragment if user already logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finishAffinity();
        }
        setContentView(R.layout.activity_main);

        final Button signUpButton = findViewById(R.id.mainSignUpButton);
        final TextView logInTextView = findViewById(R.id.mainLogInTextView);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                signUpButton.setTextColor(Color.WHITE);
                logInTextView.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                signUpButton.setTextColor(Color.BLACK);
                logInTextView.setTextColor(Color.WHITE);
                break;
        }

        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NameActivity.class);
            startActivity(intent);
        });

        logInTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
