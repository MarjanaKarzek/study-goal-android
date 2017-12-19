package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studygoal.jisc.Models.Event;
import com.studygoal.jisc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Attendance Adapter
 * <p>
 * Handles events attended information.
 *
 * @author Therapy Box - Marjana Karzek
 * @version 1.5
 * @date 12/09/17
 */
public class AttendanceAdapter extends BaseAdapter {
    private static final String TAG = AttendanceAdapter.class.getSimpleName();

    private Context context;
    private List<Event> list;

    public AttendanceAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
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
        final Event item = list.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_events_attended, parent, false);
        }

        TextView date = (TextView) convertView.findViewById(R.id.event_item_time_ago);
        date.setText(item.getDate());
        TextView activity = (TextView) convertView.findViewById(R.id.event_item_activity);
        activity.setText(item.getActivity());
        TextView module = (TextView) convertView.findViewById(R.id.event_item_module);
        module.setText(item.getModule());

        return convertView;
    }

    // Change list options

    /**
     * Gets the current list.
     *
     * @return list
     */
    public List<Event> getList() {
        return list;
    }

    /**
     * Changes the current list to the list provided.
     *
     * @param events list to be added
     */
    public void updateList(ArrayList<Event> events) {
        if (events != null && events.size() > 0) {
            list.clear();
            list.addAll(events);
            Collections.sort(list, (o1, o2) -> {
                if (o1 != null && o2 != null) {
                    Long t1 = o1.getTime();
                    Long t2 = o2.getTime();

                    return t2.compareTo(t1);
                }

                return 0;
            });
            notifyDataSetChanged();
        }
    }

    /**
     * Extends the list by the given list of events.
     *
     * @param events events to be added
     */
    public void addToList(ArrayList<Event> events) {
        if (events != null && events.size() > 0) {
            list.addAll(events);
            Collections.sort(list, (o1, o2) -> {
                if (o1 != null && o2 != null) {
                    Long t1 = o1.getTime();
                    Long t2 = o2.getTime();

                    return t2.compareTo(t1);
                }

                return 0;
            });
            notifyDataSetChanged();
        }
    }
}
