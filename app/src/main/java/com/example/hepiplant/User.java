package com.example.hepiplant;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class User extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        TextView textView = (TextView) findViewById(R.id.nazwa2);
        TextView textView1 = (TextView) findViewById(R.id.przywitanie);
        TextView textView2 = (TextView) findViewById(R.id.nazwa4);
        Button change = (Button) findViewById(R.id.change);


        //Toolbar myToolbar =  findViewById(R.id.toolbar);
        //setSupportActionBar(myToolbar);
        Intent intent = this.getIntent();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.220:8080/users";

        JSONObject postData = new JSONObject();
        try {
            postData.put("username", intent.getExtras().getString("userName"));
            postData.put("email", intent.getExtras().getString("userEmail"));
            postData.put("uid", intent.getExtras().getString("userId"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("PO POST DATA");
        // Request a string response from the provided URL.
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the response string.

                        String str = String.valueOf(response); //http request
                        System.out.println("TUUUUUUUUUUUUUUUUUUUUUUUU"+str);
                        UserDto data = new UserDto();
                        Gson gson = new Gson();
                        data = gson.fromJson(str, UserDto.class);

                        //String sb = new String(data.getUsername());
                        //Arrays.stream(data).forEach(p -> sb.append(p.getUsername()).append("\n"));
                        textView.setText(data.getUsername());
                        //String sb2 = new String(data.getEmail());
                        textView2.setText(data.getEmail());
                        textView1.setText("Witaj "+data.getUsername()+"!");
                        UserDto finalData = data;
                        change.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), UserUpdateActivity.class);
                                intent.putExtra("userId", finalData.getId());
                                intent.putExtra("userEmail", finalData.getEmail());
                                intent.putExtra("userName", finalData.getUsername());
                                startActivity(intent);
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText(error.getMessage());
            }
        });
        queue.add(jsonArrayRequest);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
}