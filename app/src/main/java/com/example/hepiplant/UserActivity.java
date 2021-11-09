package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONObject;

import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<UserDto> userResponseHandler;
    private TextView usernameValue, username, userEmail;
    private Button change;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        userResponseHandler = new JSONResponseHandler<>(config);
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
    }

    private void getRequestUser(){
        String url = getRequestUrl() +"users/"+config.getUserId();
        profilePhoto.setImageURI(config.getPhoto());
        Log.v(TAG, "Invoking categoryRequestProcessor");
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
