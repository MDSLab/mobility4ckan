/* TODO:
*  1) Sensori selezionabili  --->>  OK
*  2) Cambiare layout
*  3) Scegliere nome dataset e fare controlli su CKAN
*  4) Controlli su WIFI e GPS --->> OK
*  5) Invio per spostamento (ogni tot metri)
*  6) Vedere se siamo a piedi o in macchina
*/


package it.unime.embeddedsystems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView locationText, currentTempText, currentPressureText, currentLightText, currentTimeText, countdownText, datasetNameText;
    private Button sendNowBtn;
    private SharedPreferences sharedPref;
    private int REQUEST_PERMISSION_LOCATION = 1;
    private int countdown = 60*1000;
    private String datasetName = "android_test_27"; // cambiare
    private boolean isRegistering = false;
    private boolean isGPSReady = false;
    private double latitude;
    private double longitude;
    private MySensor mySensor;

    EditText nameText;

    private List<Sensor> sensorList = new ArrayList<>();
    boolean isDeviceCurrentSensorsRegistered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        locationText = (TextView) findViewById(R.id.txv_gps);
        currentTempText = (TextView) findViewById(R.id.txv_temp);
        currentPressureText = (TextView) findViewById(R.id.txv_pressure);
        currentLightText = (TextView) findViewById(R.id.txv_light);
        currentTimeText = (TextView) findViewById(R.id.txv_data);
        countdownText = (TextView) findViewById(R.id.txv_countdown);
        datasetNameText = (TextView) findViewById(R.id.txv_dataset) ;
        datasetNameText.setText(datasetName);
        sendNowBtn = (Button) findViewById(R.id.btn_invia);
        sendNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendTask(false);
            }
        });
        checkPermissionControl();

        sensorList = SensorConfig.sensorList;
        mySensor = new MySensor(this);
        for (int k=0; k<sensorList.size(); k++){
            mySensor.registerSensor(sensorList.get(k));
        }
        System.out.println("Sensor List: "+sensorList);

        if(isDeviceOnline()){
            Toast.makeText(this, "ONLINE", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "OFFLINE", Toast.LENGTH_LONG).show();
        }

        if(isGPSEnable()){
            Toast.makeText(this, "GPS SPENTO", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "GPS ACCESO", Toast.LENGTH_SHORT).show();
        }

        setDatasetName();

    }

    private void setDatasetName(){

        nameText = new EditText(getApplicationContext());
        nameText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nameText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        nameText.setTextColor(Color.BLACK);
        nameText.setGravity(Gravity.CENTER);

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.inserisci_nome_dataset))
                .setView(nameText)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("lenght: "+nameText.getText().length());

                        if(nameText.getText().length()>1 && !nameText.getText().equals("") /*&& !nameText.getText().equals("nomeDelDatasetEsistente")*/) {
                            System.out.println("entro");
                            dialog.dismiss();
                            Timer sendTimer = new Timer();
                            sendTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    sendTask(true);
                                }
                            }, 0, countdown);
                        }else{
                            System.out.println("non entro");
                            nameText.getText().clear();
                        }
                    }
                })
                .create()
                .show();

    }

    @SuppressLint("NewApi")
    private void checkPermissionControl() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.location_permission_request_text))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            }
        } else {
            enableGPS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionControl();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.location_permission_denied_text))
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mySensor!=null)
            mySensor.unregisterSensorListener();
    }

    private JSONObject POST(String serverUrl, String authorization, String command) {
        String responseReader="";
        String response = "";
        try{
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", authorization);

            OutputStream os = conn.getOutputStream();
            os.write(command.getBytes());
            os.flush();

            int httpResult = conn.getResponseCode();

            if (httpResult < 200 && httpResult > 300) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            while ((responseReader = br.readLine()) != null) {
                response = response + "" + responseReader;
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject result = null;
        try {
            System.out.println(serverUrl+"pre-catch result: "+response);
            result = new JSONObject(response);
        } catch (JSONException e) {
            System.out.println("catch result: "+response);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationText.setText(String.valueOf(latitude)+ "  " +String.valueOf(longitude));
        isGPSReady = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void enableGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Avvio GPS Fallito", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(getApplicationContext(), "GPS Avviato", Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private boolean isGPSEnable() {
        LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private String getCurrentDate(){
        Date date = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm:ss");
        String currDate = formatDate.format(date);
        String currTime = formatTime.format(date);

        return currDate+"T"+currTime;
    }


    private void getSensorDataToSend(String sensorUUID, String type, String currentValue){
        String[] sensor = {
                sharedPref.getString(sensorUUID,""),
                type,
                currentValue,
                ""+latitude,
                ""+longitude,
                getCurrentDate()
        };
        new SendData().execute(sensor);
    }


    void sendTask(final boolean shouldUpdateCountdown){
       // boolean isDeviceRegistered =//
        if(!isDeviceCurrentSensorsRegistered && !isRegistering){
            new RegisterDevice().execute(datasetName);
            isRegistering=true;
            return;
        }

        if(!isDeviceCurrentSensorsRegistered || !isGPSReady) {
            return;
        }

      /*  final float currentTemp = mySensor.getCurrentTemp();
        final float currentPressure = mySensor.getCurrentPressure();
        final float currentLight = mySensor.getCurrentLight();
        final String currentDate = getCurrentDate(); */


        for(int k=0; k<sensorList.size();k++){
            switch (sensorList.get(k).getType()){
                case Sensor.TYPE_AMBIENT_TEMPERATURE:   // Gradi Celsius (°C)
                    getSensorDataToSend("temperatureDatastoreUUID", "Temperature", ""+mySensor.getCurrentTemp());
                    break;

                case Sensor.TYPE_PRESSURE:
                    getSensorDataToSend("pressureDatastoreUUID", "Pressure", ""+mySensor.getCurrentPressure());
                    break;

                case Sensor.TYPE_LIGHT:    // lx
                    getSensorDataToSend("lightDatastoreUUID", "Light", ""+mySensor.getCurrentLight());
                    break;

                case Sensor.TYPE_ACCELEROMETER:    // m/s2
                    getSensorDataToSend("accelerometerDatastoreUUID", "Accelerometer", mySensor.getCurrentAcceleration());
                    break;

                case Sensor.TYPE_GYROSCOPE:     // rad/s
                    getSensorDataToSend("gyroscopeDatastoreUUID", "Gyroscope", mySensor.getCurrentGyroscope());
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                    getSensorDataToSend("magneticFieldDatastoreUUID", "MagneticField", mySensor.getCurrentMagnetic());

                    break;

                case Sensor.TYPE_PROXIMITY:     // cm
                    getSensorDataToSend("proximityDatastoreUUID", "Proximity", ""+mySensor.getCurrentProximity());
                    break;

                case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                    getSensorDataToSend("rotationVector", "RotationVector", mySensor.getCurrentRotation());
                    break;

                case Sensor.TYPE_GRAVITY:      // m/s2
                    getSensorDataToSend("gravity", "Gravity", mySensor.getCurrentGravity());
                    break;

                case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                    getSensorDataToSend("linearAcceleration", "LinearAcceleration", mySensor.getCurrentLinearAcceleration());
                    break;

                case Sensor.TYPE_RELATIVE_HUMIDITY:     // %
                    getSensorDataToSend("relativeHumidity", "RelativeHumidity", ""+mySensor.getCurrentHumidity());
                    break;

                default:
                    break;
            }
        }










        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              /*  currentTempText.setText(""+currentTemp);
                currentPressureText.setText(""+currentPressure);
                currentLightText.setText(""+currentLight);
                currentTimeText.setText(currentDate);*/
                if(shouldUpdateCountdown) {
                    new CountDownTimer(countdown, 1000) {
                        public void onTick(long millisUntilFinished) {
                            countdownText.setText("" + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                        }
                    }.start();
                }
            }
        });


    }

    private class RegisterDevice extends AsyncTask<String, Void, JSONObject>{

        String createDataset(String datasetName){
            String datasetUUID = "";
            try {
                String command = "{\"name\":\""+datasetName+"\", \"title\":\""+datasetName+"\", \"owner_org\":\"test\", \"extras\":{\"Label\":\"android-phone\",\"Manufacturer\":\"Android\", \"Model\":\"smartphone\",\"Altitude\":0,\"Latitude\":0,\"Longitude\":0}}";
                JSONObject response = POST("http://smartme-data.unime.it/api/rest/dataset", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Create Dataset
                datasetUUID = response.getString("id");
            }catch (JSONException e){
                e.printStackTrace();
            }
            return datasetUUID;
        }

        String createSensorDatastore(String datasetName, String datastoreName){
            String datastoreUUID = "";
            try {
                String command = "{\"resource\": {\"package_id\":\""+datasetName+"\", \"name\":\""+datastoreName+"\"}, \"fields\": [ {\"id\": \"Type\", \"type\":\"text\"}, {\"id\": \"Model\", \"type\":\"text\"}, {\"id\": \"Unit\", \"type\":\"text\"}, {\"id\": \"FabricName\", \"type\":\"text\"}, {\"id\": \"ResourceID\", \"type\":\"text\"}, {\"id\": \"Date\", \"type\":\"timestamp\"}] }";

                JSONObject response = POST("http://smartme-data.unime.it/api/3/action/datastore_create", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Create Datastore
                JSONObject result = new JSONObject(response.getString("result"));
                datastoreUUID = result.getString("resource_id");
            }catch (JSONException e){
                e.printStackTrace();
            }
            return datastoreUUID;
        }

        String createDatastore(String datasetName, String datastoreName, String modelName){
            String datastoreUUID = "";
            try {
                String command = "{\"resource\": {\"package_id\":\""+datasetName+"\", \"name\":\""+datastoreName+"\"}, \"fields\": [ {\"id\": \"Date\", \"type\":\"timestamp\"}, {\"id\": \""+modelName+"\", \"type\":\"text\"}, {\"id\": \"Altitude\", \"type\":\"numeric\"}, {\"id\": \"Latitude\", \"type\":\"numeric\"}, {\"id\": \"Longitude\", \"type\":\"numeric\"}] }";

                JSONObject response = POST("http://smartme-data.unime.it/api/3/action/datastore_create", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Create Datastore
                JSONObject result = new JSONObject(response.getString("result"));
                datastoreUUID = result.getString("resource_id");
            }catch (JSONException e){
                e.printStackTrace();
            }
            return datastoreUUID;
        }

        void insertInSensorDatastore(String sensorUUID, String measureUUID, String type, String name, String unit){
            String command = "{\"resource_id\":\""+sensorUUID+"\", \"method\":\"insert\", \"records\":[{\"Type\":\""+type+"\",\"Model\":\""+name+"\",\"Unit\":\""+unit+"\",\"FabricName\":\"-\",\"ResourceID\":\""+measureUUID+"\",\"Date\":\"2016-05-13T12:00:00\"}]}";

            POST("http://smartme-data.unime.it/api/3/action/datastore_upsert", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Insert in Datastore
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            SharedPreferences.Editor editor = sharedPref.edit();
            boolean isRegistered = sharedPref.getBoolean("isDeviceRegistered", false);

            if(!isRegistered) {
                String datasetUUID = createDataset(params[0]);
                String sensorsDatastoreUUID = createSensorDatastore(params[0], "sensors");

                editor.putBoolean("isDeviceRegistered", true);
                editor.putString("datasetName", params[0]);
                editor.putString("datasetUUID", datasetUUID);
                editor.putString("sensorsDatastoreUUID", sensorsDatastoreUUID);
                editor.apply();
            }

            for (int k=0;k<sensorList.size();k++){
                switch (sensorList.get(k).getType()){
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:   // Gradi Celsius (°C)
                        if(sharedPref.getString("temperatureDatastoreUUID","").isEmpty()) {
                            String temperatureDatastoreUUID = createDatastore(params[0], "temperature", "Temperature");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""), temperatureDatastoreUUID, "TYPE_TEMPERATURE", "TEMPERATURE", "celsius");
                            editor.putString("temperatureDatastoreUUID", temperatureDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_PRESSURE:
                        if(sharedPref.getString("pressureDatastoreUUID","").isEmpty()) {
                            String pressureDatastoreUUID = createDatastore(params[0],"pressure", "Pressure");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),pressureDatastoreUUID, "TYPE_PRESSURE", "PRESSURE", "mbar");
                            editor.putString("pressureDatastoreUUID", pressureDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_LIGHT:    // lx
                        if(sharedPref.getString("lightDatastoreUUID","").isEmpty()) {
                            String lightDatastoreUUID = createDatastore(params[0],"light", "Light");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),lightDatastoreUUID, "TYPE_LIGHT", "LIGHT", "lx");
                            editor.putString("lightDatastoreUUID", lightDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_ACCELEROMETER:    // m/s2
                        if(sharedPref.getString("accelerometerDatastoreUUID","").isEmpty()) {
                            String accelerometerDatastoreUUID = createDatastore(params[0],"accelerometer", "Accelerometer");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),accelerometerDatastoreUUID, "TYPE_ACCELEROMETER", "ACCELEROMETER", "m/s2");
                            editor.putString("accelerometerDatastoreUUID", accelerometerDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_GYROSCOPE:     // rad/s
                        if(sharedPref.getString("gyroscopeDatastoreUUID","").isEmpty()) {
                            String gyroscopeDatastoreUUID = createDatastore(params[0],"gyroscope", "Gyroscope");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),gyroscopeDatastoreUUID, "TYPE_GYROSCOPE", "GYROSCOPE", "rad/s");
                            editor.putString("gyroscopeDatastoreUUID", gyroscopeDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                        if(sharedPref.getString("magneticFieldDatastoreUUID","").isEmpty()) {
                            String magneticFieldDatastoreUUID = createDatastore(params[0],"magneticfield", "MagneticField");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),magneticFieldDatastoreUUID, "TYPE_MAGNETIC_FIELD", "MAGNETIC_FIELD", "uT");
                            editor.putString("magneticFieldDatastoreUUID", magneticFieldDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_PROXIMITY:     // cm
                        if(sharedPref.getString("proximityDatastoreUUID","").isEmpty()) {
                            String proximityDatastoreUUID = createDatastore(params[0],"proximity", "Proximity");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),proximityDatastoreUUID, "TYPE_PROXIMITY", "PROXIMITY", "cm");
                            editor.putString("proximityDatastoreUUID", proximityDatastoreUUID);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                        if(sharedPref.getString("rotationVector","").isEmpty()) {
                            String rotationVector = createDatastore(params[0],"rotationvector", "RotationVector");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),rotationVector, "TYPE_ROTATION_VECTOR", "ROTATION_VECTOR", "UNKNOWN");
                            editor.putString("rotationVector", rotationVector);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_GRAVITY:      // m/s2
                        if(sharedPref.getString("gravity","").isEmpty()) {
                            String gravity = createDatastore(params[0],"gravity", "Gravity");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),gravity, "TYPE_GRAVITY", "GRAVITY", "m/s2");
                            editor.putString("gravity", gravity);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                        if(sharedPref.getString("linearAcceleration","").isEmpty()) {
                            String linearAcceleration = createDatastore(params[0],"linearacceleration", "LinearAcceleration");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),linearAcceleration, "TYPE_LINEAR_ACCELERATION", "LINEAR_ACCELERATION", "m/s2");
                            editor.putString("linearAcceleration", linearAcceleration);
                            editor.apply();
                        }
                        break;

                    case Sensor.TYPE_RELATIVE_HUMIDITY:     // %
                        if(sharedPref.getString("relativeHumidity","").isEmpty()) {
                            String relativeHumidity = createDatastore(params[0],"relativehumidity", "RelativeHumidity");
                            insertInSensorDatastore(sharedPref.getString("sensorsDatastoreUUID",""),relativeHumidity, "TYPE_RELATIVE_HUMIDITY", "RELATIVE_HUMIDITY", "%");
                            editor.putString("relativeHumidity", relativeHumidity);
                            editor.apply();
                        }
                        break;

                    default:
                        break;
                }
            }


            return null;
       /*     String temperatureDatastoreUUID = createDatastore(params[0],"temperature","Temperature");
            insertInSensorDatastore(sensorsDatastoreUUID,temperatureDatastoreUUID, "TYPE_TEMPERATURE", "TEMPERATURE", "celsius");

            String pressureDatastoreUUID = createDatastore(params[0],"pressure", "Pressure");
            insertInSensorDatastore(sensorsDatastoreUUID,pressureDatastoreUUID, "TYPE_PRESSURE", "PRESSURE", "mbar");

            String lightDatastoreUUID = createDatastore(params[0],"light", "Light");
            insertInSensorDatastore(sensorsDatastoreUUID,lightDatastoreUUID, "TYPE_LIGHT", "LIGHT", "lx");

            JSONObject data = null;
            try {
                data = new JSONObject();
                data.put("datasetName",params[0]);
                data.put("datasetUUID", datasetUUID);
                data.put("sensorsDatastoreUUID", sensorsDatastoreUUID);
                data.put("temperatureDatastoreUUID", temperatureDatastoreUUID);
                data.put("pressureDatastoreUUID", pressureDatastoreUUID);
                data.put("lightDatastoreUUID", lightDatastoreUUID);
            }catch (JSONException e){
                e.printStackTrace();
            }*
            return data;*/
        }

        @Override
        protected void onPostExecute(JSONObject data) {
         /*   try {
               SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isDeviceRegistered", true);
                editor.putString("datasetName", data.getString("datasetName"));
                editor.putString("datasetUUID", data.getString("datasetUUID"));
                editor.putString("sensorsDatastoreUUID", data.getString("sensorsDatastoreUUID"));
                editor.putString("temperatureDatastoreUUID", data.getString("temperatureDatastoreUUID"));
                editor.putString("pressureDatastoreUUID", data.getString("pressureDatastoreUUID"));
                editor.putString("lightDatastoreUUID", data.getString("lightDatastoreUUID"));
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }

    }

    private class SendData extends AsyncTask<String, Void, Void> {

        void insertInDatastore(String measureUUID, String sensorName, String sensorValue, String lat, String lon, String date){
            String command = "{\"resource_id\":\""+measureUUID+"\", \"method\":\"insert\", \"records\":[{\"Latitude\":\""+lat+"\",\"Altitude\":\"0\",\"Date\":\""+date+"\",\""+sensorName+"\":\""+sensorValue+"\",\"Longitude\":\""+lon+"\"}]}";
            POST("http://smartme-data.unime.it/api/3/action/datastore_upsert", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Insert in Datastore
        }

        @Override
        protected Void doInBackground(String... params) {
            String measureUUID = params[0];
            String sensorName = params[1];
            String sensorValue = params[2];
            String lat = params[3];
            String lon = params[4];
            String date = params[5];
            insertInDatastore(measureUUID, sensorName, sensorValue, lat, lon, date);
            return null;
        }
    }

}
