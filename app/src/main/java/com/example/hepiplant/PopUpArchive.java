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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.PlantDto;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PopUpArchive extends AppCompatActivity {

    private Button yes, no;
    private Configuration config;
    private static final String TAG = "PopUpArchive";
    private PlantDto plant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_archive);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.3));
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
                makeGetDataRequest();
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
        return config.getUrl();
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
        String url = getRequestUrl()+"events/"+getIntent().getExtras().getLong("eventId");
        Log.v(TAG,url);
        Log.v(TAG,postData.toString());
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PATCH, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG,"onResponse");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            onPatchResponseEvent(response);
                        }
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onPatchResponseEvent(JSONObject response){
        Log.v(TAG, "ONResponse");
        String str = String.valueOf(response); //http request
        EventDto data = new EventDto();
        Gson gson = new Gson();
        data = gson.fromJson(str,EventDto.class);
        Log.v(TAG,"Czy sie zmieniło: "+data.isDone());
        postEventResponse();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void postEventResponse(){
        JSONObject postData = new JSONObject();
        try {
            String name = getIntent().getExtras().getString("eventName");
            postData.put("eventName", name);
            if(name.toLowerCase().equals("podlewanie"))
                postData.put("eventDate", LocalDateTime.now().plusDays(plant.getSchedule().getWateringFrequency()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
            else if(name.toLowerCase().equals("zraszanie"))
                postData.put("eventDate", LocalDateTime.now().plusDays(plant.getSchedule().getMistingFrequency()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
            else if(name.toLowerCase().equals("nawożenie"))
                postData.put("eventDate", LocalDateTime.now().plusDays(plant.getSchedule().getFertilizingFrequency()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
            postData.put("eventDescription",getIntent().getExtras().getString("eventDescription"));
            postData.put("plantId",getIntent().getExtras().getLong("plantId"));
            postData.put("plantName",getIntent().getExtras().getString("plantName"));
            postData.put("done",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getRequestUrl()+"events";
        Log.v(TAG,url);
        Log.v(TAG,postData.toString());
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG,"onResponse");
                        onPostResponseEvent(response);
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

    private void onPostResponseEvent(JSONObject response){
        Log.v(TAG, "ONResponse");
        String str = String.valueOf(response); //http request
        EventDto data = new EventDto();
        Gson gson = new Gson();
        data = gson.fromJson(str,EventDto.class);
        Toast.makeText(getApplicationContext(), R.string.archive_event, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(),MainTabsActivity.class);
        startActivity(intent);
        finish();
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl()+"plants/"+getIntent().getExtras().getLong("plantId");
        Log.v(TAG, url);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG,"plant onRespone");
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponsePlant(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonArrayRequest);
    }

    private void onGetResponseReceived(JSONObject response){
        Log.v(TAG, "onGetResponseReceived()");
        plant = config.getGson().fromJson(String.valueOf(response), PlantDto.class);
        Log.v(TAG,plant.getName());
        patchEventResponse();
    }

    private void onErrorResponsePlant(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(), R.string.archive_event_fail, Toast.LENGTH_LONG).show();
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