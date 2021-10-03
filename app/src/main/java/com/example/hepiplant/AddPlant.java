package com.example.hepiplant;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddPlant extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "AddPlant";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        getSpeciesFromDB();
        getCategoriesFromDB();
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

        Spinner spinnerGat = (Spinner)findViewById(R.id.editGatunek);
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
        Spinner spinnerCat = (Spinner)findViewById(R.id.editKategoria);
        spinnerCat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "Entering onItemSelected()");
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(this.getApplicationContext(), "Selected: " + item, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}