package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
    private static final String TAG = DrawerAdapter.class.getSimpleName();

    public String[] values;
    public TextView selectedText;
    public ImageView selectedImage;
    public ImageView profilePicture;
    public boolean statsOpened;

    private LayoutInflater inflater;
    private Context context;
    private int statOpenedNum = 2;
    private TextView textView;
    private ImageView imageView;

    public DrawerAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        statsOpened = false;

        if (DataManager.getInstance().user.isSocial) {
            statOpenedNum = 0;
            values = new String[]{"0", context.getString(R.string.feed), context.getString(R.string.log), context.getString(R.string.target), context.getString(R.string.logout)};
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            ArrayList<String> valuesList = new ArrayList<>();
            valuesList.add("0");
            valuesList.add(context.getString(R.string.feed));
            valuesList.add(context.getString(R.string.friends));
            valuesList.add(context.getString(R.string.stats));
            valuesList.add(context.getString(R.string.points));
            valuesList.add(context.getString(R.string.app_usage));
            statOpenedNum++;

            valuesList.add(context.getString(R.string.attainment));
            statOpenedNum++;

//            if (prefs.getBoolean(con.getString(R.string.attainmentData), false)) {
//                valuesList.add(con.getString(R.string.attainment));
//                statOpenedNum++;
//            }

            if (AppCore.getInstance().getPreferences().getAttendanceData()) {
                //valuesList.add(con.getString(R.string.attendance));
                //statOpenedNum++;
                valuesList.add(context.getString(R.string.attendance_menu));
                statOpenedNum++;
            }

            valuesList.add(context.getString(R.string.graphs));

            if (prefs.getBoolean(context.getString(R.string.studyGoalAttendance), false)) {
                valuesList.add(context.getString(R.string.check_in));
            }

            valuesList.add(context.getString(R.string.log));
            valuesList.add(context.getString(R.string.target));
            valuesList.add(context.getString(R.string.settings));
            valuesList.add(context.getString(R.string.logout));
            values = valuesList.toArray(new String[valuesList.size()]);
        }
    }

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
            convertView = inflater.inflate(R.layout.snippet_nav_header_main, parent, false);
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
            profilePicture = (ImageView) convertView.findViewById(R.id.imageView);

            if (DataManager.getInstance().user.profile_pic.equals("")) {
                GlideApp.with(context)
                        .load(R.drawable.profilenotfound2)
                        .transform(new CircleTransform(context))
                        .into(profilePicture);
            } else {
                GlideApp.with(context)
                        .load(NetworkManager.getInstance().host + DataManager.getInstance().user.profile_pic)
                        .transform(new CircleTransform(context))
                        .into(profilePicture);
            }
        } else {
            if (statsOpened && position > 3 && position <= 3 + statOpenedNum) {
                convertView = inflater.inflate(R.layout.menu_item_nav_sub, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.menu_item_nav, parent, false);
            }

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

            Log.d(TAG, "getView: fragment Menu Text" + DataManager.getInstance().fragment);
            if (DataManager.getInstance().fragment != null) {
                if (DataManager.getInstance().fragment == position) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.default_blue));
                    imageView.setColorFilter(ContextCompat.getColor(context, R.color.default_blue));
                    selectedImage = imageView;
                    selectedText = textView;
                    //DataManager.getInstance().fragment = null;
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
                    selectedImage = imageView;
                    selectedText = textView;
                }
            }
        }

        return convertView;
    }
}
