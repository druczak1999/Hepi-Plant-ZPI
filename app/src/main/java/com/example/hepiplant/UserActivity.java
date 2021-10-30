package com.example.hepiplant;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserDto;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private Configuration config;
    private TextView usernameValue, username, userEmail;
    private Button change;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
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
        usernameValue = (TextView) findViewById(R.id.usernameValueUserView);
        username = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.emailValueUserView);
        change = (Button) findViewById(R.id.change);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
    }

    private void getRequestUser(){
        Intent intent = this.getIntent();
        RequestQueue queue = Volley.newRequestQueue(this);
        config = (Configuration) getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url =config.getUrl()+"users/"+config.getUserId();
        profilePhoto.setImageURI(config.getPhoto());
        // Request a string response from the provided URL.
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the response string.
                        Log.v(TAG, "Request successful. Response is: " + response);
                        String str = String.valueOf(response); //http request
                        UserDto data = new UserDto();
                        data = config.getGson().fromJson(str, UserDto.class);

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
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                usernameValue.setText(error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        config.getQueue().add(jsonArrayRequest);
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = (Button) findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlantsListActivity.class);
                startActivity(intent);
            }
        });

        Button buttonForum = (Button) findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            }
        });
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        Log.v(TAG,"Token: "+config.getToken());
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}
