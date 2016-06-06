package it.unime.embeddedsystems;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by andfa on 01/06/2016.
 */
public class SensorSelectionActivity extends AppCompatActivity {

    ListView listView;
    Button button;

    SensorManager sensorManager;

    List<Sensor> list;
    List<DinamicView> dinamicViewList = new ArrayList<>();
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_selection);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        listView = (ListView) findViewById(R.id.selection_listview);
        list = sensorManager.getSensorList(Sensor.TYPE_ALL);

        button = (Button)findViewById(R.id.button);

        for(Sensor s : list) {
            DinamicView dinamicView = new DinamicView(getApplicationContext());
            dinamicView.getNoteLabel().setText(s.getName());
            dinamicView.setNoteVisibility(View.INVISIBLE);

            dinamicViewList.add(dinamicView);
        }

        listView.setAdapter(new SensorAdapter(getApplicationContext(), dinamicViewList));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dinamicViewList.get(position).isChoose()){
                    view.setBackgroundColor(Color.WHITE);
                    SensorConfig.sensorList.remove(list.get(position));

                    dinamicViewList.get(position).setChoose(false);
                }else {
                    view.setBackgroundColor(Color.parseColor("#99cc00"));
                    SensorConfig.sensorList.add(list.get(position));

                    dinamicViewList.get(position).setChoose(true);
                }
            }
        });
        Set<String> set = sharedPref.getStringSet("selectedSensors", null);
        // FIXME continua selezione salvati
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                Set<String> set = new HashSet<String>();
                for(int k=0; k<SensorConfig.sensorList.size(); k++) {
                    set.add(SensorConfig.sensorList.get(k).getName());
                }
                editor.putStringSet("selectedSensors", set);
                editor.apply();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

}
