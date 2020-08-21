package com.crystal.hello;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.crystal.hello.ui.home.HomeFragment;
import com.crystal.hello.ui.profile.ProfileFragment;
import com.crystal.hello.ui.saved.SavedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final boolean newUserBooleanExtra = getIntent().getBooleanExtra("com.crystal.hello.CREATE_USER_BOOLEAN", false);
        final String publicToken = getIntent().getStringExtra("com.crystal.hello.PUBLIC_TOKEN_STRING");

        final Bundle bundle = new Bundle();
        bundle.putBoolean("com.crystal.hello.CREATE_USER_BOOLEAN", newUserBooleanExtra);
        bundle.putString("com.crystal.hello.PUBLIC_TOKEN_STRING", publicToken);

        final Fragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutFragmentContainer, homeFragment).commit();

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        setBottomNavigationItemSelectedListener(navView);
        setBottomNavigationItemReselectedListener(navView);
    }

    private void setBottomNavigationItemSelectedListener(BottomNavigationView navView) {
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_saved:
                        fragment = new SavedFragment();
                        break;
                    case R.id.navigation_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        final Bundle bundle = new Bundle();
                        boolean booleanExtra = getIntent().getBooleanExtra("com.crystal.hello.CREATE_USER_BOOLEAN", false);
                        bundle.putBoolean("com.crystal.hello.CREATE_USER_BOOLEAN", booleanExtra);

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
        navView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (item.getItemId() == R.id.navigation_home) {
                    NestedScrollView nestedScrollView = HomeActivity.this.findViewById(R.id.nestedScrollViewHome);
                    nestedScrollView.smoothScrollTo(0, 0);
                }
            }
        });
    }
}