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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PlantDto;

import com.example.hepiplant.dto.SpeciesDto;

import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class PlantAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddPlant";
    Button dateedittext, dodaj;
    ImageButton addImageButton;
    Spinner spinnerGat, spinnerCat;
    EditText nazwa, pomieszczenie;
    String img_str = null;
    int categoryId, podlewanieDni;
    public SpeciesDto speciesDto;
    SpeciesDto[] speciesDtos;
    byte [] imageToSend;



    private static final int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        nazwa = findViewById(R.id.editNazwa);
        pomieszczenie = findViewById(R.id.editPomieszczenie);
        ArrayList<Integer> dni = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6,7,8,9,10,11,12,13,14));

        dodaj = findViewById(R.id.buttonDodajRosline);
        Spinner podlewanieS = findViewById(R.id.editPodlewanie);
        podlewanieS.setOnItemSelectedListener(this);
        ArrayAdapter<Integer> dtoArrayAdapter = new ArrayAdapter<Integer>(this.getApplicationContext(), android.R.layout.simple_spinner_item, dni);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        podlewanieS.setAdapter(dtoArrayAdapter);
        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                final Configuration config = (Configuration) getApplicationContext();
                try {
                    config.setUrl(config.readProperties());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String url = config.getUrl() + "plants";
                JSONObject speciesJson = new JSONObject();
                try {
                    speciesJson.put("id",speciesDto.getId());
                    speciesJson.put("name",speciesDto.getName());
                    speciesJson.put("wateringFrequency",speciesDto.getWateringFrequency());
                    speciesJson.put("fertilizingFrequency", speciesDto.getFertilizingFrequency());
                    speciesJson.put("mistingFrequency",speciesDto.getMistingFrequency());
//                    speciesJson.put("placement",null);
                    speciesJson.put("soil",speciesDto.getSoil());
                    speciesJson.put("categoryId",speciesDto.getCategoryId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject scheduleJ = new JSONObject();
                try {
                    Log.v(TAG, String.valueOf(podlewanieDni));
                    scheduleJ.put("wateringFrequency",podlewanieDni);
                    scheduleJ.put("fertilizingFrequency", speciesDto.getFertilizingFrequency());
                    scheduleJ.put("mistingFrequency",speciesDto.getMistingFrequency());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject postData = new JSONObject();

                try {
                    postData.put("name", nazwa.getText().toString());
                    postData.put("purchaseDate", dateedittext.getText().toString()+" 00:00:00");
                    postData.put("location", pomieszczenie.getText().toString());
//                    Log.v(TAG,img_str);
                    postData.put("photo", img_str);
                    postData.put("categoryId", categoryId);
                    postData.put("species", speciesJson);
                    postData.put("userId", config.getUserId());
                    postData.put("schedule",scheduleJ);

                } catch (JSONException  e) {
                    e.printStackTrace();
                }
                Log.v(TAG, String.valueOf(postData));
                // Request a string response from the provided URL.
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                        new Response.Listener<JSONObject>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onResponse(JSONObject response) {
                                // Display the response string.
                                Log.v(TAG, "ONResponse");
                                String str = String.valueOf(response); //http request
                                PlantDto data = new PlantDto();
                                Gson gson = new Gson();
                                data = gson.fromJson(str, PlantDto.class);

                                StringBuilder sb = new StringBuilder("Response is: \n" + data.getLocation());
                                Toast.makeText(getApplicationContext(), sb, Toast.LENGTH_SHORT).show();
                                getImageFromDB();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
                        }
                    }
                });

// Add the request to the RequestQueue.
                queue.add(jsonArrayRequest);
                Toast.makeText(getApplicationContext(), "Dodano rosline", Toast.LENGTH_SHORT).show();
            }
        });
        getSpeciesFromDB();
        getCategoriesFromDB();
        dateedittext = findViewById(R.id.editData);
        dateedittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivityForResult(intent, 1);

            }
        });
        addImageButton = findViewById(R.id.addImageBut);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                cropImage();
            }
        });

    }

    private void cropImage(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1) {
//            if (resultCode == RESULT_OK) {
//                dateedittext.setText(data.getExtras().getString("data"));
//            }
//        }
//        if (requestCode == PICK_IMAGE) {
//            if (resultCode == RESULT_OK) {
//                Uri selectedImage = data.getData();
//
//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//                addImageButton.setImageBitmap(bitmap);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
//                byte[] image = stream.toByteArray();
//                img_str = Base64.encodeToString(image, 0);
//
//            }
//        }
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                dateedittext.setText(data.getExtras().getString("data"));
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                addImageButton.setImageURI(resultUri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] image = stream.toByteArray();
                imageToSend = image;
                img_str = Base64.encodeToString(image, 0);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void getSpeciesFromDB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final Configuration config = (Configuration) getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = config.getUrl() + "species";

        // Request a string response from the provided URL.
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Display the response string.
                        String str = null; //http request
                        try {
                            str = new String(response.getBytes("ISO-8859-1"),"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        SpeciesDto[] data = new SpeciesDto[]{};
                        Gson gson = new Gson();
                        data = gson.fromJson(String.valueOf(str), SpeciesDto[].class);
                        speciesDtos = data;
                        List<String> sp = new ArrayList<String>();
                        for (int i = 0; i < data.length; i++) {
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

    public void getSpecies(List<String> species) {
        Log.v(TAG, "Species size" + species.size());

        spinnerGat = (Spinner) findViewById(R.id.editGatunek);
        spinnerGat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGat.setAdapter(dtoArrayAdapter);
    }

    public void getCategoriesFromDB() {

        RequestQueue queue = Volley.newRequestQueue(this);
        final Configuration config = (Configuration) getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = config.getUrl() + "categories";

        // Request a string response from the provided URL.
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Display the response string.
                        String str = null; //http request
                        try {
                            str = new String(response.getBytes("ISO-8859-1"),"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        CategoryDto[] data = new CategoryDto[]{};
                        Gson gson = new Gson();
                        data = gson.fromJson(String.valueOf(str), CategoryDto[].class);
                        List<String> categories = new ArrayList<String>();
                        for (int i = 0; i < data.length; i++) {
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



    public void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        spinnerCat = (Spinner) findViewById(R.id.editKategoria);
        spinnerCat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner gspin = (Spinner) parent;
        Spinner cspin = (Spinner) parent;
        Spinner pspin = (Spinner) parent;
        if(gspin.getId() == R.id.editGatunek)
        {
            speciesDto = speciesDtos[position];
        }
        if(cspin.getId() == R.id.editKategoria)
        {
            categoryId = position;
        }
        if(pspin.getId() == R.id.editPodlewanie)
        {
          podlewanieDni = position+1;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getImageFromDB(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final Configuration config = (Configuration) getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = config.getUrl() + "plants/44";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Request successful. Response is: " + response);


                        Log.v(TAG,response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
                }
            }
        });
        Log.v(TAG, "Sending the request to " + url);
        queue.add(jsonArrayRequest);
        return null;
    }

}