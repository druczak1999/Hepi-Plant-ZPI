package com.example.hepiplant;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.RequestType;

import java.io.IOException;

public class PopUpDeleteComment extends AppCompatActivity {

    private static final String TAG = "PopUpDeleteComment";

    private Button yes, no;
    private Configuration config;
    private JSONRequestProcessor requestProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_comment);
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
        yes = findViewById(R.id.buttonYes);
        no = findViewById(R.id.buttonNo);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteComment();
            }
        });
    }

    @NonNull
    private String getRequestUrl(String type, Long id, Long commentId) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + type+"/"+id+"/comments/"+commentId;
    }

    private void deleteComment(){
        String url = getRequestUrl(getIntent().getExtras().getString("type"),getIntent().getExtras().getLong("postId"), getIntent().getExtras().getLong("commentId"));
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.DELETE, url, null, RequestType.STRING,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        Toast.makeText(getApplicationContext(),R.string.delete_comment,Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        });
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(),R.string.delete_comment_failed,Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }
}
