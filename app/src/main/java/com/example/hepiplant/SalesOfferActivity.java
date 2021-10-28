package com.example.hepiplant;

import static com.example.hepiplant.helper.LangUtils.getCommentsSuffix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.hepiplant.adapter.recyclerview.CommentsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CommentDto;
import com.example.hepiplant.dto.PostDto;
import com.example.hepiplant.dto.SalesOfferDto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SalesOfferActivity extends AppCompatActivity implements CommentsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "SalesOfferActivity";
    private static final String CURRENCY = "zł";

    private Configuration config;
    private CommentsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private SalesOfferDto salesOffer;
    private CommentDto[] comments = new CommentDto[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_offer);

        config = (Configuration) getApplicationContext();
        initView();
        setLayoutManager();
        makeGetDataRequest();
        setBottomBarOnItemClickListeners();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    public void onAddButtonClick(View v){
        EditText editText = findViewById(R.id.addSalesOfferCommentEditText);
        String commentBody = editText.getText().toString();
        String placeholder = String.valueOf(R.string.add_comment);
        if(!placeholder.equals(commentBody)){
            JSONObject postData = new JSONObject();
            try {
                postData.put("body", commentBody);
                postData.put("userId", config.getUserId());
                postData.put("postId", salesOffer.getId());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            makePostDataRequest(postData);
            editText.getText().clear();
        }
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONObject response) {
                    onGetResponseReceived(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonObjectRequest);
    }

    private void makePostDataRequest(JSONObject postData){
        String url = getRequestUrl() + "/comments";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
            new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONObject response) {
                    makeGetDataRequest();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonObjectRequest);
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "salesoffers/" + getIntent().getExtras().get("salesOfferId");
    }

    private void onGetResponseReceived(JSONObject response){
        Log.v(TAG, "onGetResponseReceived()");
        salesOffer = config.getGson().fromJson(String.valueOf(response), SalesOfferDto.class);
        comments = salesOffer.getComments().toArray(comments);
        if(adapter == null){
            setAdapter();
            setupViewsData();
        } else {
            refreshDisplayedData();
        }
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);;
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

    private void initView() {
        recyclerView = findViewById(R.id.salesOfferCommentsRecyclerViewSingle);
    }

    private void setLayoutManager() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setAdapter() {
        adapter = new CommentsRecyclerViewAdapter(this, comments);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void refreshDisplayedData(){
        Log.v(TAG, "Refreshing displayed data()");
        adapter.updateData(comments);
        adapter.notifyItemRangeChanged(0, comments.length);
        TextView commentsTextView = findViewById(R.id.salesOfferCommentsCountTextViewSingle);
        int commentsCount = salesOffer.getComments().size();
        String commentsText = commentsCount + getCommentsSuffix(commentsCount);
        commentsTextView.setText(commentsText);
    }

    private void setupViewsData() {
        TextView priceTextView = findViewById(R.id.offerPriceTextViewSingle);
        priceTextView.setText(String.format(Locale.GERMANY,"%.2f %s",
                salesOffer.getPrice().doubleValue(), CURRENCY));
        TextView dateTextView = findViewById(R.id.offerLocationTextViewSingle);
        dateTextView.setText(salesOffer.getLocation());
        TextView titleTextView = findViewById(R.id.salesOfferTitleTextViewSingle);
        titleTextView.setText(salesOffer.getTitle());
        TextView tagsTextView = findViewById(R.id.salesOfferTagsTextViewSingle);
        StringBuilder tags = new StringBuilder();
        for (String s : salesOffer.getTags()) {
            tags.append(" #").append(s);
        }
        if(tags.toString().length() == 0){
            tagsTextView.setVisibility(View.GONE);
        } else {
            tagsTextView.setVisibility(View.VISIBLE);
            tagsTextView.setText(tags.toString().trim());
        }
        TextView bodyTextView = findViewById(R.id.salesOfferBodyTextViewSingle);
        bodyTextView.setText(salesOffer.getBody());
        ImageView photoImageView = findViewById(R.id.salesOfferPhotoImageViewSingle);
        if(salesOffer.getPhoto()!=null){
            Log.v(TAG,"Attempting photo bind for data: " + salesOffer.getPhoto());
            try {
                getPhotoFromFirebase(photoImageView, salesOffer);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    photoImageView.setClipToOutline(true);
                }
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            photoImageView.setVisibility(View.GONE);
        }
        TextView commentsTextView = findViewById(R.id.salesOfferCommentsCountTextViewSingle);
        int commentsCount = salesOffer.getComments().size();
        String commentsText = commentsCount + getCommentsSuffix(commentsCount);
        commentsTextView.setText(commentsText);
    }

    private static void getPhotoFromFirebase(ImageView photoImageView, SalesOfferDto post) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Log.v(TAG, post.getPhoto());

        StorageReference pathReference = storageRef.child(post.getPhoto());
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

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }

}
