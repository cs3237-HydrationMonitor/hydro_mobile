package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HomeActivity extends AppCompatActivity {

    private HydrationMonitor hydrationMonitor;
    private final String TOPIC_PREDICTION = "g19/iot/predict";
    private SharedPreferences sharedPreferences;
    private MqttAndroidClient client;
    private IMqttToken subToken;
    private Resources app_resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        disable_action_bars();
        this.app_resources = getResources();

        this.sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);

        Intent activityIntent = getIntent();
        this.hydrationMonitor = (HydrationMonitor)activityIntent.getParcelableExtra("HydrationMonitor");

        final TextView counterTextView = (TextView)findViewById(R.id.counter_text);
        counterTextView.setText(String.valueOf(this.hydrationMonitor.getDailyHydrationCount()));
        this.hydrationMonitor.getLiveDailyHydrationCountData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                counterTextView.setText(String.valueOf(hydrationMonitor.getDailyHydrationCount()));
            }
        });

        initialize_mqtt_client();
        connect_to_broker();

        Button historyButton = (Button)findViewById(R.id.history_activity_button);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                hydrationMonitor.incrementDailyCount();
//                save_to_prefs();
                Intent i = new Intent(getApplicationContext(), HistoryActivity.class);
                i.putExtra(app_resources.getString(R.string.shared_pref_hydration_monitor), hydrationMonitor);
                startActivity(i);
            }
        });

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

    public void subscribe_to_prediction_channel() {
        try {
            while(this.client == null) {
                Thread.sleep(400);
            }
            this.subToken = this.client.subscribe(TOPIC_PREDICTION, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(getApplicationContext(), "MQTT Broker Channel Active!",
//                            Toast.LENGTH_SHORT).show();
                    set_client_message_received_callback();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Subscription Faliure!",
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void save_to_prefs() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(this.hydrationMonitor.getHydrationHistory());
//        Log.i("MQTT", jsonString);
        sharedPreferences.edit().putString(this.app_resources.getString(R.string.shared_pref_current_date), this.hydrationMonitor.getCurrentDate()).apply();
        sharedPreferences.edit().putInt(this.app_resources.getString(R.string.shared_pref_hydration_count), this.hydrationMonitor.getDailyHydrationCount()).apply();
        sharedPreferences.edit().putString(this.app_resources.getString(R.string.shared_pref_hydration_history), jsonString).apply();
    }

    public void initialize_mqtt_client() {
        String clientId = MqttClient.generateClientId();
        this.client = new MqttAndroidClient(getApplicationContext(), this.app_resources.getString(R.string.mqtt_broker_uri),
                clientId);
    }

    private void connect_to_broker() {
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

    private void set_client_message_received_callback() {
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

    private void disconnect_from_broker() {
        try {
            this.client.disconnect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
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