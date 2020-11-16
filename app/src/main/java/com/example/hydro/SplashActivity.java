package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SplashActivity extends AppCompatActivity {

    private final String TOPIC_PREDICTION = "g19/iot/predict";
    private MqttAndroidClient client;
    private IMqttToken subToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialize_mqtt_client();
        connect_and_subscribe_to_mqtt_broker();
    }

    public void subscribe_to_prediction_channel() {
        try {
            this.subToken = this.client.subscribe(TOPIC_PREDICTION, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Subscription Success!",
                            Toast.LENGTH_LONG).show();
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
        this.client = new MqttAndroidClient(getApplicationContext(), "tcp://52.88.144.214",
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
                    Toast.makeText(getApplicationContext(), message.toString(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toast.makeText(getApplicationContext(), "Delivery Complete",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void connect_and_subscribe_to_mqtt_broker() {
        try {
            this.client.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Connection Success!",
                            Toast.LENGTH_LONG).show();
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