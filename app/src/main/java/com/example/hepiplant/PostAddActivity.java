package com.example.hepiplant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.hepiplant.dto.PostDto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = "AddPost";
    Spinner spinnerCat;
    int categoryId=0;
    String img_str = null;
    ImageView addImageButton;
    TextView hasztagi;
    private static final int PICK_IMAGE = 2;
    private Configuration config;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_add);
        img_str=null;
        getCategoriesFromDB();
        TextView tytul = (EditText) findViewById(R.id.editTytul);
        TextView tresc = (EditText) findViewById(R.id.editTresc);
        addImageButton =  findViewById(R.id.addImageBut);
        Button dodaj = (Button) findViewById(R.id.buttonDodajPost);
        hasztagi = (EditText) findViewById(R.id.editHasztagi);
        getCategoriesFromDB();
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });
        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                config = (Configuration) getApplicationContext();
                try {
                    config.setUrl(config.readProperties());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String url = config.getUrl() + "posts";
                JSONObject postData = new JSONObject();
                try {
                    postData.put("title", tytul.getText().toString());
                    postData.put("body", tresc.getText().toString());
                    postData.put("tags", hashReading());
                    postData.put("photo", img_str);
                    postData.put("userId", config.getUserId());
                    postData.put("categoryId", categoryId);

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
                }){
                    @Override
                    public Map<String, String> getHeaders() {
                        return prepareRequestHeaders();
                    }
                };
                queue.add(jsonArrayRequest);
            }
    });
    }
    private JSONArray hashReading()
    {
        int listSize = 0;
        List<String> hashList = new ArrayList<String>(Arrays.asList(hasztagi.getText().toString().replace(" ", "").split("#")));
        hashList.removeAll(Arrays.asList("", null));
        Log.v(TAG, String.valueOf(hashList));
        JSONArray hash = new JSONArray();
        if(hashList.size()>5) listSize = 5;
        else listSize = hashList.size();
        for(int i=0; i<listSize; i++) {
            hash.put(hashList.get(i));
        }
        return hash;
    }
    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.v(TAG, "cropActivity");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                addImageButton.setImageURI(resultUri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addImageButton.setClipToOutline(true);
                }
                img_str = resultUri.toString();
                saveImageToFirebase();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void saveImageToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String [] array = img_str.split("/");
        String path = "posts/"+array[array.length-1];
        StorageReference imagesRef = storageRef.child(path);
        addImageButton.setDrawingCacheEnabled(true);
        addImageButton.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) addImageButton.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataB = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(dataB);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(),"Fail in upload image",Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),"Success in upload image",Toast.LENGTH_LONG).show();
            }
        });
        img_str = path;
        Log.v(TAG, img_str);
    }

    public void getCategoriesFromDB(){

        RequestQueue queue = Volley.newRequestQueue(this);
        config = (Configuration) getApplicationContext();
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
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

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

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}
