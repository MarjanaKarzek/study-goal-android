package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.ActivityPoints;
import com.studygoal.jisc.Models.Attainment;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ActivityPointsAdapter extends BaseAdapter {
    private static final String TAG = ActivityPointsAdapter.class.getSimpleName();

    public List<Attainment> list;
    private LayoutInflater inflater;

    public ActivityPointsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {

        return DataManager.getInstance().user.points.size() + 1;
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


        convertView = inflater.inflate(R.layout.activity_points_item, parent, false);

        if(position == 0) {
            convertView.setBackgroundColor(Color.parseColor("#eeeeee"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#f6f6f6"));
        }

        TextView activityTitle = (TextView) convertView.findViewById(R.id.activity_title);
        activityTitle.setTypeface(DataManager.getInstance().myriadpro_regular);

        TextView countTitle = (TextView) convertView.findViewById(R.id.count_title);
        countTitle.setTypeface(DataManager.getInstance().myriadpro_regular);

        TextView pointsTitle = (TextView) convertView.findViewById(R.id.points_title);
        pointsTitle.setTypeface(DataManager.getInstance().myriadpro_regular);

        if(position > 0) {
            ActivityPoints activityPoints = DataManager.getInstance().user.points.get(position-1);

            if(activityPoints.activity.equals("Loggedin")){
                activityTitle.setText("Logged in");
            } else {
                activityTitle.setText(activityPoints.activity);
            }
            countTitle.setText(activityPoints.points);
            pointsTitle.setText(activityPoints.points);
        }

        return convertView;
    }

}
