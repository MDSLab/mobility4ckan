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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor tempSensor;
    private TextView temperatureTXV;
    static String url_server = "http://smartme-data.unime.it/api/rest/dataset";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureTXV = (TextView) findViewById(R.id.txv_temperature);

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

        new Task().execute();
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



    public static String POST(String url, Person person){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject("{\"name\":\"<NOME-DATASET>\", \"title\":\"<NOME-DATASET>\", \"owner_org\":\"test\", \"extras\":{\"Label\":\"android-phone\",\"Manufacturer\":\"Android\", \"Model\":\"smartphone\",\"Altitude\":0,\"Latitude\":0,\"Longitude\":0}}'");

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}
