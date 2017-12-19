package com.studygoal.jisc.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.ReceivedRequest;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendsRequestAdapter extends BaseAdapter {
    private static final String TAG = FriendsRequestAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    public List<ReceivedRequest> list;
    private Context context;

    public FriendsRequestAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (list.size() == 0) {
            return 1;
        } else {
            return list.size();
        }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (list.size() == 0) {
            convertView = inflater.inflate(R.layout.snippet_empty_friends_list, parent, false);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setTypeface(DataManager.getInstance().myriadpro_regular);

            convertView.setTag("-1");
            return convertView;
        } else {
            if (convertView == null || (convertView.getTag() != null && convertView.getTag().equals("-1"))) {
                convertView = inflater.inflate(R.layout.list_item_friend_request, parent, false);
            }

            final ReceivedRequest attendant = list.get(position);

            if (attendant.photo.equals("")) {
                GlideApp.with(this.context)
                        .load(R.drawable.profilenotfound)
                        .transform(new CircleTransform(context))
                        .into(((ImageView) convertView.findViewById(R.id.portrait)));
            } else {
                GlideApp.with(context)
                        .load(NetworkManager.getInstance().host + attendant.photo)
                        .transform(new CircleTransform(context))
                        .placeholder(R.drawable.profilenotfound)
                        .into(((ImageView) convertView.findViewById(R.id.portrait)));
            }

            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setTypeface(DataManager.getInstance().myriadpro_regular);
            name.setText(attendant.first_name + " " + attendant.last_name);


            convertView.findViewById(R.id.confirm).setTag("" + position);
            convertView.findViewById(R.id.confirm).setOnClickListener(v -> {
                if (ConnectionHandler.isConnected(context)) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.layout_send_friend_request);

                    if (DataManager.getInstance().mainActivity.isLandscape) {
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int width = (int) (displaymetrics.widthPixels * 0.4);

                        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = width;
                        dialog.getWindow().setAttributes(params);
                    }

                    ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);

                    ((TextView) dialog.findViewById(R.id.question)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.question)).setText(context.getString(R.string.what_would_you_like_student_to_see).replace("%s", attendant.first_name));

                    final SwitchCompat switch2 = (SwitchCompat) dialog.findViewById(R.id.switch2);
                    switch2.setTypeface(DataManager.getInstance().myriadpro_regular);
                    switch2.setChecked(true);
                    final SwitchCompat switch3 = (SwitchCompat) dialog.findViewById(R.id.switch3);
                    switch3.setTypeface(DataManager.getInstance().myriadpro_regular);
                    switch3.setChecked(true);
                    final SwitchCompat switch4 = (SwitchCompat) dialog.findViewById(R.id.switch4);
                    switch4.setTypeface(DataManager.getInstance().myriadpro_regular);
                    switch4.setChecked(true);
                    final SwitchCompat switch1 = (SwitchCompat) dialog.findViewById(R.id.switch1);
                    switch1.setTypeface(DataManager.getInstance().myriadpro_regular);
                    switch1.setChecked(true);

                    switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            switch2.setChecked(true);
                            switch3.setChecked(true);
                            switch4.setChecked(true);
                        } else {
                            switch2.setChecked(false);
                            switch3.setChecked(false);
                            switch4.setChecked(false);
                        }
                    });
                    ((TextView) dialog.findViewById(R.id.text)).setText("Accept");

                    dialog.findViewById(R.id.send).setTag(v.getTag());
                    dialog.findViewById(R.id.send).setOnClickListener(v1 -> {
                        dialog.dismiss();
                        if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FriendsRequestAdapter.this.context);
                            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + FriendsRequestAdapter.this.context.getString(R.string.demo_mode_sendfriendrequest) + "</font>"));
                            alertDialogBuilder.setNegativeButton("Ok", (dialog1, which) -> dialog1.dismiss());
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                            return;
                        }

                        HashMap<String, String> params = new HashMap<>();
                        params.put("student_id", DataManager.getInstance().user.id);
                        params.put("from_user", attendant.id);
                        params.put("language", DataManager.getInstance().language);

                        params.put("is_result", switch2.isChecked() ? "yes" : "no");
                        params.put("is_course_engagement", switch3.isChecked() ? "yes" : "no");
                        params.put("is_activity_log", switch4.isChecked() ? "yes" : "no");

                        if (NetworkManager.getInstance().acceptFriendRequest(params)) {
                            ReceivedRequest request = list.get(Integer.parseInt((String) v1.getTag()));
                            request.delete();
                            list.remove(request);
                            FriendsRequestAdapter.this.notifyDataSetChanged();
                            dialog.dismiss();
                        } else {
                            Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.fail_to_accept_friend_request, Snackbar.LENGTH_LONG).show();
                        }
                    });
                    dialog.show();
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            });

            convertView.findViewById(R.id.delete).setOnClickListener(v -> {
                if (ConnectionHandler.isConnected(context)) {
                    final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.layout_dialog_confirmation);
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    if (DataManager.getInstance().mainActivity.isLandscape) {
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int width = (int) (displaymetrics.widthPixels * 0.45);

                        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = width;
                        dialog.getWindow().setAttributes(params);
                    }
                    ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
                    ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.confirmation);

                    ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.confirm_delete_message);

                    ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

                    ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

                    dialog.findViewById(R.id.dialog_ok).setOnClickListener(v12 -> {
                        dialog.dismiss();
                        if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FriendsRequestAdapter.this.context);
                            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + FriendsRequestAdapter.this.context.getString(R.string.demo_mode_sendfriendrequest) + "</font>"));
                            alertDialogBuilder.setNegativeButton("Ok", (dialog12, which) -> dialog12.dismiss());
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                            return;
                        }
                        HashMap<String, String> params = new HashMap<>();
                        params.put("student_id", DataManager.getInstance().user.id);
                        params.put("deleted_user", attendant.id);
                        if (NetworkManager.getInstance().deleteFriendRequest(params)) {
                            list.remove(position);
                            attendant.delete();
//                          Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.deleted_successfully, Snackbar.LENGTH_LONG).show();
                            notifyDataSetChanged();
                        } else {
//                          Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.not_deleted_friend_request_message, Snackbar.LENGTH_LONG).show();
                            notifyDataSetChanged();
                        }
                    });
                    dialog.findViewById(R.id.dialog_no).setOnClickListener(v13 -> dialog.dismiss());
                    dialog.show();
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            });
            return convertView;
        }
    }
}

