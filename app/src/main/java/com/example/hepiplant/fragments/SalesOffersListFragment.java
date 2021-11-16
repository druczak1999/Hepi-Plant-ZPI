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
import com.example.hepiplant.CalendarActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.SalesOfferActivity;
import com.example.hepiplant.adapter.recyclerview.SalesOffersRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.CategoryDto;
import com.example.hepiplant.dto.PostDto;
import com.example.hepiplant.dto.SalesOfferDto;
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

public class SalesOffersListFragment extends Fragment implements
        SalesOffersRecyclerViewAdapter.ItemClickListener, AdapterView.OnItemSelectedListener  {

    private static final String TAG = "SalesOffersListFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SalesOfferDto> salesOfferResponseHandler;
    private View offersFragmentView;
    private RecyclerView offersRecyclerView;
    private SalesOffersRecyclerViewAdapter adapter;
    private SalesOfferDto[] salesOffers = new SalesOfferDto[]{};
    private Button offersSort, offersFilter, startDate, endDate;
    private Spinner filterSpinner, spinnerCat;
    private EditText tags;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private static int clickSort = 0;
    private static int tagClick = 0;
    private static int dataClick = 0;
    private static int categoryClick = 0;
    private static int filterClick = 0;

    public SalesOffersListFragment() {
    }

    public static SalesOffersListFragment newInstance() {
        SalesOffersListFragment fragment = new SalesOffersListFragment();
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
        salesOfferResponseHandler = new JSONResponseHandler<>(config);
        categoryResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        offersFragmentView = inflater.inflate(R.layout.fragment_offers_list, container, false);

        offersSort = offersFragmentView.findViewById(R.id.sortOffers);
        offersFilter = offersFragmentView.findViewById(R.id.filterButtonOffers);
        filterSpinner = offersFragmentView.findViewById(R.id.filterOffers);
        startDate = offersFragmentView.findViewById(R.id.startDateOffer);
        endDate = offersFragmentView.findViewById(R.id.endDateOffer);
        tags = offersFragmentView.findViewById(R.id.tagEditOffer);
        initView();
        setLayoutManager();
        makeGetDataRequest();
        setSortOffers();
        setDate();
        setFilterOffers();
        setFilterSpinner();
        return offersFragmentView;
    }

    private void setDate(){
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 1);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                startDate.setText(data.getExtras().getString("data"));
            }
        }
        if(requestCode==2){
            if(resultCode==-1){
                endDate.setText(data.getExtras().getString("data"));
            }
        }
    }

    private void setFilterSpinner(){
        filterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, List.of("Filtruj","Tag","Kategoria","Data"));
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(dtoArrayAdapter);
    }

    private void setSortOffers() {
        offersSort.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Log.v(TAG,"On click");
                List<SalesOfferDto> salesOfferDtos = new ArrayList<>(Arrays.asList(salesOffers));
                List<SalesOfferDto> offerDtos= new ArrayList<>();
                if(clickSort%2==0){
                    offerDtos = salesOfferDtos.stream()
                            .sorted(Comparator.comparing(SalesOfferDto::getCreatedDate).reversed())
                            .collect(Collectors.toList());
                }
                else{
                    offerDtos = salesOfferDtos.stream()
                            .sorted(Comparator.comparing(SalesOfferDto::getCreatedDate))
                            .collect(Collectors.toList());
                }
                clickSort++;
                SalesOfferDto [] newOffers = new SalesOfferDto[offerDtos.size()];
                for (int i=0;i<offerDtos.size();i++){
                    newOffers[i] = offerDtos.get(i);
                }

                salesOffers = newOffers;
                setAdapter();
            }
        });
    }

    private void setFilterOffers() {
        offersFilter.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                makeGetDataRequestWithParam();
                if(filterClick%2==0){
                    offersFilter.setText("Wyczyść");
                    makeGetDataRequestWithParam();
                    selectedCategory=null;
                }
                else {
                    offersFilter.setText("Filtruj");
                    makeGetDataRequest();
                }
                filterSpinner.setSelection(0);
                filterClick++;
                if(tags.getVisibility()==View.VISIBLE)tagClick++;
                if(spinnerCat!=null && spinnerCat.getVisibility()==View.VISIBLE)categoryClick++;
                if(startDate.getVisibility()==View.VISIBLE)dataClick++;
                tags.setVisibility(View.GONE);
                if(spinnerCat!=null)
                    spinnerCat.setVisibility(View.GONE);
                startDate.setVisibility(View.GONE);
                endDate.setVisibility(View.GONE);
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
        Intent intent = new Intent(getActivity().getApplicationContext(), SalesOfferActivity.class);
        intent.putExtra("salesOfferId", salesOffers[position].getId());
        intent.putExtra("userId", salesOffers[position].getUserId());
        startActivity(intent);
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl() + "salesoffers";
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

    private void makeGetDataRequestWithParam(){
        String url = getRequestUrl()+"salesoffers?";
        url = prepareUrl(url);
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

    private String prepareUrl(String url) {
        if(startDate.getVisibility()== View.VISIBLE
                && startDate.getText()!=null && !startDate.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "startDate=" + startDate.getText().toString().substring(0, 10);
        }
        if(endDate.getVisibility()==View.VISIBLE
                && endDate.getText()!=null && !endDate.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "endDate=" + endDate.getText().toString().substring(0, 10);
        }
        if(tags.getVisibility()==View.VISIBLE && tags.getText()!=null && !tags.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            if(tags.getText().toString().charAt(0)=='#')
                url += "tag=" + tags.getText().toString().substring(1,tags.getText().toString().length());
            else  url += "tag=" + tags.getText().toString();
        }
        if(selectedCategory!=null) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "categoryId=" + selectedCategory.getId().intValue();
        }
        return url;
    }

    @NonNull
    private String getRequestUrl() {
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.getUrl();
    }

    private void initView() {
        offersRecyclerView = offersFragmentView.findViewById(R.id.offersRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        offersRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        adapter = new SalesOffersRecyclerViewAdapter(getActivity(), salesOffers);
        adapter.setClickListener(this);
        offersRecyclerView.setAdapter(adapter);
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        salesOffers = salesOfferResponseHandler.handleArrayResponse(response, SalesOfferDto[].class);
        setAdapter();
    }

    private void onErrorResponseReceived(VolleyError error){
        Log.e(TAG, "Request unsuccessful. Message: " + error.getMessage());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            Log.e(TAG, "Status code: " + String.valueOf(networkResponse.statusCode) + " Data: " + networkResponse.data);
        }
    }

    private void onGetResponseCategories(JSONArray response){
        categoryDtos = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
        List<String> categories = new ArrayList<String>();
        for (int i = 0; i < categoryDtos.length; i++) {
            categories.add(categoryDtos[i].getName());
        }
        Log.v(TAG,"DL: "+categoryDtos.length);
        getCategories(categories);
    }

    private void getCategoriesFromDB() {
        String url = getRequestUrl() + "categories";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        onGetResponseCategories(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { onErrorResponse(error);}
                });
    }

    private void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        spinnerCat = offersFragmentView.findViewById(R.id.chooseCategoryOffer);
        spinnerCat.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(dtoArrayAdapter);
        spinnerCat.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner categorySpinner = (Spinner) parent;
        Spinner filterSpinner = (Spinner) parent;
        String selectedItem = (String) parent.getItemAtPosition(position);
        if(categorySpinner.getId()==R.id.chooseCategoryOffer){
            for(CategoryDto c : categoryDtos){
                if(c.getName().equals(selectedItem)) selectedCategory = c;
            }
        }
        if(filterSpinner.getId()==R.id.filterOffers){
            switch (position){
                case 1:
                    if(tagClick%2==0){
                        tags.setVisibility(View.VISIBLE);
                        offersFilter.setVisibility(View.VISIBLE);
                    }
                    else tags.setVisibility(View.GONE);
                    tagClick++;
                    break;
                case 2:
                    if(categoryClick%2==0){
                        getCategoriesFromDB();
                        offersFilter.setVisibility(View.VISIBLE);
                    }
                    else {
                        if(spinnerCat!=null) spinnerCat.setVisibility(View.GONE);
                    }
                    categoryClick++;
                    break;
                case 3:
                    if(dataClick%2==0){
                        startDate.setVisibility(View.VISIBLE);
                        endDate.setVisibility(View.VISIBLE);
                        offersFilter.setVisibility(View.VISIBLE);
                    }else{
                        startDate.setVisibility(View.GONE);
                        endDate.setVisibility(View.GONE);
                    }
                    dataClick++;
                    break;
            }
            filterSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
