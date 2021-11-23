package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hepiplant.configuration.Configuration;

public class EventViewActivity extends AppCompatActivity {

    private TextView plantName, eventName, eventDate, eventDescription;
    private ImageView eventImage;
    private Configuration config;
    private static final String TAG = "EventViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        setupViewsData();
    }
    private void setupViewsData(){
        config = (Configuration) getApplicationContext();
        plantName = findViewById(R.id.EventPlantNameValueView);
        eventName = findViewById(R.id.EventName);
        eventImage = findViewById(R.id.eventImage);
        eventDate = findViewById(R.id.EventDateValueView);
        eventDescription = findViewById(R.id.EventDescriptionValue);
        setupToolbar();
        setTextsToRealValues();
    }

    private void setTextsToRealValues(){
        plantName.setText(getIntent().getExtras().getString("plantName"));
        String name = getIntent().getExtras().getString("eventName");
        eventName.setText(name);
        if(name.toLowerCase().equals("podlewanie"))
            eventImage.setImageResource(R.drawable.watering_icon);
        else if(name.toLowerCase().equals("zraszanie"))
            eventImage.setImageResource(R.drawable.misting_icon);
        else if(name.toLowerCase().equals("nawożenie"))
            eventImage.setImageResource(R.drawable.fertilization_icon);
        eventDate.setText(getIntent().getExtras().getString("eventDate"));
        eventDescription.setText(getIntent().getExtras().getString("eventDescription"));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.includeToolbarPlantView);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private Intent prepareIntent(){
        Intent intent = new Intent(this,EventEditActivity.class);
        intent.putExtra("eventName", getIntent().getExtras().getString("eventName"));
        intent.putExtra("eventDate",getIntent().getExtras().getString("eventDate"));
        intent.putExtra("eventDescription",getIntent().getExtras().getString("eventDescription"));
        intent.putExtra("eventId",getIntent().getExtras().getLong("eventId"));
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if(getIntent().getExtras().getString("place").equals("archive"))
            menuInflater.inflate(R.menu.menu_archive_event, menu);
        else
            menuInflater.inflate(R.menu.menu_event, menu);
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
                Intent intent2 = new Intent(this, UserActivity.class);
                startActivity(intent2);
                return true;
            case R.id.editEvent:
                Intent intent = prepareIntent();
                startActivity(intent);
                return true;
            case R.id.deleteEvent:
                Intent intent1 = new Intent(this, PopUpDeleteEvent.class);
                intent1.putExtra("eventId",getIntent().getExtras().getLong("eventId"));
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}