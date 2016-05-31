package it.unime.embeddedsystems;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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

        sensorManager.registerListener(tempSensorListener, tempSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    void registerPressureSensor(){
        pressureSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_PRESSURE){
                    currentPressure = event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(pressureSensorListener, pressureSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    void registerLightSensor(){
        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    currentLight = event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
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
