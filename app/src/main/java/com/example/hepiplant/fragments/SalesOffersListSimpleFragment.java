package com.example.hepiplant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.hepiplant.R;
import com.example.hepiplant.SalesOfferActivity;
import com.example.hepiplant.adapter.recyclerview.SalesOffersRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.SalesOfferDto;
import com.example.hepiplant.helper.JSONRequestProcessor;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.example.hepiplant.helper.RequestType;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Arrays;

public class SalesOffersListSimpleFragment extends Fragment implements SalesOffersRecyclerViewAdapter.ItemClickListener  {

    private static final String TAG = "OffersListSmplFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SalesOfferDto> salesOffersResponseHandler;
    private View salesOffersFragmentView;
    private RecyclerView salesOffersRecyclerView;
    private SalesOffersRecyclerViewAdapter salesOffersRecyclerViewAdapter;
    private SalesOfferDto[] salesOffers = new SalesOfferDto[]{};
    private final String tag;

    public SalesOffersListSimpleFragment(String tag) {
        this.tag = tag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        config = (Configuration) getActivity().getApplicationContext();
        requestProcessor = new JSONRequestProcessor(config);
        salesOffersResponseHandler = new JSONResponseHandler<>(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        salesOffersFragmentView = inflater.inflate(R.layout.fragment_simple_list, container, false);
        initView();
        setLayoutManager();
        makeGetDataRequest();
        return salesOffersFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(config, SalesOfferActivity.class);
        intent.putExtra("salesOfferId", salesOffers[position].getId());
        intent.putExtra("userId", salesOffers[position].getUserId());
        startActivity(intent);
    }

    private void makeGetDataRequest(){
        String url = getRequestUrl() + "salesoffers/tag/" + tag;
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
        return config.getUrl();
    }

    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived(). Data is " + response);
        salesOffers = salesOffersResponseHandler.handleArrayResponse(response, SalesOfferDto[].class);
        if(salesOffers.length == 0){
            TextView noDataTextView = salesOffersFragmentView.findViewById(R.id.noDataSimpleListTextView);
            noDataTextView.setVisibility(View.VISIBLE);
            noDataTextView.setText(getText(R.string.no_sales_offers_to_display));
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
        salesOffersRecyclerView = salesOffersFragmentView.findViewById(R.id.postsRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        salesOffersRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        salesOffersRecyclerViewAdapter = new SalesOffersRecyclerViewAdapter(getActivity(), salesOffers);
        salesOffersRecyclerViewAdapter.setClickListener(this);
        salesOffersRecyclerView.setAdapter(salesOffersRecyclerViewAdapter);
    }
}
