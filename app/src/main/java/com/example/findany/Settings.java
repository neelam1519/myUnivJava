package com.example.findany;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

//Settings to change the user experience
public class Settings extends AppCompatActivity {
    String TAG="Settings";
    Switch timetablenotification;
    boolean timetableswitch;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    MaterialToolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        intializeviews();

        timetableswitch=sharedPreferences.getBoolean("timetableswitch",true);
        timetablenotification.setChecked(timetableswitch);

        timetablenotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The switch is turned on

                    Log.d(TAG, "onCreate: Sending broadcast"); // Add this log
                    Intent intents = new Intent("com.neelam.findany.NOTIFY");
                    sendBroadcast(intents);

                    editor.putBoolean("timetableswitch",true);
                    editor.apply();

                } else {
                    // The switch is turned off

                    // Initialize AlarmManager
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                    // Initialize your pending intent
                    Intent intents = new Intent("com.neelam.findany.NOTIFY");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(Settings.this, 143, intents, PendingIntent.FLAG_IMMUTABLE); // Added FLAG_IMMUTABLE

                    // Cancel the alarm
                    if (alarmManager != null) {
                        Log.d("MainActivity", "onCreate: Canceling broadcast alarm"); // Add this log
                        alarmManager.cancel(pendingIntent);
                    }

                    editor.putBoolean("timetableswitch",false);
                    editor.apply();

                }
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private  void intializeviews(){
        timetablenotification=findViewById(R.id.timetable_notification);
        toolbar = findViewById(R.id.material_toolbar);

        sharedPreferences=getSharedPreferences("settings",MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }
}