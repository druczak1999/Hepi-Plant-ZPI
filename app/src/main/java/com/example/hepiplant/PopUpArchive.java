package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.PlantDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PopUpArchive extends AppCompatActivity {

    private Button yes, no;
    private Configuration config;
    private static final String TAG = "PopUpArchive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_archive);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.3));
        Log.v(TAG,"NAzwaAAAAAAAAAAAAAa: "+getIntent().getExtras().getString("eventName"));
        setupViewsData();
    }

    private void setupViewsData(){
        config = (Configuration) getApplicationContext();
        yes = findViewById(R.id.buttonYesArchive);
        no = findViewById(R.id.buttonNoArchive);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patchEventResponse();
            }
        });
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl()+"events/";
    }

    private void patchEventResponse(){

        JSONObject postData = new JSONObject();
        try {
            postData.put("eventName",getIntent().getExtras().getString("eventName"));
            postData.put("eventDate",getIntent().getExtras().getString("eventDate"));
            postData.put("eventDescription",getIntent().getExtras().getString("eventDescription"));
            postData.put("done",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG,"patch");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getRequestUrl()+getIntent().getExtras().getLong("eventId");
        Log.v(TAG,url);
        Log.v(TAG,postData.toString());
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PATCH, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG,"onResponse");
                        onPostResponsePlant(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponsePlant(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

        queue.add(jsonArrayRequest);
    }

    private void onPostResponsePlant(JSONObject response){
        Log.v(TAG, "ONResponse");
        String str = String.valueOf(response); //http request
        EventDto data = new EventDto();
        Gson gson = new Gson();
        data = gson.fromJson(str,EventDto.class);
        Log.v(TAG,"Czy sie zmieniło: "+data.isDone());
        Intent intent = new Intent(getApplicationContext(),MainTabsActivity.class);
        startActivity(intent);
        finish();
    }

    private void onErrorResponsePlant(VolleyError error){
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
}