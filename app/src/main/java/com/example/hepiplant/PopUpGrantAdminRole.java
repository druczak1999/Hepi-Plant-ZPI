package com.example.hepiplant;

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
import java.util.Locale;

public class PopUpGrantAdminRole extends AppCompatActivity {

    private static final String TAG = "PopUpGrantAdminRole";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

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
        Button yes = findViewById(R.id.adminButtonYes);
        Button no = findViewById(R.id.adminButtonNo);

        String deleteQuestion = getResources().getString(R.string.grant_role_question) +
                " " + getIntent().getExtras().getLong("userId") +
                "?";
        questionView.setText(deleteQuestion);

        no.setOnClickListener(v -> finish());

        yes.setOnClickListener(v -> grantRole());
    }

    @NonNull
    private String getRequestUrl(Long id) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "users/" + id + "/grant-role?role=";
    }

    private void grantRole(){
        String url = getRequestUrl(getIntent().getExtras().getLong("userId")) + ROLE_ADMIN;
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.POST, url, null, RequestType.STRING,
                (Response.Listener<String>) this::onResponseReceived, this::onErrorResponseReceived);
    }

    private void onResponseReceived(String response) {
        Log.v(TAG, response);
        if(response.toLowerCase(Locale.ROOT).contains("successfully granted")){
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.admin_role_granted_success), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.admin_role_granted_fail), Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }
}
