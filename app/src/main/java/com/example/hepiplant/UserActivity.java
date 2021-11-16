package com.example.hepiplant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.UserDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<UserDto> userResponseHandler;
    private JSONResponseHandler<EventDto> eventResponseHandler;
    private TextView usernameValue, username, userEmail;
    private Button change;
    private ImageView profilePhoto;
    private Switch notifications;
    private EventDto[] events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        userResponseHandler = new JSONResponseHandler<>(config);
        eventResponseHandler = new JSONResponseHandler<>(config);
        setBottomBarOnItemClickListeners();
        setupViewsData();
        getRequestUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRequestUser();
    }

    private void setupViewsData(){
        usernameValue = findViewById(R.id.usernameValueUserView);
        username = findViewById(R.id.userName);
        userEmail = findViewById(R.id.emailValueUserView);
        change = findViewById(R.id.change);
        profilePhoto = findViewById(R.id.profilePhoto);
        notifications = findViewById(R.id.noticeSwitch);
        if(config.isNotifications()) notifications.setChecked(true);
        else notifications.setChecked(false);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setNotifications(notifications.isChecked());
                editRequestUser();
                makeGetDataRequest();
            }
        });
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl()+"events/user/"+ config.getUserId();

       requestProcessor.makeRequest(Request.Method.GET, url, null,RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
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
            if(!notifications.isChecked())
                notificationManagerCompat.cancel(event.getId().intValue());
            else{
                setupNotifications(event);
            }
        }
    }

    private void setupNotifications(EventDto event) {
        if (!event.isDone()) {
            Log.v(TAG, event.getEventName());
            Intent intent = new Intent(this, AlarmBroadcast.class);
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("eventDescription", event.getEventDescription());
            intent.putExtra("eventId", event.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, event.getId().intValue(), intent, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Log.v(TAG, simpleDateFormat.parse(event.getEventDate()).toString());
                calendar.setTime(simpleDateFormat.parse(event.getEventDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.v(TAG, String.valueOf(calendar.getTime()));
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

    }

    private void editRequestUser(){
        String url = getRequestUrl() + "users/"+config.getUserId();

        Log.v(TAG, "On Click. Attempting patch request"+config.isNotifications());
        JSONObject postData = new JSONObject();
        try {
            postData.put("notifications", config.isNotifications());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "On Click. Attempting patch request"+usernameValue.getText().toString());
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
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

    private void getRequestUser(){
        String url = getRequestUrl() +"users/"+config.getUserId();
        profilePhoto.setImageURI(config.getPhoto());
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onGetResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                usernameValue.setText(error.getMessage());
            }
        });
    }

    private void onGetResponse(JSONObject response) {
        Log.v(TAG, "Request successful. Response is: " + response);
        UserDto data = new UserDto();
        data = userResponseHandler.handleResponse(response, UserDto.class);

        usernameValue.setText(data.getUsername());
        userEmail.setText(data.getEmail());
        username.setText("Witaj "+data.getUsername()+"!");
        change.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserUpdateActivity.class);
                startActivity(intent);
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

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
                startActivity(intent);
            }
        });

        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            }
        });
    }
}
