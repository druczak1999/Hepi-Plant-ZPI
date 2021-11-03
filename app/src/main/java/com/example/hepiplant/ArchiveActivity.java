package com.example.hepiplant;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.hepiplant.adapter.recyclerview.EventsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArchiveActivity extends AppCompatActivity {

    EventDto[] events;
    private Configuration config;
    private static final String TAG = "ArchiveActivity";
    private RecyclerView rv;
    private EventsRecyclerViewAdapter adapter;
    //TODO change view on item in archive
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);
        config = (Configuration) getApplicationContext();
        rv = findViewById(R.id.eventsArchRecyclerView);
        setLayoutManager();
        makeGetDataRequest();
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+ "events/plant/"+getIntent().getExtras().getLong("plantId");
        Log.v(TAG,url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG,"onresponse");
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }};
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
        return config.getUrl() ;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        Gson gson = new Gson();
        events = gson.fromJson(String.valueOf(response), EventDto[].class);
        Log.v(TAG,"DL: "+events.length);
        setAdapter();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }
    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }

    private void setAdapter() {
        adapter = new EventsRecyclerViewAdapter(this, events);
//        adapter.setClickListener((EventsRecyclerViewAdapter.ItemClickListener) this);
        rv.setAdapter(adapter);
    }
    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
    }
}
