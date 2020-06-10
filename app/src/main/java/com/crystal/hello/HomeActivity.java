package com.crystal.hello;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
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
        BottomNavigationView navView = findViewById(R.id.nav_view);
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_saved, R.id.navigation_profile)
//                .build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        Intent intent = getIntent();
        publicToken = intent.getStringExtra(Intent.EXTRA_TEXT);

//        navView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RecyclerView recyclerView = findViewById(R.id.recycler_home);
//                recyclerView.smoothScrollToPosition(0);
//            }
//        });

//        setFragmentListener(navView);
    }

    // Not sure if it's needed
//    private void setFragmentListener(BottomNavigationView navView) {
//        final FragmentManager fragmentManager = getSupportFragmentManager();
//        final Fragment homeFragment = new HomeFragment();
//        final Fragment savedFragment = new SavedFragment();
//        final Fragment profileFragment = new ProfileFragment();
//
//        // handle navigation selection
//        navView.setOnNavigationItemSelectedListener(
//                new BottomNavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NotNull MenuItem item) {
//                        Fragment fragment;
//                        switch (item.getItemId()) {
//                            case R.id.navigation_home:
//                                fragment = homeFragment;
//                                break;
//                            case R.id.navigation_saved:
//                                fragment = savedFragment;
//                                break;
//                            case R.id.navigation_profile:
//                            default:
//                                fragment = profileFragment;
//                                break;
//                        }
//                        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
//                        return true;
//                    }
//                });
//    }
}