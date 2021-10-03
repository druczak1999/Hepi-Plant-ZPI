package com.example.hepiplant;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hepiplant.dto.UserDto;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class UserUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);
        TextView textView = (EditText) findViewById(R.id.nazwa2);
        TextView textView2 = (TextView) findViewById(R.id.nazwa4);
        Button save = (Button) findViewById(R.id.save);
        Intent intent = this.getIntent();
        String id = intent.getExtras().getString("userId");
        String username = intent.getExtras().getString("userName");
        String email = intent.getExtras().getString("userEmail");
        textView.setText(username);
        textView2.setText(email);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), User.class);
                intent.putExtra("userId", id);
                intent.putExtra("userEmail", email);
                intent.putExtra("userName", textView.toString());
                startActivity(intent);
            }
        });

    }
}