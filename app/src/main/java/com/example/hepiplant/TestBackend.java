package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserDto;
import com.firebase.ui.auth.data.model.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TestBackend extends AppCompatActivity {

    private static final String TAG = "TestBackend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_backend);
        TextView textView = (TextView) findViewById(R.id.tv1);

        Toolbar toolbar = findViewById(R.id.toolbarTest);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        RequestQueue queue = Volley.newRequestQueue(this);
        final Configuration config = (Configuration) getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url =config.getUrl()+"users";

        JSONObject postData = new JSONObject();
        try {
            postData.put("username", intent.getExtras().getString("userName"));
            postData.put("email", intent.getExtras().getString("userEmail"));
            postData.put("uid", intent.getExtras().getString("userId"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Request a string response from the provided URL.
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the response string.

                        String str = String.valueOf(response); //http request
                        UserDto data = new UserDto();
                        Gson gson = new Gson();
                        data = gson.fromJson(str, UserDto.class);

                        StringBuilder sb = new StringBuilder("Response is: \n"+data.getId());
                        //Arrays.stream(data).forEach(p -> sb.append(p.getUsername()).append("\n"));
                        textView.setText(sb);
//                        String uri = intent.getExtras().getString("photo");
//                        Uri photoUri = Uri.parse(uri);
//                        imageView.setImageURI(photoUri);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText(error.getMessage());
            }
        });

// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
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
            case R.id.wyloguj:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.infoMenu:
                Toast.makeText(this,"Informacje",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}