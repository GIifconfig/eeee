package com.example.aabbg.handlers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class SensorHandler implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView txtSensorData;

    public SensorHandler(Context ctx, TextView txtSensorData) {
        this.txtSensorData = txtSensorData;
        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void startSensorMonitoring() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSensorMonitoring() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        txtSensorData.setText("Acc: " + x + ", " + y + ", " + z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
