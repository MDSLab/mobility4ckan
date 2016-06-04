package it.unime.embeddedsystems;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

class MySensor{

    Context mContext;

    SensorManager sensorManager;
    Sensor tempSensor, pressureSensor, lightSensor;
    SensorEventListener tempSensorListener, pressureSensorListener, lightSensorListener;

    float currentTemp, currentPressure, currentLight;

    List<Sensor> sensorList = new ArrayList<>();

    public MySensor(Context context) {
        //super(context);
        mContext = context;

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        System.out.println("size: "+getSensorList().size()+"    "+sensorList.size());

        for (int k = 0; k < getSensorList().size(); k++) {
            System.out.println("TIKKITI: " + getSensorList().get(k));
        }

        registerTempSensor();
        registerPressureSensor();
        registerLightSensor();
    }

    void registerSensor(Sensor mSensor){
        SensorEventListener sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float currentValue;
                float currentValue1;
                float currentValue2;

                if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)// Gradi Celsius (°C)
                    currentValue = event.values[0];
                if(event.sensor.getType() == Sensor.TYPE_PRESSURE)// hPa o mbar
                    currentValue = event.values[0];
                if(event.sensor.getType() == Sensor.TYPE_LIGHT)// lx
                    currentValue = event.values[0];
                if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {// m/s2
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){// rad/s
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){// μT
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)// cm
                    currentValue = event.values[0];
                if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){// metrica sconosciuta
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_GRAVITY){// m/s2
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){// m/s2
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){//DEPRECATO
                    currentValue = event.values[0];
                    currentValue1 = event.values[0];
                    currentValue2 = event.values[0];
                }
                if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY)// %
                    currentValue = event.values[0];


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
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

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public List<Sensor> getSensorList() {
        return sensorList;
    }

    public void setSensorList(List<Sensor> sensorList) {
        this.sensorList = sensorList;
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
