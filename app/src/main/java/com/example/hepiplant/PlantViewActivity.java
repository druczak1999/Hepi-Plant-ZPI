package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlantViewActivity extends AppCompatActivity {

    TextView plantName, species, category, watering, fertilizing, misting, soil, location, placement, date;
    ImageView plantImage;
    String categoryName = null;
    private Configuration config;
    private static final String TAG = "PlantViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_view);
        setupViewsData();
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
        setBottomBarOnItemClickListeners();
        setTextsToRealValues();
    }

    private void setTextsToRealValues(){
        plantName.setText(getIntent().getExtras().getString("plantName"));
        species.setText(getIntent().getExtras().getString("species"));
        if(getIntent().getExtras().getString("category")!=null && !getIntent().getExtras().getString("category").isEmpty()){
            getCategoryName(Integer.parseInt(Objects.requireNonNull(getIntent().getExtras().getString("category"))));

        }
        else{
            category.setText("");
        }
        watering.setText("Co "+getIntent().getExtras().getString("watering")+" dni");
        fertilizing.setText("Co "+getIntent().getExtras().getString("fertilizing")+" dni");
        misting.setText("Co "+getIntent().getExtras().getString("misting")+" dni");
        soil.setText(getIntent().getExtras().getString("soil"));
        placement.setText(getIntent().getExtras().getString("location"));
        location.setText(getIntent().getExtras().getString("placement").toLowerCase());
        date.setText(getIntent().getExtras().getString("date").replaceFirst("00:00:00",""));
        if(!getIntent().getExtras().getString("photo").isEmpty())
        plantImage.setImageURI(Uri.parse(getIntent().getExtras().getString("photo")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           plantImage.setClipToOutline(true);
        }

    }

    private void getCategoryName(int id){
        makeGetDataRequest(id);
    }

    private void makeGetDataRequest(int id){
        String url = getRequestUrl(id);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        categoryName = onGetResponseReceived(response);
                        Log.v(TAG,"In set categoyr "+categoryName);
                        if(categoryName!=null)
                        category.setText(categoryName);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
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
    private String getRequestUrl(int id) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "categories/"+id;
    }

    private String onGetResponseReceived(JSONObject response){
        Log.v(TAG, "onGetResponseReceived()");
        CategoryDto categoryDto = config.getGson().fromJson(String.valueOf(response), CategoryDto.class);
        Log.v(TAG,categoryDto.getName());
        return categoryDto.getName();
    }

    private void onErrorResponseReceived(VolleyError error){
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

    private Intent prepareIntent(){
        Intent intent = new Intent(getApplicationContext(),EditPlantActivity.class);
        intent.putExtra("name",plantName.getText().toString());
        intent.putExtra("photo",getIntent().getExtras().getString("photo"));
        intent.putExtra("species",species.getText());
        intent.putExtra("watering",getIntent().getExtras().getString("watering"));
        intent.putExtra("fertilizing",getIntent().getExtras().getString("fertilizing"));
        intent.putExtra("misting",getIntent().getExtras().getString("misting"));
        intent.putExtra("placement",placement.getText());
        intent.putExtra("date",date.getText());
        Log.v(TAG, "Id: "+getIntent().getExtras().getLong("plantId"));
        intent.putExtra("plantId",getIntent().getExtras().getLong("plantId"));
        intent.putExtra("scheduleId",getIntent().getExtras().getLong("scheduleId"));
        return intent;
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
                Toast.makeText(this.getApplicationContext(),"Informacje",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.deletePlant:
                Intent intent3 = new Intent(this, PopUpDelete.class);
                intent3.putExtra("plantId",getIntent().getExtras().getLong("plantId"));
                startActivity(intent3);
                return true;
            case R.id.editPlant:
                Intent intent = prepareIntent();
                startActivity(intent);
                return true;
            case R.id.miProfile:
                Intent intent2 = new Intent(this, UserActivity.class);
                startActivity(intent2);
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