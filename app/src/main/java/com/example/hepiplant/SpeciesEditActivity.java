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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.dto.enums.Placement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpeciesEditActivity extends AppCompatActivity {

    private static final String TAG = "SpeciesEditActivity";
    private static final List<Placement> placementList = Arrays.asList(Placement.BARDZO_JASNE, Placement.JASNE, Placement.POLCIENISTE, Placement.ZACIENIONE);

    private Configuration config;
    private SpeciesDto species;
    private CategoryDto[] categories;
    private EditText nameEditText, wateringEditText, fertilizingEditText,
            mistingEditText, soilEditText;
    private Spinner categorySpinner, placementSpinner;
    private Placement selectedPlacement;
    private CategoryDto selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_edit);

        config = (Configuration) getApplicationContext();
        makeGetDataRequest(getRequestUrl() + "species/" + getIntent().getExtras().get("speciesId"), SpeciesDto.class, false);
        makeGetDataRequest(getRequestUrl() + "categories", CategoryDto.class, true);
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
        Log.v(TAG, "onSaveButtonClick");
        JSONObject postData = new JSONObject();
        String editedName = nameEditText.getText().toString().trim();
        if(!species.getName().equals(editedName) && !editedName.isEmpty()){
            try {
                postData.put("name", editedName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        int editedWatering = Integer.parseInt(wateringEditText.getText().toString().trim());
        int editedFertilizing = Integer.parseInt(fertilizingEditText.getText().toString().trim());
        int editedMisting = Integer.parseInt(mistingEditText.getText().toString().trim());
        try {
            postData.put("wateringFrequency", editedWatering);
            postData.put("fertilizingFrequency", editedFertilizing);
            postData.put("mistingFrequency", editedMisting);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String editedSoil = soilEditText.getText().toString().trim();
        if(!species.getSoil().equals(editedSoil) && !editedSoil.isEmpty()){
            try {
                postData.put("soil", editedSoil);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(selectedCategory != null && !selectedCategory.getId().equals(species.getCategoryId())){
            try {
                postData.put("categoryId", selectedCategory.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(selectedPlacement != null && selectedPlacement!=species.getPlacement()){
            try {
                postData.put("placement", selectedPlacement);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        makePatchDataRequest(postData);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarSpeciesEdit);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void setupViewsData() {
        Log.v(TAG, "setupViewsData");
        TextView editTitleView = findViewById(R.id.speciesEditTitleTextView);
        nameEditText = findViewById(R.id.speciesNameEditEditText);
        wateringEditText = findViewById(R.id.speciesWateringFrequencyEditEditText);
        fertilizingEditText = findViewById(R.id.speciesFertilizingFrequencyEditEditText);
        mistingEditText = findViewById(R.id.speciesMistingFrequencyEditEditText);
        placementSpinner = findViewById(R.id.speciesPlacementEditSpinner);
        soilEditText = findViewById(R.id.speciesSoilEditEditText);
        categorySpinner = findViewById(R.id.speciesCategoryEditSpinner);

        if(species!=null){
            Log.v(TAG, "species is not null");
            String editTitle = getResources().getString(R.string.edit_species_title) +
                    " " + species.getId();
            editTitleView.setText(editTitle);
            nameEditText.setText(species.getName());
            wateringEditText.setText(String.valueOf(species.getWateringFrequency()));
            fertilizingEditText.setText(String.valueOf(species.getFertilizingFrequency()));
            mistingEditText.setText(String.valueOf(species.getMistingFrequency()));
            soilEditText.setText(species.getSoil());
            setPlacementSpinnerValues();
            if (categories != null){
                Log.v(TAG, "categories is not null");
                setCategoriesSpinnerValues();
                createAndAttachSpinnerListeners();
            }
        }
    }

    private void setCategoriesSpinnerValues() {
        Log.v(TAG, "setCategoriesSpinnerValues");
        int selectedPosition = 0;
        List<String> categoryNameList = newArrayList();
        for(CategoryDto c:categories){
            categoryNameList.add(c.getName());
            if(c.getId().equals(species.getCategoryId())){
                selectedPosition = categoryNameList.indexOf(c.getName());
            }
        }
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categoryNameList);
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoriesAdapter);
        categorySpinner.setSelection(selectedPosition);
    }

    private void setPlacementSpinnerValues() {
        Log.v(TAG, "setPlacementSpinnerValues");
        List<String> placementNameList = newArrayList();
        placementNameList.add("");
        for(Placement p:placementList){
            placementNameList.add(p.getName());
        }
        ArrayAdapter<String> placementAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, placementNameList);
        placementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placementSpinner.setAdapter(placementAdapter);
        if(species.getPlacement() != null){
            placementSpinner.setSelection(placementAdapter.getPosition(species.getPlacement().getName()));
        }
    }

    private void createAndAttachSpinnerListeners(){
        Log.v(TAG, "createAndAttachSpinnerListeners");
        AdapterView.OnItemSelectedListener categoryListener =
            new AdapterView.OnItemSelectedListener() {
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
        AdapterView.OnItemSelectedListener placementListener =
            new AdapterView.OnItemSelectedListener() {
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

    private void makeGetDataRequest(String url, Class clazz, boolean isArrayRequest) {
        JsonRequest jsonRequest;
        if(isArrayRequest){
            jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseReceived(response, clazz);
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
        } else {
            jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        onGetResponseReceived(response, clazz);
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
        }

        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonRequest);
    }

    private void makePatchDataRequest(JSONObject postData) {
        String url = getRequestUrl() + "species/" + getIntent().getExtras().get("speciesId");

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
        Log.v(TAG, "Post data: " + postData);
        config.getQueue().add(jsonObjectRequest);
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

    private void onGetResponseReceived(JSONArray response, Class clazz) {
        Log.v(TAG, "onGetResponseReceived() for JSONArray");
        if(clazz.getSimpleName().equals("CategoryDto")){
            categories = (CategoryDto[]) config.getGson().fromJson(String.valueOf(response), CategoryDto[].class);
        }
        setupViewsData();
    }

    private void onGetResponseReceived(JSONObject response, Class clazz) {
        Log.v(TAG, "onGetResponseReceived() for JSONObject");
        if(clazz.getSimpleName().equals("SpeciesDto")){
            species = (SpeciesDto) config.getGson().fromJson(String.valueOf(response), clazz);
        }
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