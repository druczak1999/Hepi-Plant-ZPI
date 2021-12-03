package com.example.hepiplant;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.RequestType;

import java.io.IOException;
import java.util.Arrays;

public class PopUpDeleteEvent extends AppCompatActivity {

    private static final String TAG = "PopUpDeleteEvent";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_delete);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.3));
        setupViewsData();
    }

    private void setupViewsData(){
        TextView announcement = findViewById(R.id.textView);
        announcement.setText(R.string.popu_message_event);
        Button yes = findViewById(R.id.buttonYes);
        Button no = findViewById(R.id.buttonNo);

        no.setOnClickListener(v -> finish());

        yes.setOnClickListener(v -> deleteEvent());
    }

    @NonNull
    private String getRequestUrl(Long id) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "events/"+id;
    }

    private void deleteEvent(){
        String url = getRequestUrl(getIntent().getExtras().getLong("eventId"));
        requestProcessor.makeRequest(Request.Method.DELETE, url, null, RequestType.STRING,
            (Response.Listener<String>) response -> {
                Log.v(TAG, response);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel((int) getIntent().getExtras().getLong("eventId"));
                Toast.makeText(getApplicationContext(),R.string.delete_event,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }, this::onErrorResponseReceived);
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(),R.string.delete_event_failed,Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }
}
