package com.example.hepiplant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.hepiplant.dto.SalesOfferDto;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesOfferEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = "EditSalesOffer";
    private static final String CURRENCY = "zł";
    private Configuration config;
    private long categoryId=0;
    private long salesOfferId = 0;
    private ImageView salesOfferImage;
    private Spinner spinnerCat;
    private EditText salesOfferName, salesOfferBody, salesOfferTags, salesOfferPrice, salesOfferLocation;
    private String img_str;
    private Button editSalesOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_offer_edit);
        config = (Configuration) getApplicationContext();
        salesOfferId = getIntent().getExtras().getLong("id");
        categoryId = getIntent().getExtras().getLong("category");
        setupViewsData();
        setValuesToEdit();
    }

    private void setupViewsData(){
        salesOfferName = findViewById(R.id.editTitle);
        spinnerCat = findViewById(R.id.editCategory);
        salesOfferBody = findViewById(R.id.editBody);
        salesOfferTags = findViewById(R.id.editTags);
        salesOfferImage = findViewById(R.id.editImageBut);
        salesOfferPrice = findViewById(R.id.editPrice);
        salesOfferLocation = findViewById(R.id.editLocation);
        editSalesOffer = findViewById(R.id.editSalesOffer);
        getCategoriesFromDB();
        setBottomBarOnItemClickListeners();
        setOnClickListeners();
    }

    private void setValuesToEdit()
    {
        salesOfferName.setText(getIntent().getExtras().getString("name"));
        salesOfferBody.setText(getIntent().getExtras().getString("body"));
        salesOfferTags.setText(getIntent().getExtras().getString("tags"));
        salesOfferLocation.setText(getIntent().getExtras().getString("location"));
        salesOfferPrice.setText(getIntent().getExtras().getString("price"));
        getPhotoFromFirebase(salesOfferImage, getIntent().getExtras().getString("photo"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            salesOfferImage.setClipToOutline(true);
        }
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
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
                        categories.add("Brak");
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
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    private static void getPhotoFromFirebase(ImageView photoImageView, String post) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Log.v(TAG, post);
        StorageReference pathReference = storageRef.child(post);
        final long ONE_MEGABYTE = 2048 * 2048;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.v(TAG,"IN on success");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                photoImageView.setImageBitmap(bitmap);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    photoImageView.setClipToOutline(true);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v(TAG,"IN on failure");
                Log.v(TAG,exception.getMessage());
                Log.v(TAG,exception.getCause().toString());
            }
        });
    }

    private void saveImageToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String [] array = img_str.split("/");
        String path = "posts/"+array[array.length-1];
        StorageReference imagesRef = storageRef.child(path);
        salesOfferImage.setDrawingCacheEnabled(true);
        salesOfferImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) salesOfferImage.getDrawable()).getBitmap();
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

    public void getCategories(List<String> categories){
        Log.v(TAG,"Categories size"+categories.size());
        spinnerCat = (Spinner)findViewById(R.id.editCategory);
        spinnerCat.setOnItemSelectedListener( this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner cspin = (Spinner) parent;
        if(cspin.getId() == R.id.editCategory)
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

    private void setOnClickListeners(){
        setBottomBarOnItemClickListeners();
        salesOfferImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });

        onClickAddSalesOffer();
    }

    private void onClickAddSalesOffer(){
        Log.v(TAG,"onClick Edit");
        editSalesOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(salesOfferName.getText()!=null && !salesOfferName.getText().toString().equals("...")) patchRequestSalesOffer();
                else Toast.makeText(getApplicationContext(),"Podaj nazwę oferty",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void patchRequestSalesOffer(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getRequestUrl()+"salesoffers/"+getIntent().getExtras().getLong("id");
        JSONObject postData = makeSalesOfferDataJson();
        Log.v(TAG, String.valueOf(postData));
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PATCH, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        onPostResponseSalesOffer(response);
                        Intent intent = new Intent(getApplicationContext(),ForumTabsActivity.class);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseSalesOffer(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };

        queue.add(jsonArrayRequest);
    }

    private JSONObject makeSalesOfferDataJson(){
        JSONObject postData = new JSONObject();
        try {
            if(salesOfferName.getText().toString().equals("..."))  postData.put("title", "");
            else postData.put("title", salesOfferName.getText().toString());
            if(salesOfferBody.getText().toString().equals("..."))  postData.put("body", "");
            else postData.put("body", salesOfferBody.getText().toString());
            if(salesOfferTags.getText().toString().equals("..."))  postData.put("tags", "");
            else postData.put("tags", hashReading());
            postData.put("price", salesOfferPrice.getText().toString());
            if(salesOfferLocation.getText().toString().equals("..."))  postData.put("location", "");
            else postData.put("location", salesOfferLocation.getText().toString());
            postData.put("photo", img_str);
            postData.put("categoryId", categoryId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.v(TAG, "cropActivity");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                salesOfferImage.setImageURI(resultUri);
                img_str=resultUri.toString();
                saveImageToFirebase();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    salesOfferImage.setClipToOutline(true);
                }
                Log.v(TAG, img_str);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private JSONArray hashReading()
    {
        int listSize = 0;
        List<String> hashList = new ArrayList<String>(Arrays.asList(salesOfferTags.getText().toString().replace(" ", "").split("#")));
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

    private void onPostResponseSalesOffer(JSONObject response){
        Log.v(TAG, "ONResponse");
        String str = String.valueOf(response); //http request
        SalesOfferDto data = new SalesOfferDto();
        Gson gson = new Gson();
        data = gson.fromJson(str, SalesOfferDto.class);
    }

    private void onErrorResponseSalesOffer(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }
}
