package com.studygoal.jisc.Fragments.Stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

    private ListView mList;

    private AttainmentAdapter mAdapter;

    private View mMainView;

    private TextView mNowData;

    private SwipeRefreshLayout mLayout;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.attainment));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        new Thread(() -> {
            if (NetworkManager.getInstance().getAssignmentRanking()) {
                mAdapter.list = new Select().from(Attainment.class).execute();
            } else {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                String attainmentDataBackup = sharedPref.getString(getString(R.string.attainmentData), "no_data_stored");
                mAdapter.list = new ArrayList<Attainment>();
                String[] attainmentData = attainmentDataBackup.split("----");
                for (String data : attainmentData) {
                    String[] attainment = data.split(";");
                    if(attainment.length == 4)
                        mAdapter.list.add(new Attainment(attainment[0], attainment[1], attainment[2], attainment[3]));
                }
            }

            String attainmentDataBackup = "";
            for (int i = 0; i < mAdapter.list.size(); i++) {
                Attainment attainment = mAdapter.list.get(i);

                if (attainment.percent.length() > 1
                        && Integer.parseInt(attainment.percent.substring(0, attainment.percent.length() - 1)) == 0) {
                    mAdapter.list.remove(i);
                }

                attainmentDataBackup += mAdapter.list.get(i).id + ";"
                        + mAdapter.list.get(i).date + ";"
                        + mAdapter.list.get(i).module + ";"
                        + mAdapter.list.get(i).percent + "----";
            }

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.attainmentData), attainmentDataBackup);
            editor.commit();

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                if (mAdapter.list != null && mAdapter.list.size() > 0) {
                    mNowData.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);
                } else {
                    mNowData.setVisibility(View.VISIBLE);
                    mLayout.setVisibility(View.GONE);
                }

                mAdapter.notifyDataSetChanged();
            });
        }).

                start();

        XApiManager.getInstance().

                sendLogActivityEvent(LogActivityEvent.NavigateAttainment);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.stats_attainment, container, false);
        mLayout = (SwipeRefreshLayout) mMainView.findViewById(R.id.stats_swipe_refresh);
        mLayout.setColorSchemeResources(R.color.colorPrimary);

        mNowData = (TextView) mMainView.findViewById(R.id.no_data);
        mNowData.setTypeface(DataManager.getInstance().myriadpro_regular);

        mLayout.setOnRefreshListener(() -> new Thread(() -> {
            if (NetworkManager.getInstance().getAssignmentRanking()) {
                mAdapter.list = new Select().from(Attainment.class).execute();
            } else {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                String attainmentDataBackup = sharedPref.getString(getString(R.string.attainmentData), "no_data_stored");
                mAdapter.list = new ArrayList<Attainment>();
                String[] attainmentData = attainmentDataBackup.split("----");
                for (String data : attainmentData) {
                    String[] attainment = data.split(";");
                    mAdapter.list.add(new Attainment(attainment[0], attainment[1], attainment[2], attainment[3]));
                }
            }

            if (mAdapter.list.size() > 0) {
                String attainmentDataBackup = "";
                for (int i = 0; i < mAdapter.list.size(); i++) {
                    if (mAdapter.list.get(i).percent != null && !mAdapter.list.get(i).percent.isEmpty()) {
                        String stringIndex = mAdapter.list.get(i).percent.substring(0, mAdapter.list.get(i).percent.length() - 1);

                        try {
                            if (stringIndex != null && !stringIndex.isEmpty()) {
                                int index = Integer.parseInt(stringIndex);

                                if (index == 0) {
                                    mAdapter.list.remove(i);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        attainmentDataBackup += mAdapter.list.get(i).id + ";"
                                + mAdapter.list.get(i).date + ";"
                                + mAdapter.list.get(i).module + ";"
                                + mAdapter.list.get(i).percent + "----";
                    }
                }
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.attainmentData), attainmentDataBackup);
                editor.commit();
            }

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
                call_refresh();
            });
        }).start());

        mList = (ListView) mMainView.findViewById(R.id.list);
        mList.setOnTouchListener((v, event) -> {
            // Setting on Touch Listener for handling the touch inside ScrollView
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        mAdapter = new AttainmentAdapter(DataManager.getInstance().mainActivity);
        mList.setAdapter(mAdapter);

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

        return mMainView;
    }

    private void call_refresh() {
        mLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
