package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        disable_action_bars();

    }

    public void disable_action_bars() {
        try {
            getActionBar().hide();
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            // Do nothing
        }
    }
}