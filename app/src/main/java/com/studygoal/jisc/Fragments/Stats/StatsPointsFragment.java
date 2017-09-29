package com.studygoal.jisc.Fragments.Stats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.studygoal.jisc.Adapters.ActivityPointsAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Fragments.BaseFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.ActivityPoints;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.SegmentController.SegmentClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StatsPointsFragment extends BaseFragment {
    private static final String TAG = StatsPointsFragment.class.getSimpleName();

    private View mainView;
    private WebView piChartWebView;
    private ViewFlipper upperContainer;
    private TextView activityPointsValueWeek;
    private TextView activityPointsValueAll;
    private Switch pieChartSwitch;
    private ActivityPointsAdapter adapter;
    private SegmentClickListener segmentClickListener;

    private boolean isThisWeek = true;
    private TextView segmentButtonThisWeek;
    private TextView segmentButtonOverall;

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = DataManager.getInstance().mainActivity;
        mainActivity.setTitle(getString(R.string.points));
        mainActivity.hideAllButtons();
        mainActivity.showCertainButtons(5);

        refreshView();

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigatePoints);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.layout_stats_points, container, false);
        activityPointsValueWeek = (TextView) mainView.findViewById(R.id.activity_points_value);
        activityPointsValueAll = (TextView) mainView.findViewById(R.id.activity_points_value2);
        piChartWebView = (WebView) mainView.findViewById(R.id.pi_chart_web_view);
        piChartWebView.setVisibility(View.INVISIBLE);
        upperContainer = (ViewFlipper) mainView.findViewById(R.id.activity_points_container);
        pieChartSwitch = (Switch) mainView.findViewById(R.id.pie_chart_switch);
        pieChartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            upperContainer.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
            piChartWebView.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        });

        ListView activityPointsListView = (ListView) mainView.findViewById(R.id.activity_points_list_view);
        adapter = new ActivityPointsAdapter(getContext());
        activityPointsListView.setAdapter(adapter);

        segmentButtonThisWeek = (TextView) mainView.findViewById(R.id.segment_button_this_week);
        segmentButtonOverall = (TextView) mainView.findViewById(R.id.segment_button_overall);

        ArrayList<TextView> segments = new ArrayList<>();
        segments.add(segmentButtonThisWeek);
        segments.add(segmentButtonOverall);

        segmentClickListener = new SegmentClickListener(upperContainer, segments, getContext(), 0){
            @Override
            public void onClick(View view){
                super.onClick(view);
                isThisWeek = !isThisWeek;
                callRefresh();
            }
        };

        segmentButtonThisWeek.setOnClickListener(segmentClickListener);
        segmentButtonOverall.setOnClickListener(segmentClickListener);

        showAlertDialog();

        return mainView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        if(adapter.list == null || adapter.list.size() == 0){
            mainView.findViewById(R.id.points_list_emptyView).setVisibility(View.VISIBLE);
        }else{
            mainView.findViewById(R.id.points_list_emptyView).setVisibility(View.GONE);
        }
        WebSettings s = piChartWebView.getSettings();
        s.setJavaScriptEnabled(true);

        try {
            InputStream is = getContext().getAssets().open("stats_points_pi_chart.html");
            int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            piChartWebView.post(() -> {
                String pointsDataBackup = "";
                double density = getActivity().getResources().getDisplayMetrics().density;
                int height = (int) (piChartWebView.getHeight() / density) - 20;
                int width = (int) (piChartWebView.getWidth() / density) - 20;

                String data = "";
                for (ActivityPoints p : DataManager.getInstance().user.points) {
                    data += "{";
                    if (p.activity.equals("Loggedin"))
                        data += "name:" + "\'Logged in\',";
                    else
                        data += "name:" + "\'" + p.activity + "\',";
                    data += "y:" + p.points;
                    data += "},";
                    pointsDataBackup += p.activity + ";"
                            + p.points + ";"
                            + p.id + ";"
                            + p.key + "----";
                }

                String rawhtml = new String(buffer);
                rawhtml = rawhtml.replace("280px", width + "px");
                rawhtml = rawhtml.replace("220px", height + "px");
                rawhtml = rawhtml.replace("REPLACE_DATA", data);
                piChartWebView.loadDataWithBaseURL("", rawhtml, "text/html", "UTF-8", "");

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.pointsData), pointsDataBackup);
                editor.commit();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshView() {
        DataManager.getInstance().mainActivity.showProgressBar(null);

        new Thread(() -> {
            if (!NetworkManager.getInstance().getStudentActivityPoint(isThisWeek ? "7d" : "overall")) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                String pointsDataBackup = sharedPref.getString(getString(R.string.pointsData), "no_data_stored");
                String[] pointsData = pointsDataBackup.split("----");
                DataManager.getInstance().user.points.clear();
                for (String data : pointsData) {
                    String[] point = data.split(";");
                    DataManager.getInstance().user.points.add(new ActivityPoints(point[0], point[1], point[2], point[3]));
                }
            }

            runOnUiThread(() -> {
                callRefresh();
                loadWebView();
            });
        }).start();
    }

    private void callRefresh() {
        DataManager.getInstance().mainActivity.hideProgressBar();
        adapter.notifyDataSetChanged();

        int sum = 0;
        for (ActivityPoints p : DataManager.getInstance().user.points) {
            sum += Integer.parseInt(p.points);
        }

        Log.d("", "callRefresh: isThisWeek " + isThisWeek);

        if(isThisWeek)
            activityPointsValueWeek.setText(String.valueOf(sum));
        else
            activityPointsValueAll.setText(String.valueOf(sum));

    }

    private void showAlertDialog() {
        final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean isStaff = DataManager.getInstance().user.isStaff;
        boolean isStatsAlert = preferences.getBoolean("stats_alert", true);

        if (isStaff && isStatsAlert) {
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage(R.string.statistics_admin_view);
            alertDialogBuilder.setPositiveButton("Don't show again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("stats_alert", false);
                    editor.apply();
                }
            });
            alertDialogBuilder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}
