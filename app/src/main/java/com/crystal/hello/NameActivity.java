package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        Button button = findViewById(R.id.buttonNameContinue);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(NameActivity.this, EmailActivity.class);
            startActivity(intent);
        });
    }
}
