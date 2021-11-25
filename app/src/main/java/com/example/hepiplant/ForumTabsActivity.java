package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
        Toolbar toolbar = findViewById(R.id.forumToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.forumViewPager);
        forumFragmentStateAdapter = new ForumFragmentPagerAdapter(getSupportFragmentManager(),
                this);
        viewPager.setAdapter(forumFragmentStateAdapter);

        TabLayout tabLayout = findViewById(R.id.forumTabsLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(v -> {
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
        });
    }

    private void setFloatingButtonOnItemClickListener(){
        FloatingActionButton buttonAdd = findViewById(R.id.floatingActionButton);
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
            startActivity(intent);
        });
    }
}
