package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.hepiplant.configuration.Configuration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PopUpDeletePost extends AppCompatActivity {
    private Button yes, no;
    private Configuration config;
    private static final String TAG = "PopUpDeletePost";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_post_delete);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.3));
        setupViewsData();
    }

    private void setupViewsData(){
        config = (Configuration) getApplicationContext();
        yes = findViewById(R.id.buttonYes);
        no = findViewById(R.id.buttonNo);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });
    }

    private void deletePhotoFromFirebase(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(getIntent().getExtras().getString("photo"));
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Delete photo from Firebase storage",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(),"Unsuccessful delete photo from Firebase storage",Toast.LENGTH_LONG).show();
            }
        });

    }

    @NonNull
    private String getRequestUrl(Long id) {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "posts/"+id;
    }

    private void deletePost(){
        String url = getRequestUrl(getIntent().getExtras().getLong("postId"));
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, response);
                        if(!getIntent().getExtras().getString("photo", "").isEmpty()) deletePhotoFromFirebase();
                        Toast.makeText(getApplicationContext(),"Usunięto post",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                        startActivity(intent);
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

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}
