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
import com.example.hepiplant.EventViewActivity;
import com.example.hepiplant.PostActivity;
import com.example.hepiplant.R;
import com.example.hepiplant.adapter.recyclerview.EventsRecyclerViewAdapter;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.EventDto;
import com.example.hepiplant.dto.PlantDto;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class EventsListFragment extends Fragment implements EventsRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "EventsListFragment";

    private Configuration config;
    private View eventsFragmentView;
    private RecyclerView eventsRecyclerView;
    private EventsRecyclerViewAdapter adapter;
    private EventDto[] events = new EventDto[]{};
    private PlantDto[] plants = new PlantDto[]{};

    public EventsListFragment() {
    }

    public static EventsListFragment newInstance() {
        EventsListFragment fragment = new EventsListFragment();
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
        eventsFragmentView = inflater.inflate(R.layout.fragment_events_list, container, false);
        initView();
        makeGetDataRequest();
        setLayoutManager();
        return eventsFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGetDataRequest();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(TAG, "onItemClick()");
        Intent intent = new Intent(getActivity().getApplicationContext(), EventViewActivity.class);
        intent.putExtra("eventId", events[position].getId());
        intent.putExtra("eventName", events[position].getEventName());
        intent.putExtra("plantName", events[position].getPlantName());
        intent.putExtra("eventDate",events[position].getEventDate());
        intent.putExtra("eventDescription",events[position].getEventDescription());
        intent.putExtra("place","list");
        startActivity(intent);
    }

    private void makeGetDataRequest() {
        String url = getRequestUrl()+ "events/user/"+config.getUserId();
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
            }};
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
        return config.getUrl() ;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onGetResponseReceived(JSONArray response){
        Log.v(TAG, "onGetResponseReceived()");
        Gson gson = new Gson();
        events = gson.fromJson(String.valueOf(response), EventDto[].class);
        List<EventDto> eventDtoList = Arrays.asList(events);
        List<EventDto> sortedEvents = eventDtoList.stream()
                .sorted(Comparator.comparing(EventDto::getEventName))
                .sorted(Comparator.comparing(EventDto::getEventDate))
                .collect(Collectors.toList());
        EventDto[] newEvents= new EventDto[events.length];
        for (int i=0; i<events.length;i++){
            newEvents[i] = sortedEvents.get(i);
        }
        events = newEvents;
        Log.v(TAG, "DL: "+events.length);
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
        eventsRecyclerView = eventsFragmentView.findViewById(R.id.eventsRecyclerView);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        eventsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setAdapter() {
        adapter = new EventsRecyclerViewAdapter(getActivity(), events);
        adapter.setClickListener(this);
        eventsRecyclerView.setAdapter(adapter);
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }
}
