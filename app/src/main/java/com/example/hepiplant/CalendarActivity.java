package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    Button button;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        calendarView = findViewById(R.id.calendarView);
        button = findViewById(R.id.buttonDate);
        textView = findViewById(R.id.textViewDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month+=1;
                String date = year+"-";
                if (month<10){
                    date+="0"+month+"-";
                }
                else date+=month+"-";
                if(dayOfMonth<10){
                    date+="0"+dayOfMonth;
                }
                else date+=dayOfMonth;

                textView.setText(date);

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("data",textView.getText());
                setResult(RESULT_OK,intent);
               finish();
            }
        });
    }
}