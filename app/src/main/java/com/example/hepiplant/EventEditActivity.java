package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EventEditActivity extends AppCompatActivity {

    private EditText eventName, eventDescription;
    private Button eventDate, saveEvent;
    private ImageView eventImage;
    private EventDto event;
    private Configuration config;
    private JSONResponseHandler<EventDto> eventResponseHandler;
    private JSONRequestProcessor requestProcessor;
    private static final String TAG = "EventEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        eventResponseHandler = new JSONResponseHandler<>(config);
        setupViewsData();
    }

    private void setupViewsData(){
        eventName = findViewById(R.id.eventEditTitle);
        eventDescription = findViewById(R.id.eventDescriptionEdit);
        eventDate = findViewById(R.id.eventDateEdit);
        saveEvent = findViewById(R.id.editEventButton);
        eventImage = findViewById(R.id.editImageBut);
        setBottomBarOnItemClickListeners();
        setOnClickListeners();
        makeGetDataRequest();
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+ +getIntent().getExtras().getLong("eventId");
        Log.v(TAG,url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG,"onresponse");
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorResponseReceived(error);
                    }
                });
    }

    private void onGetResponseReceived(JSONObject response){
        Log.v(TAG, "onGetResponseReceived()");
        event = eventResponseHandler.handleResponse(response, EventDto.class);
        Log.v(TAG,event.getEventDate());
        setValuesToEdit();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setValuesToEdit(){
        String name = event.getEventName();
        eventName.setText(name);
        eventDate.setText(event.getEventDate());
        eventDescription.setText(event.getEventDescription());
        if(name.toLowerCase().equals("podlewanie"))
            eventImage.setImageResource(R.drawable.watering_icon);
        else if(name.toLowerCase().equals("zraszanie"))
            eventImage.setImageResource(R.drawable.misting_icon);
        else if(name.toLowerCase().equals("nawożenie"))
            eventImage.setImageResource(R.drawable.fertilization_icon);
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

    private void setOnClickListeners(){
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra("event","event");
                startActivityForResult(intent, 1);
            }
        });
        onClickEditEvent();
    }

    private void onClickEditEvent(){
        Log.v(TAG,"onClick Edit");
        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eventName.getText()!=null && !eventName.getText().toString().equals(" ")) patchEventResponse();
                else Toast.makeText(getApplicationContext(),R.string.event_title,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                eventDate.setText(data.getExtras().getString("data"));
            }
        }
    }

    private void patchEventResponse(){
        JSONObject postData = prepareJSONEventObject();
        String url = getRequestUrl()+getIntent().getExtras().getLong("eventId");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData,RequestType.OBJECT,
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
                onErrorResponseEvent(error);
            }
        });
    }

    private JSONObject prepareJSONEventObject() {
        JSONObject postData = new JSONObject();
        try {
            postData.put("eventName",eventName.getText().toString());
            if(eventDate.getText().toString().contains(":"))
                postData.put("eventDate",eventDate.getText().toString());
            else
                postData.put("eventDate",eventDate.getText().toString().trim()+" "+config.getHourOfNotifications());
            postData.put("eventDescription",eventDescription.getText().toString());
            postData.put("done",false);
        } catch (JSONException e) {
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
        Intent intent = new Intent(this, MainTabsActivity.class);
        Toast.makeText(getApplicationContext(),R.string.edit_saved,Toast.LENGTH_LONG).show();
        startActivity(intent);
        finish();
    }

    private void onErrorResponseEvent(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(),R.string.edit_saved_failed,Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setupNotifications(EventDto data) {
        if(data!=null){
            if(!data.isDone()){
                Log.v(TAG,data.getEventName());
                Intent intent = new Intent(this, AlarmBroadcast.class);
                intent.putExtra("eventName",data.getEventName());
                intent.putExtra("eventDescription",data.getEventDescription());
                intent.putExtra("eventId",data.getId());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,data.getId().intValue(), intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Log.v(TAG,simpleDateFormat.parse(data.getEventDate()).toString());
                    calendar.setTime(simpleDateFormat.parse(data.getEventDate()+" "+config.getHourOfNotifications()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.v(TAG, String.valueOf(calendar.getTime()));
                alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
            }
        }
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

}