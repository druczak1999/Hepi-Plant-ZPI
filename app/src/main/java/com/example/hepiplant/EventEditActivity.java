package com.example.hepiplant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class EventEditActivity extends AppCompatActivity {

    private static final String TAG = "EventEditActivity";

    private EditText eventName, eventDescription;
    private Button eventDate, saveEvent;
    private ImageView eventImage;
    private EventDto event;
    private Configuration config;
    private JSONResponseHandler<EventDto> eventResponseHandler;
    private JSONRequestProcessor requestProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        eventResponseHandler = new JSONResponseHandler<>(config);
        setupViewsData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                eventDate.setText(data.getExtras().getString("date"));
            }
        }
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
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.eventEditToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewsData(){
        eventName = findViewById(R.id.eventEditTitle);
        eventDescription = findViewById(R.id.eventDescriptionEdit);
        eventDate = findViewById(R.id.eventDateEdit);
        saveEvent = findViewById(R.id.editEventButton);
        eventImage = findViewById(R.id.editImageBut);
        setupToolbar();
        setBottomBarOnItemClickListeners();
        setOnClickListeners();
        makeGetDataRequest();
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+ +getIntent().getExtras().getLong("eventId");
        Log.v(TAG,url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onGetResponseReceived, this::onErrorResponseReceived);
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
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void setValuesToEdit(){
        String name = event.getEventName();
        eventName.setText(name);
        eventDate.setText(event.getEventDate());
        eventDescription.setText(event.getEventDescription());
        if(name.equalsIgnoreCase("podlewanie"))
            eventImage.setImageResource(R.drawable.watering_icon);
        else if(name.equalsIgnoreCase("zraszanie"))
            eventImage.setImageResource(R.drawable.misting_icon);
        else if(name.equalsIgnoreCase("nawożenie"))
            eventImage.setImageResource(R.drawable.fertilization_icon);
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
            startActivity(intent);
        });

        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
            startActivity(intent);
        });
    }

    private void setOnClickListeners(){
        eventDate.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
            intent.putExtra("event","event");
            startActivityForResult(intent, 1);
        });
        onClickEditEvent();
    }

    private void onClickEditEvent(){
        Log.v(TAG,"onClick Edit");
        saveEvent.setOnClickListener(v -> {
            if(eventName.getText()!=null && !eventName.getText().toString().equals(" ")) patchEventResponse();
            else Toast.makeText(getApplicationContext(),getResources().getString(R.string.event_title),Toast.LENGTH_LONG).show();
        });
    }

    private void patchEventResponse(){
        JSONObject postData = prepareJSONEventObject();
        String url = getRequestUrl()+getIntent().getExtras().getLong("eventId");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData,RequestType.OBJECT,
                (Response.Listener<JSONObject>) response -> {
                    Log.v(TAG,"onResponse");
                    onPatchResponseEvent(response);
                }, this::onErrorResponseEvent);
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

    private void onPatchResponseEvent(JSONObject response){
        Log.v(TAG, "ONResponse");
        EventDto data = new EventDto();
        data = eventResponseHandler.handleResponse(response,EventDto.class);
        if(config.isNotifications())
            setupNotifications(data);
        Intent intent = new Intent(this, MainTabsActivity.class);
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.edit_saved),Toast.LENGTH_LONG).show();
        startActivity(intent);
        finish();
    }

    private void onErrorResponseEvent(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.edit_saved_failed),Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
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
