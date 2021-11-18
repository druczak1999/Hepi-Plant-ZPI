package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CategoryEditActivity extends AppCompatActivity {

    private static final String TAG = "CategoryEditActivity";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private CategoryDto category;
    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);
        makeGetDataRequest();
        setupToolbar();
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
                Toast.makeText(this.getApplicationContext(),R.string.informations,Toast.LENGTH_SHORT).show();
                return true;
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSaveButtonClick(View view){
        String editedName = nameEditText.getText().toString().trim();
        if(!category.getName().equals(editedName) && !editedName.isEmpty()){
            JSONObject postData = new JSONObject();
            try {
                postData.put("name", editedName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            makePatchDataRequest(postData);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarCategoryEdit);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewsData() {
        TextView editTitleView = findViewById(R.id.categoryEditTitleTextView);
        String editTitle = getResources().getString(R.string.edit_category_title) +
                " " + category.getId();
        editTitleView.setText(editTitle);
        nameEditText = findViewById(R.id.categoryNameEditEditText);
        nameEditText.setText(category.getName());
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl();
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
            new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONObject response) {
                    onGetResponseReceived(response);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        });
    }

    private void makePatchDataRequest(JSONObject postData) {
        String url = getRequestUrl();
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData, RequestType.OBJECT,
            new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONObject response) {
                    makeInfoToast(R.string.edit_category + category.getId().toString());
                    finish();
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        });
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "categories/" + getIntent().getExtras().get("categoryId");
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived()");
        category = categoryResponseHandler.handleResponse(response, CategoryDto.class);
        setupViewsData();
    }

    private void onErrorResponseReceived(VolleyError error) {
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        makeInfoToast(String.valueOf(R.string.edit_saved_failed));
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void makeInfoToast(String info) {
        Toast.makeText(getApplicationContext(),info,Toast.LENGTH_LONG).show();
    }

}