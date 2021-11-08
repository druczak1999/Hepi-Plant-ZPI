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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.TagsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.TagDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;

import java.io.IOException;

public class TagListFragment extends Fragment implements TagsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "TagListFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<TagDto> tagResponseHandler;
    private View tagFragmentView;
    private RecyclerView tagsRecyclerView;
    private TagsRecyclerViewAdapter adapter;
    private TagDto[] tags = new TagDto[]{};

    public TagListFragment() {
    }

    public static TagListFragment newInstance() {
        TagListFragment fragment = new TagListFragment();
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
        tagResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        tagFragmentView = inflater.inflate(R.layout.fragment_tag_list, container, false);

        initView();
        setLayoutManager();
        makeGetDataRequest();

        return tagFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
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
            }});
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "tags";
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        tags = tagResponseHandler.handleArrayResponse(response, TagDto[].class);
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
        Log.v(TAG, "initView()");
        tagsRecyclerView = tagFragmentView.findViewById(R.id.tagRecyclerView);
    }

    private void setLayoutManager() {
        Log.v(TAG, "setLayoutManager()");
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        tagsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        Log.v(TAG, "setAdapter()");
        adapter = new TagsRecyclerViewAdapter(getActivity(), tags);
        adapter.setClickListener(this);
        tagsRecyclerView.setAdapter(adapter);
    }

}
