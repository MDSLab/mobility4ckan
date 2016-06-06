package it.unime.embeddedsystems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView locationText, countdownText, datasetNameText;
    private Button sendNowBtn;
    private SharedPreferences sharedPref;
    private int REQUEST_PERMISSION_LOCATION = 1;
    private int countdown = 60*1000;
    private String datasetName = "";
    private boolean isRegistering = false;
    private boolean isGPSReady = false;
    private double latitude;
    private double longitude;
    private MySensor mySensor;
    private EditText nameText;
    private ListView listView;
    private List<Overview> overviewList = new ArrayList<>();
    private List<Sensor> sensorList = new ArrayList<>();
    private boolean isDeviceCurrentSensorsRegistered = false;
    private String lastTempValue = "";
    private String lastPressureValue = "";
    private String lastLightValue = "";
    private String lastAccelerationValue = "";
    private String lastGyroscopeValue = "";
    private String lastMagneticValue = "";
    private String lastProximityValue = "";
    private String lastRotationValue = "";
    private String lastGravityValue = "";
    private String lastLinearAccelerationValue = "";
    private String lastRelativeHumidity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        locationText = (TextView) findViewById(R.id.txv_gps);
        countdownText = (TextView) findViewById(R.id.txv_countdown);
        datasetNameText = (TextView) findViewById(R.id.txv_dataset) ;
        listView = (ListView)findViewById(R.id.sensor_listview);

        sendNowBtn = (Button) findViewById(R.id.btn_invia);
        sendNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTask(false);
            }
        });
        checkPermissionControl();

        sensorList = SensorConfig.sensorList;
        mySensor = new MySensor(this);
        for (int k=0; k<sensorList.size(); k++){
            mySensor.registerSensor(sensorList.get(k));
        }

        if(!isDeviceOnline()){
            final AlertDialog mDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.sei_offline))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();
            mDialog.show();
            return;
        }

        if(!isGPSEnable()){
            final AlertDialog mDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.gps_disattivato))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();
            mDialog.show();
            return;
        }

        if(sharedPref.getString("datasetName","").isEmpty()) {
            setDatasetName();
        }
        else{
            datasetNameText.setText(sharedPref.getString("datasetName",""));
            datasetName = sharedPref.getString("datasetName","");
            startTimer();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createListView();
                    }
                });
            }
        }, 0, 3000);

    }

    private void createListView(){

        overviewList.clear();

        Overview overviewTitle = new Overview(getApplicationContext());
        overviewTitle.setSensorName("Nome Sensore");
        overviewTitle.setCurrentValue("Valore Corrente");
        overviewTitle.setValueSend("Valore Inviato");
        overviewList.add(overviewTitle);

        for (int k=0; k<sensorList.size(); k++){
            Overview overview = new Overview(getApplicationContext());
            switch (sensorList.get(k).getType()){
                case Sensor.TYPE_AMBIENT_TEMPERATURE:   // Gradi Celsius (°C)
                    overview.setSensorName("Temperature");
                    overview.setCurrentValue(""+mySensor.getCurrentTemp());
                    overview.setValueSend(lastTempValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_PRESSURE:
                    overview.setSensorName("Pressure");
                    overview.setCurrentValue(""+mySensor.getCurrentPressure());
                    overview.setValueSend(lastPressureValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_LIGHT:    // lx
                    overview.setSensorName("Light");
                    overview.setCurrentValue(""+mySensor.getCurrentLight());
                    overview.setValueSend(lastLightValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_ACCELEROMETER:    // m/s2
                    overview.setSensorName("Accelerometer");
                    overview.setCurrentValue(""+mySensor.getCurrentAcceleration());
                    overview.setValueSend(lastAccelerationValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_GYROSCOPE:     // rad/s
                    overview.setSensorName("Gyroscope");
                    overview.setCurrentValue(""+mySensor.getCurrentGyroscope());
                    overview.setValueSend(lastGyroscopeValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                    overview.setSensorName("Magnetic Field");
                    overview.setCurrentValue(""+mySensor.getCurrentMagnetic());
                    overview.setValueSend(lastMagneticValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_PROXIMITY:     // cm
                    overview.setSensorName("Proximity");
                    overview.setCurrentValue(""+mySensor.getCurrentProximity());
                    overview.setValueSend(lastProximityValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                    overview.setSensorName("Rotation Vector");
                    overview.setCurrentValue(""+mySensor.getCurrentRotation());
                    overview.setValueSend(lastRotationValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_GRAVITY:      // m/s2
                    overview.setSensorName("Gravity");
                    overview.setCurrentValue(""+mySensor.getCurrentGravity());
                    overview.setValueSend(lastGravityValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                    overview.setSensorName("Linear Acceleration");
                    overview.setCurrentValue(""+mySensor.getCurrentLinearAcceleration());
                    overview.setValueSend(lastLinearAccelerationValue);
                    overviewList.add(overview);
                    break;

                case Sensor.TYPE_RELATIVE_HUMIDITY:     // %
                    overview.setSensorName("Relative Humidity");
                    overview.setCurrentValue(""+mySensor.getCurrentHumidity());
                    overview.setValueSend(lastRelativeHumidity);
                    overviewList.add(overview);
                    break;

                default:
                    break;
            }
        }

        listView.setAdapter(new OverviewAdapter(getApplicationContext(), overviewList));

    }

    private void startTimer(){
        Timer sendTimer = new Timer();
        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendTask(true);
            }
        }, 0, countdown);
    }

    private void setDatasetName(){

        DinamicView dinamicView = new DinamicView(getApplicationContext());
        dinamicView.getNoteLabel().setText(getString(R.string.note_dialog));
        dinamicView.getNoteLabel().setTextSize(12);

        nameText = new EditText(getApplicationContext());
        nameText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nameText.setGravity(Gravity.CENTER);
        nameText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        nameText.setTextColor(Color.BLACK);
        nameText.setSingleLine(true);
        nameText.setTextSize(18);

        dinamicView.getBodyLayout().addView(nameText);

        final AlertDialog mDialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.inserisci_nome_dataset))
                .setView(dinamicView)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        datasetName = nameText.getText().toString().trim().toLowerCase();
                        String regexp = "([a-z-_]|[0-9-_])";
                        Matcher matcher = Pattern.compile(regexp).matcher(datasetName);

                        if(matcher.find()) {
                            mDialog.dismiss();
                            datasetNameText.setText(datasetName);
                            startTimer();
                        }else{
                            System.out.println("non entro");
                            nameText.getText().clear();
                            nameText.setError(getString(R.string.note_dialog));
                        }
                    }
                });
            }
        });
        mDialog.show();

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
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
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
        if(!isDeviceCurrentSensorsRegistered && !isRegistering){
            new RegisterDevice(this).execute(datasetName);
            isRegistering=true;
            return;
        }

        if(!isDeviceCurrentSensorsRegistered || !isGPSReady) {
            return;
        }

        for(int k=0; k<sensorList.size();k++){
            switch (sensorList.get(k).getType()){
                case Sensor.TYPE_AMBIENT_TEMPERATURE:   // Gradi Celsius (°C)
                    String currentTempValue = ""+mySensor.getCurrentTemp();
                    getSensorDataToSend("temperatureDatastoreUUID", "Temperature", currentTempValue);
                    lastTempValue = currentTempValue;
                    break;

                case Sensor.TYPE_PRESSURE:
                    String currentPressureValue = ""+mySensor.getCurrentPressure();
                    getSensorDataToSend("pressureDatastoreUUID", "Pressure",currentPressureValue);
                    lastPressureValue = currentPressureValue;
                    break;

                case Sensor.TYPE_LIGHT:    // lx
                    String currentLightValue = ""+mySensor.getCurrentLight();
                    getSensorDataToSend("lightDatastoreUUID", "Light", currentLightValue);
                    lastLightValue = currentLightValue;
                    break;

                case Sensor.TYPE_ACCELEROMETER:    // m/s2
                    String currentAccelerationValue = mySensor.getCurrentAcceleration();
                    getSensorDataToSend("accelerometerDatastoreUUID", "Accelerometer", currentAccelerationValue);
                    lastAccelerationValue = currentAccelerationValue;
                    break;

                case Sensor.TYPE_GYROSCOPE:     // rad/s
                    String currentGyroscopeValue = mySensor.getCurrentGyroscope();
                    getSensorDataToSend("gyroscopeDatastoreUUID", "Gyroscope", currentGyroscopeValue);
                    lastGyroscopeValue = currentGyroscopeValue;
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:    // μT
                    String currentMagneticValue = mySensor.getCurrentMagnetic();
                    getSensorDataToSend("magneticFieldDatastoreUUID", "MagneticField", currentMagneticValue);
                    lastMagneticValue = currentMagneticValue;
                    break;

                case Sensor.TYPE_PROXIMITY:     // cm
                    String currentProximityValue = ""+mySensor.getCurrentProximity();
                    getSensorDataToSend("proximityDatastoreUUID", "Proximity", currentProximityValue);
                    lastProximityValue = currentProximityValue;
                    break;

                case Sensor.TYPE_ROTATION_VECTOR:   // unita di misura sconosciuta
                    String currentRotationValue = mySensor.getCurrentRotation();
                    getSensorDataToSend("rotationVector", "RotationVector", currentRotationValue);
                    lastRotationValue = currentRotationValue;
                    break;

                case Sensor.TYPE_GRAVITY:      // m/s2
                    String currentGravityValue = mySensor.getCurrentGravity();
                    getSensorDataToSend("gravity", "Gravity", currentGravityValue);
                    lastGravityValue = currentGravityValue;
                    break;

                case Sensor.TYPE_LINEAR_ACCELERATION:   // m/s2
                    String currentLinearAccelerationValue = mySensor.getCurrentLinearAcceleration();
                    getSensorDataToSend("linearAcceleration", "LinearAcceleration", currentLinearAccelerationValue);
                    lastLinearAccelerationValue = currentLinearAccelerationValue;
                    break;

                case Sensor.TYPE_RELATIVE_HUMIDITY:     // %
                    String currentRelativeHumidity = ""+mySensor.getCurrentHumidity();
                    getSensorDataToSend("relativeHumidity", "RelativeHumidity", currentRelativeHumidity);
                    lastRelativeHumidity = currentRelativeHumidity;
                    break;

                default:
                    break;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

    private class RegisterDevice extends AsyncTask<String, Void, Void>{
        Activity mActivity;
        public RegisterDevice(Activity activity)
        {
            super();
            this.mActivity=activity;
        }

        String createDataset(String datasetName){
            String datasetUUID = "";
            try {
                String command = "{\"name\":\""+datasetName+"\", \"title\":\""+datasetName+"\", \"owner_org\":\"test\", \"extras\":{\"Label\":\"android-phone\",\"Manufacturer\":\"Android\", \"Model\":\"smartphone\",\"Altitude\":0,\"Latitude\":0,\"Longitude\":0}}";
                JSONObject response = POST("http://smartme-data.unime.it/api/rest/dataset", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Create Dataset

                if(response==null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Il nome del database è gia in uso. Selezionarne uno diverso.", Toast.LENGTH_LONG).show();

                        }
                    });
                    this.mActivity.finish();
                    return null;
                }

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
        protected Void doInBackground(String... params) {
            SharedPreferences.Editor editor = sharedPref.edit();
            boolean isRegistered = sharedPref.getBoolean("isDeviceRegistered", false);

            if(!isRegistered) {
                String datasetUUID = createDataset(params[0]);
                if(datasetUUID==null)
                    return null;
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
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isDeviceCurrentSensorsRegistered = true;
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
