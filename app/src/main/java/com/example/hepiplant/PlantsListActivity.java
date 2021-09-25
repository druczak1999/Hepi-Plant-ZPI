package com.example.hepiplant;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.hepiplant.adapter.CustomPlantsRecyclerViewAdapter;
import com.example.hepiplant.dto.PlantDto;
import com.google.gson.Gson;

import org.json.JSONArray;

public class PlantsListActivity extends AppCompatActivity implements CustomPlantsRecyclerViewAdapter.ItemClickListener {

    CustomPlantsRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    private static final String TAG = "PlantsListActivity";
    private int testUserId = 1;
    private PlantDto[] plants = new PlantDto[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants_list);

        recyclerView = findViewById(R.id.plantsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // data to populate the RecyclerView with
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.0.0.118:8080/plants/user/" + testUserId;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Request successful. Response is: " + response);
                        onResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
                }
            }
        });
        Log.v(TAG, "Sending the request to " + url);
        queue.add(jsonArrayRequest);
        // set up the RecyclerView
        adapter = new CustomPlantsRecyclerViewAdapter(this, plants);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private void onResponseReceived(JSONArray response){
        Log.v(TAG, "onResponseReceived()");
        Gson gson = new Gson();
        plants = gson.fromJson(String.valueOf(response), PlantDto[].class);
        adapter.updateData(plants);
        adapter.notifyItemRangeChanged(0, plants.length);
    }
}
