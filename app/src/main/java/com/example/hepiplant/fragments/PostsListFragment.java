package com.example.hepiplant.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.PostActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.PostsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.PostDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PostsListFragment extends Fragment implements PostsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "PostsListFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PostDto> postResponseHandler;
    private View postsFragmentView;
    private RecyclerView postsRecyclerView;
    private PostsRecyclerViewAdapter adapter;
    private PostDto[] posts = new PostDto[]{};
    private Button postSort;
    private static int click=0;

    public PostsListFragment() {
    }

    public static PostsListFragment newInstance() {
        PostsListFragment fragment = new PostsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        config = (Configuration) getActivity().getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        postResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        postsFragmentView = inflater.inflate(R.layout.fragment_posts_list, container, false);
        initView();
        setLayoutManager();
        makeGetDataRequest();
        postSort = postsFragmentView.findViewById(R.id.sortPosts);
        setSortPosts();
        return postsFragmentView;
    }

    private void setSortPosts() {
        postSort.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Log.v(TAG,"On click");
                List<PostDto> postDtoList = new ArrayList<>(Arrays.asList(posts));
                List<PostDto> postDtos= new ArrayList<>();
                if(click%2==0){
                    postDtos = postDtoList.stream()
                            .sorted(Comparator.comparing(PostDto::getCreatedDate).reversed())
                            .collect(Collectors.toList());
                }
                else{
                    postDtos = postDtoList.stream()
                            .sorted(Comparator.comparing(PostDto::getCreatedDate))
                            .collect(Collectors.toList());
                }
                click++;
                PostDto [] newPosts = new PostDto[postDtos.size()];
                for (int i=0;i<postDtos.size();i++){
                    newPosts[i] = postDtos.get(i);
                }

                Log.v(TAG,"Dł: "+newPosts.length);
                posts = newPosts;
                setAdapter();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
        intent.putExtra("postId", posts[position].getId());
        intent.putExtra("userId", posts[position].getUserId());
        startActivity(intent);
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl();
        Log.v(TAG, "Invoking categoryRequestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONArray response) {
                    onGetResponseReceived(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                onErrorResponseReceived(error);
            }
        });
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "posts";
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived(). Data is " + response);
        posts = postResponseHandler.handleArrayResponse(response, PostDto[].class);
        setAdapter();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void initView() {
        postsRecyclerView = postsFragmentView.findViewById(R.id.postsRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        adapter = new PostsRecyclerViewAdapter(getActivity(), posts);
        adapter.setClickListener(this);
        postsRecyclerView.setAdapter(adapter);
    }

}
