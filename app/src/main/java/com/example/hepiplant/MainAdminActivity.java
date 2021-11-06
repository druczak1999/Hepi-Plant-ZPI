package com.example.hepiplant;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.hepiplant.adapter.pager.AdminFragmentPagerAdapter;
import com.example.hepiplant.adapter.recyclerview.CategoriesRecyclerViewAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainAdminActivity extends AppCompatActivity {
    
    private static final String TAG = "MainAdminActivity";
    private ViewPager viewPager;
    private AdminFragmentPagerAdapter adminFragmentStateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        
        setupViewPager();
    }

    private void setupViewPager() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.adminViewPager);
        adminFragmentStateAdapter = new AdminFragmentPagerAdapter(getSupportFragmentManager(),
                this);
        viewPager.setAdapter(adminFragmentStateAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.adminTabsLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

}