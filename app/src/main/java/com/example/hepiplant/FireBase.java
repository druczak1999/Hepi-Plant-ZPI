package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.hepiplant.configuration.Configuration;
import com.example.hepiplant.dto.AuthenticationResponseDto;
import com.example.hepiplant.dto.UserDto;
import com.example.hepiplant.helper.JSONResponseHandler;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FireBase extends AppCompatActivity {

    private static final String TAG = "FireBaseActivity";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private Configuration config;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_base);

        config = (Configuration) getApplicationContext();

        createSignInIntent();
    }

    public void createSignInIntent() {
        Log.v(TAG, "Entering createSignInIntent()");
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.plant)      // Set logo drawable
                .setTheme(R.style.Theme_AppCompat_Light)
                .build();
        signInLauncher.launch(signInIntent);
    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.v(TAG, "Entering onSignInResult()");
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            Log.v(TAG, "Sign in successful");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            makePostUserRequest(user);
        } else {
            Log.v(TAG, "Result code: " + result.getResultCode());
            Log.v(TAG, "Sign in failed. Response: "+
                    response.getError().getMessage()+" Code: " +
                    response.getError().getErrorCode() + " Cause: "+
                    response.getError().getCause());
        }
    }


    public void signOut() {
        Log.v(TAG, "Entering signOut()");
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getApplicationContext(), FireBase.class));
                    }
                });
    }

    public void themeAndLogo() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_theme_logo]
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.plant)      // Set logo drawable
                .setTheme(R.style.Theme_AppCompat)      // Set theme
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_theme_logo]
    }

    private void makePostUserRequest(FirebaseUser user){
        try {
            config.setUrl(config.readProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (user.getPhotoUrl()!=null) {
            Log.v(TAG, "Entering setting photo");
            config.setPhoto(user.getPhotoUrl());
        }
        else
        {
            Log.v(TAG, "Photo is null");
        }
        String url = config.getUrl() + "users";
        JSONObject postData = new JSONObject();
        try {
            postData.put("username", user.getDisplayName());
            postData.put("email", user.getEmail());
            postData.put("uid", user.getUid());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
            new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResponse(JSONObject response) {
                    onPostResponseReceived(response);
                    makeTokenRequest(user);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "POST user request unsuccessful. Error message: " + error.getMessage());
                }
        });
        config.getQueue().add(jsonArrayRequest);
    }

    private void makeTokenRequest(FirebaseUser user){
        String url = config.getUrl() + "authenticate";
        JSONObject postData = new JSONObject();
        try {
            postData.put("email", user.getEmail());
            postData.put("uid", user.getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        onTokenReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "POST authenticate request unsuccessful. Error message: " + error.getMessage());
            }
        });
        config.getQueue().add(jsonArrayRequest);
    }

    private void onPostResponseReceived(JSONObject response) {
        Log.v(TAG, "POST user request successful. Returned user: " + response);
        UserDto data = new UserDto();
        JSONResponseHandler<UserDto> userResponseHandler = new JSONResponseHandler<>(config);
        data = userResponseHandler.handleResponse(response, UserDto.class);
        config.setUserId(data.getId());
        config.setUserRoles(data.getRoles());
        config.setNotifications(data.isNotifications());
        Log.v(TAG, "POST user id " + config.getUserId());
    }

    private void onTokenReceived(JSONObject response) {
        AuthenticationResponseDto data = new AuthenticationResponseDto();
        JSONResponseHandler<AuthenticationResponseDto> authResponseHandler = new JSONResponseHandler<>(config);
        data = authResponseHandler.handleResponse(response, AuthenticationResponseDto.class);
        config.setToken(data.getJwt());
        Log.v(TAG, "POST authentication request successful");
        Intent intent;
        if (config.getUserRoles().contains(ROLE_ADMIN)){
            intent = new Intent(getApplicationContext(), MainAdminActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), MainTabsActivity.class);
        }
        startActivity(intent);
    }

}