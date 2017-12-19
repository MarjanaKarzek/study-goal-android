package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;

import java.util.ArrayList;

/**
 * Choose Activity Adapter
 * <p>
 * Handles list of activity items.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class ChooseActivityAdapter extends BaseAdapter {

    private static final String TAG = ChooseActivityAdapter.class.getSimpleName();

    public ArrayList<String> activityList;
    private LayoutInflater inflater;
    private String selected;

    public ChooseActivityAdapter(Context context, String selected, String activityType) {
        this.selected = selected;
        this.activityList = DataManager.getInstance().choose_activity.get(activityType);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return activityList.size();
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
            convertView = inflater.inflate(R.layout.snippet_custom_spinner_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.dialog_item_name);
        textView.setTypeface(DataManager.getInstance().myriadpro_regular);
        textView.setText(activityList.get(position));

        if (activityList.get(position).equals(selected)) {
            convertView.findViewById(R.id.dialog_item_selected).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.dialog_item_selected).setVisibility(View.GONE);
        }

        return convertView;
    }
}
