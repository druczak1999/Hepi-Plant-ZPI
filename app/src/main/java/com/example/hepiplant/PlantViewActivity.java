package com.example.hepiplant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PlantViewActivity extends AppCompatActivity {

    TextView plantName, species, category, watering, fertilizing, misting, soil, location, placement, date;
    ImageView plantImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_view);
        setupViewsData();
    }

    private void setupViewsData(){
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

    private void setTextsToRealValues(){
        plantName.setText(getIntent().getExtras().getString("plantName"));
        species.setText(getIntent().getExtras().getString("species"));
        category.setText(getIntent().getExtras().getString("category"));
        watering.setText("Co "+getIntent().getExtras().getString("watering")+" dni");
        fertilizing.setText("Co "+getIntent().getExtras().getString("fertilizing")+" dni");
        misting.setText("Co "+getIntent().getExtras().getString("misting")+" dni");
        soil.setText(getIntent().getExtras().getString("soil"));
        placement.setText(getIntent().getExtras().getString("location"));
        location.setText(getIntent().getExtras().getString("placement").toLowerCase());
        date.setText(getIntent().getExtras().getString("date").replaceFirst("00:00:00",""));
        if(!getIntent().getExtras().getString("photo").isEmpty())
        plantImage.setImageURI(Uri.parse(getIntent().getExtras().getString("photo")));

    }
}