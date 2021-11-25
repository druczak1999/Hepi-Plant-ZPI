package com.example.hepiplant.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import com.example.hepiplant.PlantViewActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.PlantsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;
import com.example.hepiplant.dto.PostDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantsListFragment extends Fragment implements PlantsRecyclerViewAdapter.ItemClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "PlantsListFragment";

    private View plantsFragmentView;
    private RecyclerView plantsRecyclerView;
    private PlantsRecyclerViewAdapter adapter;
    private PlantDto[] plants = new PlantDto[]{};
    private Spinner plantFilterSpinner, speciesSpinner, locationSpinner;
    private Button plantFilterButton;
    private EditText plantNameEditText;
    private SpeciesDto[] speciesDtos;
    private SpeciesDto selectedSpecies;
    private String[] locations;
    private String selectedLocation;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PlantDto> plantResponseHandler;
    private JSONResponseHandler<SpeciesDto> speciesResponseHandler;
    private JSONResponseHandler<String> locationsResponseHandler;

    private static int nameClick = 0;
    private static int speciesClick = 0;
    private static int locationClick = 0;
    private static int filterClick = 0;

    private Configuration config;

    public PlantsListFragment() {}

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
        requestProcessor = new JSONRequestProcessor(config);
        plantResponseHandler = new JSONResponseHandler<>(config);
        speciesResponseHandler = new JSONResponseHandler<>(config);
        locationsResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        plantsFragmentView = inflater.inflate(R.layout.fragment_plants_list, container, false);

        initView();
        setLayoutManager();
        makeGetDataRequest();
        plantFilterSpinner = plantsFragmentView.findViewById(R.id.filterPlantsSpinner);
        plantFilterButton = plantsFragmentView.findViewById(R.id.filterPlantsButton);
        plantNameEditText = plantsFragmentView.findViewById(R.id.nameEditTextInPlantFilter);
        setPlantFilterButtonOnClickListener();
        setPlantFilterSpinnerAdapter();
        return plantsFragmentView;
    }

    private void setPlantFilterSpinnerAdapter(){
        plantFilterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, List.of("Filtruj","Nazwa","Gatunek","Pomieszczenie"));
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plantFilterSpinner.setAdapter(dtoArrayAdapter);
    }

    private void setPlantFilterButtonOnClickListener() {
        plantFilterButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if(filterClick%2==0){
                    makeGetDataRequestWithParam();
                    plantFilterButton.setText(R.string.cleanButton);
                    selectedSpecies=null;
                    selectedLocation=null;
                }
                else {
                    plantFilterButton.setText(R.string.filterButton);
                    makeGetDataRequest();
                }
                plantFilterSpinner.setSelection(0);
                filterClick++;
                if(plantNameEditText.getVisibility()==View.VISIBLE) nameClick++;
                if(speciesSpinner !=null && speciesSpinner.getVisibility()==View.VISIBLE) speciesClick++;
                if(locationSpinner !=null && locationSpinner.getVisibility()==View.VISIBLE) locationClick++;
                plantNameEditText.setVisibility(View.GONE);
                if(speciesSpinner !=null)
                    speciesSpinner.setVisibility(View.GONE);
                if(locationSpinner !=null)
                    locationSpinner.setVisibility(View.GONE);
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
        Intent intent = new Intent(getContext(), PlantViewActivity.class);
        Log.v(TAG,String.valueOf(plants[position].getId()));
        intent.putExtra("plantId",plants[position].getId());
        startActivity(intent);
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl();
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
        return config.getUrl() + "plants/user/" + config.getUserId();
    }

    @NonNull
    private String getRequestUrlSpecies() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl() + "species";
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        plants = plantResponseHandler.handleArrayResponse(response, PlantDto[].class);
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

    private void getSpecies(List<String> species) {
        Log.v(TAG, "Species size" + species.size());
        speciesSpinner = plantsFragmentView.findViewById(R.id.speciesSpinnerInPlantFilter);
        speciesSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(dtoArrayAdapter);
        speciesSpinner.setVisibility(View.VISIBLE);
    }

    private void getSpeciesFromDB() {
        String url = getRequestUrlSpecies();
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseSpecies(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { onErrorResponseReceived(error);}
                });
    }

    private void onGetResponseSpecies(JSONArray response){
        speciesDtos = speciesResponseHandler.handleArrayResponse(response, SpeciesDto[].class);
        List<String> species = new ArrayList<String>();
        for (int i = 0; i < speciesDtos.length; i++) {
            species.add(speciesDtos[i].getName());
        }
        Log.v(TAG,"Species size: "+speciesDtos.length);
        getSpecies(species);
    }

    private void getLocationFromDB() {
        String url = getRequestUrl()+ "/locations";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseLocation(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { onErrorResponseReceived(error);}
                });
    }

    private void onGetResponseLocation(JSONArray response){
        locations = locationsResponseHandler.handleArrayResponse(response, String[].class);
        locationSpinner = plantsFragmentView.findViewById(R.id.locationSpinnerInPlantFilter);
        locationSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, locations);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(dtoArrayAdapter);
        locationSpinner.setVisibility(View.VISIBLE);
    }

    private void makeGetDataRequestWithParam(){
        String url = getRequestUrl()+"?";
        url = prepareUrlForGetDataRequest(url);
        Log.v(TAG, "Invoking requestProcessor");
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

    private String prepareUrlForGetDataRequest(String url) {
        if(plantNameEditText.getVisibility()== View.VISIBLE
                && plantNameEditText.getText()!=null && !plantNameEditText.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "name=" + plantNameEditText.getText().toString();
        }
        if(selectedSpecies!=null) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "speciesDTo=" + selectedSpecies.getId();
        }
        if(selectedLocation!=null) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "location=" + selectedLocation;
        }
        return url;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner speciesSpinner = (Spinner) parent;
        Spinner locationSpinner = (Spinner) parent;
        Spinner filterSpinner = (Spinner) parent;
        String selectedItem = (String) parent.getItemAtPosition(position);
        if(speciesSpinner.getId()==R.id.speciesSpinnerInPlantFilter){
            for(SpeciesDto c : speciesDtos){
                if(c.getName().equals(selectedItem)) selectedSpecies = c;
            }
        }
        if(locationSpinner.getId()==R.id.locationSpinnerInPlantFilter){
            selectedLocation = selectedItem;
        }
        if(filterSpinner.getId()==R.id.filterPlantsSpinner){
            switch (position){
                case 1:
                    if(nameClick%2==0){
                        plantNameEditText.setVisibility(View.VISIBLE);
                        plantFilterButton.setVisibility(View.VISIBLE);
                    }
                    else plantNameEditText.setVisibility(View.GONE);
                    nameClick++;
                    break;
                case 2:
                    if(speciesClick%2==0){
                        getSpeciesFromDB();
                        plantFilterButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        if(this.speciesSpinner !=null) this.speciesSpinner.setVisibility(View.GONE);
                    }
                    speciesClick++;
                    break;
                case 3:
                    if(locationClick%2==0){
                        getLocationFromDB();
                        plantFilterButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        if(this.locationSpinner !=null) this.locationSpinner.setVisibility(View.GONE);
                    }
                    locationClick++;
                    break;
            }
            filterSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
