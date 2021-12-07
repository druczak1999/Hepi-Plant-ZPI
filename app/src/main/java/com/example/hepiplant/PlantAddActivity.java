package com.example.hepiplant;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
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


public class PlantAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddPlant";

    private Button dateEditText, addPlantButton;
    private ImageView addImageButton;
    private EditText plantName, location, wateringText;
    private String img_str;
    private SpeciesDto speciesDto;
    private SpeciesDto[] speciesDtos;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;
    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PlantDto> plantResponseHandler;
    private JSONResponseHandler<SpeciesDto> speciesResponseHandler;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        plantResponseHandler = new JSONResponseHandler<>(config);
        speciesResponseHandler = new JSONResponseHandler<>(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);
        img_str=null;
        setupViewsData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                dateEditText.setText(data.getExtras().getString("date"));
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.v(TAG, "cropActivity");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                addImageButton.setImageURI(resultUri);
                img_str=resultUri.toString();
                addImageButton.setClipToOutline(true);
                saveImageToFirebase();
                Log.v(TAG, img_str);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner speciesSpinner = (Spinner) parent;
        Spinner categorySpinner = (Spinner) parent;
        String selectedItem = (String) parent.getItemAtPosition(position);
        if (speciesSpinner.getId() == R.id.editSpecies) {
            for(SpeciesDto s : speciesDtos){
                if(s.getName().equals(selectedItem)) {
                    speciesDto = s;
                }

            }
        }
        if(categorySpinner.getId()==R.id.editCategory){
            for(CategoryDto c : categoryDtos){
                if(c.getName().equals(selectedItem)) selectedCategory = c;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

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
        Toolbar toolbar = findViewById(R.id.plantAddToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
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
        setupToolbar();
        setOnClickListeners();
        createNotificationChannel();
    }

    private void createNotificationChannel(){
        CharSequence charSequence = "Hepi Plant reminder channel";
        String description = "Channel for Hepi Plant";
        int importance  = NotificationManager.IMPORTANCE_DEFAULT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("notifyHepiPlant",charSequence,importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void setOnClickListeners(){
        setBottomBarOnItemClickListeners();

        dateEditText.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
            intent.putExtra("event","plant");
            startActivityForResult(intent, 1);
        });

        addImageButton.setOnClickListener(v -> cropImage());

        onClickAddPlant();
    }

    private void onClickAddPlant(){
        addPlantButton.setOnClickListener(v -> {
            if(plantName.getText()!=null) postRequestPlant();
            else Toast.makeText(getApplicationContext(),R.string.plant_name,Toast.LENGTH_LONG).show();
        });
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

    private void postRequestPlant(){
        String url = getRequestUrl()+"plants";
        JSONObject speciesJson = makeSpeciesJSON();
        JSONObject scheduleJ = makeScheduleJSON();
        JSONObject postData = makePostDataJson(speciesJson,scheduleJ);
        Log.v(TAG, String.valueOf(postData));
        Log.v(TAG, "Invoking requestProcessor");
        Log.v(TAG, String.valueOf(postData));Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.POST, url, postData, RequestType.OBJECT,
                (Response.Listener<JSONObject>) response -> {
                    onPostResponsePlant(response);
                    Toast.makeText(getApplicationContext(), R.string.add_plant, Toast.LENGTH_LONG).show();
                    finish();
                }, this::onErrorResponsePlant);
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
            int watering = Integer.parseInt(String.valueOf(wateringText.getText()));
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
            if(plantName.getText()==null)  postData.put("name", "");
            else postData.put("name", plantName.getText().toString());
            if (dateEditText.getText()==null || dateEditText.getText().toString().equals("Wybierz datę"))
                postData.put("purchaseDate", null);
            else postData.put("purchaseDate", dateEditText.getText().toString() + " 00:00:00");
            Log.v(TAG,dateEditText.getText().toString());
            Log.v(TAG,location.getText().toString());
            if (location.getText()==null){
                postData.put("location", null);
            }
            else postData.put("location", location.getText().toString());
            postData.put("photo", img_str);
            Log.v(TAG, selectedCategory.getId().toString());
            postData.put("categoryId", selectedCategory.getId());
            if(speciesDto == null ) postData.put("species", null);
            else postData.put("species", speciesJson);
            postData.put("userId", config.getUserId());
            postData.put("schedule", scheduleJ);
            postData.put("categoryId",selectedCategory.getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void onPostResponsePlant(JSONObject response){
        Log.v(TAG, "ONResponse plant");
        PlantDto data = new PlantDto();
        data = plantResponseHandler.handleResponse(response, PlantDto.class);
        Intent intent = new Intent(getApplicationContext(),MainTabsActivity.class);
        startActivity(intent);
        Log.v(TAG,"Events: "+data.getEvents());
        if(config.isNotifications())
            setupNotifications(data);
        Intent intent1 = new Intent(getApplicationContext(),MainTabsActivity.class);
        startActivity(intent1);
    }

    private void setupNotifications(PlantDto data) {
        if(data.getEvents()!=null && !data.getEvents().isEmpty()){
            for (EventDto event: data.getEvents()) {
                if(!event.isDone()){
                    Intent intent = new Intent(this, AlarmBroadcast.class);
                    intent.putExtra("eventName",event.getEventName());
                    intent.putExtra("eventDescription",event.getEventDescription());
                    intent.putExtra("eventId",event.getId());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this,event.getId().intValue(), intent, 0);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        calendar.setTime(simpleDateFormat.parse(event.getEventDate().substring(0,10)+ " "+config.getHourOfNotifications()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                }
            }
        }
    }

    private void onErrorResponsePlant(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(), R.string.add_plant_failed, Toast.LENGTH_LONG).show();
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
        addImageButton.setDrawingCacheEnabled(true);
        addImageButton.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) addImageButton.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataB = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(dataB);
        uploadTask.addOnFailureListener(exception -> Toast.makeText(getApplicationContext(),R.string.upload_photo_failed,Toast.LENGTH_LONG).show())
                .addOnSuccessListener(taskSnapshot -> {});
        img_str = path;
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
        Spinner spinnerGat = (Spinner) findViewById(R.id.editSpecies);
        spinnerGat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGat.setAdapter(dtoArrayAdapter);
        spinnerGat.setSelection(species.indexOf("Brak"));
    }

    private void onGetResponseCategories(JSONArray response){
        categoryDtos = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
        List<String> categories = new ArrayList<>();
        for (CategoryDto categoryDto : categoryDtos) {
            categories.add(categoryDto.getName());
        }
        Log.v(TAG,"DL: "+categoryDtos.length);
        getCategories(categories);
    }

    private void getCategoriesFromDB() {
        String url = getRequestUrl() + "categories";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseCategories, this::onErrorResponsePlant);
    }

    private void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        Spinner spinnerCat = findViewById(R.id.editCategory);
        spinnerCat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }
}
