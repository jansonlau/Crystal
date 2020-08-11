package com.crystal.hello;

import android.content.Intent;
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finishAffinity();
        }
        setContentView(R.layout.activity_main);

        Button signUp = findViewById(R.id.button_sign_up);
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NameActivity.class);
            startActivity(intent);
        });

        TextView logIn = findViewById(R.id.text_log_in);
        logIn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
