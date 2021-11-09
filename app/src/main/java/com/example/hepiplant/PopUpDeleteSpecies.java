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
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.RequestType;

import java.io.IOException;
import java.util.Locale;

public class PopUpDeleteSpecies extends AppCompatActivity {

    private static final String TAG = "PopUpDeleteSpecies";

    private Button yes, no;
    private Configuration config;
    private JSONRequestProcessor requestProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_delete_admin);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);

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

        String deleteQuestion = getResources().getString(R.string.species_delete_question) +
                " " + getIntent().getExtras().getLong("speciesId") +
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
                deleteSpecies();
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
        return config.getUrl() + "species/" + id;
    }

    private void deleteSpecies(){
        String url = getRequestUrl(getIntent().getExtras().getLong("speciesId"));
        Log.v(TAG, "Invoking categoryRequestProcessor");
        requestProcessor.makeRequest(Request.Method.DELETE, url, null, RequestType.STRING,
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
        });
    }

    private void onResponseReceived(String response) {
        Log.v(TAG, response);
        if(response.toLowerCase(Locale.ROOT).contains("successfully deleted")){
            Toast.makeText(getApplicationContext(),"Usunięto gatunek",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"Usuwanie gatunku nie powiodło się",Toast.LENGTH_LONG).show();
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
}