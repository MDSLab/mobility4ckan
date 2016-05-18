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


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor tempSensor;
    private TextView temperatureTXV;
    private String serverUrl = "http://smartme-data.unime.it/api/3/action/datastore_upsert";
    private String authorization = "22c5cfa7-9dea-4dd9-9f9d-eedf296852ae";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureTXV = (TextView) findViewById(R.id.txv_temperature);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, tempSensor , SensorManager.SENSOR_DELAY_NORMAL);


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
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        float temp = event.values[0];
        temperatureTXV.setText("TEMP: "+temp);


        // Do something with this sensor value.
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

    private class Task extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {
            JSONObject result = POST(params[0], params[1], params[2]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

}
