package com.example.hepiplant.configuration;

import android.app.Application;
import android.content.res.AssetManager;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

public class Configuration extends Application {
    private String url;
    private Long userId;
    //backend security
    private Set<String> userRoles;
    private String token;
    private RequestQueue queue;
    private Gson gson;
    private Uri photo;

    public Uri getPhoto() { return photo; }

    public void setPhoto(Uri photo) { this.photo = photo; }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<String> userRoles) {
        this.userRoles = userRoles;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RequestQueue getQueue() {
        if (queue == null){
            queue = Volley.newRequestQueue(this);
        }
        return queue;
    }

    public Gson getGson() {
        if (gson == null){
            gson = new Gson();
        }
        return gson;
    }

    public String readProperties() throws IOException {
        Properties prop = new Properties();
        AssetManager assetManager = getApplicationContext().getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        prop.load(inputStream);
        return prop.getProperty("url");
    }
}
