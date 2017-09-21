package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studygoal.jisc.AppCore;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {
    public String[] values;
    public TextView selected_text;
    public ImageView selected_image;
    public ImageView profile_pic;
    public boolean statsOpened;

    private LayoutInflater inflater;
    private Context context;
    private int statOpenedNum = 2;

    public DrawerAdapter(Context con) {
        context = con;
        inflater = LayoutInflater.from(con);
        statsOpened = false;

        if (DataManager.getInstance().user.isSocial) {
            statOpenedNum = 0;
            values = new String[]{"0", con.getString(R.string.feed), con.getString(R.string.log), con.getString(R.string.target), con.getString(R.string.logout)};
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
            ArrayList<String> valuesList = new ArrayList<>();
            valuesList.add("0");
            valuesList.add(con.getString(R.string.feed));
            valuesList.add(con.getString(R.string.friends));
            valuesList.add(con.getString(R.string.stats));
            valuesList.add(con.getString(R.string.points));
            //valuesList.add(con.getString(R.string.app_usage));
            //statOpenedNum++;

            valuesList.add(con.getString(R.string.attainment));
            statOpenedNum++;

//            if (prefs.getBoolean(con.getString(R.string.attainmentData), false)) {
//                valuesList.add(con.getString(R.string.attainment));
//                statOpenedNum++;
//            }

            if (AppCore.getInstance().getPreferences().getAttendanceData()) {
                valuesList.add(con.getString(R.string.attendance));
                statOpenedNum++;
                valuesList.add(con.getString(R.string.events_attended));
                statOpenedNum++;
            }

            valuesList.add(con.getString(R.string.graphs));

            if (prefs.getBoolean(con.getString(R.string.studyGoalAttendance), false)) {
                valuesList.add(con.getString(R.string.check_in));
            }

            valuesList.add(con.getString(R.string.log));
            valuesList.add(con.getString(R.string.target));
            valuesList.add(con.getString(R.string.settings));
            valuesList.add(con.getString(R.string.logout));
            values = valuesList.toArray(new String[valuesList.size()]);
        }
    }

    //Numarul de rows
    public int getCount() {
        return statsOpened ? values.length : values.length - statOpenedNum;
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
        if (position == 0) {
            convertView = inflater.inflate(R.layout.nav_header_main, parent, false);
            GlideApp.with(context).load(R.drawable.menu_header_bg).into((ImageView) convertView.findViewById(R.id.navheader));

            TextView email = (TextView) convertView.findViewById(R.id.drawer_email);
            TextView studentId = (TextView) convertView.findViewById(R.id.drawer_studentId);
            studentId.setTypeface(DataManager.getInstance().myriadpro_regular);
            studentId.setText(context.getString(R.string.student_id) + " : " + DataManager.getInstance().user.jisc_student_id);
            email.setTypeface(DataManager.getInstance().myriadpro_regular);
            email.setText(DataManager.getInstance().user.email);
            TextView name = (TextView) convertView.findViewById(R.id.drawer_name);
            name.setTypeface(DataManager.getInstance().myriadpro_regular);
            name.setText(DataManager.getInstance().user.name);
            profile_pic = (ImageView) convertView.findViewById(R.id.imageView);

            if (DataManager.getInstance().user.profile_pic.equals("")) {
                GlideApp.with(context)
                        .load(R.drawable.profilenotfound2)
                        .transform(new CircleTransform(context))
                        .into(profile_pic);
            } else {
                GlideApp.with(context)
                        .load(NetworkManager.getInstance().host + DataManager.getInstance().user.profile_pic)
                        .transform(new CircleTransform(context))
                        .into(profile_pic);
            }
        } else {
            if (statsOpened && position > 3 && position <= 3 + statOpenedNum) {
                convertView = inflater.inflate(R.layout.nav_item_sub, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.nav_item, parent, false);
            }

            TextView textView;
            ImageView imageView;
            textView = (TextView) convertView.findViewById(R.id.drawer_item_text);
            textView.setTypeface(DataManager.getInstance().myriadpro_regular);
            imageView = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
            imageView.setImageBitmap(null);

            ImageView arrow_button = (ImageView) convertView.findViewById(R.id.arrow_button);
            arrow_button.setVisibility(View.GONE);

            if (!statsOpened && position > 3) {
                position += statOpenedNum;
            }

            textView.setText(values[position]);

            int iconResID = -1;

            if (values[position].equals(context.getString(R.string.feed))) {
                iconResID = R.drawable.feed_icon;
            }
            if (values[position].equals(context.getString(R.string.check_in))) {
                iconResID = R.drawable.checkin;
            }
            if (values[position].equals(context.getString(R.string.stats))) {
                iconResID = R.drawable.stats_icon;
            }
            if (values[position].equals(context.getString(R.string.log))) {
                iconResID = R.drawable.log_icon;
            }
            if (values[position].equals(context.getString(R.string.target))) {
                iconResID = R.drawable.target_icon;
            }
            if (values[position].equals(context.getString(R.string.logout))) {
                iconResID = R.drawable.logout_icon;
            }
            if (values[position].equals(context.getString(R.string.friends))) {
                iconResID = R.drawable.friend_icon_2;
            }
            if (values[position].equals(context.getString(R.string.settings))) {
                iconResID = R.drawable.settings_2;
            }

            if (values[position].equals(context.getString(R.string.stats))) {
                arrow_button.setVisibility(View.VISIBLE);
                if (statsOpened) {
                    arrow_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_button_new_up));
                } else {
                    arrow_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_button_new));
                }
            }

            if (iconResID != -1)
                GlideApp.with(context).load(iconResID).into(imageView);

            if (DataManager.getInstance().fragment != null) {
                if (DataManager.getInstance().fragment == position) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.default_blue));
                    imageView.setColorFilter(ContextCompat.getColor(context, R.color.default_blue));
                    selected_image = imageView;
                    selected_text = textView;
                    DataManager.getInstance().fragment = null;
                }
            } else {
                String selected_value = "";
                switch (DataManager.getInstance().home_screen.toLowerCase()) {
                    case "feed": {
                        selected_value = context.getString(R.string.feed);
                        break;
                    }
                    case "friends": {
                        selected_value = context.getString(R.string.friends);
                        break;
                    }
                    case "stats": {
                        selected_value = context.getString(R.string.stats);
                        break;
                    }
                    case "log": {
                        selected_value = context.getString(R.string.log);
                        break;
                    }
                    case "checkin": {
                        selected_value = context.getString(R.string.check_in);
                        break;
                    }
                    case "target": {
                        selected_value = context.getString(R.string.target);
                        break;
                    }
                }
                if (textView.getText().toString().toLowerCase().equals(selected_value.toLowerCase())) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.default_blue));
                    imageView.setColorFilter(ContextCompat.getColor(context, R.color.default_blue));
                    selected_image = imageView;
                    selected_text = textView;
                }
            }
        }

        return convertView;
    }
}
