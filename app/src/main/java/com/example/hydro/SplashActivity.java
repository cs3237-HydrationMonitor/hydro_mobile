package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private IMqttToken subToken;
    private SharedPreferences sharedPreferences;
    private HydrationMonitor hydrationMonitor = null;
    private Resources app_resources;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        disable_action_bars();
        app_resources = getResources();

        this.sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);

        // Initialize MQTT Framework
        initialize_mqtt_client();
        check_for_active_broker();
    }

    public void disable_action_bars() {
        ActionBar actionBar = getActionBar();
        androidx.appcompat.app.ActionBar supportActionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.hide();
        }

        if(supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    public void transition_to_home_activity() {

        String hydrationHistoryData = sharedPreferences.getString(this.app_resources.getString(R.string.shared_pref_hydration_history), "");
        Log.i("MQTT", hydrationHistoryData);
        String currentDate = sharedPreferences.getString(this.app_resources.getString(R.string.shared_pref_current_date), "");
        int dailyHydrationCount = sharedPreferences.getInt(this.app_resources.getString(R.string.shared_pref_hydration_count), 0);

        try {
            if (!hydrationHistoryData.isEmpty()) {

                Type dataType = new TypeToken<HashMap<String, Integer>>() {
                }.getType();

                Gson gson = new Gson();
                HashMap<String, Integer> hydrationHistory = gson.fromJson(hydrationHistoryData, dataType);
                this.hydrationMonitor = new HydrationMonitor(dailyHydrationCount, currentDate, hydrationHistory);

            } else {
                this.hydrationMonitor = new HydrationMonitor(dailyHydrationCount, currentDate, new HashMap<String, Integer>());
            }
        } catch (Exception e) {
            this.hydrationMonitor = new HydrationMonitor();
        }

        final Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra(this.app_resources.getString(R.string.shared_pref_hydration_monitor), this.hydrationMonitor);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 3000);

    }

    public void subscribe_to_prediction_channel() {
        try {
            this.subToken = this.client.subscribe(this.app_resources.getString(R.string.mqtt_topic_prediction), 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "MQTT Broker Channel Active!",
                            Toast.LENGTH_SHORT).show();
                    disconnect_from_broker();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Subscription Faliure!",
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void initialize_mqtt_client() {
        String clientId = MqttClient.generateClientId();
        this.client = new MqttAndroidClient(getApplicationContext(), this.app_resources.getString(R.string.mqtt_broker_uri),
                clientId);
    }

    private void disconnect_from_broker() {
        try {
            this.client.disconnect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    transition_to_home_activity();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void check_for_active_broker() {
        try {
//            Log.i("MQTT", "Checking");
            this.timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No connection to MQTT Broker!",
                                    Toast.LENGTH_LONG).show();
                            client.close();
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    finishAffinity();
                                }
                            }, 5000);
                        }
                    });
                }
            }, 8000);
            this.client.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(getApplicationContext(), "Connection Success!",
//                            Toast.LENGTH_LONG).show();
                    timer.cancel();
                    subscribe_to_prediction_channel();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Connection Failed!",
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}