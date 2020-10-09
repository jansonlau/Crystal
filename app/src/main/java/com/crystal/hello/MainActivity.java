package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.crystal.hello.signup.NameActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();

        // Move to Home Fragment if user already logged in
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finishAffinity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button signUpButton = findViewById(R.id.mainSignUpButton);
        final TextView logInButton = findViewById(R.id.mainLogInButton);

        signUpButton.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, NameActivity.class);
            startActivity(intent);
        });

        logInButton.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
