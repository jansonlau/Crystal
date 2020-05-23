package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MobileNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number);

        Button button = findViewById(R.id.button_mobile_number_continue);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(MobileNumberActivity.this, InitialConnectActivity.class);
            startActivity(intent);
        });
    }
}
