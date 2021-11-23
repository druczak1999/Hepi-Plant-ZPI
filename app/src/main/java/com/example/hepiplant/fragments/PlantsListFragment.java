package com.example.hepiplant.fragments;

import android.content.Intent;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.hepiplant.PlantViewActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.PlantsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.PlantDto;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlantsListFragment extends Fragment implements PlantsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "PlantsListFragment";

    private Configuration config;
    private View plantsFragmentView;
    private RecyclerView plantsRecyclerView;
    private PlantsRecyclerViewAdapter adapter;
    private PlantDto[] plants = new PlantDto[]{};

    public PlantsListFragment() {
    }

    public static PlantsListFragment newInstance() {
        PlantsListFragment fragment = new PlantsListFragment();
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
        plantsFragmentView = inflater.inflate(R.layout.fragment_plants_list, container, false);

        initView();
        setLayoutManager();
        makeGetDataRequest();

        return plantsFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(getContext(), PlantViewActivity.class);
        Log.v(TAG,String.valueOf(plants[position].getId()));
        intent.putExtra("plantId",plants[position].getId());
        intent.putExtra("scheduleId",plants[position].getSchedule().getId());
        intent.putExtra("plantName",plants[position].getName());
        if(plants[position].getSpecies()!=null){
            intent.putExtra("species",plants[position].getSpecies().getName());
            intent.putExtra("soil",plants[position].getSpecies().getSoil());
            if(plants[position].getSpecies().getPlacement()!=null)
                intent.putExtra("placement",plants[position].getSpecies().getPlacement().toString());
            else
                intent.putExtra("placement","");
        }else{
            intent.putExtra("species","");
            intent.putExtra("soil","");
            intent.putExtra("placement","");
        }
        intent.putExtra("watering",String.valueOf(plants[position].getSchedule().getWateringFrequency()));
        intent.putExtra("fertilizing",String.valueOf(plants[position].getSchedule().getFertilizingFrequency()));
        intent.putExtra("misting",String.valueOf(plants[position].getSchedule().getMistingFrequency()));
        if(plants[position].getCategoryId()!=null)
            intent.putExtra("category",plants[position].getCategoryId().toString());
        else intent.putExtra("category","");
        if(plants[position].getLocation()!=null)
            intent.putExtra("location",plants[position].getLocation());
        else intent.putExtra("location","");
        if(plants[position].getPurchaseDate()!=null)
            intent.putExtra("date", plants[position].getPurchaseDate());
        else intent.putExtra("date","");
        if(plants[position].getPhoto()!=null)
            intent.putExtra("photo", plants[position].getPhoto());
        else intent.putExtra("photo", "");
        startActivity(intent);
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
        return config.getUrl() + "plants/user/" + config.getUserId();
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        plants = config.getGson().fromJson(String.valueOf(response), PlantDto[].class);
        ArrayList plantsIdList = new ArrayList();
        for (PlantDto plant: plants) {
           plantsIdList.add(plant.getId());
        }
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
        plantsRecyclerView = plantsFragmentView.findViewById(R.id.plantsRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        plantsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        adapter = new PlantsRecyclerViewAdapter(getActivity(), plants);
        adapter.setClickListener(this);
        plantsRecyclerView.setAdapter(adapter);
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}
