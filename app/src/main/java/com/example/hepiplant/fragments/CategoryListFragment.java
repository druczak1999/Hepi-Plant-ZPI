package com.example.hepiplant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.CategoryAddActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.CategoriesRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Arrays;

public class CategoryListFragment extends Fragment implements CategoriesRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "CategoryListFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private View categoriesFragmentView;
    private RecyclerView categoriesRecyclerView;
    private CategoryDto[] categories = new CategoryDto[]{};

    public CategoryListFragment() {
    }

    public static CategoryListFragment newInstance() {
        CategoryListFragment fragment = new CategoryListFragment();
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
        categoryResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        categoriesFragmentView = inflater.inflate(R.layout.fragment_category_list, container, false);

        initView();
        setLayoutManager();
        makeGetDataRequest();
        setButtonOnClickListener();

        return categoriesFragmentView;
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
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "categories";
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        categories = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
        setAdapter();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void initView() {
        categoriesRecyclerView = categoriesFragmentView.findViewById(R.id.categoryRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        categoriesRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        CategoriesRecyclerViewAdapter adapter = new CategoriesRecyclerViewAdapter(getActivity(), categories);
        adapter.setClickListener(this);
        categoriesRecyclerView.setAdapter(adapter);
    }

    private void setButtonOnClickListener() {
        Button addButton = categoriesFragmentView.findViewById(R.id.categoryAddButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), CategoryAddActivity.class);
            startActivity(intent);
        });
    }

}
