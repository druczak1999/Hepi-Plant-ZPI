package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.hepiplant.adapter.pager.TagTabsFragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class TagTabsAdminActivity extends AppCompatActivity {

    private static final String TAG = "TagTabsAdminActivity";

    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_tabs_admin);
        tag = getIntent().getExtras().getString("tag");
        setupToolbar();
        setupPageTitle();
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
        switch (item.getItemId()) {
            case R.id.logoff:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.informationAboutApp:
                Intent intentInfo = new Intent(this, InfoActivity.class);
                startActivity(intentInfo);
                return true;
            case R.id.miProfile:
                Intent intent2 = new Intent(this, UserActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.tagsToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupPageTitle() {
        TextView titleTextView = findViewById(R.id.tagTabsTitleTextView);
        String titleString = getText(R.string.tag_tabs_title) + " #" + tag;
        titleTextView.setText(titleString);
    }

    private void setupViewPager() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.tagViewPager);
        TagTabsFragmentPagerAdapter tagTabsFragmentPagerAdapter = new TagTabsFragmentPagerAdapter(getSupportFragmentManager(),
                this, tag);
        viewPager.setAdapter(tagTabsFragmentPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tagTabsLayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
