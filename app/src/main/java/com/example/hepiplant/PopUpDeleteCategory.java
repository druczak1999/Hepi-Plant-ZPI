package com.example.hepiplant;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hepiplant.configuration.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PopUpDeleteCategory extends AppCompatActivity {

    private static final String TAG = "PopUpDeleteCategory";

    private Button yes, no;
    private Configuration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_delete_admin);
        config = (Configuration) getApplicationContext();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width * 0.9), (int)(height * 0.3));
        setupViewsData();
    }

    private void setupViewsData(){
        TextView questionView = findViewById(R.id.adminDeleteQuestionTextView);
        yes = findViewById(R.id.adminButtonYes);
        no = findViewById(R.id.adminButtonNo);

        String deleteQuestion = getResources().getString(R.string.category_delete_question) +
                " " + getIntent().getExtras().getLong("categoryId") +
                "?";
        questionView.setText(deleteQuestion);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCategory();
            }
        });
    }

    @NonNull
    private String getRequestUrl(Long id) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "categories/" + id;
    }

    private void deleteCategory(){
        String url = getRequestUrl(getIntent().getExtras().getLong("categoryId"));
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.DELETE, url,
            new Response.Listener<String>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(String response) {
                    onResponseReceived(response);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonArrayRequest);
    }

    private void onResponseReceived(String response) {
        Log.v(TAG, response);
        if(response.toLowerCase(Locale.ROOT).contains("successfully deleted")){
            Toast.makeText(getApplicationContext(),"Usunięto kategorię",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"Usuwanie kategorii nie powiodło się",Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}