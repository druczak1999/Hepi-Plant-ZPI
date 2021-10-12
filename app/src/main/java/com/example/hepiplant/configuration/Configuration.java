package com.example.hepiplant.configuration;

import android.app.Application;
import android.content.res.AssetManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration extends Application {
    private String url;
    private Long userId;
    //backend security
    private String token;
    private RequestQueue queue;

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

    public String readProperties() throws IOException {

        Properties prop = new Properties();
        AssetManager assetManager = getApplicationContext().getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        prop.load(inputStream);
        return prop.getProperty("url");
    }
}
