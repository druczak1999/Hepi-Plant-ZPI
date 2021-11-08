package com.example.hepiplant.helper;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.hepiplant.configuration.Configuration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JSONRequestProcessor {

    private static final String TAG = "JSONRequestProcessor";
    private final Configuration config;

    public JSONRequestProcessor(Configuration config) {
        this.config = config;
    }

    public void makeRequest(final int method,
                            final String url,
                            final JSONObject postData,
                            final RequestType type,
                            final Response.Listener<?> responseListener,
                            final Response.ErrorListener errorListener){
        switch(type){
            case ARRAY:
                makeJSONArrayRequest(method, url, null, (Response.Listener<JSONArray>) responseListener, errorListener);
                break;
            case OBJECT:
                makeJSONObjectRequest(method, url, postData, (Response.Listener<JSONObject>) responseListener, errorListener);
                break;
            case STRING:
                makeStringRequest(method, url, (Response.Listener<String>) responseListener, errorListener);
        }
    }

    private void makeStringRequest(final int method,
                                   final String url,
                                   final Response.Listener<String> responseListener,
                                   final Response.ErrorListener errorListener){
        StringRequest stringRequest = new StringRequest(method, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(stringRequest);
    }

    private void makeJSONObjectRequest(final int method,
                                       final String url,
                                       final JSONObject postData,
                                       final Response.Listener<JSONObject> responseListener,
                                       final Response.ErrorListener errorListener){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, postData, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonObjectRequest);
    }

    private void makeJSONArrayRequest(final int method,
                                      final String url,
                                      final JSONArray postData,
                                      final Response.Listener<JSONArray> responseListener,
                                      final Response.ErrorListener errorListener){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(method, url, postData, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return prepareRequestHeaders();
            }
        };
        Log.v(TAG, "Sending the request to " + url);
        config.getQueue().add(jsonArrayRequest);
    }

    private Map<String, String> prepareRequestHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getToken());
        return headers;
    }

}
