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
 * Created by Marjana-Tbox on 12/09/17.
 */
public class EventsAttendedAdapter extends BaseAdapter {
    private static final String TAG = EventsAttendedAdapter.class.getSimpleName();

    private Context context;
    private List<Event> list;

    public EventsAttendedAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public List<Event> getList() {
        return list;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_events_attended, parent, false);
        }

        TextView date = (TextView) convertView.findViewById(R.id.event_item_time_ago);
        date.setText(item.getDate());
        TextView activity = (TextView) convertView.findViewById(R.id.event_item_activity);
        activity.setText(item.getActivity());
        TextView module = (TextView) convertView.findViewById(R.id.event_item_module);
        module.setText(item.getModule());

        return convertView;
    }

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
