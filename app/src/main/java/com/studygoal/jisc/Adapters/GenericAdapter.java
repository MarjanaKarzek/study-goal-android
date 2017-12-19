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
 * Generic Adapter
 * <p>
 * Handles generically items.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class GenericAdapter extends BaseAdapter {

    private static final String TAG = GenericAdapter.class.getSimpleName();

    public ArrayList<String> list;
    private LayoutInflater inflater;
    private String selected;

    public GenericAdapter(Context context, String selected, ArrayList<String> items) {
        this.selected = selected;
        this.list = items;
        inflater = LayoutInflater.from(context);
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
            convertView = inflater.inflate(R.layout.snippet_custom_spinner_item, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.dialog_item_name);
        textView.setTypeface(DataManager.getInstance().myriadpro_regular);
        textView.setText(list.get(position));

        if (list.get(position).equals(selected)) {
            convertView.findViewById(R.id.dialog_item_selected).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.dialog_item_selected).setVisibility(View.GONE);
        }
        return convertView;
    }
}
