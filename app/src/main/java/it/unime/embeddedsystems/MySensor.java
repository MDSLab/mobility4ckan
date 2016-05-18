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
    Sensor tempSensor, pressureSensor, crepuscolarSensor;
    SensorEventListener tempSensorListener, pressureSensorListener, crepuscolarSensorListener;

    float currentTemp;

    public MySensor(Context context) {
        mContext = context;

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        registerTempSensor(tempSensor);

        // Inserisci qui pressure e crepuscolar
    }

    void registerTempSensor(Sensor sensor){
        tempSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                currentTemp = event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(tempSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float getCurrentTemp(){
        return currentTemp;
    }

    public void unregisterSensorListener(){
        sensorManager.unregisterListener(tempSensorListener);
        // RIMUOVI QUI pressureSensorListener e crepuscolarSensorListener
    }
}
