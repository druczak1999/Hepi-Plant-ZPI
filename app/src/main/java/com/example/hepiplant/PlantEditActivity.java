package com.example.hepiplant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class PlantEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "PlantEditActivity";

    private EditText plantName, watering, fertilizing, misting, placement;
    private Spinner spinnerGat;
    private ImageView plantImage;
    private Button date, editPlant;
    private SpeciesDto speciesDto;
    private SpeciesDto[] speciesDtos;
    private String img_str;
    private Long plantId;
    private PlantDto plant;
    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PlantDto> plantResponseHandler;
    private JSONResponseHandler<SpeciesDto> speciesResponseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        plantResponseHandler = new JSONResponseHandler<>(config);
        speciesResponseHandler = new JSONResponseHandler<>(config);

        setupToolbar();
        setBottomBarOnItemClickListeners();
        setupViewsData();
        makeGetDataRequest();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = (String) parent.getItemAtPosition(position);
        for(SpeciesDto s : speciesDtos){
            if(s.getName().equals(selectedItem)){
                speciesDto = s;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, plant.getPurchaseDate());
                date.setText(data.getExtras().getString("date"));
                Log.v(TAG, plant.getPurchaseDate());
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.v(TAG, "cropActivity");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                plantImage.setImageURI(resultUri);
                img_str=resultUri.toString();
                saveImageToFirebase();
                plantImage.setClipToOutline(true);
                Log.v(TAG, img_str);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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
        Toolbar toolbar = findViewById(R.id.plantEditToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
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
    }

    private void setValuesToEdit(){
        plantId = getIntent().getExtras().getLong("plantId");
        plantName.setText(plant.getName());
        if(plant.getPhoto()!=null)
            getPhotoFromFirebase(plantImage, plant.getPhoto());
        watering.setText(String.valueOf(plant.getSchedule().getWateringFrequency()));
        fertilizing.setText(String.valueOf(plant.getSchedule().getFertilizingFrequency()));
        misting.setText(String.valueOf(plant.getSchedule().getMistingFrequency()));
        placement.setText(plant.getLocation());
        date.setText(plant.getPurchaseDate().replaceFirst("00:00:00",""));
        Log.v(TAG,"plant Id: "+plantId);
    }

    private void getPhotoFromFirebase(ImageView photoImageView, String post) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Log.v(TAG, post);
        StorageReference pathReference = storageRef.child(post);
        cacheImage(pathReference,photoImageView);
        photoImageView.setClipToOutline(true);
    }

    private void cacheImage(StorageReference storageRef, ImageView photoImageView){
        Glide.with(this.getApplicationContext())
                .load(storageRef)
                .into(photoImageView);
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+"plants/" + getIntent().getExtras().get("plantId");
        Log.v(TAG, "Invoking RequestProcessor"+ url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived()");
        plant = plantResponseHandler.handleResponse(response, PlantDto.class);
        setOnClickListeners();
        getSpeciesFromDB();
        setValuesToEdit();
    }

    private void onErrorResponseReceived(VolleyError error) {
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void onGetResponseSpecies(JSONArray response){
        SpeciesDto[] data = new SpeciesDto[]{};
        data = speciesResponseHandler.handleArrayResponse(response, SpeciesDto[].class);
        speciesDtos = data;
        List<String> sp = new ArrayList<>();
        for (SpeciesDto datum : data) {
            sp.add(datum.getName());
        }
        getSpecies(sp);
    }

    private void getSpeciesFromDB() {
        String url = getRequestUrl() + "species";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseSpecies, this::onErrorResponsePlant);
    }

    private void getSpecies(List<String> species) {
        Log.v(TAG, "Species size" + species.size());
        spinnerGat = findViewById(R.id.speciesEdit);
        spinnerGat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGat.setAdapter(dtoArrayAdapter);
        if(plant.getSpecies().getId() != null) {
            for(SpeciesDto s : speciesDtos) {
                if(Objects.equals(s.getId(), plant.getSpecies().getId())) {
                    speciesDto = s;
                    spinnerGat.setSelection(species.indexOf(s.getName()));
                }
            }
        }
    }

    private void onErrorResponsePlant(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(), getText(R.string.edit_saved_failed), Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
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

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
            startActivity(intent);
        });

        Button buttonForum = findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
            startActivity(intent);
        });
    }

    private void setOnClickListeners(){
        date.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
            intent.putExtra("event","plant");
            startActivityForResult(intent, 1);
        });
        plantImage.setOnClickListener(v -> cropImage());

        onClickAddPlant();
    }

    private void onClickAddPlant(){
        Log.v(TAG,"onClick Edit");
        editPlant.setOnClickListener(v -> {
            if(plantName.getText()!=null) patchRequestPlant();
            else Toast.makeText(getApplicationContext(),getText(R.string.plant_name),Toast.LENGTH_LONG).show();
        });
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private void saveImageToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String [] array = img_str.split("/");
        String path = "userPlants/"+config.getUserId()+"/"+array[array.length-1];
        StorageReference imagesRef = storageRef.child(path);
        plantImage.setDrawingCacheEnabled(true);
        plantImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) plantImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataB = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(dataB);
        uploadTask.addOnFailureListener(exception -> Toast.makeText(getApplicationContext(),getText(R.string.upload_photo_failed),Toast.LENGTH_LONG).show())
                .addOnSuccessListener(taskSnapshot -> {});
        img_str = path;
        Log.v(TAG, img_str);
    }

    private void patchRequestSchedule(){
        String url = getRequestUrl()+"schedules/"+plant.getSchedule().getId();
        JSONObject scheduleJ = makeScheduleJSON();
        requestProcessor.makeRequest(Request.Method.PATCH, url, scheduleJ, RequestType.OBJECT,
                (Response.Listener<JSONObject>) response -> {}, this::onErrorResponsePlant);
    }

    private void patchRequestPlant(){
        patchRequestSchedule();
        String url = getRequestUrl()+"plants/"+getIntent().getExtras().getLong("plantId");
        JSONObject speciesJson = makeSpeciesJSON();
        JSONObject scheduleJ = makeScheduleJSON();
        JSONObject postData = makePostDataJson(speciesJson,scheduleJ);
        Log.v(TAG, String.valueOf(postData));
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData, RequestType.OBJECT,
                (Response.Listener<JSONObject>) this::onPatchResponsePlant, this::onErrorResponsePlant);
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
            scheduleJ.put("id", plant.getSchedule().getId());
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
            if(plantName.getText()==null)  postData.put("name", "");
            else postData.put("name", plantName.getText().toString());
            Log.v(TAG,date.getText().toString());
            Log.v(TAG, date.getText().toString());
            if (date.getText()==null || date.getText().toString().equals(getResources().getString(R.string.pick_date)))
                postData.put("purchaseDate", null);
            else {
                Log.v(TAG, "put: "+date.getText().toString());
                postData.put("purchaseDate", date.getText().toString().substring(0,10).trim() + " "+config.getHourOfNotifications());
            }
            Log.v(TAG,placement.getText().toString());
            if (placement.getText()==null){
                postData.put("location", null);
            }
            else postData.put("location", placement.getText().toString());
            postData.put("photo", img_str);
            Log.v(TAG, "SPINNER" + spinnerGat.getSelectedItemId());
            if(speciesDto == null){
                Log.v(TAG, "SPINNER" + spinnerGat.getSelectedItemId());
                postData.put("species", null);
            }
            else postData.put("species", speciesJson);
            postData.put("schedule", scheduleJ);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void onPatchResponsePlant(JSONObject response){
        Log.v(TAG, "ONResponse");
        PlantDto data = new PlantDto();
        data = plantResponseHandler.handleResponse(response, PlantDto.class);
        Log.v(TAG,"Events: "+data.getEvents());
        setupNotifications(data);
        Toast.makeText(getApplicationContext(),getText(R.string.edit_saved),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(),MainTabsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setupNotifications(PlantDto data) {
        if(data.getEvents()!=null && !data.getEvents().isEmpty()) {
            for (EventDto event : data.getEvents()) {
                if (!event.isDone()) {
                    Log.v(TAG, event.getEventName());
                    Intent intent = new Intent(this, AlarmBroadcast.class);
                    intent.putExtra("eventName", event.getEventName());
                    intent.putExtra("eventDescription", event.getEventDescription());
                    intent.putExtra("eventId", event.getId());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, event.getId().intValue(), intent, 0);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Log.v(TAG, simpleDateFormat.parse(event.getEventDate()).toString());
                        calendar.setTime(simpleDateFormat.parse(event.getEventDate()+ " "+config.getHourOfNotifications()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.v(TAG, String.valueOf(calendar.getTime()));
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        }
    }
}
