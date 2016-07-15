/*
*                                Apache License
*                           Version 2.0, January 2004
*                        http://www.apache.org/licenses/
*
*      Copyright (c) 2016 Luca D'Amico, Andrea Faraone
*
*/

package it.unime.mobility4ckan;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Overview extends LinearLayout {

    LinearLayout sensorLayout;
    LinearLayout currentLayout;
    LinearLayout sendLayout;

    TextView sensor;
    TextView current;
    TextView send;

    public Overview(Context context) {
        super(context);
        init();
    }

    public Overview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Overview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.overview_layout, this);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        sensorLayout = (LinearLayout)findViewById(R.id.sensor_layout);
        currentLayout = (LinearLayout)findViewById(R.id.current_layout);
        sendLayout = (LinearLayout)findViewById(R.id.value_layout);

        sensor = (TextView)findViewById(R.id.sensor_name);
        current = (TextView)findViewById(R.id.current_value);
        send = (TextView)findViewById(R.id.value_send);

        sensorLayout.getLayoutParams().width = displayMetrics.widthPixels/5;
        currentLayout.getLayoutParams().width = displayMetrics.widthPixels/5*2;
        sendLayout.getLayoutParams().width = displayMetrics.widthPixels/5*2;

    }

    public void setSensorName(String sensorName){
        sensor.setText(sensorName);
    }

    public void setCurrentValue(String currentValue){
        current.setText(currentValue);
    }

    public void setValueSend(String valueSend){
        send.setText(valueSend);
    }

}
