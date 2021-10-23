package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserDto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserUpdateActivity extends AppCompatActivity {
    private static final String TAG = "UserUpdateActivity";
    private UserDto userDto;
    private Configuration config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);
        TextView textView = (EditText) findViewById(R.id.usernameValueUserView);
        TextView textView2 = (TextView) findViewById(R.id.emailValueUserView);
        Button save = (Button) findViewById(R.id.save);

        Intent intent = this.getIntent();
        RequestQueue queue = Volley.newRequestQueue(this);
        config = (Configuration) getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url =config.getUrl()+"users/"+config.getUserId();


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
                        userDto = data;
                        textView.setText(data.getUsername());
                        textView2.setText(data.getEmail());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                textView.setText(error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        queue.add(jsonArrayRequest);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.v(TAG, "On Click. Attempting patch request"+userDto.getUsername());
                JSONObject postData = new JSONObject();
                try {
                    postData.put("username", textView.getText().toString());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v(TAG, "On Click. Attempting patch request"+textView.getText().toString());
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PATCH, url, postData,
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
                }){
                    @Override
                    public Map<String, String> getHeaders() {
                        return prepareRequestHeaders();
                    }
                };
                queue.add(jsonArrayRequest);
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            }
        });

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
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}