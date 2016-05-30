package it.unime.embeddedsystems;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor tempSensor;
    private TextView temperatureTXV;
    private String serverUrl = "http://smartme-data.unime.it/api/3/action/datastore_upsert";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureTXV = (TextView) findViewById(R.id.txv_temperature);

        Gps gps = new Gps(getApplicationContext());

        temperatureTXV.setText("Latitudine: "+gps.getLatitude()+ "  Logitudine: "+gps.getLongitude());

     /*   sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);*/

      /*  RestAdapter adapter = new RestAdapter.Builder().setEndpoint(url).build();

        RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method to get whether report
        restInterface.getWheatherReport(new Callback<Model>() {
            @Override
            public void success(Model model, Response response) {
                System.out.println(model.getName());
            }

            @Override
            public void failure(RetrofitError error) {

                String merror = error.getMessage();
                System.out.println(merror);
            }
        });*/

        String command = "{\"resource_id\":\"c35b761d-8f4a-4b89-a68e-fcdb8063b636\", \"method\":\"insert\", \"records\":[{\"Type\":\"TYPE_ACCELEROMETER\",\"Model\":\"Accelerometer\",\"Unit\":\"m/s^2\",\"FabricName\":\"-\",\"ResourceID\":\"71ae4c3c-3f2b-4c31-ba09-1d83444327d2\",\"Date\":\"2016-05-13T12:00:00\"}]}";
        //new Task().execute(serverUrl, authorization, command);
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

        String createDatastore(String datasetName, String datastoreName){
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

        void insertInSensorDatastore(String sensorUUID, String measureUUID, String type, String name, String unit){
            String command = "{\"resource_id\":\""+sensorUUID+"\", \"method\":\"insert\", \"records\":[{\"Type\":\""+type+"\",\"Model\":\""+name+"\",\"Unit\":\""+unit+"\",\"FabricName\":\"-\",\"ResourceID\":\""+measureUUID+"\",\"Date\":\"2016-05-13T12:00:00\"}]}";
            POST("http://smartme-data.unime.it/api/3/action/datastore_upsert", "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae", command); // Create Datastore
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            String datasetUUID = createDataset(params[0]);
            String sensorsDatastoreUUID = createDatastore("","sensors");

            String temperatureDatastoreUUID = createDatastore("","temperature");
            insertInSensorDatastore(sensorsDatastoreUUID,temperatureDatastoreUUID, "TYPE_TEMPERATURE", "TEMPERATURE", "celsius");

            String pressureDatastoreUUID = createDatastore("","pressure");
            insertInSensorDatastore(sensorsDatastoreUUID,pressureDatastoreUUID, "TYPE_PRESSURE", "PRESSURE", "mbar");

            String lightDatastoreUUID = createDatastore("","light");
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

        }

    }

}
