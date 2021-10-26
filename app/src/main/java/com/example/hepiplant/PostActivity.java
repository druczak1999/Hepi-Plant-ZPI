package com.example.hepiplant;

import static com.example.hepiplant.helper.LangUtils.getCommentsSuffix;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements CommentsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "PostActivity";
    private Configuration config;
    private CommentsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private PostDto post;
    private CommentDto[] comments = new CommentDto[]{};
    private TextView dateTextView, titleTextView, tagsTextView, bodyTextView;
    private ImageView photoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        config = (Configuration) getApplicationContext();
        initView();
        setLayoutManager();
        makeGetDataRequest();
        setBottomBarOnItemClickListeners();
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongCLick(View view, int position) {
        Log.v(TAG, "onItemLongClick()");
        Intent intent3 = new Intent(this, PopUpDeleteComment.class);
        intent3.putExtra("type", "posts");
        intent3.putExtra("postId",getIntent().getExtras().getLong("postId"));
        intent3.putExtra("commentId", post.getComments().get(position).getId());
        startActivity(intent3);
    }

    private void setupToolbar()
    {
        Toolbar toolbar = findViewById(R.id.includeToolbarPostView);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }
    public void onAddButtonClick(View v) {
        EditText editText = findViewById(R.id.addPostCommentEditText);
        String commentBody = editText.getText().toString();
        String placeholder = String.valueOf(R.string.add_comment);
        if (!placeholder.equals(commentBody)) {
            JSONObject postData = new JSONObject();
            try {
                postData.put("body", commentBody);
                postData.put("userId", config.getUserId());
                postData.put("postId", post.getId());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            makePostDataRequest(postData);
            editText.getText().clear();
        }
    }

    private void makeGetDataRequest() {
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

    private void makePostDataRequest(JSONObject postData) {
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
        return config.getUrl() + "posts/" + getIntent().getExtras().get("postId");
    }

    private void onGetResponseReceived(JSONObject response) {
        Log.v(TAG, "onGetResponseReceived()");
        post = config.getGson().fromJson(String.valueOf(response), PostDto.class);
        comments = post.getComments().toArray(comments);
        int tempSize = 0;
        for (int i = 0; i < comments.length; i++) {
            if (comments[i]!= null)
            {
                tempSize+=1;
            }
        }
        CommentDto[] tempComments = new CommentDto[tempSize];
        int a = 0;
        for (int i = 0; i < comments.length; i++) {
            if (comments[i]!= null)
            {
                tempComments[a] = comments[i];
                a++;
            }
        }
        comments = tempComments;

        if (adapter == null) {
            setAdapter();
            setupViewsData();
        } else {
            refreshDisplayedData();
        }
    }

    private void onErrorResponseReceived(VolleyError error) {
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void setBottomBarOnItemClickListeners() {
        Button buttonHome = findViewById(R.id.buttonDom);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
                ;
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

    private void refreshDisplayedData() {
        Log.v(TAG, "Refreshing displayed data()");
        adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
        adapter.updateData(comments);
        adapter.notifyItemRangeChanged(0, comments.length);
        TextView commentsTextView = findViewById(R.id.postCommentsCountTextViewSingle);
        int commentsCount = post.getComments().size();
        String commentsText = commentsCount + getCommentsSuffix(commentsCount);
        commentsTextView.setText(commentsText);
    }

    private void setupViewsData() {
        dateTextView = findViewById(R.id.postDateTextViewSingle);
        dateTextView.setText(post.getCreatedDate());
        titleTextView = findViewById(R.id.postTitleTextViewSingle);
        titleTextView.setText(post.getTitle());
        tagsTextView = findViewById(R.id.postTagsTextViewSingle);
        StringBuilder tags = new StringBuilder();
        for (String s : post.getTags()) {
            tags.append(" #").append(s);
        }
        if (tags.toString().length() == 0) {
            tagsTextView.setVisibility(View.GONE);
        } else {
            tagsTextView.setVisibility(View.VISIBLE);
            tagsTextView.setText(tags.toString().trim());
        }
        bodyTextView = findViewById(R.id.postBodyTextViewSingle);
        bodyTextView.setText(post.getBody());
        photoImageView = findViewById(R.id.postPhotoImageViewSingle);
        if (post.getPhoto() != null) {
            Log.v(TAG, "Attempting photo bind for data: " + post.getPhoto());
            try {
                photoImageView.setImageURI(Uri.parse(post.getPhoto()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    photoImageView.setClipToOutline(true);
                }
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            photoImageView.setVisibility(View.GONE);
        }
        TextView commentsTextView = findViewById(R.id.postCommentsCountTextViewSingle);
        int commentsCount = post.getComments().size();
        String commentsText = commentsCount + getCommentsSuffix(commentsCount);
        commentsTextView.setText(commentsText);
    }


    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_post, menu);
        return true;
    }
    private Intent prepareIntent(){
        Intent intent = new Intent(getApplicationContext(),PostEditActivity.class);
        intent.putExtra("id", post.getId());
        intent.putExtra("name", titleTextView.getText().toString());
        intent.putExtra("body", bodyTextView.getText().toString());
        intent.putExtra("tags", tagsTextView.getText().toString());
        if(post.getPhoto()!=null)
        intent.putExtra("photo", post.getPhoto());
        else intent.putExtra("photo", "");
        intent.putExtra("category", post.getCategoryId());
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoff:
                FireBase fireBase = new FireBase();
                fireBase.signOut();
                return true;
            case R.id.informationAboutApp:
                Toast.makeText(this.getApplicationContext(), "Informacje", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.deletePost:
                Intent intent3 = new Intent(this, PopUpDeletePost.class);
                intent3.putExtra("postId",getIntent().getExtras().getLong("postId"));
                startActivity(intent3);
                return true;
            case R.id.editPost:
                Intent intent = prepareIntent();
                startActivity(intent);
                return true;
            case R.id.miProfile:
                Intent intent2 = new Intent(this, UserActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
