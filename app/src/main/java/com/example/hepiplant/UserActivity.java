package com.example.hepiplant;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserDto;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    Configuration configuration = new Configuration();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setBottomBarOnItemClickListeners();
        TextView textView = (TextView) findViewById(R.id.nazwa2);
        TextView textView1 = (TextView) findViewById(R.id.przywitanie);
        TextView textView2 = (TextView) findViewById(R.id.nazwa4);
        Button change = (Button) findViewById(R.id.change);


        Intent intent = this.getIntent();
        RequestQueue queue = Volley.newRequestQueue(this);
        final Configuration config = (Configuration) getApplicationContext();
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
                        Gson gson = new Gson();
                        data = gson.fromJson(str, UserDto.class);

                        textView.setText(data.getUsername());
                        textView2.setText(data.getEmail());
                        textView1.setText("Witaj "+data.getUsername()+"!");
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
                textView.setText(error.getMessage());
            }
        });
        queue.add(jsonArrayRequest);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
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
}
