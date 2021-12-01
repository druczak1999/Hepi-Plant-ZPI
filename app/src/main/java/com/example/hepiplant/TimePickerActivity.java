package com.example.hepiplant;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class TimePickerActivity extends AppCompatActivity {

    private static final String TAG = "TimePickerActivity";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<EventDto> eventResponseHandler;
    private Button saveTimeButton;
    private EventDto[] events;
    private boolean patchError = false;

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        eventResponseHandler = new JSONResponseHandler<>(config);

        TimePicker picker = (TimePicker)findViewById(R.id.timePicker);
        picker.setIs24HourView(true);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.4));

        setUpSaveTimeButtonOnClickListener(picker);
    }

    private void setUpSaveTimeButtonOnClickListener(TimePicker picker) {
        saveTimeButton = findViewById(R.id.saveTimeButton);
        saveTimeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String hour = "";
                if(picker.getHour()<=9) hour+="0";
                hour+=picker.getHour()+":";
                if(picker.getMinute()<=9) hour+="0";
                hour+=picker.getMinute()+":00";
                config.setHourOfNotifications(hour);
                Log.v(TAG,config.getHourOfNotifications());
                patchRequestUser();
                getRequestEvents();
                finish();
            }
        });
    }

    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
    }

    private void patchRequestUser(){
        String url = getRequestUrl() + "users/"+config.getUserId();

        Log.v(TAG, "On Click. Attempting patch request"+config.isNotifications());
        JSONObject postData = new JSONObject();
        try {
            postData.put("notifications",true);
            postData.put("hourOfNotifications", config.getHourOfNotifications());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "On Click. Attempting patch request"+config.getUserId());
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "Request successful. Response is: " + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v(TAG, "User request unsuccessful. Error message: " + error.getMessage());
                    }
                });
    }

    private void getRequestEvents(){
        String url = getRequestUrl()+"events/user/"+ config.getUserId();
        requestProcessor.makeRequest(Request.Method.GET, url, null,RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v(TAG, "User request unsuccessful. Error message: " + error.getMessage());
                    }
                });
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        events = eventResponseHandler.handleArrayResponse(response, EventDto[].class);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        for (EventDto event:events) {
            patchEventResponse(event.getId().intValue(), event.getEventDate());
        }
        if(!patchError) Toast.makeText(getApplicationContext(),R.string.edit_saved,Toast.LENGTH_LONG).show();
    }

    private void patchEventResponse(int eventId, String eventDate){
        JSONObject postData = preparePatchEventData(eventDate);
        String url = getRequestUrl()+"events/"+eventId;
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData,RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
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
                        patchError = true;
                        onErrorResponseEvent(error);
                    }
                });
    }

    private JSONObject preparePatchEventData(String eventDate) {
        JSONObject postData = new JSONObject();
        try {
            Log.v(TAG, eventDate);
            if (eventDate.contains(":")) {
                postData.put("eventDate", eventDate.substring(0, 10) + " " + config.getHourOfNotifications());
                Log.v(TAG,eventDate.substring(0, 10) + " " + config.getHourOfNotifications());
            }
            else {
                postData.put("eventDate", eventDate.trim() + " " + config.getHourOfNotifications());
                Log.v(TAG,eventDate.trim() + " " + config.getHourOfNotifications());
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onPatchResponseEvent(JSONObject response){
        Log.v(TAG, "ONResponse");
        EventDto data = new EventDto();
        data = eventResponseHandler.handleResponse(response,EventDto.class);
        if(config.isNotifications())
            setupNotifications(data);
        finish();
    }

    private void onErrorResponseEvent(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(),R.string.edit_saved_failed,Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void setupNotifications(EventDto data) {
        if(data!=null){
            if(!data.isDone()){
                Intent intent = prepareEventIntentForAlarmBroadcast(data);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,data.getId().intValue(), intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    calendar.setTime(simpleDateFormat.parse(data.getEventDate().substring(0,10)+" "+config.getHourOfNotifications()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
            }
        }
    }

    private Intent prepareEventIntentForAlarmBroadcast(EventDto data) {
        Intent intent = new Intent(this, AlarmBroadcast.class);
        intent.putExtra("eventName",data.getEventName());
        intent.putExtra("eventDescription",data.getEventDescription());
        intent.putExtra("eventId",data.getId());
        return intent;
    }
}
