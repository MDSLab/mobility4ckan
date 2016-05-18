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
class MySensor implements SensorEventListener {

    Context mContext;

    SensorManager sensorManager;
    Sensor sensor;

    List<Sensor> mSensorList;

    public MySensor(Context context, List<Sensor> sensorList) {
        mContext = context;
        mSensorList = sensorList;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        for(int k=0; k<mSensorList.size(); k++){

            switch (mSensorList.get(k).getType()){

                case Sensor.TYPE_PRESSURE:
                    break;

                default:
                    break;

            }
        }

    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

    }

    public Sensor initializeSensor(int sensorType){
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_NORMAL);

        return sensor;

    }

    public void registerSensorListener(){
        sensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensorListener(){
        sensorManager.unregisterListener(this);
    }
}
