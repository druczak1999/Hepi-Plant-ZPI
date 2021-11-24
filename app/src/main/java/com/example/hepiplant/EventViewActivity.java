package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
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
import org.json.JSONObject;
import java.io.IOException;

public class EventViewActivity extends AppCompatActivity {

    private static final String TAG = "EventViewActivity";

    private TextView plantName, eventName, eventDate, eventDescription;
    private ImageView eventImage;
    private EventDto event;
    private Configuration config;
    private JSONResponseHandler<EventDto> eventResponseHandler;
    private JSONRequestProcessor requestProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        eventResponseHandler = new JSONResponseHandler<>(config);
        setupViewsData();
        makeGetDataRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if(getIntent().getExtras().getString("place").equals("archive"))
            menuInflater.inflate(R.menu.menu_archive_event, menu);
        else
            menuInflater.inflate(R.menu.menu_event, menu);
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
                Toast.makeText(this.getApplicationContext(),"Informacje",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.miProfile:
                Intent intent2 = new Intent(this, UserActivity.class);
                startActivity(intent2);
                return true;
            case R.id.editEvent:
                Intent intent = new Intent(this,EventEditActivity.class);
                intent.putExtra("eventId",getIntent().getExtras().getLong("eventId"));
                startActivity(intent);
                return true;
            case R.id.deleteEvent:
                Intent intent1 = new Intent(this, PopUpDeleteEvent.class);
                intent1.putExtra("eventId",getIntent().getExtras().getLong("eventId"));
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewsData(){
        plantName = findViewById(R.id.EventPlantNameValueView);
        eventName = findViewById(R.id.EventName);
        eventImage = findViewById(R.id.eventImage);
        eventDate = findViewById(R.id.EventDateValueView);
        eventDescription = findViewById(R.id.EventDescriptionValue);
        setupToolbar();
    }

    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl()+"events/";
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
        setTextsToRealValues();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setTextsToRealValues(){
        plantName.setText(event.getPlantName());
        String name = event.getEventName();
        eventName.setText(name);
        if(name.toLowerCase().equals("podlewanie"))
            eventImage.setImageResource(R.drawable.watering_icon);
        else if(name.toLowerCase().equals("zraszanie"))
            eventImage.setImageResource(R.drawable.misting_icon);
        else if(name.toLowerCase().equals("nawożenie"))
            eventImage.setImageResource(R.drawable.fertilization_icon);
        eventDate.setText(event.getEventDate());
        eventDescription.setText(event.getEventDescription());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarPlantView);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }
}
