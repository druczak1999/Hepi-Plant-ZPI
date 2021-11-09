package com.example.hepiplant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PostDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "AddPost";
    private static final int PICK_IMAGE = 2;

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PostDto> postResponseHandler;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private Spinner spinnerCat;
    private CategoryDto selectedCategory;
    private String img_str = null;
    private ImageView addImageButton;
    private TextView tags, title, body;
    private Button add;
    private CategoryDto[] categoryDtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_add);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        postResponseHandler = new JSONResponseHandler<>(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);

        setupViewsData();
        getCategoriesFromDB();
        getCategoriesFromDB();
        setOnClickListeners();
        onClickAddPost();
        setBottomBarOnItemClickListeners();
    }

    private void onClickAddPost() {
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRequestPost();
            }
        });
    }

    private JSONObject makePostDataJson(){
        JSONObject postData = new JSONObject();
        try {
            postData.put("title", title.getText().toString());
            postData.put("body", body.getText().toString());
            postData.put("tags", hashReading());
            postData.put("photo", img_str);
            postData.put("userId", config.getUserId());
            postData.put("categoryId", selectedCategory.getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void addRequestPost(){
        String url = getRequestUrl() + "posts";
        JSONObject postData = makePostDataJson();
        Log.v(TAG, String.valueOf(postData));
        requestProcessor.makeRequest(Request.Method.POST, url, postData, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "OnResponse");
                        PostDto data = new PostDto();
                        data = postResponseHandler.handleResponse(response, PostDto.class);
                        StringBuilder sb = new StringBuilder("Response is: \n" + data.getTitle());
                        finish();
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
    }

    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
    }

    private void setOnClickListeners(){
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });
    }

    private void setupViewsData() {
        img_str=null;
        title = findViewById(R.id.editTitle);
        body = findViewById(R.id.editBody);
        addImageButton =  findViewById(R.id.editImageBut);
        add = findViewById(R.id.buttonDodajPost);
        tags = findViewById(R.id.editTags);
    }

    private JSONArray hashReading() {
        int listSize = 0;
        List<String> hashList = new ArrayList<String>(Arrays.asList(tags.getText().toString().replace(" ", "").split("#")));
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
        String url = getRequestUrl() + "categories";
        Log.v(TAG, "Invoking categoryRequestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        CategoryDto[] data = new CategoryDto[]{};
                        data = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
                        List<String> categories = new ArrayList<String>();
                        categoryDtos = data;
                        for (int i=0;i<data.length;i++){
                            categories.add(data[i].getName());
                        }
                        getCategories(categories);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
            }
        });
    }

    public void getCategories(List<String> categories){
        Log.v(TAG,"Categories size"+categories.size());
        spinnerCat = findViewById(R.id.editCategory);
        spinnerCat.setOnItemSelectedListener( this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = (String) parent.getItemAtPosition(position);
            for(CategoryDto c : categoryDtos){
                if(c.getName().equals(selectedItem)){
                    selectedCategory = c;
                    break;
                }
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
}

