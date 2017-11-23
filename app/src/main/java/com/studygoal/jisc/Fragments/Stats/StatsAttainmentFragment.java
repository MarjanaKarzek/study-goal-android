package com.studygoal.jisc.Fragments.Stats;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Adapters.AttainmentAdapter;
import com.studygoal.jisc.Adapters.ModuleAdapter;
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
    private TextView noData;
    private TextView moduleFilter;
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
            }

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateAttainment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.layout_stats_attainment, container, false);
        layout = (SwipeRefreshLayout) mainview.findViewById(R.id.stats_swipe_refresh);
        layout.setColorSchemeResources(R.color.colorPrimary);

        noData = (TextView) mainview.findViewById(R.id.no_data);
        noData.setTypeface(DataManager.getInstance().myriadpro_regular);

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
        listView.setEmptyView(noData);
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

        /* used in future api update
        moduleFilter = (TextView) mainview.findViewById(R.id.module_filter);
        moduleFilter.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.snippet_custom_spinner);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(dialog13 -> {
                dialog13.dismiss();
                ((MainActivity) getActivity()).hideProgressBar();
            });

            if (DataManager.getInstance().mainActivity.isLandscape) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = (int) (displaymetrics.widthPixels * 0.3);

                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = width;
                dialog.getWindow().setAttributes(params);
            }

            ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.filter_modules);

            final ListView listView1 = (ListView) dialog.findViewById(R.id.dialog_listview);
            listView1.setAdapter(new ModuleAdapter(DataManager.getInstance().mainActivity, moduleFilter.getText().toString()));
            listView1.setOnItemClickListener((parent, view, position, id) -> {
                String titleText = ((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString();

                dialog.dismiss();
                if (!titleText.equals("All Activity")) {
                    moduleFilter.setText(titleText);
                } else {
                    moduleFilter.setText(R.string.filter_modules);
                }
                ((MainActivity) getActivity()).hideProgressBar();
                //loadData();
            });
            ((MainActivity) getActivity()).showProgressBar2("");
            dialog.show();
        });*/

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
