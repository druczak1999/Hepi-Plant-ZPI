package com.example.hepiplant;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.ScheduleDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.dto.UserDto;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddPlant extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "AddPlant";
    Button dateedittext, dodaj;
    ImageButton addImageButton;
    Spinner spinnerGat, spinnerCat;
    EditText nazwa, pomieszczenie, podlewanie;
    String img_str;
    int categoryId, userId;
    SpeciesDto speciesDto;
    ScheduleDto scheduleDto;


    private static final int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        nazwa = findViewById(R.id.editNazwa);
        pomieszczenie= findViewById(R.id.editPomieszczenie);
        podlewanie = findViewById(R.id.editPodlewanie);
        dodaj = findViewById(R.id.buttonDodajRosline);
        scheduleDto = new ScheduleDto();
        scheduleDto.setFertilizingFrequency(1);
        scheduleDto.setMistingFrequency(1);
        scheduleDto.setWateringFrequency(1);
        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url ="http://192.168.0.45:8080/plants";

                JSONObject postData = new JSONObject();
                try {
                    postData.put("name", nazwa.getText().toString());
                    postData.put("purchaseDate", null);
                    postData.put("location", pomieszczenie.getText().toString());
                    postData.put("photo", null);
                    postData.put("categoryId", 1);
                    postData.put("species",null);
                    postData.put("userId",15);
                    postData.put("schedule",null);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Request a string response from the provided URL.
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                        new Response.Listener<JSONObject>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onResponse(JSONObject response) {
                                // Display the response string.
                                Log.v(TAG,"ONResponse");
                                String str = String.valueOf(response); //http request
                                PlantDto[] data = new PlantDto[]{};
                                Gson gson = new Gson();
                                data = gson.fromJson(str, PlantDto[].class);

                                StringBuilder sb = new StringBuilder("Response is: \n"+data[0].getLocation());
                                Toast.makeText(getApplicationContext(),sb,Toast.LENGTH_SHORT).show();
                                //Arrays.stream(data).forEach(p -> sb.append(p.getUsername()).append("\n"));
//                        textView.setText(sb);
//                        String uri = intent.getExtras().getString("photo");
//                        Uri photoUri = Uri.parse(uri);
//                        imageView.setImageURI(photoUri);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                textView.setText(error.getMessage());
                    }
                });

// Add the request to the RequestQueue.
                queue.add(jsonArrayRequest);
                finish();
            }
        });
        getSpeciesFromDB();
        getCategoriesFromDB();
        dateedittext = findViewById(R.id.editData);
        dateedittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivityForResult(intent,1);

            }
        });
        addImageButton = findViewById(R.id.addImageBut);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode == RESULT_OK){
                dateedittext.setText(data.getExtras().getString("data"));
            }
        }
        if(requestCode==PICK_IMAGE){
            if(resultCode == RESULT_OK){
                Uri selectedImage = data.getData();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap resized = Bitmap.createScaledBitmap(bitmap,300,300,true);
                addImageButton.setImageBitmap(resized);
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] image=stream.toByteArray();
                img_str = Base64.encodeToString(image, 0);

            }
        }

    }

    public void getSpeciesFromDB(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.45:8080/species";
       // Request a string response from the provided URL.
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Display the response string.
                        String  str = response; //http request
                        SpeciesDto[] data = new SpeciesDto[]{};
                        Gson gson = new Gson();
                        data = gson.fromJson(String.valueOf(str), SpeciesDto[].class);
                        List<String> sp = new ArrayList<String>();
                       for (int i=0;i<data.length;i++){
                           sp.add(data[i].getName());
                       }
                        getSpecies(sp);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }
    public void getSpecies(List<String> species){
        Log.v(TAG,"Species size"+species.size());

        spinnerGat = (Spinner)findViewById(R.id.editGatunek);
        spinnerGat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGat.setAdapter(dtoArrayAdapter);
    }
    public void getCategoriesFromDB(){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.45:8080/categories";


        // Request a string response from the provided URL.
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Display the response string.
                        String  str = response; //http request
                        CategoryDto[] data = new CategoryDto[]{};
                        Gson gson = new Gson();
                        data = gson.fromJson(String.valueOf(str), CategoryDto[].class);
                        List<String> categories = new ArrayList<String>();
                        for (int i=0;i<data.length;i++){
                            categories.add(data[i].getName());
                        }
                        getCategories(categories);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    public void getCategories(List<String> categories){
        Log.v(TAG,"Species size"+categories.size());
        spinnerCat = (Spinner)findViewById(R.id.editKategoria);
        spinnerCat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getAdapter().toString()=="android.widget.ArrayAdapter@9536607"){
            categoryId = position;
        }
        else {
            //species dto
        }

        String item = parent.getItemAtPosition(position).toString();


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    public void dodajRoslineDoBazy(View view){
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url ="http://192.168.0.45:8080/plants";
//
//        JSONObject postData = new JSONObject();
//        try {
//            postData.put("name", nazwa.getText());
//            postData.put("purchaseDate", dateedittext.getText());
//            postData.put("location", pomieszczenie.getText());
//            postData.put("photo", img_str);
//            postData.put("categoryId", pomieszczenie.getText());
//            postData.put("species",null);
//            postData.put("userId",15);
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        // Request a string response from the provided URL.
//        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
//                new Response.Listener<JSONObject>() {
//                    @RequiresApi(api = Build.VERSION_CODES.N)
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        // Display the response string.
//
//                        String str = String.valueOf(response); //http request
//                        PlantDto[] data = new PlantDto[]{};
//                        Gson gson = new Gson();
//                        data = gson.fromJson(str, PlantDto[].class);
//
//                       StringBuilder sb = new StringBuilder("Response is: \n"+data[0].getLocation());
//                        Toast.makeText(getApplicationContext(),sb,Toast.LENGTH_SHORT).show();
//                        //Arrays.stream(data).forEach(p -> sb.append(p.getUsername()).append("\n"));
////                        textView.setText(sb);
////                        String uri = intent.getExtras().getString("photo");
////                        Uri photoUri = Uri.parse(uri);
////                        imageView.setImageURI(photoUri);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
////                textView.setText(error.getMessage());
//            }
//        });
//
//// Add the request to the RequestQueue.
//        queue.add(jsonArrayRequest);
//        finish();
//    }
}