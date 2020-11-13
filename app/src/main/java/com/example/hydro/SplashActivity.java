package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SplashActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private IMqttToken subToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    public void initialize_mqtt_client() {
        String clientId = MqttClient.generateClientId();
        this.client = new MqttAndroidClient(getApplicationContext(), "BROKER SERVER",
                clientId);
    }

    public void connect_to_mqtt_broker() {
        try {
            this.client.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Connection Success!",
                            Toast.LENGTH_LONG).show();
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