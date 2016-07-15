/*
*                                Apache License
*                           Version 2.0, January 2004
*                        http://www.apache.org/licenses/
*
*      Copyright (c) 2016 Luca D'Amico, Andrea Faraone, Giovanni Merlino
*
*/

package it.unime.mobility4ckan;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

class MySensor {

    Context mContext;

    SensorManager sensorManager;
    LocationManager locationManager;
    Sensor tempSensor, pressureSensor, lightSensor;
    SensorEventListener tempSensorListener, pressureSensorListener, lightSensorListener;

    float currentTemp, currentPressure, currentLight, currentHumidity, currentProximity, currentSpeed;
    float[] currentAcceleration, currentGyroscope, currentMagnetic, currentRotation, currentGravity, currentLinearAcceleration;

    List<Sensor> sensorList = new ArrayList<>();

    public MySensor(Context context) {
        //super(context);
        mContext = context;

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        currentAcceleration = new float[3];
        currentGyroscope = new float[3];
        currentMagnetic = new float[3];
        currentRotation = new float[3];
        currentGravity = new float[3];
        currentLinearAcceleration = new float[3];

        /*tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        registerTempSensor();
        registerPressureSensor();
        registerLightSensor();

        */
    }

    public void registerSensor(Sensor mSensor) {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //latitude = location.getLatitude();
                //longitude = location.getLongitude();
                currentSpeed = location.getSpeed();
                //locationText.setText(String.valueOf(latitude)+ "  " +String.valueOf(longitude)+ "  " +String.valueOf(speed));
                //isGPSReady = true;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //
            }

            @Override
            public void onProviderEnabled(String provider) {
                //
            }

            @Override
            public void onProviderDisabled(String provider) {
                //
            }
        };

        SensorEventListener sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                switch (event.sensor.getType()) {
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
                        currentAcceleration[1] = event.values[1];
                        currentAcceleration[2] = event.values[2];
                        break;

                    case Sensor.TYPE_GYROSCOPE:     // rad/s
                        currentGyroscope[0] = event.values[0];
                        currentGyroscope[1] = event.values[1];
                        currentGyroscope[2] = event.values[2];
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                        currentMagnetic[0] = event.values[0];
                        currentMagnetic[1] = event.values[1];
                        currentMagnetic[2] = event.values[2];
                        break;

                    case Sensor.TYPE_PROXIMITY:     // cm
                        currentProximity = event.values[0];
                        break;

                    case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                        currentRotation[0] = event.values[0];
                        currentRotation[1] = event.values[1];
                        currentRotation[2] = event.values[2];
                        break;

                    case Sensor.TYPE_GRAVITY:      // m/s2
                        currentGravity[0] = event.values[0];
                        currentGravity[1] = event.values[1];
                        currentGravity[2] = event.values[2];
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                        currentLinearAcceleration[0] = event.values[0];
                        currentLinearAcceleration[1] = event.values[1];
                        currentLinearAcceleration[2] = event.values[2];
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

        //sensorManager.registerListener(locationListener, mSensor, LocationManager.GPS_PROVIDER);
/*        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public String getCurrentAcceleration() {
        return ""+currentAcceleration[0]+" "+currentAcceleration[1]+" "+currentAcceleration[2];
    }

    public String getCurrentGyroscope() {
        return ""+currentGyroscope[0]+" "+currentGyroscope[1]+" "+currentGyroscope[2];
    }

    public String getCurrentMagnetic() {
        return ""+currentMagnetic[0]+" "+currentMagnetic[1]+" "+currentMagnetic[2];
    }

    public String getCurrentRotation() {
        return ""+currentRotation[0]+" "+currentRotation[1]+" "+currentRotation[2];
    }

    public String getCurrentGravity() {
        return ""+currentGravity[0]+" "+currentGravity[1]+" "+currentGravity[2];
    }

    public String getCurrentLinearAcceleration() {
        return ""+currentLinearAcceleration[0]+" "+currentLinearAcceleration[1]+" "+currentLinearAcceleration[2];
    }

    //***********************************************//

    public void unregisterSensorListener(){
        sensorManager.unregisterListener(tempSensorListener);
        sensorManager.unregisterListener(pressureSensorListener);
        sensorManager.unregisterListener(lightSensorListener);
    }
}
