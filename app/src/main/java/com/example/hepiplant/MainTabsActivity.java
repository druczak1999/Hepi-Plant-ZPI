package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.hepiplant.adapter.pager.MainFragmentPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainTabsActivity extends AppCompatActivity {

    private static final String TAG = "MainTabsActivity";
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);
        setupToolbar();
        setBottomBarOnItemClickListeners();
        setFloatingButtonOnItemClickListener();
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
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        setUpFloatingButton();
    }

    private void setUpFloatingButton() {
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlantAddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupViewPager() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = findViewById(R.id.mainViewPager);
        MainFragmentPagerAdapter mainFragmentStateAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(),
                this);
        viewPager.setAdapter(mainFragmentStateAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.mainTabsLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(v -> {
            RecyclerView recyclerView = null;
            int currentItem = viewPager.getCurrentItem();
            Log.v(TAG, "Current item: "+currentItem);
            switch (currentItem){
                case 0:
                    recyclerView = findViewById(R.id.plantsRecyclerView);
                    break;
                case 1:
                    recyclerView =  findViewById(R.id.eventsRecyclerView);
                    break;
            }
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
        });
    }

    private void setFloatingButtonOnItemClickListener(){
        FloatingActionButton buttonAdd = findViewById(R.id.floatingActionButton);
        buttonAdd.setOnClickListener(v -> {
            Intent intent2 = new Intent(getApplicationContext(), PlantAddActivity.class);
            startActivity(intent2);
        });
    }

}
