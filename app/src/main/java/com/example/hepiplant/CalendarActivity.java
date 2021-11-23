package com.example.hepiplant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button saveDateButton;
    private TextView dateTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        calendarView = findViewById(R.id.calendarView);
        if(getIntent().getExtras().getString("event").equals("plant"))
            calendarView.setMaxDate(new Date().getTime());
        else
            calendarView.setMinDate(new Date().getTime());
        saveDateButton = findViewById(R.id.buttonDate);
        dateTextView = findViewById(R.id.textViewDate);

        setCalendarViewOnDateChangeListener();
        setSaveDateButtonOnClickListener();
    }

    private void setSaveDateButtonOnClickListener() {
        saveDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("data", dateTextView.getText());
                setResult(RESULT_OK,intent);
               finish();
            }
        });
    }

    private void setCalendarViewOnDateChangeListener() {
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

                dateTextView.setText(date);

            }
        });
    }
}