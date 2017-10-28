package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Fragments.Settings.MyTrophiesFragment;
import com.studygoal.jisc.Models.TrophyMy;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import java.util.List;

/**
 * Created by MarcelC on 1/14/16.
 */
public class MyTrophiesAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = MyTrophiesAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    public List<TrophyMy> list;
    private Context context;
    private Fragment fragment;

    public MyTrophiesAdapter(Context context, Fragment fragment) {
        this.context = context;
        list = new Select().from(TrophyMy.class).execute();
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
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
            convertView = inflater.inflate(R.layout.list_item_trophies, parent, false);
        } else {
            ImageView image = (ImageView) convertView.findViewById(R.id.trophy_image);
            image.setImageDrawable(null);
        }

        ImageView image = (ImageView) convertView.findViewById(R.id.trophy_image);
        image.setImageDrawable(null);

        TrophyMy trophy = list.get(position);
        GlideApp.with(context).load(trophy.getImageDrawable(context)).into(image);

        TextView textSilver = (TextView) convertView.findViewById(R.id.total_gold);
        TextView textGold = (TextView) convertView.findViewById(R.id.total_silver);
        textGold.setVisibility(View.GONE);
        textSilver.setVisibility(View.GONE);
        textGold.setText(trophy.total);
        textSilver.setText(trophy.total);

        if (trophy.trophy_type.equals("silver")) {
            textGold.setVisibility(View.VISIBLE);
        } else {
            textSilver.setVisibility(View.VISIBLE);

        }

        convertView.setTag("" + position);
        convertView.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        TrophyMy trophy = list.get(Integer.parseInt((String) v.getTag()));
        ((MyTrophiesFragment) fragment).showTrophy(trophy);
    }
}