package it.unime.embeddedsystems;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Created by andfa on 18/05/2016.
 */
class MySensor {

    Context mContext;

    SensorManager sensorManager;
    Sensor tempSensor, pressureSensor, lightSensor;
    SensorEventListener tempSensorListener, pressureSensorListener, lightSensorListener;

    float currentTemp, currentPressure, currentLight;

    public MySensor(Context context) {
        mContext = context;

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        registerTempSensor();
        registerPressureSensor();
        registerLightSensor();
    }

    void registerTempSensor(){
        tempSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)
                    currentTemp = event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(tempSensorListener, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void registerPressureSensor(){
        pressureSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                System.out.println("Type sensor pressure: "+event.sensor.getName());
                if(event.sensor.getType() == Sensor.TYPE_PRESSURE){
                    currentPressure = event.values[0];
                    System.out.println("Pressure: "+currentPressure);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(pressureSensorListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void registerLightSensor(){
        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                System.out.println("Type sensor light: "+event.sensor.getName());
                if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    currentLight = event.values[0];
                    System.out.println("LIGHT: "+currentLight);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float getCurrentTemp(){
        return currentTemp;
    }

    public float getCurrentPressure() {
        return currentPressure;
    }

    public float getCurrentLight() {
        return currentLight;
    }

    public void unregisterSensorListener(){
        sensorManager.unregisterListener(tempSensorListener);
        sensorManager.unregisterListener(pressureSensorListener);
        sensorManager.unregisterListener(lightSensorListener);
    }
}
