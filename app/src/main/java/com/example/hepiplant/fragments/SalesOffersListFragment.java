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

    private View offersFragmentView;
    private RecyclerView offersRecyclerView;
    private SalesOfferDto[] salesOffers = new SalesOfferDto[]{};
    private Button offersSortButton, offersFilterButton, offersStartDateButton, offersEndDateButton;
    private Spinner offersFilterSpinner, categorySpinner;
    private TextView closeTagFiler, closeCategoryFilter, closeDateFilters;
    private LinearLayout tagLinearLayout, categoryLinearLayout,datesLinearLayout;
    private EditText offersTagsEditText;
    private CategoryDto[] categoryDtos;
    private CategoryDto selectedCategory;

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SalesOfferDto> salesOfferResponseHandler;
    private JSONResponseHandler<CategoryDto> categoryResponseHandler;

    private static int sortClick = 0;
    private static int filterClick = 0;

    public SalesOffersListFragment() {}

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

        setUpViewsForFilters();
        initView();
        setLayoutManager();
        makeGetDataRequest();
        getCategoriesFromDB();
        setOffersSortButtonOnClickListener();
        setDateButtonsOnClickListener();
        setOffersFilterButtonOnClickListener();
        setOffersFilterSpinnerAdapter();
        setCloseViewsOnClickListeners();
        adjustLayoutForAdmin();
        return offersFragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                offersStartDateButton.setText(data.getExtras().getString("date"));
            }
        }
        if(requestCode==2){
            if(resultCode==-1){
                offersEndDateButton.setText(data.getExtras().getString("date"));
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
                    setRecyclerViewLayoutParams(1);
                    offersTagsEditText.setVisibility(View.VISIBLE);
                    tagLinearLayout.setVisibility(View.VISIBLE);
                    closeTagFiler.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    getCategoriesFromDB();
                    setRecyclerViewLayoutParams(1);
                    categoryLinearLayout.setVisibility(View.VISIBLE);
                    closeCategoryFilter.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    setRecyclerViewLayoutParams(1);
                    offersStartDateButton.setVisibility(View.VISIBLE);
                    offersEndDateButton.setVisibility(View.VISIBLE);
                    datesLinearLayout.setVisibility(View.VISIBLE);
                    closeDateFilters.setVisibility(View.VISIBLE);
                    break;
            }
            filterSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void setUpViewsForFilters() {
        offersSortButton = offersFragmentView.findViewById(R.id.sortOffersButton);
        offersFilterButton = offersFragmentView.findViewById(R.id.filterOffersButton);
        offersFilterSpinner = offersFragmentView.findViewById(R.id.filterOffersSpinner);
        offersStartDateButton = offersFragmentView.findViewById(R.id.startDateButtonInOfferFilter);
        offersEndDateButton = offersFragmentView.findViewById(R.id.endDateButtonInOfferFilter);
        offersTagsEditText = offersFragmentView.findViewById(R.id.tagEditTextInOfferFilter);
        closeTagFiler = offersFragmentView.findViewById(R.id.closeTagFilterOffer);
        closeCategoryFilter = offersFragmentView.findViewById(R.id.closeCategoryFilterOffer);
        closeDateFilters = offersFragmentView.findViewById(R.id.closeDateFiltersOffer);
        tagLinearLayout = offersFragmentView.findViewById(R.id.tagFilterLinearLayoutOffer);
        categoryLinearLayout = offersFragmentView.findViewById(R.id.categoryFilterLinearLayoutOffer);
        datesLinearLayout = offersFragmentView.findViewById(R.id.datesFilterLinearLayoutOffer);
    }

    private void setCloseViewsOnClickListeners(){
        closeTagFiler.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            offersTagsEditText.setText("");
            offersTagsEditText.setVisibility(View.GONE);
            tagLinearLayout.setVisibility(View.GONE);
            closeTagFiler.setVisibility(View.GONE);
        });

        closeCategoryFilter.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            categorySpinner.setVisibility(View.GONE);
            selectedCategory=null;
            categoryLinearLayout.setVisibility(View.GONE);
            closeCategoryFilter.setVisibility(View.GONE);
        });

        closeDateFilters.setOnClickListener(v -> {
            setRecyclerViewLayoutParams(-1);
            offersEndDateButton.setText("");
            offersStartDateButton.setText("");
            offersStartDateButton.setVisibility(View.GONE);
            offersEndDateButton.setVisibility(View.GONE);
            datesLinearLayout.setVisibility(View.GONE);
            closeDateFilters.setVisibility(View.GONE);
        });
    }

    private void setRecyclerViewLayoutParams(int plusOrMinus) {
        offersRecyclerView.setPadding(offersRecyclerView.getPaddingLeft(),
                offersRecyclerView.getPaddingTop(),
                offersRecyclerView.getPaddingRight(),
                offersRecyclerView.getPaddingBottom()
                + (int)(plusOrMinus * getResources().getDimension(R.dimen.SortLinearLayout)));
    }

    private void setDateButtonsOnClickListener(){
        offersStartDateButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CalendarActivity.class);
            intent.putExtra("event","plant");
            startActivityForResult(intent, 1);
        });

        offersEndDateButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CalendarActivity.class);
            intent.putExtra("event","plant");
            startActivityForResult(intent, 2);
        });
    }

    private void adjustLayoutForAdmin() {
        if(config.getUserRoles().contains(ROLE_ADMIN)){
            offersRecyclerView.setPadding(
                    offersRecyclerView.getPaddingLeft(),
                    offersRecyclerView.getPaddingTop(),
                    offersRecyclerView.getPaddingRight(),
                    (int) getResources().getDimension(R.dimen.padding_bottom_admin_sales_offers));
            offersRecyclerView.setClipToPadding(true);
        }
    }

    private void setOffersFilterSpinnerAdapter(){
        offersFilterSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, Arrays.asList("Filtruj","Tag","Kategoria","Data"));
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
        offersFilterButton.setOnClickListener(v -> {
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
        });
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl() + "salesoffers";
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    private void makeGetDataRequestWithParam(){
        String url = getRequestUrl()+"salesoffers?";
        url = prepareUrlForGetDataRequest(url);
        Log.v(TAG, "Invoking requestProcessor");
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseReceived, this::onErrorResponseReceived);
    }

    private String prepareUrlForGetDataRequest(String url) {
        if(offersStartDateButton.getVisibility()== View.VISIBLE
                && offersStartDateButton.getText()!=null && !offersStartDateButton.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "startDate=" + offersStartDateButton.getText().toString().substring(0, 10).trim();
        }
        if(offersEndDateButton.getVisibility()==View.VISIBLE
                && offersEndDateButton.getText()!=null && !offersEndDateButton.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            url += "endDate=" + offersEndDateButton.getText().toString().substring(0, 10).trim();
        }
        if(offersTagsEditText.getVisibility()==View.VISIBLE && offersTagsEditText.getText()!=null && !offersTagsEditText.getText().toString().isEmpty()) {
            if (url.charAt(url.length() - 1) != '?') url += "&";
            if(offersTagsEditText.getText().toString().charAt(0)=='#')
                url += "tag=" + offersTagsEditText.getText().toString().substring(1).trim();
            else  url += "tag=" + offersTagsEditText.getText().toString().trim();
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
        SalesOffersRecyclerViewAdapter adapter = new SalesOffersRecyclerViewAdapter(getActivity(), salesOffers);
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
            Log.e(TAG, "Status code: " + networkResponse.statusCode +
                    " Data: " + Arrays.toString(networkResponse.data));
        }
    }

    private void onGetResponseCategories(JSONArray response){
        categoryDtos = categoryResponseHandler.handleArrayResponse(response, CategoryDto[].class);
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < categoryDtos.length; i++) {
            categories.add(categoryDtos[i].getName());
        }
        Log.v(TAG,"DL: "+categoryDtos.length);
        getCategories(categories);
    }

    private void getCategoriesFromDB() {
        String url = getRequestUrl() + "categories";
        requestProcessor.makeRequest(Request.Method.GET, url, null, RequestType.ARRAY,
                (Response.Listener<JSONArray>) this::onGetResponseCategories, this::onErrorResponseReceived);
    }

    private void getCategories(List<String> categories) {
        Log.v(TAG, "Species size" + categories.size());
        categories.add(0,"");
        categorySpinner = offersFragmentView.findViewById(R.id.categorySpinnerInOfferFilter);
        categorySpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dtoArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        dtoArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dtoArrayAdapter);
        categorySpinner.setVisibility(View.VISIBLE);
    }

}
