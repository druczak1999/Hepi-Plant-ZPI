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

public class SalesOffersListFragment extends Fragment implements
        SalesOffersRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "SalesOffersListFragment";

    private Configuration config;
    private JSONRequestProcessor requestProcessor;
    private JSONResponseHandler<SalesOfferDto> salesOfferResponseHandler;
    private View offersFragmentView;
    private RecyclerView offersRecyclerView;
    private SalesOffersRecyclerViewAdapter adapter;
    private SalesOfferDto[] salesOffers = new SalesOfferDto[]{};

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        offersFragmentView = inflater.inflate(R.layout.fragment_offers_list, container, false);

        initView();
        setLayoutManager();
        makeGetDataRequest();

        return offersFragmentView;
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
        return config.getUrl() + "salesoffers";
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

}
