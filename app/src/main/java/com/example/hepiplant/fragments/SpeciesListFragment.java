package com.example.hepiplant.fragments;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.SpeciesRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.SpeciesDto;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpeciesListFragment extends Fragment implements SpeciesRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "SpeciesListFragment";

    private Configuration config;
    private View speciesFragmentView;
    private RecyclerView speciesRecyclerView;
    private SpeciesRecyclerViewAdapter adapter;
    private SpeciesDto[] species = new SpeciesDto[]{};

    public SpeciesListFragment() {
    }

    public static SpeciesListFragment newInstance() {
        SpeciesListFragment fragment = new SpeciesListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        config = (Configuration) getActivity().getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        speciesFragmentView = inflater.inflate(R.layout.fragment_species_list, container, false);

        initView();
        setLayoutManager();
        makeGetDataRequest();
        setButtonOnClickListener();

        return speciesFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
//        Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class); //todo change activity to view?
//        intent.putExtra("postId", species[position].getId());
//        startActivity(intent);
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
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
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonArrayRequest);
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "species";
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        Gson gson = new Gson();
        species = gson.fromJson(String.valueOf(response), SpeciesDto[].class);
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
        speciesRecyclerView = speciesFragmentView.findViewById(R.id.speciesRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        speciesRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        adapter = new SpeciesRecyclerViewAdapter(getActivity(), species);
        adapter.setClickListener(this);
        speciesRecyclerView.setAdapter(adapter);
    }

    private void setButtonOnClickListener() {
        Button addButton = speciesFragmentView.findViewById(R.id.speciesAddButton);
        // todo implement
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity().getApplicationContext(), SpeciesAddActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }

}
