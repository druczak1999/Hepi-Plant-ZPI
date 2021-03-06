package com.example.hepiplant;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PopUpArchive extends AppCompatActivity {

    private static final String TAG = "PopUpArchive";

    private Button yes, no;
    private Configuration config;
    private PlantDto plant;
    private JSONResponseHandler<EventDto> eventResponseHandler;
    private JSONResponseHandler<PlantDto> plantResponseHandler;
    private JSONRequestProcessor requestProcessor;
    private EventDto event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_delete);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.3));

        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        eventResponseHandler = new JSONResponseHandler<>(config);
        plantResponseHandler= new JSONResponseHandler<>(config);

        TextView announcement = findViewById(R.id.textView);
        announcement.setText(R.string.popup_message_archive);
        ImageView imageOfPopUp = findViewById(R.id.imageViewPopUp);
        imageOfPopUp.setImageResource(R.drawable.check_box);
        setupViewsData();
    }

    private void setupViewsData(){
        yes = findViewById(R.id.buttonYes);
        no = findViewById(R.id.buttonNo);

        setUpYesNoButtonsOnClickListeners();
    }

    private void setUpYesNoButtonsOnClickListeners() {
        no.setOnClickListener(v -> finish());

        yes.setOnClickListener(v -> makeGetEventRequest());
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

    private void makeGetEventRequest() {
        String url = getRequestUrl()+ "events/"+getIntent().getExtras().getLong("eventId");
        Log.v(TAG,url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onGetResponseReceivedEvent, this::onErrorResponseReceivedEvent);
    }

    private void onGetResponseReceivedEvent(JSONObject response){
        Log.v(TAG, "onGetResponseReceived()");
        event = eventResponseHandler.handleResponse(response, EventDto.class);
        Log.v(TAG,event.getEventDate());
        makeGetDataRequest();
    }

    private void onErrorResponseReceivedEvent(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void patchEventResponse(){
        JSONObject postData = preparePatchDataEvent();
        String url = getRequestUrl()+"events/"+getIntent().getExtras().getLong("eventId");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData, RequestType.OBJECT,
                (Response.Listener<JSONObject>) response -> {
                    Log.v(TAG,"onResponse");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        onPatchResponseEvent(response);
                    }
                }, this::onErrorResponsePlant);
    }

    private JSONObject preparePatchDataEvent() {
        JSONObject postData = new JSONObject();
        try {
            postData.put("eventName", event.getEventName());
            postData.put("eventDate",event.getEventDate());
            postData.put("eventDescription",event.getEventDescription());
            postData.put("done",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onPatchResponseEvent(JSONObject response){
        Log.v(TAG, "ONResponse");
        event = eventResponseHandler.handleResponse(response,EventDto.class);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel((int) getIntent().getExtras().getLong("eventId"));
        postEventResponse();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void postEventResponse(){
        JSONObject postData = preparePostEventData();
        String url = getRequestUrl()+"events";
        requestProcessor.makeRequest(Request.Method.POST, url, postData, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onPostResponseEvent, this::onErrorResponsePlant);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private JSONObject preparePostEventData() {
        JSONObject postData = new JSONObject();
        try {
            String name = event.getEventName();
            postData.put("eventName", name);
            if(name.equalsIgnoreCase("podlewanie"))
                postData.put("eventDate",
                        LocalDateTime.now().plusDays(plant.getSchedule().getWateringFrequency())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).substring(0,11)+config.getHourOfNotifications());
            else if(name.equalsIgnoreCase("zraszanie"))
                postData.put("eventDate",
                        LocalDateTime.now().plusDays(plant.getSchedule().getMistingFrequency())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).substring(0,11)+config.getHourOfNotifications());
            else if(name.equalsIgnoreCase("nawo??enie"))
                postData.put("eventDate",
                        LocalDateTime.now().plusDays(plant.getSchedule().getFertilizingFrequency())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).substring(0,11)+config.getHourOfNotifications());
            postData.put("eventDescription",event.getEventDescription());
            postData.put("plantId",event.getPlantId());
            postData.put("plantName",event.getPlantName());
            postData.put("done",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void onPostResponseEvent(JSONObject response){
        Log.v(TAG, "ONResponse");
        Toast.makeText(getApplicationContext(), getText(R.string.archive_event), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
        startActivity(intent);
        finish();
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl()+"plants/"+event.getPlantId();
        Log.v(TAG, url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onGetResponseReceived, this::onErrorResponsePlant);
    }

    private void onGetResponseReceived(JSONObject response){
        Log.v(TAG, "onGetResponseReceived()");
        plant = plantResponseHandler.handleResponse(response, PlantDto.class);
        Log.v(TAG,plant.getName());
        patchEventResponse();
    }

    private void onErrorResponsePlant(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(), getText(R.string.archive_event_fail), Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }
}
