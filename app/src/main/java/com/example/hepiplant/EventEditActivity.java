package com.example.hepiplant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventEditActivity extends AppCompatActivity {

    private EditText eventName, eventDescription;
    private Button eventDate, saveEvent;
    private ImageView eventImage;
    private Configuration config;
    private static final String TAG = "EventEditActivity";
    private EventDto eventDto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        config = (Configuration) getApplicationContext();
        setupViewsData();
    }

    private void setupViewsData(){
        eventName = findViewById(R.id.eventEditTitle);
        eventDescription = findViewById(R.id.eventDescriptionEdit);
        eventDate = findViewById(R.id.eventDateEdit);
        saveEvent = findViewById(R.id.editEventButton);
        eventImage = findViewById(R.id.editImageBut);
        setBottomBarOnItemClickListeners();
        setOnClickListeners();
        setValuesToEdit();
    }

    private void setValuesToEdit(){
        String name = getIntent().getExtras().getString("eventName");
        eventName.setText(name);
        eventDate.setText(getIntent().getExtras().getString("eventDate"));
        eventDescription.setText(getIntent().getExtras().getString("eventDescription"));
        if(name.toLowerCase().equals("podlewanie"))
            eventImage.setImageResource(R.drawable.podelwanie);
        else if(name.toLowerCase().equals("zraszanie"))
            eventImage.setImageResource(R.drawable.zraszanie);
        else if(name.toLowerCase().equals("nawożenie"))
            eventImage.setImageResource(R.drawable.nawozenie);

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

    private void setOnClickListeners(){
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        onClickEditEvent();
    }

    private void onClickEditEvent(){
        Log.v(TAG,"onClick Edit");
        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eventName.getText()!=null && !eventName.getText().toString().equals(" ")) ;
                else Toast.makeText(getApplicationContext(),"Podaj tytuł wydarzenia",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, data.getExtras().getString("data"));
                eventDate.setText(data.getExtras().getString("data"));
                Log.v(TAG, eventDate.getText().toString());
            }
        }
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl()+"events/";
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}