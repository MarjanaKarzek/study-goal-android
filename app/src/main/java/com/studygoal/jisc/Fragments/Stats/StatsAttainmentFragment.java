package com.studygoal.jisc.Fragments.Stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.AttainmentAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Attainment;
import com.studygoal.jisc.R;

import java.util.ArrayList;

public class StatsAttainmentFragment extends Fragment {
    private static final String TAG = StatsAttainmentFragment.class.getSimpleName();

    private ListView listView;
    private AttainmentAdapter adapter;
    private View mainview;
    private TextView nowData;
    private SwipeRefreshLayout layout;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.attainment));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        new Thread(() -> {
            if (NetworkManager.getInstance().getAssignmentRanking()) {
                adapter.list = new Select().from(Attainment.class).execute();
            } else {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                String attainmentDataBackup = sharedPref.getString(getString(R.string.attainmentData), "no_data_stored");
                adapter.list = new ArrayList<Attainment>();
                String[] attainmentData = attainmentDataBackup.split("----");
                for (String data : attainmentData) {
                    String[] attainment = data.split(";");
                    if(attainment.length == 4)
                        adapter.list.add(new Attainment(attainment[0], attainment[1], attainment[2], attainment[3]));
                }
            }

            String attainmentDataBackup = "";
            for (int i = 0; i < adapter.list.size(); i++) {
                Attainment attainment = adapter.list.get(i);

                if (attainment.percent.length() > 1
                        && Integer.parseInt(attainment.percent.substring(0, attainment.percent.length() - 1)) == 0) {
                    adapter.list.remove(i);
                }

                attainmentDataBackup += adapter.list.get(i).id + ";"
                        + adapter.list.get(i).date + ";"
                        + adapter.list.get(i).module + ";"
                        + adapter.list.get(i).percent + "----";
            }

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.attainmentData), attainmentDataBackup);
            editor.commit();

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                if (adapter.list != null && adapter.list.size() > 0) {
                    nowData.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                } else {
                    nowData.setVisibility(View.VISIBLE);
                    layout.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            });
        }).

                start();

        XApiManager.getInstance().

                sendLogActivityEvent(LogActivityEvent.NavigateAttainment);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.layout_stats_attainment, container, false);
        layout = (SwipeRefreshLayout) mainview.findViewById(R.id.stats_swipe_refresh);
        layout.setColorSchemeResources(R.color.colorPrimary);

        nowData = (TextView) mainview.findViewById(R.id.no_data);
        nowData.setTypeface(DataManager.getInstance().myriadpro_regular);

        layout.setOnRefreshListener(() -> new Thread(() -> {
            if (NetworkManager.getInstance().getAssignmentRanking()) {
                adapter.list = new Select().from(Attainment.class).execute();
                for(Attainment obj:adapter.list){
                    Log.d(TAG, "onCreateView: attainment element: " + obj.module);
                }
            } else {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                String attainmentDataBackup = sharedPref.getString(getString(R.string.attainmentData), "no_data_stored");
                adapter.list = new ArrayList<Attainment>();
                String[] attainmentData = attainmentDataBackup.split("----");
                for (String data : attainmentData) {
                    String[] attainment = data.split(";");
                    adapter.list.add(new Attainment(attainment[0], attainment[1], attainment[2], attainment[3]));
                }
            }

            if (adapter.list.size() > 0) {
                String attainmentDataBackup = "";
                for (int i = 0; i < adapter.list.size(); i++) {
                    if (adapter.list.get(i).percent != null && !adapter.list.get(i).percent.isEmpty()) {
                        String stringIndex = adapter.list.get(i).percent.substring(0, adapter.list.get(i).percent.length() - 1);

                        try {
                            if (stringIndex != null && !stringIndex.isEmpty()) {
                                int index = Integer.parseInt(stringIndex);

                                if (index == 0) {
                                    adapter.list.remove(i);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        attainmentDataBackup += adapter.list.get(i).id + ";"
                                + adapter.list.get(i).date + ";"
                                + adapter.list.get(i).module + ";"
                                + adapter.list.get(i).percent + "----";
                    }
                }
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.attainmentData), attainmentDataBackup);
                editor.commit();
            }

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                callRefresh();
            });
        }).start());

        listView = (ListView) mainview.findViewById(R.id.list);
        listView.setOnTouchListener((v, event) -> {
            // Setting on Touch Listener for handling the touch inside ScrollView
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        adapter = new AttainmentAdapter(DataManager.getInstance().mainActivity);
        listView.setAdapter(adapter);

        final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (DataManager.getInstance().user.isStaff && preferences.getBoolean("stats_alert", true)) {
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity());
            alertDialogBuilder.setMessage(R.string.statistics_admin_view);
            alertDialogBuilder.setPositiveButton("Don't show again", (dialog, which) -> {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("stats_alert", false);
                editor.apply();
            });

            alertDialogBuilder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        return mainview;
    }

    private void callRefresh() {
        layout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
