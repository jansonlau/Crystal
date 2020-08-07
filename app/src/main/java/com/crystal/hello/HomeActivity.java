package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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

        Bundle bundle = new Bundle();
        boolean newUserBooleanExtra = getIntent().getBooleanExtra("com.crystal.hello.CREATE_USER", false);
        bundle.putBoolean("com.crystal.hello.CREATE_USER", newUserBooleanExtra);

        Fragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutFragmentContainer, homeFragment).commit();

        publicToken = getIntent().getStringExtra("com.crystal.hello.PUBLIC_TOKEN");

        BottomNavigationView navView = findViewById(R.id.nav_view);
        setBottomNavigationItemSelectedListener(navView);
        setBottomNavigationItemReselectedListener(navView);
    }

    private void setBottomNavigationItemSelectedListener(BottomNavigationView navView) {
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_saved:
                        fragment = new SavedFragment();
                        break;
                    case R.id.navigation_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        Bundle bundle = new Bundle();
                        boolean booleanExtra = getIntent().getBooleanExtra("com.crystal.hello.CREATE_USER", false);
                        bundle.putBoolean("com.crystal.hello.CREATE_USER", booleanExtra);
                        fragment = new HomeFragment();
                        fragment.setArguments(bundle);
                        break;
                }
                HomeActivity.this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayoutFragmentContainer, fragment)
                        .commit();
                return true;
            }
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