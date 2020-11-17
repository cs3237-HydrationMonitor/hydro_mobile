package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    HydrationMonitor hydrationMonitor;
    SharedPreferences sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
    MyMqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        disable_action_bars();
        Intent activityIntent = getIntent();

        this.hydrationMonitor = (HydrationMonitor)activityIntent.getSerializableExtra("MqttClient");
        this.client = (MyMqttClient)activityIntent.getSerializableExtra("HydrationMonitor");

        TextView counterTextView = (TextView)findViewById(R.id.counter_text);
        counterTextView.setText(String.valueOf(this.hydrationMonitor.getDailyHydrationCount()));
    }

    public void disable_action_bars() {
        try {
            getActionBar().hide();
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            // Do nothing
        }
    }

    public void updateCounterText() {
        TextView counterTextView = (TextView)findViewById(R.id.counter_text);
        counterTextView.setText(String.valueOf(this.hydrationMonitor.getDailyHydrationCount()));
    }
}