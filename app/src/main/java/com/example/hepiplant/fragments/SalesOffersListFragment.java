package com.example.hepiplant.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.PostActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.SalesOffersRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.SalesOfferDto;
import com.google.gson.Gson;

import org.json.JSONArray;
import java.io.IOException;

public class SalesOffersListFragment extends Fragment implements
        SalesOffersRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "SalesOffersListFragment";
    private static final String BASE_URL = "http://10.0.0.163:8080";

    private View offersFragmentView;
    private RecyclerView offersRecyclerView;
    private SalesOffersRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreateView()");
        offersFragmentView = inflater.inflate(R.layout.fragment_offers_list, container, false);

        initView();
        setLayoutManager();
        makeDataRequest();

        return offersFragmentView;
    }

    // todo change for sales offer
    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
        intent.putExtra("salesOffer", (Parcelable) salesOffers[position]); // todo investigate
        startActivity(intent);
    }

    private void makeDataRequest(){
        // data to populate the RecyclerView with
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final Configuration config = (Configuration) getActivity().getApplicationContext();
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url =config.getUrl() + "salesoffers";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONArray>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONArray response) {
                    onResponseReceived(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onErrorResponseReceived(error);
                }
        });
        Log.v(TAG, "Sending the request to " + url);
        queue.add(jsonArrayRequest);
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

    private void onResponseReceived(JSONArray response){
        Log.v(TAG, "onResponseReceived()");
        Gson gson = new Gson();
        salesOffers = gson.fromJson(String.valueOf(response), SalesOfferDto[].class);
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
