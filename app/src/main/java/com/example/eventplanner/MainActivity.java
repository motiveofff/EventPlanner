package com.example.eventplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    DBHelper db;
    ListView eventList;
    TextView tvNoEvents;
    CalendarView calendarView;
    ArrayAdapter<String> adapter;
    ArrayList<String> events;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);
        eventList = findViewById(R.id.eventList);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        calendarView = findViewById(R.id.calendarView);
        Button btnAdd = findViewById(R.id.btnAdd);

        events = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, events);
        eventList.setAdapter(adapter);

        selectedDate = getTodayDate();

        loadEvents(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadEvents(selectedDate);
        });

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEventActivity.class));
        });
    }

    private void loadEvents(String dateFilter) {
        events.clear();
        Cursor cursor = db.getAllEvents();
        boolean found = false;
        while (cursor.moveToNext()) {
            String title = cursor.getString(1);
            String datetime = cursor.getString(2);
            if (datetime.startsWith(dateFilter)) {
                events.add(title + " - " + datetime.substring(11));
                found = true;
            }
        }
        adapter.notifyDataSetChanged();
        tvNoEvents.setVisibility(found ? TextView.GONE : TextView.VISIBLE);
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(selectedDate);
    }
}
