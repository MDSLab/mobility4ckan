package it.unime.embeddedsystems;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by andfa on 06/06/2016.
 */
public class OverviewAdapter extends BaseAdapter {

    Context mContext;
    List<Overview> mList;

    public OverviewAdapter(Context context, List<Overview> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mList.get(position);
    }
}
