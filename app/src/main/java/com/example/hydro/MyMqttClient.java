package com.example.hydro;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.io.Serializable;

public class MyMqttClient extends MqttAndroidClient implements Serializable {

    public MyMqttClient(Context context, String serverURI, String clientId) {
        super(context, serverURI, clientId);
    }


}
