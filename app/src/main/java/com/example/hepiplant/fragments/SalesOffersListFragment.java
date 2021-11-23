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
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SalesOfferDto> salesOfferResponseHandler;
    private View offersFragmentView;
    private RecyclerView offersRecyclerView;
    private SalesOffersRecyclerViewAdapter adapter;
    private SalesOfferDto[] salesOffers = new SalesOfferDto[]{};
    private Button offersSortButton, offersFilterButton, offersStartDateButton, offersEndDateButton;
    private Spinner offersFilterSpinner, categorySpinner;
    private EditText offersTagsEditText;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;
    private static int sortClick = 0;
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

        offersSortButton = offersFragmentView.findViewById(R.id.sortOffersButton);
        offersFilterButton = offersFragmentView.findViewById(R.id.filterOffersButton);
        offersFilterSpinner = offersFragmentView.findViewById(R.id.filterOffersSpinner);
        offersStartDateButton = offersFragmentView.findViewById(R.id.startDateButtonInOfferFilter);
        offersEndDateButton = offersFragmentView.findViewById(R.id.endDateButtonInOfferFilter);
        offersTagsEditText = offersFragmentView.findViewById(R.id.tagEditTextInOfferFilter);
        initView();
        setLayoutManager();
        makeGetDataRequest();
        setOffersSortButtonOnClickListener();
        setDateButtonsOnClickListener();
        setOffersFilterButtonOnClickListener();
        setOffersFilterSpinnerAdapter();
        adjustLayoutForAdmin();
        return offersFragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                offersStartDateButton.setText(data.getExtras().getString("data"));
            }
        }
        if(requestCode==2){
            if(resultCode==-1){
                offersEndDateButton.setText(data.getExtras().getString("data"));
            }
        }
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner categorySpinner = (Spinner) parent;
        Spinner filterSpinner = (Spinner) parent;
        String selectedItem = (String) parent.getItemAtPosition(position);
        if(categorySpinner.getId()==R.id.categorySpinnerInOfferFilter){
            for(CategoryDto c : categoryDtos){
                if(c.getName().equals(selectedItem)) selectedCategory = c;
            }
        }
        if(filterSpinner.getId()==R.id.filterOffersSpinner){
            switch (position){
                case 1:
                    if(tagClick%2==0){
                        offersTagsEditText.setVisibility(View.VISIBLE);
                        offersFilterButton.setVisibility(View.VISIBLE);
                    }
                    else offersTagsEditText.setVisibility(View.GONE);
                    tagClick++;
                    break;
                case 2:
                    if(categoryClick%2==0){
                        getCategoriesFromDB();
                        offersFilterButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        if(this.categorySpinner !=null) this.categorySpinner.setVisibility(View.GONE);
                    }
                    categoryClick++;
                    break;
                case 3:
                    if(dataClick%2==0){
                        offersStartDateButton.setVisibility(View.VISIBLE);
                        offersEndDateButton.setVisibility(View.VISIBLE);
                        offersFilterButton.setVisibility(View.VISIBLE);
                    }else{
                        offersStartDateButton.setVisibility(View.GONE);
                        offersEndDateButton.setVisibility(View.GONE);
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

    private void setDateButtonsOnClickListener(){
        offersStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 1);
            }
        });

        offersEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("event","plant");
                startActivityForResult(intent, 2);
            }
        });
    }

    private void adjustLayoutForAdmin() {
        if(config.getUserRoles().contains(ROLE_ADMIN)){
            offersRecyclerView.setPadding(
                    offersRecyclerView.getPaddingLeft(),
                    offersRecyclerView.getPaddingTop(),
                    offersRecyclerView.getPaddingRight(), 0);
        }
    }

    private void setOffersFilterSpinnerAdapter(){
        offersFilterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, List.of("Filtruj","Tag","Kategoria","Data"));
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        offersFilterSpinner.setAdapter(dtoArrayAdapter);
    }

    private void setOffersSortButtonOnClickListener() {
        offersSortButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Log.v(TAG,"On click");
                List<SalesOfferDto> salesOfferDtos = new ArrayList<>(Arrays.asList(salesOffers));
                List<SalesOfferDto> offerDtos= new ArrayList<>();
                if(sortClick %2==0){
                    offerDtos = salesOfferDtos.stream()
                            .sorted(Comparator.comparing(SalesOfferDto::getCreatedDate).reversed())
                            .collect(Collectors.toList());
                }
                else{
                    offerDtos = salesOfferDtos.stream()
                            .sorted(Comparator.comparing(SalesOfferDto::getCreatedDate))
                            .collect(Collectors.toList());
                }
                sortClick++;
                SalesOfferDto [] newOffers = new SalesOfferDto[offerDtos.size()];
                for (int i=0;i<offerDtos.size();i++){
                    newOffers[i] = offerDtos.get(i);
                }

                salesOffers = newOffers;
                setAdapter();
            }
        });
    }

    private void setOffersFilterButtonOnClickListener() {
        offersFilterButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                makeGetDataRequestWithParam();
                if(filterClick%2==0){
                    offersFilterButton.setText(R.string.clean_button);
                    makeGetDataRequestWithParam();
                    selectedCategory=null;
                }
                else {
                    offersFilterButton.setText(R.string.filter_button);
                    makeGetDataRequest();
                }
                offersFilterSpinner.setSelection(0);
                filterClick++;
                if(offersTagsEditText.getVisibility()==View.VISIBLE)tagClick++;
                if(categorySpinner !=null && categorySpinner.getVisibility()==View.VISIBLE)categoryClick++;
                if(offersStartDateButton.getVisibility()==View.VISIBLE)dataClick++;
                offersTagsEditText.setVisibility(View.GONE);
                if(categorySpinner !=null)
                    categorySpinner.setVisibility(View.GONE);
                offersStartDateButton.setVisibility(View.GONE);
                offersEndDateButton.setVisibility(View.GONE);
            }
        });
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
        if(offersStartDateButton.getVisibility()== View.VISIBLE
                && offersStartDateButton.getText()!=null && !offersStartDateButton.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "startDate=" + offersStartDateButton.getText().toString().substring(0, 10);
        }
        if(offersEndDateButton.getVisibility()==View.VISIBLE
                && offersEndDateButton.getText()!=null && !offersEndDateButton.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "endDate=" + offersEndDateButton.getText().toString().substring(0, 10);
        }
        if(offersTagsEditText.getVisibility()==View.VISIBLE && offersTagsEditText.getText()!=null && !offersTagsEditText.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            if(offersTagsEditText.getText().toString().charAt(0)=='#')
                url += "tag=" + offersTagsEditText.getText().toString().substring(1, offersTagsEditText.getText().toString().length());
            else  url += "tag=" + offersTagsEditText.getText().toString();
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
        categorySpinner = offersFragmentView.findViewById(R.id.categorySpinnerInOfferFilter);
        categorySpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dtoArrayAdapter);
        categorySpinner.setVisibility(View.VISIBLE);
    }
}
