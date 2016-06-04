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

    float currentTemp, currentPressure, currentLight, currentHumidity, currentProximity;
    float[] currentAcceleration, currentGyroscope, currentMagnetic, currentRotation, currentGravity, currentLinearAcceleration;

    List<Sensor> sensorList = new ArrayList<>();

    public MySensor(Context context) {
        //super(context);
        mContext = context;

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        /*tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        registerTempSensor();
        registerPressureSensor();
        registerLightSensor();

        */
    }

    public void registerSensor(Sensor mSensor){
        SensorEventListener sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                switch (event.sensor.getType()){
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:   // Gradi Celsius (°C)
                        currentTemp = event.values[0];
                        break;

                    case Sensor.TYPE_PRESSURE:
                        currentPressure = event.values[0];  // hPa o mbar
                        break;

                    case Sensor.TYPE_LIGHT:    // lx
                        currentLight = event.values[0];
                        break;

                    case Sensor.TYPE_ACCELEROMETER:    // m/s2
                        currentAcceleration[0] = event.values[0];
                        currentAcceleration[1] = event.values[0];
                        currentAcceleration[2] = event.values[0];
                        break;

                    case Sensor.TYPE_GYROSCOPE:     // rad/s
                        currentGyroscope[0] = event.values[0];
                        currentGyroscope[1] = event.values[0];
                        currentGyroscope[2] = event.values[0];
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                        currentMagnetic[0] = event.values[0];
                        currentMagnetic[1] = event.values[0];
                        currentMagnetic[2] = event.values[0];
                        break;

                    case Sensor.TYPE_PROXIMITY:     // cm
                        currentProximity = event.values[0];
                        break;

                    case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                        currentRotation[0] = event.values[0];
                        currentRotation[1] = event.values[0];
                        currentRotation[2] = event.values[0];
                        break;

                    case Sensor.TYPE_GRAVITY:      // m/s2
                        currentGravity[0] = event.values[0];
                        currentGravity[1] = event.values[0];
                        currentGravity[2] = event.values[0];
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                        currentLinearAcceleration[0] = event.values[0];
                        currentLinearAcceleration[1] = event.values[0];
                        currentLinearAcceleration[2] = event.values[0];
                        break;

                    case Sensor.TYPE_PRESSURE:
                        currentPressure = event.values[0];  // hPa o mbar
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:     // %
                        currentHumidity = event.values[0];
                        break;

                    default:
                        break;
                }

                    case Sensor.TYPE_LIGHT:    // lx
                        currentLight = event.values[0];
                        break;

                    case Sensor.TYPE_ACCELEROMETER:    // m/s2
                        currentAcceleration[0] = event.values[0];
                        currentAcceleration[1] = event.values[0];
                        currentAcceleration[2] = event.values[0];
                        break;

                    case Sensor.TYPE_GYROSCOPE:     // rad/s
                        currentGyroscope[0] = event.values[0];
                        currentGyroscope[1] = event.values[0];
                        currentGyroscope[2] = event.values[0];
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                        currentMagnetic[0] = event.values[0];
                        currentMagnetic[1] = event.values[0];
                        currentMagnetic[2] = event.values[0];
                        break;

                    case Sensor.TYPE_PROXIMITY:     // cm
                        currentProximity = event.values[0];
                        break;

                    case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                        currentRotation[0] = event.values[0];
                        currentRotation[1] = event.values[0];
                        currentRotation[2] = event.values[0];
                        break;

                    case Sensor.TYPE_GRAVITY:      // m/s2
                        currentGravity[0] = event.values[0];
                        currentGravity[1] = event.values[0];
                        currentGravity[2] = event.values[0];
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                        currentLinearAcceleration[0] = event.values[0];
                        currentLinearAcceleration[1] = event.values[0];
                        currentLinearAcceleration[2] = event.values[0];
                        break;

                    case Sensor.TYPE_RELATIVE_HUMIDITY:     // %
                        currentHumidity = event.values[0];
                        break;

                    default:
                        break;
                }

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

    //***** METODI GET PER I VALORI DEI SENSORI *****//

    public float getCurrentTemp(){
        return currentTemp;
    }

    public float getCurrentPressure() {
        return currentPressure;
    }

    public float getCurrentLight() {
        return currentLight;
    }

    public float getCurrentHumidity() {
        return currentHumidity;
    }

    public float getCurrentProximity() {
        return currentProximity;
    }

    public float[] getCurrentAcceleration() {
        return currentAcceleration;
    }

    public float[] getCurrentGyroscope() {
        return currentGyroscope;
    }

    public float[] getCurrentMagnetic() {
        return currentMagnetic;
    }

    public float[] getCurrentRotation() {
        return currentRotation;
    }

    public float[] getCurrentGravity() {
        return currentGravity;
    }

    public float[] getCurrentLinearAcceleration() {
        return currentLinearAcceleration;
    }

    //***********************************************//

    public void unregisterSensorListener(){
        sensorManager.unregisterListener(tempSensorListener);
        sensorManager.unregisterListener(pressureSensorListener);
        sensorManager.unregisterListener(lightSensorListener);
    }
}
