package com.example.sabzazaar.activities.main;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sabzazaar.R;
import com.example.sabzazaar.fragments.CommunityFragment;
import com.example.sabzazaar.fragments.HomeFragment;
import com.example.sabzazaar.fragments.MoreFragment;
import com.example.sabzazaar.fragments.ScanFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.sabzazaar.utils.LocaleHelper.setLocaleFromPreferences(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Force green active indicator — XML attribute causes build errors on some AGP versions
        bottomNav.setItemActiveIndicatorColor(
                ColorStateList.valueOf(Color.parseColor("#A5D6A7")));

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_scan) {
                selectedFragment = new ScanFragment();
            } else if (itemId == R.id.navigation_community) {
                selectedFragment = new CommunityFragment();
            } else if (itemId == R.id.navigation_more) {
                selectedFragment = new MoreFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}
