package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.hepiplant.adapter.pager.AdminFragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainAdminActivity extends AppCompatActivity {
    
    private static final String TAG = "MainAdminActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        setupToolbar();
        setupViewPager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoff:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.informationAboutApp:
                Intent intentInfo = new Intent(this, InfoActivity.class);
                startActivity(intentInfo);
                return true;
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.adminToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewPager() {
        ViewPager viewPager = findViewById(R.id.adminViewPager);
        AdminFragmentPagerAdapter adminFragmentStateAdapter = new AdminFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adminFragmentStateAdapter);

        TabLayout tabLayout = findViewById(R.id.adminTabsLayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        String tabTitle = getIntent().getStringExtra("tabTitle");
        int tabIndex = adminFragmentStateAdapter.getIndexByTitle(tabTitle);
        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        if (tab != null) {
            tab.select();
        }
    }
}
