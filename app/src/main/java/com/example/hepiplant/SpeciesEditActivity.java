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
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.SpeciesDto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpeciesEditActivity extends AppCompatActivity {

    private static final String TAG = "SpeciesEditActivity";

    private Configuration config;
    private SpeciesDto species;
    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_edit);

        config = (Configuration) getApplicationContext();
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
                Toast.makeText(this.getApplicationContext(),"Informacje",Toast.LENGTH_SHORT).show();
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
        if(!species.getName().equals(editedName) && !editedName.isEmpty()){
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
        Toolbar toolbar = findViewById(R.id.includeToolbarSpeciesEdit);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewsData() {
        TextView editTitleView = findViewById(R.id.speciesEditTitleTextView);
        String editTitle = getResources().getString(R.string.edit_species_title) +
                " " + species.getId();
        editTitleView.setText(editTitle);
        nameEditText = findViewById(R.id.speciesNameEditEditText);
        nameEditText.setText(species.getName());
        // todo add the rest of views
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
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
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonObjectRequest);
    }

    private void makePatchDataRequest(JSONObject postData) {
        String url = getRequestUrl();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        makeInfoToast("Edytowano gatunek o id " + species.getId());
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonObjectRequest);
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "species/" + getIntent().getExtras().get("speciesId");
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived()");
        species = config.getGson().fromJson(String.valueOf(response), SpeciesDto.class);
        setupViewsData();
    }

    private void onErrorResponseReceived(VolleyError error) {
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

    private void makeInfoToast(String info) {
        Toast.makeText(getApplicationContext(),info,Toast.LENGTH_LONG).show();
    }
}