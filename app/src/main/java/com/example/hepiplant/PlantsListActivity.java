package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.adapter.recyclerview.PlantsRecyclerViewAdapter;
import com.example.hepiplant.dto.PlantDto;
import com.google.gson.Gson;

import org.json.JSONArray;

public class PlantsListActivity extends AppCompatActivity implements PlantsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "PlantsListActivity";
    private static final String BASE_URL = "http://10.0.0.163:8080";

    private int testUserId = 1;
    private PlantsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private PlantDto[] plants = new PlantDto[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants_list);

        setBottomBarOnItemClickListeners();
        setupRecyclerView();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private void makeDataRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "/plants/user/" + testUserId;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONArray>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONArray response) {
                    onResponseReceived(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onErrorResponseReceived(error);
                }
        });
        Log.v(TAG, "Sending the request to " + url);
        queue.add(jsonArrayRequest);
    }

    private void onResponseReceived(JSONArray response){
        Log.v(TAG, "onResponseReceived()");
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

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.plantsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        makeDataRequest();
        // set up the RecyclerView
        adapter = new PlantsRecyclerViewAdapter(this, plants);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

}
