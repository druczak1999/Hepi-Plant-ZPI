package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class InfoActivity extends AppCompatActivity {

    private static final String TAG = "PostsListSimpleFragment";
    public static final int APPROXIMATE_LIST_ITEM_HEIGHT = 120;

    private ListView appAuthorsListView;
    private ListView iconAuthorsListView;
    private String[] appAuthors;
    private String[] iconAuthors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setupToolbar();
        setTitleAndData();
        initView();
        setAdapter();
        setListHeight(appAuthorsListView, appAuthors.length);
        setListHeight(iconAuthorsListView, iconAuthors.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoff:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
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
        Toolbar toolbar = findViewById(R.id.infoToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setTitleAndData() {
        appAuthors = getResources().getStringArray(R.array.app_authors);
        iconAuthors = getResources().getStringArray(R.array.icon_authors);
        TextView copyrightTextView = findViewById(R.id.infoCopyrightTextView);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String copyrightText = getText(R.string.app_copyright).toString().replace("YEAR", String.valueOf(currentYear));
        copyrightTextView.setText(copyrightText);
    }

    private void initView() {
        appAuthorsListView = findViewById(R.id.infoAuthorsListView);
        iconAuthorsListView = findViewById(R.id.infoIconAuthorsListView);
    }

    private void setAdapter() {
        appAuthorsListView.setAdapter(new ArrayAdapter<>(this, R.layout.simple_row_item, appAuthors));
        iconAuthorsListView.setAdapter(new ArrayAdapter<>(this, R.layout.simple_row_item, iconAuthors));
    }

    private void setListHeight(ListView listView, int objectsCount){
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = APPROXIMATE_LIST_ITEM_HEIGHT * objectsCount;
        listView.setLayoutParams(layoutParams);
        listView.setEnabled(false);
    }
}
