package com.example.hepiplant.helper;

import com.example.hepiplant.configuration.Configuration;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONResponseHandler<T> {

    private final Configuration config;

    public JSONResponseHandler(Configuration config) {
        this.config = config;
    }

    public T handleResponse(JSONObject response, Class<T> clazz){
        return config.getGson().fromJson(String.valueOf(response), clazz);
    }

    public T[] handleArrayResponse(JSONArray response, Class<T[]> clazz){
        return config.getGson().fromJson(String.valueOf(response), clazz);
    }

}
