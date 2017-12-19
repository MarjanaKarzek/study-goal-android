package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.Attainment;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Attainment Adapter
 * <p>
 * Handles list of attainment data.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttainmentAdapter extends BaseAdapter {

    private static final String TAG = AttainmentAdapter.class.getSimpleName();

    public List<Attainment> list;
    private LayoutInflater inflater;

    public AttainmentAdapter(Context context) {
        this.list = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int size = list.size();
        if (DataManager.getInstance().user.affiliation.contains("glos.ac.uk")) {
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_attainment, parent, false);
        }

        TextView module = (TextView) convertView.findViewById(R.id.attainment_item_module);
        TextView mark = (TextView) convertView.findViewById(R.id.attainment_item_marks);
        TextView date = (TextView) convertView.findViewById(R.id.attainment_item_date);

        if (position >= list.size() && DataManager.getInstance().user.affiliation.contains("glos.ac.uk")) {
            module.setText(DataManager.getInstance().mainActivity.getString(R.string.attainment_info));
            mark.setVisibility(View.GONE);
            date.setVisibility(View.GONE);
        } else {
            Attainment attainment = list.get(position);
            date.setVisibility(View.VISIBLE);
            date.setText(Utils.attainmentDate(attainment.date).replace("-", "/"));
            mark.setVisibility(View.VISIBLE);
            mark.setText(attainment.percent + "%");
            module.setText(attainment.module);
        }

        return convertView;
    }
}
