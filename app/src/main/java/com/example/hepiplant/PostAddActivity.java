package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.PostDto;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PostAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = "AddPost";
    Spinner spinnerCat;
    int categoryId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_add);
        getCategoriesFromDB();
        TextView tytul = (EditText) findViewById(R.id.editTytul);
        TextView tresc = (EditText) findViewById(R.id.editTresc);
        Button dodaj = (Button) findViewById(R.id.buttonDodajPost);
        getCategoriesFromDB();
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

                String url = config.getUrl() + "posts";
                JSONObject postData = new JSONObject();
                try {
                    postData.put("title", tytul.getText().toString());
                    postData.put("categoryId", categoryId);
                    postData.put("userId", config.getUserId());
                    postData.put("body", tresc.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v(TAG, String.valueOf(postData));
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                        new Response.Listener<JSONObject>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.v(TAG, "ONResponse");
                                String str = String.valueOf(response); //http request
                                PostDto data = new PostDto();
                                Gson gson = new Gson();
                                data = gson.fromJson(str, PostDto.class);
                                StringBuilder sb = new StringBuilder("Response is: \n" + data.getTitle());
                                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                                startActivity(intent);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                        Log.v(TAG, String.valueOf(postData));
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
                        }
                    }
                });
                queue.add(jsonArrayRequest);
            }
    });
    }
    public void getCategoriesFromDB(){

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
        Log.v(TAG,"Categories size"+categories.size());
        spinnerCat = (Spinner)findViewById(R.id.editKategoria);
        spinnerCat.setOnItemSelectedListener( this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner cspin = (Spinner) parent;
        if(cspin.getId() == R.id.editKategoria)
        {
            categoryId = position+1;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
