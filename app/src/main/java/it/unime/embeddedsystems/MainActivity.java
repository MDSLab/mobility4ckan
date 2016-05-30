package it.unime.embeddedsystems;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor tempSensor;
    private TextView locationText;
    private String serverUrl = "http://smartme-data.unime.it/api/3/action/datastore_upsert";
    private SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

    LocationManager locationManager;
    Location mLocation;
    LocationListener locationListener;

    double latitude;
    double longitude;

    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    boolean isGPSEnabled = false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private int REQUEST_PERMISSION_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = (TextView) findViewById(R.id.txv_temperature);
        //String command = "{\"resource_id\":\"c35b761d-8f4a-4b89-a68e-fcdb8063b636\", \"method\":\"insert\", \"records\":[{\"Type\":\"TYPE_ACCELEROMETER\",\"Model\":\"Accelerometer\",\"Unit\":\"m/s^2\",\"FabricName\":\"-\",\"ResourceID\":\"71ae4c3c-3f2b-4c31-ba09-1d83444327d2\",\"Date\":\"2016-05-13T12:00:00\"}]}";
        //new Task().execute(serverUrl, authorization, command);

        checkPermissionControl();
    }

        temperatureTXV.setText("Latitudine: "+gps.getLatitude()+ "  Logitudine: "+gps.getLongitude());


        boolean isDeviceRegistered = sharedPref.getBoolean("isDeviceRegistered",false);

        if(!isDeviceRegistered){
            new RegisterDevice().execute();
        }
    }
    private void checkPermissionControl(){
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.location_permission_request_text))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                }
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

    private void enableGPS() {
        System.out.println("ENABLE GPS!!!");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Errore! GPS disabilitato o assenza di rete.", Toast.LENGTH_SHORT).show();
        } else {
            this.canGetLocation = true;
            // First get location from Network Provider
            if (isNetworkEnabled) {
                System.out.println("ENTRO QUI");
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, mContext);
                if (locationManager != null) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (mLocation != null) {
                        latitude = mLocation.getLatitude();
                        longitude = mLocation.getLongitude();

                        System.out.println("Lat: "+latitude+"   long: "+longitude);
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                locationText.setText("GPS ATTIVO");
                if (mLocation == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    Log.d("GPS Enabled", "GPS Enabled");
                  //  if (locationManager != null) {
                        System.out.println("Entro in manager != null");
                        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (mLocation != null) {
                            latitude = mLocation.getLatitude();
                            longitude = mLocation.getLongitude();

                            System.out.println("Lat: "+latitude+"   long: "+longitude);
                        }
                   // }
                }
            }
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("Latitude: "+mLocation.getLatitude()+"   Longitude: "+mLocation.getLongitude());
                locationText.setText("Latitude: "+mLocation.getLatitude()+"   Longitude: "+mLocation.getLongitude());
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
        };

    }



    @Override
    protected void onResume() {
        super.onResume();
        //sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  sensorManager.unregisterListener(this);
    }

    private JSONObject POST(String serverUrl, String authorization, String command) {
        String response="";
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

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            while ((response = br.readLine()) != null) {
                System.out.println(response);
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }


        JSONObject result = null;
        try {
            result = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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

        String createSensorDatastore(String datasetName, String datastoreName, String modelName){
            String datastoreUUID = "";
            try {
                String command = "{\"resource\": {\"package_id\":\""+datasetName+"\", \"name\":\""+datastoreName+"\"}, \"fields\": [ {\"id\": \"Type\", \"type\":\"text\"}, {\"id\": "+modelName+", \"type\":\"text\"}, {\"id\": \"Unit\", \"type\":\"text\"}, {\"id\": \"FabricName\", \"type\":\"text\"}, {\"id\": \"ResourceID\", \"type\":\"text\"}, {\"id\": \"Date\", \"type\":\"timestamp\"}] }";
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
            String datasetUUID = createDataset(params[0]);
            String sensorsDatastoreUUID = createSensorDatastore("","sensors","Model");

            String temperatureDatastoreUUID = createDatastore("","temperature","Temperature");
            insertInSensorDatastore(sensorsDatastoreUUID,temperatureDatastoreUUID, "TYPE_TEMPERATURE", "TEMPERATURE", "celsius");

            String pressureDatastoreUUID = createDatastore("","pressure", "Pressure");
            insertInSensorDatastore(sensorsDatastoreUUID,pressureDatastoreUUID, "TYPE_PRESSURE", "PRESSURE", "mbar");

            String lightDatastoreUUID = createDatastore("","light", "Light");
            insertInSensorDatastore(sensorsDatastoreUUID,lightDatastoreUUID, "TYPE_LIGHT", "LIGHT", "lx");

            JSONObject data = null;
            try {
                data = new JSONObject();
                data.put("datasetUUID", datasetUUID);
                data.put("sensorsDatastoreUUID", sensorsDatastoreUUID);
                data.put("temperatureDatastoreUUID", temperatureDatastoreUUID);
                data.put("pressureDatastoreUUID", pressureDatastoreUUID);
                data.put("lightDatastoreUUID", lightDatastoreUUID);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            try {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isDeviceRegistered", true);
                editor.putString("datasetUUID", data.getString("datasetUUID"));
                editor.putString("sensorsDatastoreUUID", data.getString("sensorsDatastoreUUID"));
                editor.putString("temperatureDatastoreUUID", data.getString("temperatureDatastoreUUID"));
                editor.putString("pressureDatastoreUUID", data.getString("pressureDatastoreUUID"));
                editor.putString("lightDatastoreUUID", data.getString("lightDatastoreUUID"));
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    private class SendData extends AsyncTask<String, Void, Void> {

        void insertInDatastore(String measureUUID, String sensorName, String sensorValue, String lat, String lon, String date){
            String command = "{\"resource_id\":\""+measureUUID+"\", \"method\":\"insert\", \"records\":[{\"Latitude\":\""+lat+"\",\"Altitude\":\"0\",\"Date\":\""+date+"\",\""+sensorName+"\":\""+sensorValue+"\",\"Longitude\":\"+"lon"+\"}]}";
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
