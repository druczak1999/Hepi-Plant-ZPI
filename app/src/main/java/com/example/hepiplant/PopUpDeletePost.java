package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.RequestType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;

public class PopUpDeletePost extends AppCompatActivity {

    private static final String TAG = "PopUpDeletePost";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_delete);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width *0.9), (int)(height*0.3));
        setupViewsData();
    }

    private void setupViewsData(){
        TextView announcement = findViewById(R.id.textView);
        announcement.setText(R.string.popup_message_post);
        Button yes = findViewById(R.id.buttonYes);
        Button no = findViewById(R.id.buttonNo);

        no.setOnClickListener(v -> finish());

        yes.setOnClickListener(v -> deletePost());
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
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.DELETE, url, null, RequestType.STRING,
                (Response.Listener<String>) this::onDeleteResponseReceived, this::onErrorResponseReceived);
    }

    private void onDeleteResponseReceived(String response) {
        Log.v(TAG, response);
        if(!getIntent().getExtras().getString("photo", "").isEmpty()) deletePhotoFromFirebase();
        Toast.makeText(getApplicationContext(),getText(R.string.delete_post_message),Toast.LENGTH_LONG).show();
        Intent intent;
        if (config.getUserRoles().contains(ROLE_ADMIN)){
            intent = new Intent(getApplicationContext(), MainAdminActivity.class);
            intent.putExtra("tabTitle", "Forum");
        } else {
            intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(),getText(R.string.delete_post_failed),Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void deletePhotoFromFirebase(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(getIntent().getExtras().getString("photo"));
        imageRef.delete().addOnSuccessListener(aVoid -> {}).addOnFailureListener(exception -> {});
    }
}
