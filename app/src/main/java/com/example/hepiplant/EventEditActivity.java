package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventEditActivity extends AppCompatActivity {

    private EditText eventName, eventDescription;
    private Button eventDate, saveEvent;
    private ImageView eventImage;
    private Configuration config;
    private static final String TAG = "EventEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        config = (Configuration) getApplicationContext();
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
        setValuesToEdit();
    }

    private void setValuesToEdit(){
        String name = getIntent().getExtras().getString("eventName");
        eventName.setText(name);
        eventDate.setText(getIntent().getExtras().getString("eventDate"));
        eventDescription.setText(getIntent().getExtras().getString("eventDescription"));
        if(name.toLowerCase().equals("podlewanie"))
            eventImage.setImageResource(R.drawable.podelwanie);
        else if(name.toLowerCase().equals("zraszanie"))
            eventImage.setImageResource(R.drawable.zraszanie);
        else if(name.toLowerCase().equals("nawożenie"))
            eventImage.setImageResource(R.drawable.nawozenie);

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
                else Toast.makeText(getApplicationContext(),"Podaj tytuł wydarzenia",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, data.getExtras().getString("data"));
                eventDate.setText(data.getExtras().getString("data"));
                Log.v(TAG, eventDate.getText().toString());
            }
        }
    }

    private void patchEventResponse(){
        JSONObject postData = new JSONObject();
        try {
            postData.put("eventName",eventName.getText().toString());
            if(eventDate.getText().toString().contains(":"))
                postData.put("eventDate",eventDate.getText().toString());
            else
                postData.put("eventDate",eventDate.getText().toString().trim()+" 00:00:00");
            postData.put("eventDescription",eventDescription.getText().toString());
            postData.put("done",false);
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            onPatchResponseEvent(response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponse(error);
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
        Intent intent = new Intent(this, MainTabsActivity.class);
        startActivity(intent);
        finish();
    }

    private void onErrorResponse(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
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

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}