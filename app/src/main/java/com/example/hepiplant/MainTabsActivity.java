package com.example.hepiplant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.hepiplant.adapter.pager.MainFragmentPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainTabsActivity extends AppCompatActivity {

    private static final String TAG = "MainTabsActivity";
    private ViewPager viewPager;
    private MainFragmentPagerAdapter mainFragmentStateAdapter;
    private int tab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);
        setBottomBarOnItemClickListeners();
        setupViewPager();
    }

    private void setupViewPager() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mainFragmentStateAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(),
                this);
        viewPager.setAdapter(mainFragmentStateAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabsLayout);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), PlantAddActivity.class);
                startActivity(intent2);
            }
        });
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = (Button) findViewById(R.id.buttonForum);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            }
        });

        Button buttonForum = (Button) findViewById(R.id.buttonDom);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RecyclerView recyclerView = null;
                RecyclerView recyclerView1 = null;
                int currentItem = viewPager.getCurrentItem();
                Log.v(TAG, "Current item: "+currentItem);
                switch (currentItem){
                    case 0:
                        recyclerView = findViewById(R.id.plantsRecyclerView);
                        break;
                    case 1:
                        recyclerView =  findViewById(R.id.eventsRecyclerView);
                        recyclerView1 = findViewById(R.id.eventsArchRecyclerView);
                        break;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        });
    }
}