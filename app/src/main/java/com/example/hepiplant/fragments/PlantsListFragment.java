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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.example.hepiplant.dto.PlantDto;
import com.example.hepiplant.dto.SpeciesDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlantsListFragment extends Fragment implements PlantsRecyclerViewAdapter.ItemClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "PlantsListFragment";

    private View plantsFragmentView;
    private RecyclerView plantsRecyclerView;
    private PlantsRecyclerViewAdapter adapter;
    private PlantDto[] plants = new PlantDto[]{};
    private Spinner plantFilterSpinner, speciesSpinner, locationSpinner;
    private Button plantFilterButton;
    private EditText plantNameEditText;
    private TextView closeNameFilter, closeSpicesFilter, closeLocationFilter;
    private SpeciesDto[] speciesDtos;
    private SpeciesDto selectedSpecies;
    private String[] locations;
    private String selectedLocation;
    private LinearLayout nameLinearLayout, speciesLinearLayout, locationLinearLayout;
    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<PlantDto> plantResponseHandler;
    private JSONResponseHandler<SpeciesDto> speciesResponseHandler;
    private JSONResponseHandler<String> locationsResponseHandler;

    private static int filterClick = 0;

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
        setUpViewsForFilters();
        setPlantFilterButtonOnClickListener();
        setPlantFilterSpinnerAdapter();
        getSpeciesFromDB();
        getLocationFromDB();
        setCloseViewsOnClickListeners();
        return plantsFragmentView;
    }

    private void setUpViewsForFilters() {
        plantFilterSpinner = plantsFragmentView.findViewById(R.id.filterPlantsSpinner);
        plantFilterButton = plantsFragmentView.findViewById(R.id.filterPlantsButton);
        plantNameEditText = plantsFragmentView.findViewById(R.id.nameEditTextInPlantFilter);
        closeNameFilter = plantsFragmentView.findViewById(R.id.closeNameFilter);
        closeSpicesFilter = plantsFragmentView.findViewById(R.id.closeSpeciesFilter);
        closeLocationFilter = plantsFragmentView.findViewById(R.id.closeLocationFilters);
        nameLinearLayout = plantsFragmentView.findViewById(R.id.nameFilterLinearLayout);
        speciesLinearLayout = plantsFragmentView.findViewById(R.id.speciesFilterLinearLayout);
        locationLinearLayout = plantsFragmentView.findViewById(R.id.locationFilterLinearLayout);
    }

    private void setCloseViewsOnClickListeners(){
        closeNameFilter.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            plantNameEditText.setText("");
            plantNameEditText.setVisibility(View.GONE);
            nameLinearLayout.setVisibility(View.GONE);
        });

        closeSpicesFilter.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            speciesSpinner.setVisibility(View.GONE);
            selectedSpecies=null;
            speciesLinearLayout.setVisibility(View.GONE);
        });

        closeLocationFilter.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            locationSpinner.setVisibility(View.GONE);
            locationSpinner.setSelection(0);
            selectedLocation=null;
            locationLinearLayout.setVisibility(View.GONE);
        });
    }

    private void setRecyclerViewLayoutParams(int plusOrMinus) {
        plantsRecyclerView.setPadding(plantsRecyclerView.getPaddingLeft(),
                plantsRecyclerView.getPaddingTop(),
                plantsRecyclerView.getPaddingRight(),
                plantsRecyclerView.getPaddingBottom()
                + (int)(plusOrMinus * getResources().getDimension(R.dimen.SortLinearLayout)));
    }

    private void setPlantFilterSpinnerAdapter(){
        plantFilterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, Arrays.asList("Filtruj","Nazwa","Gatunek","Pomieszczenie"));
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plantFilterSpinner.setAdapter(dtoArrayAdapter);
    }

    private void setPlantFilterButtonOnClickListener() {
        plantFilterButton.setOnClickListener(v -> {
            if(filterClick%2==0){
                makeGetDataRequestWithParam();
                plantFilterButton.setText(R.string.clean_button);
            }
            else {
                makeGetDataRequest();
                plantFilterButton.setText(R.string.filter_button);
            }
            plantFilterSpinner.setSelection(0);
            filterClick++;
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
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        plants = plantResponseHandler.handleArrayResponse(response, PlantDto[].class);
        ArrayList<Long> plantsIdList = new ArrayList<>();
        for (PlantDto plant: plants) {
           plantsIdList.add(plant.getId());
        }
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

    private void getSpecies(List<String> species) {
        Log.v(TAG, "Species size" + species.size());
        speciesSpinner = plantsFragmentView.findViewById(R.id.speciesSpinnerInPlantFilter);
        speciesSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, species);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(dtoArrayAdapter);
        speciesSpinner.setVisibility(View.VISIBLE);
    }

    private void getSpeciesFromDB() {
        String url = getRequestUrlSpecies();
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseSpecies, this::onErrorResponseReceived);
    }

    private void onGetResponseSpecies(JSONArray response){
        speciesDtos = speciesResponseHandler.handleArrayResponse(response, SpeciesDto[].class);
        List<String> species = new ArrayList<>();
        for (SpeciesDto speciesDto : speciesDtos) {
            species.add(speciesDto.getName());
        }
        getSpecies(species);
    }

    private void getLocationFromDB() {
        String url = getRequestUrl()+ "/locations";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseLocation, this::onErrorResponseReceived);
    }

    private void onGetResponseLocation(JSONArray response){
        locations = locationsResponseHandler.handleArrayResponse(response, String[].class);
        locationSpinner = plantsFragmentView.findViewById(R.id.locationSpinnerInPlantFilter);
        locationSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, locations);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(dtoArrayAdapter);
        locationSpinner.setVisibility(View.VISIBLE);
    }

    private void makeGetDataRequestWithParam(){
        String url = getRequestUrl()+"?";
        url = prepareUrlForGetDataRequest(url);
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    private String prepareUrlForGetDataRequest(String url) {
        if(plantNameEditText.getVisibility()== View.VISIBLE
                && plantNameEditText.getText()!=null && !plantNameEditText.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "name=" + plantNameEditText.getText().toString();
            Log.v(TAG, "url"+url);
        }
        if(selectedSpecies!=null) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "speciesId=" + selectedSpecies.getId().toString();
            Log.v(TAG, "url"+url);
        }
        if(selectedLocation!=null) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "location=" + selectedLocation;
            Log.v(TAG, "url"+url);
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
                Log.v(TAG, "Species id after filtering : "+selectedSpecies.getId());
            }
        }
        if(locationSpinner.getId()==R.id.locationSpinnerInPlantFilter){
            selectedLocation = selectedItem;
        }
        if(filterSpinner.getId()==R.id.filterPlantsSpinner){
            switch (position){
                case 1:
                    setRecyclerViewLayoutParams(1);
                    plantNameEditText.setVisibility(View.VISIBLE);
                    nameLinearLayout.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    setRecyclerViewLayoutParams(1);
                    getSpeciesFromDB();
                    speciesLinearLayout.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    setRecyclerViewLayoutParams(1);
                    getLocationFromDB();
                    locationLinearLayout.setVisibility(View.VISIBLE);
                    break;
            }
            filterSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
