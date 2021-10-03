package com.example.hepiplant;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.hepiplant.adapter.recyclerview.PostsRecyclerViewAdapter;
import com.example.hepiplant.adapter.pager.ForumFragmentPagerAdapter;
import com.example.hepiplant.adapter.recyclerview.SalesOffersRecyclerViewAdapter;
import com.google.android.material.tabs.TabLayout;

public class ForumTabsActivity extends AppCompatActivity
        implements PostsRecyclerViewAdapter.ItemClickListener,
        SalesOffersRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "ForumTabsActivity";
    private ViewPager viewPager;
    private ForumFragmentPagerAdapter forumFragmentStateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_tabs);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.forumViewPager);
        viewPager.setAdapter(new ForumFragmentPagerAdapter(getSupportFragmentManager(),
                this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.forumTabsLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    // todo
    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Toast.makeText(this, "You clicked item on row number " + position, Toast.LENGTH_SHORT).show();
    }
}