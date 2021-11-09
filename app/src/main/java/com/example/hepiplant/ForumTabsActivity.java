package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.hepiplant.adapter.pager.ForumFragmentPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class ForumTabsActivity extends AppCompatActivity {

    private static final String TAG = "ForumTabsActivity";
    private ViewPager viewPager;
    private ForumFragmentPagerAdapter forumFragmentStateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_tabs);
        setBottomBarOnItemClickListeners();
        setupViewPager();
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.forumViewPager);
        forumFragmentStateAdapter = new ForumFragmentPagerAdapter(getSupportFragmentManager(),
                this);
        viewPager.setAdapter(forumFragmentStateAdapter);

        TabLayout tabLayout = findViewById(R.id.forumTabsLayout);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton buttonAdd = findViewById(R.id.floatingActionButton);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), QuestionActivity.class);
                startActivity(intent2);
            }
        });
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RecyclerView recyclerView = null;
                int currentItem = viewPager.getCurrentItem();
                Log.v(TAG, "Current item: "+currentItem);
                switch (currentItem){
                    case 0:
                        recyclerView = findViewById(R.id.postsRecyclerView);
                        break;
                    case 1:
                        recyclerView = findViewById(R.id.offersRecyclerView);
                        break;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        });
    }
}
