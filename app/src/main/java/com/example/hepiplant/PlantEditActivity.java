package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private EditText plantName, watering, fertilizing, misting, placement;
    private Spinner spinnerGat;
    private ImageView plantImage;
    private Button date, editPlant;
    private Configuration config;
    private static final String TAG = "PlantEditActivity";
    private SpeciesDto speciesDto;
    private SpeciesDto[] speciesDtos;
    private String img_str;
    private Long plantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant);
        config = (Configuration) getApplicationContext();
        setupViewsData();
        setValuesToEdit();
    }

    private void setupViewsData(){
        editPlant = findViewById(R.id.buttonEditPlant);
        plantName = findViewById(R.id.PlantNameEdit);
        spinnerGat = findViewById(R.id.speciesEdit);
        watering = findViewById(R.id.wateringEdit);
        fertilizing = findViewById(R.id.fertilizingEdit);
        misting = findViewById(R.id.mistingEdit);
        placement = findViewById(R.id.placementEdit);
        date = findViewById(R.id.dateEdit);
        plantImage = findViewById(R.id.plantImageEdit);
        getSpeciesFromDB();
        setBottomBarOnItemClickListeners();
        setOnClickListeners();
    }

    private void setValuesToEdit(){
        plantName.setText(getIntent().getExtras().getString("name"));
        plantImage.setImageURI(Uri.parse(getIntent().getExtras().getString("photo")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            plantImage.setClipToOutline(true);
        }
        watering.setText(getIntent().getExtras().getString("watering"));
        fertilizing.setText(getIntent().getExtras().getString("fertilizing"));
        misting.setText(getIntent().getExtras().getString("misting"));
        placement.setText(getIntent().getExtras().getString("placement"));
        date.setText(getIntent().getExtras().getString("date"));
        plantId = getIntent().getExtras().getLong("plantId");
        Log.v(TAG,"plant Id: "+plantId);
    }

    private void onGetResponseSpecies(String response){
        String str=onResponseStr(response);
        SpeciesDto[] data = new SpeciesDto[]{};
        Gson gson = new Gson();
        data = gson.fromJson(String.valueOf(str), SpeciesDto[].class);
        speciesDtos = data;
        List<String> sp = new ArrayList<String>();
        sp.add("Brak");
        for (int i = 0; i < data.length; i++) {
            sp.add(data[i].getName());
        }
        getSpecies(sp);
    }

    private void getSpeciesFromDB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getRequestUrl() + "species";
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        onGetResponseSpecies(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponsePlant(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

        queue.add(jsonArrayRequest);
    }

    private void getSpecies(List<String> species) {
        Log.v(TAG, "Species size" + species.size());
        spinnerGat = (Spinner) findViewById(R.id.speciesEdit);
        spinnerGat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGat.setAdapter(dtoArrayAdapter);
    }

    private String onResponseStr(String response){
        String str = null;
        try {
            str = new String(response.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    private void onErrorResponsePlant(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner speciesSpinner = (Spinner) parent;
        if (speciesSpinner.getId() == R.id.speciesEdit) {
            if(position==0) speciesDto = null;
            else speciesDto = speciesDtos[position-1];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlantsListActivity.class);
                startActivity(intent);
            }
        });

        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setOnClickListeners(){
        setBottomBarOnItemClickListeners();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        plantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });

        onClickAddPlant();
    }

    private void onClickAddPlant(){
        Log.v(TAG,"onClick Edit");
        editPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(plantName.getText()!=null && !plantName.getText().toString().equals("...")) patchRequestPlant();
                else Toast.makeText(getApplicationContext(),"Podaj nazwę rośliny",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, data.getExtras().getString("data"));
                date.setText(data.getExtras().getString("data"));
                Log.v(TAG, date.getText().toString());
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.v(TAG, "cropActivity");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                plantImage.setImageURI(resultUri);
                img_str=resultUri.toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    plantImage.setClipToOutline(true);
                }
                Log.v(TAG, img_str);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void patchRequestSchedule(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getRequestUrl()+"schedules/"+getIntent().getExtras().getLong("scheduleId");
        JSONObject scheduleJ = makeScheduleJSON();
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PATCH, url, scheduleJ,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponsePlant(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

        queue.add(jsonArrayRequest);
    }

    private void patchRequestPlant(){
        patchRequestSchedule();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getRequestUrl()+"plants/"+getIntent().getExtras().getLong("plantId");
        JSONObject speciesJson = makeSpeciesJSON();
        JSONObject scheduleJ = makeScheduleJSON();
        JSONObject postData = makePostDataJson(speciesJson,scheduleJ);
        Log.v(TAG, String.valueOf(postData));
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PATCH, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        onPostResponsePlant(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponsePlant(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

        queue.add(jsonArrayRequest);
    }

    private JSONObject makeSpeciesJSON(){
        JSONObject speciesJson = new JSONObject();

        if(speciesDto!=null)
        {
            try {
                speciesJson.put("id", speciesDto.getId());
                speciesJson.put("name", speciesDto.getName());
                speciesJson.put("wateringFrequency", speciesDto.getWateringFrequency());
                speciesJson.put("fertilizingFrequency", speciesDto.getFertilizingFrequency());
                speciesJson.put("mistingFrequency", speciesDto.getMistingFrequency());
                speciesJson.put("soil", speciesDto.getSoil());
                speciesJson.put("categoryId", speciesDto.getCategoryId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return speciesJson;
    }

    private JSONObject makeScheduleJSON() {
        JSONObject scheduleJ = new JSONObject();

        try {
            scheduleJ.put("id",getIntent().getExtras().getLong("scheduleId"));
            scheduleJ.put("wateringFrequency", watering.getText());
            scheduleJ.put("fertilizingFrequency", fertilizing.getText());
            scheduleJ.put("mistingFrequency", misting.getText());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scheduleJ;
    }

    private JSONObject makePostDataJson(JSONObject speciesJson,JSONObject scheduleJ){
        JSONObject postData = new JSONObject();

        try {
            if(plantName.getText().toString().equals("..."))  postData.put("name", "");
            else postData.put("name", plantName.getText().toString());
            Log.v(TAG,date.getText().toString());
            Log.v(TAG, date.getText().toString());
            if (date.getText().toString().equals("...") || date.getText().toString().equals("Wybierz datę"))
                postData.put("purchaseDate", null);
            else {
                Log.v(TAG, "put: "+date.getText().toString());
                postData.put("purchaseDate", date.getText().toString().trim() + " 00:00:00");
            }
            Log.v(TAG,placement.getText().toString());
            if (placement.getText().toString().equals("...")){
                postData.put("location", null);
            }
            else postData.put("location", placement.getText().toString());
            postData.put("photo", img_str);

            if(speciesDto == null ) postData.put("species", null);
            else postData.put("species", speciesJson);
            postData.put("userId", config.getUserId());
            postData.put("schedule", scheduleJ);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void onPostResponsePlant(JSONObject response){
        Log.v(TAG, "ONResponse");
        String str = String.valueOf(response); //http request
        PlantDto data = new PlantDto();
        Gson gson = new Gson();
        data = gson.fromJson(str, PlantDto.class);
        Intent intent = new Intent(getApplicationContext(),PlantsListActivity.class);
        startActivity(intent);
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }

}