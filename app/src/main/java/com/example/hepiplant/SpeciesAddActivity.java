package com.example.hepiplant;

import static com.google.android.gms.common.util.ArrayUtils.newArrayList;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.dto.enums.Placement;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SpeciesAddActivity extends AppCompatActivity {

    private static final String TAG = "SpeciesAddActivity";
    private static final List<Placement> placementList = Arrays.asList(
            Placement.BARDZO_JASNE,
            Placement.JASNE,
            Placement.POLCIENISTE,
            Placement.ZACIENIONE);

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SpeciesDto> speciesResponseHandler;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private CategoryDto[] categories;
    private EditText nameAddText, wateringAddText, fertilizingAddText,
            mistingAddText, soilAddText;
    private Spinner categorySpinner, placementSpinner;
    private Placement selectedPlacement;
    private CategoryDto selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_add);

        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        speciesResponseHandler = new JSONResponseHandler<>(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);
        makeGetDataRequest(getRequestUrl() + "categories", true);
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

    public void onSaveButtonClick(View view){
        Log.v(TAG, "onSaveButtonClick");
        JSONObject postData = new JSONObject();
        String addedName = nameAddText.getText().toString().trim();
        if(!addedName.isEmpty()){
            try {
                postData.put("name", addedName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        int addedWatering = Integer.parseInt(wateringAddText.getText().toString().trim());
        int addedFertilizing = Integer.parseInt(fertilizingAddText.getText().toString().trim());
        int addedMisting = Integer.parseInt(mistingAddText.getText().toString().trim());
        try {
            postData.put("wateringFrequency", addedWatering);
            postData.put("fertilizingFrequency", addedFertilizing);
            postData.put("mistingFrequency", addedMisting);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String addedSoil = soilAddText.getText().toString().trim();
        if(!addedSoil.isEmpty()){
            try {
                postData.put("soil", addedSoil);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(selectedCategory != null){
            try {
                postData.put("categoryId", selectedCategory.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(selectedPlacement != null){
            try {
                postData.put("placement", selectedPlacement);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        makePostDataRequest(postData);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarSpeciesAdd);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewsData() {
        Log.v(TAG, "setupViewsData");
        nameAddText = findViewById(R.id.speciesNameAddEditText);
        wateringAddText = findViewById(R.id.speciesWateringFrequencyAddEditText);
        fertilizingAddText = findViewById(R.id.speciesFertilizingFrequencyAddEditText);
        mistingAddText = findViewById(R.id.speciesMistingFrequencyAddEditText);
        placementSpinner = findViewById(R.id.speciesPlacementAddSpinner);
        soilAddText = findViewById(R.id.speciesSoilAddEditText);
        categorySpinner = findViewById(R.id.speciesCategoryAddSpinner);
        setPlacementSpinnerValues();
        if (categories != null){
            Log.v(TAG, "categories is not null");
            setCategoriesSpinnerValues();
            createAndAttachSpinnerListeners();
        }
    }

    private void setCategoriesSpinnerValues() {
        Log.v(TAG, "setCategoriesSpinnerValues");
        List<String> categoryNameList = newArrayList();
        for(CategoryDto c:categories){
            categoryNameList.add(c.getName());
        }
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categoryNameList);
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoriesAdapter);
    }

    private void setPlacementSpinnerValues() {
        Log.v(TAG, "setPlacementSpinnerValues");
        List<String> placementNameList = newArrayList();
        placementNameList.add("");
        for(Placement p:placementList){
            placementNameList.add(p.getName());
        }
        ArrayAdapter<String> placementAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, placementNameList);
        placementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placementSpinner.setAdapter(placementAdapter);
    }

    private void createAndAttachSpinnerListeners(){
        Log.v(TAG, "createAndAttachSpinnerListeners");
        AdapterView.OnItemSelectedListener categoryListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                for(CategoryDto category : categories){
                    if(category.getName().equals(selectedItem)){
                        selectedCategory = category;
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        };
        categorySpinner.setOnItemSelectedListener(categoryListener);
        AdapterView.OnItemSelectedListener placementListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                for(Placement placement : placementList){
                    if(placement.getName().equals(selectedItem)){
                        selectedPlacement = placement;
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        };
        placementSpinner.setOnItemSelectedListener(placementListener);
    }

    private void makeGetDataRequest(String url, boolean isArrayRequest) {
        requestProcessor.makeRequest(Request.Method.GET, url, null, isArrayRequest ? RequestType.ARRAY : RequestType.OBJECT,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorResponseReceived(error);
                    }
                });
    }

    private void makePostDataRequest(JSONObject postData) {
        String url = getRequestUrl() + "species";
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.POST, url, postData, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onPostResponseReceived(response);
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
        return config.getUrl();
    }

    private void onGetResponseReceived(JSONArray response) {
        Log.v(TAG, "onGetResponseReceived() for JSONArray");
        categories = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
        setupViewsData();
    }

    private void onPostResponseReceived(JSONObject response) {
        SpeciesDto species = speciesResponseHandler.handleResponse(response, SpeciesDto.class);
        makeInfoToast(R.string.add_species + species.getId().toString());
        finish();
    }

    private void onErrorResponseReceived(VolleyError error) {
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        makeInfoToast(String.valueOf(R.string.add_species_failed));
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void makeInfoToast(String info) {
        Toast.makeText(getApplicationContext(),info,Toast.LENGTH_LONG).show();
    }

}