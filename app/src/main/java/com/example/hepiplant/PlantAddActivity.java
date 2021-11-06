package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;


public class PlantAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddPlant";
    private Button dateEditText, addPlantButton;
    private ImageView addImageButton;
    private Spinner spinnerGat, spinnerCat;
    private EditText plantName, location, wateringText;
    private String img_str;
    private int categoryId, watering;
    private SpeciesDto speciesDto;
    private SpeciesDto[] speciesDtos;
    private Configuration config;
    private static final int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        config = (Configuration) getApplicationContext();
        img_str=null;
        setupViewsData();
    }

    private void setupViewsData() {
        plantName = findViewById(R.id.editPlantName);
        location = findViewById(R.id.editPlantLocation);
        wateringText = findViewById(R.id.editWatering);
        addPlantButton = findViewById(R.id.buttonAddPlant);
        dateEditText = findViewById(R.id.editPlantDate);
        addImageButton = findViewById(R.id.editImageBut);
        getSpeciesFromDB();
        getCategoriesFromDB();
        setOnClickListeners();
    }

    private void setOnClickListeners(){
        setBottomBarOnItemClickListeners();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 1);
            }
        });
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });

        onClickAddPlant();
    }

    private void onClickAddPlant(){
        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(plantName.getText()!=null && !plantName.getText().toString().equals("...")) postRequestPlant();
                else Toast.makeText(getApplicationContext(),"Podaj nazwę rośliny",Toast.LENGTH_LONG).show();
            }
        });
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

    private void postRequestPlant(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getRequestUrl()+"plants";
        JSONObject speciesJson = makeSpeciesJSON();
        JSONObject scheduleJ = makeScheduleJSON();
        JSONObject postData = makePostDataJson(speciesJson,scheduleJ);
        Log.v(TAG, String.valueOf(postData));
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            onPostResponsePlant(response);
                        }
                        finish();
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
            watering = Integer.parseInt(String.valueOf(wateringText.getText()));
            Log.v(TAG, String.valueOf(watering));
            scheduleJ.put("wateringFrequency", watering);
            if(speciesDto!=null){
                scheduleJ.put("fertilizingFrequency", speciesDto.getFertilizingFrequency());
                scheduleJ.put("mistingFrequency", speciesDto.getMistingFrequency());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scheduleJ;
    }

    private JSONObject makePostDataJson(JSONObject speciesJson,JSONObject scheduleJ){
        JSONObject postData = new JSONObject();
        try {
            Log.v(TAG,dateEditText.getText().toString());
            if(plantName.getText().toString().equals("..."))  postData.put("name", "");
            else postData.put("name", plantName.getText().toString());
            Log.v(TAG,dateEditText.getText().toString());
            if (dateEditText.getText().toString().equals("...") || dateEditText.getText().toString().equals("Wybierz datę"))
                postData.put("purchaseDate", null);
            else postData.put("purchaseDate", dateEditText.getText().toString() + " 00:00:00");
            Log.v(TAG,location.getText().toString());
            if (location.getText().toString().equals("...")){
                postData.put("location", null);
            }
            else postData.put("location", location.getText().toString());
            postData.put("photo", img_str);
            postData.put("categoryId", categoryId);
            if(speciesDto == null ) postData.put("species", null);
            else postData.put("species", speciesJson);
            postData.put("userId", config.getUserId());
            postData.put("schedule", scheduleJ);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onPostResponsePlant(JSONObject response){
        Log.v(TAG, "ONResponse");
        String str = String.valueOf(response); //http request
        PlantDto data = new PlantDto();
        data = config.getGson().fromJson(str, PlantDto.class);
    }

    private void onErrorResponsePlant(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
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
                dateEditText.setText(data.getExtras().getString("data"));
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.v(TAG, "cropActivity");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                addImageButton.setImageURI(resultUri);
                img_str=resultUri.toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addImageButton.setClipToOutline(true);
                }
                saveImageToFirebase();
                Log.v(TAG, img_str);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void saveImageToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String [] array = img_str.split("/");
        String path = "userPlants/"+config.getUserId()+"/"+array[array.length-1];
        StorageReference imagesRef = storageRef.child(path);
        addImageButton.setDrawingCacheEnabled(true);
        addImageButton.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) addImageButton.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataB = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(dataB);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(),"Fail in upload image",Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),"Success in upload image",Toast.LENGTH_LONG).show();
            }
        });
        img_str = path;
        Log.v(TAG, img_str);
    }

    private void onGetResponseSpecies(String response){
        String str=onResponseStr(response);
        SpeciesDto[] data = new SpeciesDto[]{};
        data = config.getGson().fromJson(String.valueOf(str), SpeciesDto[].class);
        speciesDtos = data;
        List<String> sp = new ArrayList<String>();
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
        spinnerGat = (Spinner) findViewById(R.id.editSpecies);
        spinnerGat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGat.setAdapter(dtoArrayAdapter);
    }

    private void onGetResponseCategories(String response){
        String str = onResponseStr(response);
        CategoryDto[] data = new CategoryDto[]{};
        data = config.getGson().fromJson(String.valueOf(str), CategoryDto[].class);
        List<String> categories = new ArrayList<String>();
        for (int i = 0; i < data.length; i++) {
            categories.add(data[i].getName());
        }
        getCategories(categories);
    }

    private void getCategoriesFromDB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getRequestUrl() + "categories";
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        onGetResponseCategories(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { onErrorResponsePlant(error);}
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        queue.add(jsonArrayRequest);
    }

    private void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        spinnerCat = (Spinner) findViewById(R.id.editCategory);
        spinnerCat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
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

    //Methods for spinners
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner speciesSpinner = (Spinner) parent;
        Spinner categorySpinner = (Spinner) parent;
        if (speciesSpinner.getId() == R.id.editSpecies) {
            speciesDto = speciesDtos[position];
        }
        if (categorySpinner.getId() == R.id.editCategory) {
            categoryId = position+1;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}
