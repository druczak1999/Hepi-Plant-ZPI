package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UserUpdateActivity extends AppCompatActivity {

    private static final String TAG = "UserUpdateActivity";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<UserDto> userResponseHandler;
    private UserDto userDto;
    private TextView usernameValue, userEmail;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        userResponseHandler = new JSONResponseHandler<>(config);
        setBottomBarOnItemClickListeners();
        setupViewsData();
        getRequestUser();
        editRequestUser();
    }

    private void getRequestUser(){
        String url = getRequestUrl() +"users/"+config.getUserId();
        Log.v(TAG, "Invoking categoryRequestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the response string.
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
        userDto = data;
        usernameValue.setText(data.getUsername());
        userEmail.setText(data.getEmail());
    }

    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
    }

    private void editRequestUser(){
        String url = getRequestUrl() + "users/"+config.getUserId();
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.v(TAG, "On Click. Attempting patch request"+userDto.getUsername());
                JSONObject postData = new JSONObject();
                try {
                    postData.put("username", usernameValue.getText().toString());
                    postData.put("notifications", config.isNotifications());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v(TAG, "On Click. Attempting patch request"+usernameValue.getText().toString());
                Log.v(TAG, "Invoking categoryRequestProcessor");
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
                finish();
            }
        });
    }

    private void setupViewsData(){
        usernameValue = findViewById(R.id.usernameValueUserView);
        userEmail = findViewById(R.id.emailValueUserView);
        save = findViewById(R.id.save);
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlantsListActivity.class);
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
