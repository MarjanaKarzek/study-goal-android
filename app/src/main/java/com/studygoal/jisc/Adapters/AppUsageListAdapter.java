package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AppUsageListAdapter extends BaseAdapter {
    private static final String TAG = AppUsageListAdapter.class.getSimpleName();

    public ArrayList<String> list;
    public HashMap<String,String> data;
    private LayoutInflater inflater;
    private Context context;

    public AppUsageListAdapter(Context context) {
        this.list = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        int size = list.size();
        if(DataManager.getInstance().user.affiliation.contains("glos.ac.uk")) {
            size++;
        }
        return size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = inflater.inflate(R.layout.list_item_app_usage, parent, false);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.app_usage_image);

        if(position == 0){
            convertView.findViewById(R.id.app_usage_single_layout).setVisibility(View.GONE);
            convertView.findViewById(R.id.app_usage_double_layout).setVisibility(View.VISIBLE);
            TextView textView1 = (TextView) convertView.findViewById(R.id.app_usage_double_text1);
            TextView textView2 = (TextView) convertView.findViewById(R.id.app_usage_double_text2);

            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.target_met));
            textView1.setText(context.getString(R.string.targets_met_on_time) + " " + data.get("met_targets"));
            textView2.setText(context.getString(R.string.targets_not_met_on_time) + " " + data.get("failed_targets"));
        } else {
            TextView textView = (TextView) convertView.findViewById(R.id.app_usage_single_text);

            switch (position){
                case 1:
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.target_set_icon));
                    textView.setText(context.getString(R.string.targets_set_app_usage) + " " + data.get("set_targets"));
                    break;
                case 2:
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hours_of_activites));
                    textView.setText(context.getString(R.string.hours_of_activities_logged) + " " + data.get("activities"));
                    break;
                case 3:
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sessions));
                    textView.setText(context.getString(R.string.sessions) + " " + data.get("sessions"));
                    break;
            }
        }

        return convertView;
    }

}
