package com.example.hepiplant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import java.util.List;

public class PostAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "AddPost";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

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

        setupToolbar();
        setupViewsData();
        getCategoriesFromDB();
        getCategoriesFromDB();
        setOnClickListeners();
        onClickAddPost();
        setBottomBarOnItemClickListeners();
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
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
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
                Intent intentInfo = new Intent(this, InfoActivity.class);
                startActivity(intentInfo);
                return true;
            case R.id.miProfile:
                Intent intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.plantAddToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
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
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "OnResponse");
                        PostDto data = new PostDto();
                        data = postResponseHandler.handleResponse(response, PostDto.class);
                        StringBuilder sb = new StringBuilder("Response is: \n" + data.getTitle());
                        Toast.makeText(getApplicationContext(), R.string.add_post, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                Log.v(TAG, String.valueOf(postData));
                NetworkResponse networkResponse = error.networkResponse;
                Toast.makeText(getApplicationContext(), R.string.add_post_failed, Toast.LENGTH_LONG).show();
                if (networkResponse != null) {
                    Log.e(TAG, "Status code: " + networkResponse.statusCode +
                            " Data: " + Arrays.toString(networkResponse.data));
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
        add = findViewById(R.id.editPostButton);
        tags = findViewById(R.id.editTags);
    }

    private JSONArray hashReading() {
        int listSize = 0;
        List<String> hashList = new ArrayList<>(Arrays.asList(tags.getText().toString().replace(" ", "").split("#")));
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
                Toast.makeText(getApplicationContext(),R.string.upload_photo_failed,Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
        img_str = path;
    }

    private void getCategoriesFromDB(){
        String url = getRequestUrl() + "categories";
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        categoryDtos = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
                        List<String> categories = new ArrayList<>();
                        for (int i=0;i<categoryDtos.length;i++){
                            categories.add(categoryDtos[i].getName());
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

    private void getCategories(List<String> categories){
        Log.v(TAG,"Categories size"+categories.size());
        spinnerCat = findViewById(R.id.editCategory);
        spinnerCat.setOnItemSelectedListener( this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }

    private void setBottomBarOnItemClickListeners(){
        if(config.getUserRoles().contains(ROLE_ADMIN)){
            findViewById(R.id.postAddBottomBar).setVisibility(View.GONE);
        } else {
            Button buttonHome = findViewById(R.id.buttonDom);
            buttonHome.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
                startActivity(intent);
            });

            Button buttonForum = findViewById(R.id.buttonForum);
            buttonForum.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            });
        }
    }
}
