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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.SalesOfferDto;
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

public class SalesOfferEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "SalesOfferEditActivity";
    private static final String CURRENCY = "zł";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SalesOfferDto> salesOfferResponseHandler;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private long categoryId=0;
    private long salesOfferId = 0;
    private ImageView salesOfferImage;
    private Spinner spinnerCat;
    private EditText salesOfferName, salesOfferBody, salesOfferTags, salesOfferPrice, salesOfferLocation;
    private String img_str;
    private Button editSalesOffer;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;
    private SalesOfferDto salesOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_offer_edit);
        config = (Configuration) getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        salesOfferResponseHandler = new JSONResponseHandler<>(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);

        setBottomBarOnItemClickListeners();
        setupViewsData();
        makeGetDataRequest();
        salesOfferId = getIntent().getExtras().getLong("salesOfferId");
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

    private void setupViewsData(){
        salesOfferName = findViewById(R.id.editTitle);
        spinnerCat = findViewById(R.id.editCategory);
        salesOfferBody = findViewById(R.id.editBody);
        salesOfferTags = findViewById(R.id.editTags);
        salesOfferImage = findViewById(R.id.editImageBut);
        salesOfferPrice = findViewById(R.id.editPrice);
        salesOfferLocation = findViewById(R.id.editLocation);
        editSalesOffer = findViewById(R.id.editSalesOffer);
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+"salesoffers/" + getIntent().getExtras().get("salesOfferId");
        Log.v(TAG, "Invoking postRequestProcessor"+ url);
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onGetResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorResponseReceived(error);
                    }
                });
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived()");
        salesOffer = salesOfferResponseHandler.handleResponse(response, SalesOfferDto.class);
        categoryId = salesOffer.getCategoryId();
        getCategoriesFromDB();
        setOnClickListeners();
        setValuesToEdit();
    }

    private void onErrorResponseReceived(VolleyError error) {
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setValuesToEdit() {
        salesOfferName.setText(salesOffer.getTitle());
        salesOfferBody.setText(salesOffer.getBody());
        salesOfferTags.setText(getIntent().getExtras().getString("tags"));
        salesOfferLocation.setText(salesOffer.getLocation());
        salesOfferPrice.setText(salesOffer.getPrice().toString());
        if(salesOffer.getPhoto()!=null)
            getPhotoFromFirebase(salesOfferImage, salesOffer.getPhoto());
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

    private void getCategoriesFromDB(){
        String url = getRequestUrl() + "categories";

        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        categoryDtos = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
                        List<String> categories = new ArrayList<String>();
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
                Toast.makeText(getApplicationContext(),R.string.upload_photo_failed,Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
        img_str = path;
        Log.v(TAG, img_str);
    }

    private void getCategories(List<String> categories){
        Log.v(TAG,"Categories size"+categories.size());
        spinnerCat = findViewById(R.id.editCategory);
        spinnerCat.setOnItemSelectedListener( this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
        for(CategoryDto c : categoryDtos){
            if(c.getId() == categoryId){
                selectedCategory = c;
                spinnerCat.setSelection(categories.indexOf(c.getName()));
            }
        }
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainTabsActivity.class);
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
                if(salesOfferName.getText()!=null) patchRequestSalesOffer();
                else Toast.makeText(getApplicationContext(),R.string.sales_offer_title,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void patchRequestSalesOffer(){
        String url = getRequestUrl()+"salesoffers/"+getIntent().getExtras().getLong("salesOfferId");
        JSONObject postData = makeSalesOfferDataJson();
        Log.v(TAG, String.valueOf(postData));
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.PATCH, url, postData, RequestType.OBJECT,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onPostResponseSalesOffer(response);
                        Toast.makeText(getApplicationContext(), R.string.edit_saved, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(),ForumTabsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { onErrorResponseSalesOffer(error); }
        });
    }

    private JSONObject makeSalesOfferDataJson(){
        JSONObject postData = new JSONObject();
        try {
            if(salesOfferName.getText()==null)  postData.put("title", "");
            else postData.put("title", salesOfferName.getText().toString());
            if(salesOfferBody.getText()==null)  postData.put("body", "");
            else postData.put("body", salesOfferBody.getText().toString());
            if(salesOfferTags.getText()==null)  postData.put("tags", "");
            else postData.put("tags", hashReading());
            postData.put("price", salesOfferPrice.getText().toString());
            if(salesOfferLocation.getText()==null)  postData.put("location", "");
            else postData.put("location", salesOfferLocation.getText().toString());
            postData.put("photo", img_str);
            postData.put("categoryId", selectedCategory.getId());

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

    private JSONArray hashReading(){
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
        SalesOfferDto data = new SalesOfferDto();
        data = salesOfferResponseHandler.handleResponse(response, SalesOfferDto.class);
    }

    private void onErrorResponseSalesOffer(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        Toast.makeText(getApplicationContext(), R.string.edit_saved_failed, Toast.LENGTH_LONG).show();
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }
}
