package com.example.hepiplant;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.adapter.recyclerview.CommentsRecyclerViewAdapter;
import com.example.hepiplant.adapter.recyclerview.PostsRecyclerViewAdapter;
import com.example.hepiplant.dto.CommentDto;
import com.example.hepiplant.dto.PostDto;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

public class PostActivity extends AppCompatActivity implements CommentsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "PostActivity";
    private static final String COMMENTS_SUFFIX = " komentarz";
    private static final String BASE_URL = "http://10.0.0.163:8080";
    private CommentsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private PostDto post;
    private CommentDto[] comments = new CommentDto[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initView();
        setLayoutManager();
        makeDataRequest();
        setBottomBarOnItemClickListeners();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private void makeDataRequest(){
        // data to populate the RecyclerView with
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "/posts/" + getIntent().getExtras().get("postId");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        onResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        });
        Log.v(TAG, "Sending the request to " + url);
        queue.add(jsonObjectRequest);
    }

    private void onResponseReceived(JSONObject response){
        Log.v(TAG, "onResponseReceived()");
        Gson gson = new Gson();
        post = gson.fromJson(String.valueOf(response), PostDto.class);
        comments = post.getComments().toArray(comments);
        setAdapter();
        setupViewsData();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setBottomBarOnItemClickListeners(){
        Button buttonHome = (Button) findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);;
            }
        });

        Button buttonForum = (Button) findViewById(R.id.buttonForum);
        buttonForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForumTabsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        recyclerView = findViewById(R.id.postCommentsRecyclerViewSingle);
    }

    private void setLayoutManager() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setAdapter() {
        adapter = new CommentsRecyclerViewAdapter(this, comments);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewsData() {
        TextView dateTextView = (TextView) findViewById(R.id.postDateTextViewSingle);
        dateTextView.setText(post.getCreatedDate());
        TextView titleTextView = (TextView) findViewById(R.id.postTitleTextViewSingle);
        titleTextView.setText(post.getTitle());
        TextView tagsTextView = (TextView) findViewById(R.id.postTagsTextViewSingle);
        StringBuilder tags = new StringBuilder();
        for (String s : post.getTags()) {
            tags.append(" #").append(s);
        }
        if(tags.toString().length() == 0){
            tagsTextView.setVisibility(View.GONE);
        } else {
            tagsTextView.setVisibility(View.VISIBLE);
            tagsTextView.setText(tags.toString().trim());
        }
        TextView bodyTextView = (TextView) findViewById(R.id.postBodyTextViewSingle);
        bodyTextView.setText(post.getBody());
        TextView commentsTextView = (TextView) findViewById(R.id.postCommentsCountTextViewSingle);
        String commentsText = post.getComments().size() + COMMENTS_SUFFIX; //todo add końcówka dla słowa komentarz + extract constants into one file
        commentsTextView.setText(commentsText);
    }

}
