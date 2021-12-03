package com.example.hepiplant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private TextView usernameValue, username, userEmail, notificationsTimeText, notificationsTimeValue;
    private Button changeUserData, changeNotificationsTime, goToUserPostsButton;
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
        setupToolbar();
        setupViewsData();
        setGoToPostsButtonOnClickListener();
        getRequestUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRequestUser();
        refreshDisplayedData();
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
            case R.id.logoff:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.informationAboutApp:
                Intent intentInfo = new Intent(this, InfoActivity.class);
                startActivity(intentInfo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.userToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewsData(){
        usernameValue = findViewById(R.id.usernameValueUserView);
        username = findViewById(R.id.userName);
        userEmail = findViewById(R.id.emailValueUserView);
        changeUserData = findViewById(R.id.change);
        profilePhoto = findViewById(R.id.profilePhoto);
        changeNotificationsTime = findViewById(R.id.changeNotificationsTimeButton);
        notificationsTimeText = findViewById(R.id.notificationTimeUserView);
        notificationsTimeValue = findViewById(R.id.notificationTimeUserValue);
        notificationsTimeValue.setText(config.getHourOfNotifications());
        notifications = findViewById(R.id.noticeSwitch);
        goToUserPostsButton = findViewById(R.id.goToUsersPosts);
        setUpNotifications();
    }

    private void setGoToPostsButtonOnClickListener(){
        goToUserPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ForumTabsActivity.class);
                intent.putExtra("view", "user");
                startActivity(intent);
            }
        });
    }

    private void setUpNotifications() {
        if(config.isNotifications()) {
            notifications.setChecked(true);
        }
        else{
            notifications.setChecked(false);
            notificationsTimeValue.setVisibility(View.GONE);
            notificationsTimeText.setVisibility(View.GONE);
            changeNotificationsTime.setVisibility(View.GONE);
        }
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setNotifications(notifications.isChecked());
                if(notifications.isChecked()){
                    notificationsTimeValue.setVisibility(View.VISIBLE);
                    notificationsTimeText.setVisibility(View.VISIBLE);
                    changeNotificationsTime.setVisibility(View.VISIBLE);
                }
                else{
                    notificationsTimeValue.setVisibility(View.GONE);
                    notificationsTimeText.setVisibility(View.GONE);
                    changeNotificationsTime.setVisibility(View.GONE);
                }
                editRequestUser();
                makeGetDataRequest();
            }
        });

        changeNotificationsTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TimePickerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void makeGetDataRequest(){
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
            if(!notifications.isChecked())
                notificationManagerCompat.cancel(event.getId().intValue());
            else{
                setupNotifications(event);
            }
        }
    }

    private void refreshDisplayedData() {
        Log.v(TAG, "Refreshing displayed data()");
        notificationsTimeValue = findViewById(R.id.notificationTimeUserValue);
        notificationsTimeValue.setText(config.getHourOfNotifications());
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
                calendar.setTime(simpleDateFormat.parse(event.getEventDate()+" "+config.getHourOfNotifications()));
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
        changeUserData.setOnClickListener(new View.OnClickListener(){
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
}
