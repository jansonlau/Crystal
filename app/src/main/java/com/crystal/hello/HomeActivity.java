package com.crystal.hello;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.crystal.hello.monthlyactivity.MonthlyActivityFragment;
import com.crystal.hello.ui.home.HomeFragment;
import com.crystal.hello.ui.profile.ProfileFragment;
import com.crystal.hello.ui.saved.SavedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final String publicToken = getIntent().getStringExtra("com.crystal.hello.PUBLIC_TOKEN_STRING");
        final Bundle bundle = new Bundle();
        bundle.putString("com.crystal.hello.PUBLIC_TOKEN_STRING", publicToken);

        final Fragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentFrameLayout, homeFragment).commit();

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        setBottomNavigationItemSelectedListener(navView);
        setBottomNavigationItemReselectedListener(navView);
    }

    private void setBottomNavigationItemSelectedListener(@NotNull BottomNavigationView navView) {
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_saved:
                        fragment = new SavedFragment();
                        break;
                    case R.id.navigation_monthly:
                        fragment = new MonthlyActivityFragment();
                        break;
                    case R.id.navigation_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        fragment = new HomeFragment();
                        break;
                }

                for (int count = 0; count < getSupportFragmentManager().getBackStackEntryCount(); count++) {
                    getSupportFragmentManager().popBackStack();
                }

                HomeActivity.this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentFrameLayout, fragment)
                        .commit();
                return true;
            }
        });
    }

    private void setBottomNavigationItemReselectedListener(@NotNull BottomNavigationView navView) {
        navView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    if (item.getItemId() == R.id.navigation_home) {
                        final NestedScrollView homeFragmentNestedScrollView = findViewById(R.id.homeFragmentNestedScrollView);
                        homeFragmentNestedScrollView.smoothScrollTo(0, 0);
                    } else if (item.getItemId() == R.id.navigation_monthly) {
                        final ViewPager2 viewPager = findViewById(R.id.pager);
                        viewPager.setCurrentItem(Objects.requireNonNull(viewPager.getAdapter()).getItemCount() - 1, true);
                    }
                } else {
                    for (int count = 0; count < getSupportFragmentManager().getBackStackEntryCount(); count++) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            }
        });
    }
}