package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.ui.home.HomeFragment;
import com.crystal.hello.ui.profile.ProfileFragment;
import com.crystal.hello.ui.saved.SavedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class HomeActivity extends AppCompatActivity {
    public static String publicToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutFragmentContainer, new HomeFragment()).commit();

        publicToken = getIntent().getStringExtra("com.crystal.hello.PUBLIC_TOKEN");

        BottomNavigationView navView = findViewById(R.id.nav_view);
        setBottomNavigationItemSelectedListener(navView);
        setBottomNavigationItemReselectedListener(navView);
    }

    private void setBottomNavigationItemSelectedListener(BottomNavigationView navView) {
        navView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_saved:
                    fragment = new SavedFragment();
                    break;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    break;
                default:
                    fragment = new HomeFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutFragmentContainer, fragment).commit();
            return true;
        });
    }

    private void setBottomNavigationItemReselectedListener(BottomNavigationView navView) {
        navView.setOnNavigationItemReselectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollViewHome);
                nestedScrollView.smoothScrollTo(0, 0);
            }
        });
    }
}