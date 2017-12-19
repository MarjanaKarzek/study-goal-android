package com.studygoal.jisc.Fragments.Friends;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.studygoal.jisc.Adapters.FriendsSearchAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.Friend;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.Utils;

import java.util.HashMap;

public class FriendsSearchFragment extends Fragment {
    private static final String TAG = FriendsSearchFragment.class.getSimpleName();

    private ListView list;
    private FriendsSearchAdapter adapter;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.friends));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkManager.getInstance().getFriends(DataManager.getInstance().user.id);
                NetworkManager.getInstance().getFriendRequests(DataManager.getInstance().user.id);
                NetworkManager.getInstance().getSentFriendRequests(DataManager.getInstance().user.id);
            }
        }).start();
    }

    @Override
    public void onPause() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.layout_friends_search, container, false);

        ((TextView) mainView.findViewById(R.id.send_friend_request_text)).setTypeface(DataManager.getInstance().myriadpro_regular);

        adapter = new FriendsSearchAdapter(getContext());
        list = (ListView) mainView.findViewById(R.id.list);
        list.setAdapter(adapter);

        final EditText search = (EditText) mainView.findViewById(R.id.friends_search_edittext);
        View sendFriendRequest = mainView.findViewById(R.id.send_friend_request);
        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FriendsSearchFragment.this.getActivity());
                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_sendfriendrequest) + "</font>"));
                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return;
                }

                if(ConnectionHandler.isConnected(getContext())) {
                    final String email = search.getText().toString();
                    if (!Utils.validate_email(email)) {
                        Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.please_search_for_friend, Snackbar.LENGTH_LONG).show();
                    } else {
                        InputMethodManager imm = (InputMethodManager) FriendsSearchFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

                        search.clearFocus();
                        final Friend friend = NetworkManager.getInstance().getStudentByEmail(search.getText().toString());

                        if (friend != null) {
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.layout_send_friend_request);

                            if (DataManager.getInstance().mainActivity.isLandscape) {
                                DisplayMetrics displaymetrics = new DisplayMetrics();
                                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                                int width = (int) (displaymetrics.widthPixels * 0.4);

                                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                                params.width = width;
                                dialog.getWindow().setAttributes(params);
                            }

                            ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);

                            ((TextView) dialog.findViewById(R.id.question)).setTypeface(DataManager.getInstance().myriadpro_regular);
                            ((TextView) dialog.findViewById(R.id.question)).setText(getActivity().getString(R.string.what_would_you_like_student_to_see).replace("%s", ""));

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
                            switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        switch2.setChecked(true);
                                        switch3.setChecked(true);
                                        switch4.setChecked(true);
                                    } else {
                                        switch2.setChecked(false);
                                        switch3.setChecked(false);
                                        switch4.setChecked(false);
                                    }
                                }
                            });
                            dialog.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("from_student_id", DataManager.getInstance().user.id);
                                    params.put("to_student_id", friend.id);
                                    params.put("is_result", switch2.isChecked() ? "yes" : "no");
                                    params.put("is_course_engagement", switch3.isChecked() ? "yes" : "no");
                                    params.put("is_activity_log", switch4.isChecked() ? "yes" : "no");
                                    if (NetworkManager.getInstance().sendFriendRequest(params)) {
                                        dialog.dismiss();
                                    } else {
                                        Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.friend_request_sent, Snackbar.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                            dialog.show();
                        } else {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("from_student_id", DataManager.getInstance().user.id);
                            params.put("to_email", email);
                            params.put("language", DataManager.getInstance().language);

                            NetworkManager.getInstance().sendFriendRequest(params);
                            Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.friend_request_sent, Snackbar.LENGTH_LONG).show();
                        }
                    }
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        return mainView;
    }
}
