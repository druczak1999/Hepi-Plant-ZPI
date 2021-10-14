package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.hepiplant.adapter.recyclerview.PlantsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.PlantDto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;

public class PlantsListActivity extends AppCompatActivity implements PlantsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "PlantsListActivity";

    private Configuration config;
    private PlantsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private PlantDto[] plants = new PlantDto[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants_list);

        config = (Configuration) getApplicationContext();

        initView();
        setLayoutManager();
        setAdapter();
        makeGetDataRequest();
        setBottomBarOnItemClickListeners();
        setupToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(this,PlantViewActivity.class);
        intent.putExtra("plantName",plants[position].getName());
        if(plants[position].getSpecies()!=null){
            intent.putExtra("species",plants[position].getSpecies().getName());
            intent.putExtra("soil",plants[position].getSpecies().getSoil());
            intent.putExtra("placement",plants[position].getSpecies().getPlacement().toString());
        }
        else{
            intent.putExtra("species","");
            intent.putExtra("soil","");
            intent.putExtra("placement","");
        }
        intent.putExtra("watering",String.valueOf(plants[position].getSchedule().getWateringFrequency()));
        intent.putExtra("fertilizing",String.valueOf(plants[position].getSchedule().getFertilizingFrequency()));
        intent.putExtra("misting",String.valueOf(plants[position].getSchedule().getMistingFrequency()));
        if(plants[position].getCategoryId()!=null)
        intent.putExtra("category",plants[position].getCategoryId().toString());
        else intent.putExtra("category","");
        if(plants[position].getLocation()!=null)
        intent.putExtra("location",plants[position].getLocation());
        else intent.putExtra("location","");
        if(plants[position].getPurchaseDate()!=null)
        intent.putExtra("date",plants[position].getPurchaseDate());
        else intent.putExtra("date","");
        if(plants[position].getPhoto()!=null)
        intent.putExtra("photo", plants[position].getPhoto());
        else intent.putExtra("photo", "");
        startActivity(intent);
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
            case R.id.wyloguj:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.infoMenu:
                Toast.makeText(this.getApplicationContext(),"Informacje",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void makeGetDataRequest(){
        String url = getRequestUrl();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONArray>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONArray response) {
                    onGetResponseReceived(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onErrorResponseReceived(error);
                }
        });
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonArrayRequest);
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "plants/user/" + config.getUserId();
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        Gson gson = new Gson();
        plants = gson.fromJson(String.valueOf(response), PlantDto[].class);
        adapter.updateData(plants);
        adapter.notifyItemRangeChanged(0, plants.length);
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void initView() {
        recyclerView = findViewById(R.id.plantsRecyclerView);
    }

    private void setLayoutManager() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setAdapter() {
        adapter = new PlantsRecyclerViewAdapter(this, plants);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = (Button) findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);;
            }
        });

        Button buttonForum = (Button) findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.plantsListToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlantAddActivity.class);
                startActivity(intent);
            }
        });
    }

}
