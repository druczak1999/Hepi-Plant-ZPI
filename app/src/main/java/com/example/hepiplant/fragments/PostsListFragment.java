package com.example.hepiplant.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.PostsRecyclerViewAdapter;
import com.example.hepiplant.dto.PostDto;
import com.google.gson.Gson;

import org.json.JSONArray;

public class PostsListFragment extends Fragment {

    private View postsFragmentView;
    private RecyclerView postsRecyclerView;
    private PostsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private static final String TAG = "PostsListFragment";
    private PostDto[] posts = new PostDto[]{};

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        postsFragmentView = inflater.inflate(R.layout.fragment_posts_list, container, false);

        initView();
        setLayoutManager();
        sendRequestForData();

        return postsFragmentView;
    }

    private void sendRequestForData(){
        // data to populate the RecyclerView with
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="http://10.0.0.118:8080/posts";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Request successful. Response is: " + response);
                        onResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
                }
            }
        });
        Log.v(TAG, "Sending the request to " + url);
        queue.add(jsonArrayRequest);
    }

    private void initView() {
        postsRecyclerView = postsFragmentView.findViewById(R.id.offersRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        adapter = new PostsRecyclerViewAdapter(getActivity(), posts);
        adapter.setClickListener((PostsRecyclerViewAdapter.ItemClickListener) getActivity());
        postsRecyclerView.setAdapter(adapter);
    }

    private void onResponseReceived(JSONArray response){
        Log.v(TAG, "onResponseReceived()");
        Gson gson = new Gson();
        posts = gson.fromJson(String.valueOf(response), PostDto[].class);
        setAdapter();
    }
}
