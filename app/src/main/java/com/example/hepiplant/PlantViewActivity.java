package com.example.hepiplant;

import static com.example.hepiplant.helper.LangUtils.getFrequency;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlantViewActivity extends AppCompatActivity {

    private TextView plantName, species, category, watering, fertilizing, misting, soil, location, placement, date;
    private ImageView plantImage;
    private String categoryName = null;
    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PlantDto> plantResponseHandler;
    private PlantDto plant;
    private static final String TAG = "PlantViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_view);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        plantResponseHandler = new JSONResponseHandler<>(config);

        setupViewsData();
        makeGetDataRequest();
        setupToolbar();
    }

    private void setupViewsData(){
        config = (Configuration) getApplicationContext();
        plantName = findViewById(R.id.PlantName);
        species = findViewById(R.id.SpeciesValueView);
        category = findViewById(R.id.CategoryValueView);
        watering = findViewById(R.id.WateringValueView);
        fertilizing = findViewById(R.id.FertilizingValueView);
        misting = findViewById(R.id.MistingValueView);
        soil = findViewById(R.id.SoilValueView);
        location = findViewById(R.id.LocationValueView);
        placement = findViewById(R.id.PlacementValueView);
        date = findViewById(R.id.DateValueView);
        plantImage = findViewById(R.id.plantImage);
    }

    private void setTextsToRealValues(){
        plantName.setText(plant.getName());
        if(plant.getSpecies().getId()!=null && !plant.getSpecies().getName().equals("Brak") && !plant.getSpecies().getName().isEmpty())
            species.setText(plant.getSpecies().getName());
        else
            species.setText("Brak przypisanego gatunku");
        if(plant.getCategoryId()!=null){
            getCategoryName(Integer.parseInt(Objects.requireNonNull(plant.getCategoryId()).toString()));
        }
        else{
            category.setText("Brak przypisanej kategorii");
        }
        watering.setText(String.valueOf(plant.getSchedule().getWateringFrequency()));
        fertilizing.setText(String.valueOf(plant.getSchedule().getFertilizingFrequency()));
        misting.setText(String.valueOf(plant.getSchedule().getMistingFrequency()));
        if(plant.getSpecies().getSoil()!=null && !plant.getSpecies().getSoil().isEmpty())
            soil.setText(plant.getSpecies().getSoil());
        else
            soil.setText("Brak zalecanej gleby");
        if(plant.getLocation()!=null && !plant.getLocation().isEmpty())
            placement.setText(plant.getLocation());
        else
            placement.setText("Brak przypisanego pomieszczenia");
        if(plant.getSpecies().getPlacement()!=null && !plant.getSpecies().getPlacement().getName().isEmpty())
            location.setText(plant.getSpecies().getPlacement().getName());
        else
            location.setText("Brak zalecanego stanowiska");
        date.setText(plant.getPurchaseDate().replaceFirst("00:00:00",""));
        if(plant.getPhoto()!=null){
            getPhotoFromFirebase(plantImage, plant.getPhoto());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                plantImage.setClipToOutline(true);
            }
        }
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+"plants/" + getIntent().getExtras().get("plantId");
        Log.v(TAG, "Invoking plantRequestProcessor"+ url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "onResponse");
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorResponseReceived(error);
                    }
                });
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived()");
        plant = plantResponseHandler.handleResponse(response, PlantDto.class);
        setBottomBarOnItemClickListeners();
        setTextsToRealValues();
    }

    private void onErrorResponseReceived(VolleyError error) {
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

    private static void getPhotoFromFirebase(ImageView photoImageView, String post) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Log.v(TAG, post);

        StorageReference pathReference = storageRef.child(post);
        final long ONE_MEGABYTE = 2048 * 2048;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.v(TAG,"IN on success");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                photoImageView.setImageBitmap(bitmap);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    photoImageView.setClipToOutline(true);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v(TAG,"IN on failure");
                Log.v(TAG,exception.getMessage());
                Log.v(TAG,exception.getCause().toString());
            }
        });
    }

    private void getCategoryName(int id){
        makeGetDataRequest(id);
    }

    private void makeGetDataRequest(int id){
        String url = getRequestUrlById(id);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        categoryName = onGetResponseReceivedCategory(response);
                        Log.v(TAG,"In set category "+categoryName);
                        if(categoryName!=null && !categoryName.equals("Brak"))
                        category.setText(categoryName);
                        else category.setText("Brak przypisanej kategorii");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceivedCategory(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonArrayRequest);
    }

    @NonNull
    private String getRequestUrlById(int id) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "categories/"+id;
    }

    private String onGetResponseReceivedCategory(JSONObject response){
        Log.v(TAG, "onGetResponseReceivedCategory()");
        CategoryDto categoryDto = config.getGson().fromJson(String.valueOf(response), CategoryDto.class);
        Log.v(TAG,categoryDto.getName());
        return categoryDto.getName();
    }

    private void onErrorResponseReceivedCategory(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarPlantView);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlantAddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_plant, menu);
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
            case R.id.deletePlant:
                Intent intent3 = new Intent(this, PopUpDelete.class);
                intent3.putExtra("plantId", plant.getId());
                intent3.putExtra("photo", plant.getPhoto());
                startActivity(intent3);
                return true;
            case R.id.editPlant:
                Intent intent = new Intent(getApplicationContext(), PlantEditActivity.class);
                intent.putExtra("plantId", plant.getId());
                startActivity(intent);
                return true;
            case R.id.miProfile:
                Intent intent2 = new Intent(this, UserActivity.class);
                startActivity(intent2);
                return true;
            case R.id.archivePlant:
                Intent intent1 = new Intent(this,ArchiveActivity.class);
                intent1.putExtra("plantId", plant.getId());
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }

}