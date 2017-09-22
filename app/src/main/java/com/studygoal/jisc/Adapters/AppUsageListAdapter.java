package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.Attainment;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Utils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppUsageListAdapter extends BaseAdapter {
    public ArrayList<String> list;
    public HashMap<String,String> data;
    LayoutInflater inflater;
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
            convertView = inflater.inflate(R.layout.item_app_usage, parent, false);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.app_usage_image);

        if(position == 0){
            convertView.findViewById(R.id.app_usage_single_layout).setVisibility(View.GONE);
            convertView.findViewById(R.id.app_usage_double_layout).setVisibility(View.VISIBLE);
            TextView textView1 = (TextView) convertView.findViewById(R.id.app_usage_double_text1);
            TextView textView2 = (TextView) convertView.findViewById(R.id.app_usage_double_text2);

            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.target_met));
            textView1.setText("Targets met on time: " + data.get("targets met"));
            textView2.setText("Targets not met on time: " + data.get("targets failed"));
        } else {
            TextView textView = (TextView) convertView.findViewById(R.id.app_usage_single_text);

            switch (position){
                case 1:
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.activity_icon_2));
                    textView.setText("Targets set: " + data.get("targets set"));
                    break;
                case 2:
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hours_of_activites));
                    textView.setText("Hours of activities logged: " + data.get("activities"));
                    break;
                case 3:
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sessions));
                    textView.setText("Sessions: " + data.get("sessions"));
                    break;
            }
        }

        return convertView;
    }

}
