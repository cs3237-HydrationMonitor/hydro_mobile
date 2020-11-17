package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private final String TOPIC_PREDICTION = "g19/iot/predict";
    private MyMqttClient client;
    private IMqttToken subToken;
    private SharedPreferences sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
    private HydrationMonitor hydrationMonitor = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        disable_action_bars();

        // Initialize MQTT Framework
        initialize_mqtt_client();
        connect_and_subscribe_to_mqtt_broker();
    }

    public void disable_action_bars() {
        try {
            getActionBar().hide();
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            // Do nothing
        }
    }

    public void transition_to_home_activity() {

        String hydrationHistoryData = sharedPreferences.getString("HydrationHistory", "");

        try {
            if (!hydrationHistoryData.isEmpty()) {

                Type dataType = new TypeToken<ArrayList<Pair<String, Integer>>>() {
                }.getType();

                Gson gson = new Gson();

                String currentDate = sharedPreferences.getString("CurrentDate", "");
                int dailyHydrationCount = sharedPreferences.getInt("DailyHydrationCount", 0);
                ArrayList<Pair<String, Integer>> hydrationHistory = gson.fromJson(hydrationHistoryData, dataType);

                this.hydrationMonitor = new HydrationMonitor(dailyHydrationCount, currentDate, hydrationHistory);
            } else {
                this.hydrationMonitor = new HydrationMonitor();
            }
        } catch (Exception e) {
            this.hydrationMonitor = new HydrationMonitor();
        }

        final Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("MqttClient", this.client);
        intent.putExtra("HydrationMonitor", this.hydrationMonitor);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 3000);

    }

    public void subscribe_to_prediction_channel() {
        try {
            this.subToken = this.client.subscribe(TOPIC_PREDICTION, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(getApplicationContext(), "Subscription Success!",
////                            Toast.LENGTH_LONG).show();
                    transition_to_home_activity();
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

    private void save_to_prefs() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(this.hydrationMonitor.getHydrationHistory());
        sharedPreferences.edit().putString("HydrationHistory", jsonString).apply();
    }

    public void initialize_mqtt_client() {
        String clientId = MqttClient.generateClientId();
        this.client = new MyMqttClient(getApplicationContext(), "tcp://52.88.144.214",
                clientId);
        this.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getApplicationContext(), "Connection Loss!",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals(TOPIC_PREDICTION)) {
//                    Toast.makeText(getApplicationContext(), message.toString(),
//                            Toast.LENGTH_LONG).show();
                    if(message.toString().equals("1")) {
                        // In the case that hydrationMonitor has not initialized by the time it
                        // gets the first result.
                        while(hydrationMonitor == null) {
                            // Wait for hydration monitor to be initialized
                            Thread.sleep(1000);
                        }
                        if(hydrationMonitor.isNewDay()) {
                            hydrationMonitor.startNewDay();
                        }
                        hydrationMonitor.incrementDailyCount();
                        save_to_prefs();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
//                Toast.makeText(getApplicationContext(), "Delivery Complete",
//                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void connect_and_subscribe_to_mqtt_broker() {
        try {
            this.client.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(getApplicationContext(), "Connection Success!",
//                            Toast.LENGTH_LONG).show();
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