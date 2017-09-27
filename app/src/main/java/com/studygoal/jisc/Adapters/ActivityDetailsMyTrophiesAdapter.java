package com.studygoal.jisc.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.TrophyMy;
import com.studygoal.jisc.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityDetailsMyTrophiesAdapter extends BaseAdapter {
    private static final String TAG = ActivityDetailsMyTrophiesAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    public List<TrophyMy> list;

    public ActivityDetailsMyTrophiesAdapter() {
        list = new ArrayList<>();
        inflater = LayoutInflater.from(DataManager.getInstance().mainActivity);
    }

    @Override
    public int getCount() {
        return list.size();
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_activity_details_trophy, parent, false);
        }
        TrophyMy trophy = list.get(position);

        TextView trophyName = (TextView) convertView.findViewById(R.id.trophy_name);
        TextView trophyHours = (TextView) convertView.findViewById(R.id.trophy_hours);
        trophyName.setTypeface(DataManager.getInstance().myriadpro_regular);
        trophyName.setText(trophy.trophy_name);
        trophyHours.setTypeface(DataManager.getInstance().myriadpro_regular);
        trophyHours.setText(trophy.count);

        Glide.with(DataManager.getInstance().mainActivity).load(trophy.getImageDrawable(DataManager.getInstance().mainActivity)).into((ImageView) convertView.findViewById(R.id.image));

        return convertView;
    }
}
