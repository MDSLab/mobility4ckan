package it.unime.embeddedsystems;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andfa on 01/06/2016.
 */
public class SensorSelectionActivity extends AppCompatActivity {

    ListView listView;
    Button button;

    SensorManager sensorManager;

    List<Sensor> list;
    List<String> nameSensorList = new ArrayList<>();

    ArrayAdapter adapter;

    boolean isSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_selection);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        listView = (ListView) findViewById(R.id.selection_listview);
        list = sensorManager.getSensorList(Sensor.TYPE_ALL);

        button = (Button)findViewById(R.id.button);

        for(Sensor s : list) {
            nameSensorList.add(s.getName());
        }

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameSensorList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listView.getChildAt(position).isActivated()){
                    view.setBackgroundColor(Color.WHITE);
                    SensorConfig.sensorList.remove(list.get(position));

                    listView.getChildAt(position).setActivated(false);
                }else {
                    view.setBackgroundColor(Color.parseColor("#99cc00"));
                    SensorConfig.sensorList.add(list.get(position));

                    listView.getChildAt(position).setActivated(true);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("size sensor: "+SensorConfig.sensorList.size());
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

}
