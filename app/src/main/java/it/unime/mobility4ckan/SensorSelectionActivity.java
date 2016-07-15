/*
*                                Apache License
*                           Version 2.0, January 2004
*                        http://www.apache.org/licenses/
*
*      Copyright (c) 2016 Luca D'Amico, Andrea Faraone, Giovanni Merlino
*
*/

package it.unime.mobility4ckan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensorSelectionActivity extends AppCompatActivity {

    private static final String TAG = "SensorSelectionActivity";
    ListView listView;
    Button button;

    SensorManager sensorManager;

    List<Sensor> list;
    List<DinamicView> dinamicViewList = new ArrayList<>();
    SharedPreferences sharedPref;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_selection);
        context = this;
        sharedPref = getSharedPreferences("sharedprefs", MODE_PRIVATE);
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

        checkSensorsSelected();

        listView.setAdapter(new SensorAdapter(getApplicationContext(), dinamicViewList));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dinamicViewList.get(position).isChoose()){
                    dinamicViewList.get(position).setBackgroundColor(Color.WHITE);
                    SensorConfig.sensorList.remove(list.get(position));

                    dinamicViewList.get(position).setChoose(false);
                }else {
                    dinamicViewList.get(position).setBackgroundColor(Color.parseColor("#99cc00"));
                    SensorConfig.sensorList.add(list.get(position));

                    dinamicViewList.get(position).setChoose(true);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                Set<String> set = new HashSet<String>();
                for (int k = 0; k < SensorConfig.sensorList.size(); k++) {
                    set.add(SensorConfig.sensorList.get(k).getName());
                }
                editor.putStringSet("selectedSensors", set);
                editor.apply();

                DinamicView timerView = new DinamicView(getApplicationContext());
                timerView.getNoteLabel().setText(getString(R.string.tempo_countdown));
                timerView.getNoteLabel().setTextSize(12);

                final EditText timerText = new EditText(getApplicationContext());
                timerText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                timerText.setGravity(Gravity.CENTER);
                timerText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                timerText.setTextColor(Color.BLACK);
                timerText.setSingleLine(true);
                timerText.setInputType(InputType.TYPE_CLASS_NUMBER);
                timerText.setTextSize(18);

                timerView.getBodyLayout().addView(timerText);
                final AlertDialog timerMDialog = new AlertDialog.Builder(context)
                        .setView(timerView)
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .create();

                timerMDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button tb = timerMDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        tb.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String selectedTimerText = timerText.getText().toString().trim().toLowerCase();
                                String regexp = "^[0-9]*$";
                                Matcher matcher = Pattern.compile(regexp).matcher(selectedTimerText);

                                if (matcher.find()) {
                                    timerMDialog.dismiss();
                                    SensorConfig.countDownTimer = Integer.parseInt(selectedTimerText);
                                    SensorConfig.countDownTimer *= 1000;
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    timerText.getText().clear();
                                    timerText.setError(getString(R.string.note_timer));
                                }
                            }
                        });
                    }
                });
                timerMDialog.show();

                DinamicView apiView = new DinamicView(getApplicationContext());
                apiView.getNoteLabel().setText(getString(R.string.apikey_req));
                apiView.getNoteLabel().setTextSize(12);

                final EditText apiKey = new EditText(getApplicationContext());
                apiKey.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                apiKey.setGravity(Gravity.CENTER);
                apiKey.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                apiKey.setTextColor(Color.BLACK);
                apiKey.setSingleLine(true);
                apiKey.setInputType(InputType.TYPE_CLASS_TEXT);
                apiKey.setTextSize(18);

                apiView.getBodyLayout().addView(apiKey);
                final AlertDialog apiMDialog = new AlertDialog.Builder(context)
                        .setView(apiView)
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .create();

                apiMDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button akb = apiMDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        akb.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Log.i(TAG, "apiKey set to " + apiKey);
                                String selectedApiKey = apiKey.getText().toString().trim().toLowerCase();
                                Log.i(TAG, "selectedApiKey set to " + selectedApiKey);
                                String regexp = "^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$";
                                Matcher matcher = Pattern.compile(regexp).matcher(selectedApiKey);

                                if (matcher.find()) {
                                    apiMDialog.dismiss();
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("userAPIkey", selectedApiKey);
                                    editor.apply();
                                } else {
                                    timerText.getText().clear();
                                    timerText.setError(getString(R.string.note_timer));
                                }
                            }
                        });
                    }
                });
                apiMDialog.show();
             }
        });
    }

    private void checkSensorsSelected(){
        Set<String> set = sharedPref.getStringSet("selectedSensors", null);
        if(set != null) {
            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                String next = it.next();
                for (int j = 0; j < list.size(); j++) {
                    if (next.equals(list.get(j).getName())) {
                        dinamicViewList.get(j).setBackgroundColor(Color.parseColor("#99cc00"));
                        dinamicViewList.get(j).setChoose(true);
                        SensorConfig.sensorList.add(list.get(j));
                    }
                }
            }
        }
    }

}
