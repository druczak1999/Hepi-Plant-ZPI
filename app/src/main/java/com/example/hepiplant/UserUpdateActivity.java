package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                intent.putExtra("userId", id);
                intent.putExtra("userEmail", email);
                intent.putExtra("userName", textView.toString());
                startActivity(intent);
            }
        });

    }
}