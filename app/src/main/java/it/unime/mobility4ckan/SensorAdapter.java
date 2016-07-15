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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class SensorAdapter extends BaseAdapter {

    Context mContext;
    List<DinamicView> mSensorList;

    public SensorAdapter(Context context, List<DinamicView> sensorList) {
        mContext = context;
        mSensorList = sensorList;
    }

    @Override
    public int getCount() {
        return mSensorList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSensorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mSensorList.get(position);
    }
}
