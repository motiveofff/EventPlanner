package com.example.eventplanner;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AddEventActivity extends AppCompatActivity {

    EditText titleInput;
    DatePicker datePicker;
    TimePicker timePicker;
    Button saveBtn;
    DBHelper db;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = new DBHelper(this);

        titleInput = findViewById(R.id.titleInput);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        saveBtn = findViewById(R.id.saveBtn);

        // Запрос разрешения на точные AlarmManager для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название события", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            int hour, minute;
            if (Build.VERSION.SDK_INT >= 23) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }

            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), hour, minute);

            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(calendar.getTime());

            db.insertEvent(title, datetime);
            setReminder(calendar, title);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CODE);
            } else {
                addToSystemCalendar(title, calendar);
            }

            Toast.makeText(this, "Событие добавлено", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setReminder(Calendar calendar, String title) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            try {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            } catch (SecurityException e) {
                Toast.makeText(this, "Нет разрешения на точный AlarmManager", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addToSystemCalendar(String title, Calendar calendar) {
        long startMillis = calendar.getTimeInMillis();
        long endMillis = startMillis + 60 * 60 * 1000;

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, "Событие из Event Planner");
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // обычно 1 — основной календарь
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri != null) {
            Toast.makeText(this, "Добавлено в системный календарь", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка при добавлении в календарь", Toast.LENGTH_SHORT).show();
        }
    }

    // Обработка ответа на запрос разрешения WRITE_CALENDAR
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Разрешение на календарь получено", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Нет разрешения на системный календарь", Toast.LENGTH_SHORT).show();
        }
    }
}
