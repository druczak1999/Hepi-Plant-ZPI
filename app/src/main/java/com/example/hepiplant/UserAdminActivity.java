package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.UserStatisticsDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class UserAdminActivity extends AppCompatActivity {

    private static final String TAG = "UserAdminActivity";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<UserStatisticsDto> statisticsResponseHandler;
    private UserStatisticsDto userStatistics;
    private Button grantRoleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_admin);

        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        statisticsResponseHandler = new JSONResponseHandler<>(config);
        makeGetDataRequest();
        setupToolbar();
        setGrantRoleButtonOnClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        grantRoleButton.setVisibility(View.GONE);
        makeGetDataRequest();
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
            case R.id.logoff:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.informationAboutApp:
                Intent intentInfo = new Intent(this, InfoActivity.class);
                startActivity(intentInfo);
                return true;
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarUserAdmin);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setGrantRoleButtonOnClickListener(){
        grantRoleButton = findViewById(R.id.userGrantRoleButton);
        grantRoleButton.setOnClickListener(v -> {
            Log.v(TAG, "onItemClick() Grant Role");
            Intent intent = new Intent(v.getContext(), PopUpGrantAdminRole.class);
            intent.putExtra("userId", userStatistics.getUser().getId());
            startActivity(intent);
        });
    }

    private void setupViewsData() {
        Log.v(TAG, "setupViewsData");
        TextView titleView = findViewById(R.id.userTitleTextView);
        TextView nameTextView = findViewById(R.id.userNameValueTextView);
        TextView emailTextView = findViewById(R.id.userEmailValueTextView);
        TextView rolesTextView = findViewById(R.id.userRolesValueTextView);
        TextView plantsTextView = findViewById(R.id.userPlantsValueTextView);
        TextView postsTextView = findViewById(R.id.userPostsValueTextView);
        TextView salesOffersTextView = findViewById(R.id.userSalesOffersValueTextView);
        TextView commentsTextView = findViewById(R.id.userCommentsValueTextView);
        String title = getResources().getString(R.string.view_user_title) +
                " " + userStatistics.getUser().getId();
        titleView.setText(title);
        nameTextView.setText(userStatistics.getUser().getUsername());
        emailTextView.setText(userStatistics.getUser().getEmail());
        StringBuilder sb = new StringBuilder();
        for(String role:userStatistics.getUser().getRoles()){
            sb.append(role).append(", ");
        }
        if(!sb.toString().isEmpty()){
            rolesTextView.setText(sb.substring(0, sb.length()-2));
        } else {
            rolesTextView.setText(R.string.none);
        }
        if(!userStatistics.getUser().getRoles().contains(ROLE_ADMIN)){
            grantRoleButton.setVisibility(View.VISIBLE);
        }
        plantsTextView.setText(String.valueOf(userStatistics.getPlantsAmount()));
        postsTextView.setText(String.valueOf(userStatistics.getPostsAmount()));
        salesOffersTextView.setText(String.valueOf(userStatistics.getSalesOffersAmount()));
        commentsTextView.setText(String.valueOf(userStatistics.getCommentsAmount()));
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl() + "/statistics";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "users/" + getIntent().getExtras().get("userId");
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived() for JSONObject");
        userStatistics = statisticsResponseHandler.handleResponse(response, UserStatisticsDto.class);
        setupViewsData();
    }

    private void onErrorResponseReceived(VolleyError error) {
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }
}