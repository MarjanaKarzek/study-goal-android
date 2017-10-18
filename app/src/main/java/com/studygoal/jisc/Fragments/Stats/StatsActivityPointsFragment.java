package com.studygoal.jisc.Fragments.Stats;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.studygoal.jisc.Adapters.ActivityPointsAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Adapters.ModuleAdapter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class StatsActivityPointsFragment extends BaseFragment {
    private static final String TAG = StatsActivityPointsFragment.class.getSimpleName();

    private View mainView;
    private WebView piChartWebView;
    private ViewFlipper viewFlipper;
    private TextView activityPointsValue;
    private ActivityPointsAdapter adapter;
    private SegmentClickListener segmentClickListener;
    private TextView emptyView;

    private boolean isThisWeek = true;
    private TextView segmentButtonSummary;
    private TextView segmentButtonChart;

    private TextView startDate;
    private TextView endDate;
    private Calendar startDatePicked = Calendar.getInstance();
    private Calendar endDatePicked = Calendar.getInstance();
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DatePickerDialog.OnDateSetListener datePickerEnd;
    private DatePickerDialog.OnDateSetListener datePickerStart;

    private TextView moduleFilter;

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
        activityPointsValue = (TextView) mainView.findViewById(R.id.activity_points_value);
        piChartWebView = (WebView) mainView.findViewById(R.id.pi_chart_web_view);
        piChartWebView.setVisibility(View.INVISIBLE);

        ListView activityPointsListView = (ListView) mainView.findViewById(R.id.activity_points_list_view);
        adapter = new ActivityPointsAdapter(getContext());
        activityPointsListView.setAdapter(adapter);
        emptyView = (TextView) mainView.findViewById(R.id.points_list_emptyView);
        emptyView.setVisibility(View.GONE);

        if(!DataManager.getInstance().mainActivity.isLandscape) {
            viewFlipper = (ViewFlipper) mainView.findViewById(R.id.activity_points_container);
            segmentButtonSummary = (TextView) mainView.findViewById(R.id.segment_button_summary);
            segmentButtonChart = (TextView) mainView.findViewById(R.id.segment_button_chart);

            ArrayList<TextView> segments = new ArrayList<>();
            segments.add(segmentButtonSummary);
            segments.add(segmentButtonChart);

            segmentClickListener = new SegmentClickListener(viewFlipper, segments, getContext(), 0) {
                @Override
                public void onClick(View view) {
                    super.onClick(view);
                    isThisWeek = !isThisWeek;
                    callRefresh();
                }
            };

            segmentButtonSummary.setOnClickListener(segmentClickListener);
            segmentButtonChart.setOnClickListener(segmentClickListener);
        }
        showAlertDialog();
        setUpDatePicker();

        DatePickerDialog startDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerStart, startDatePicked
                .get(Calendar.YEAR), startDatePicked.get(Calendar.MONTH),
                startDatePicked.get(Calendar.DAY_OF_MONTH));

        startDate = (TextView) mainView.findViewById(R.id.activity_points_start);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateDatePickerDialog.show();
            }
        });

        DatePickerDialog endDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerEnd, endDatePicked
                .get(Calendar.YEAR), endDatePicked.get(Calendar.MONTH),
                endDatePicked.get(Calendar.DAY_OF_MONTH));

        endDate = (TextView) mainView.findViewById(R.id.activity_points_end);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDateDatePickerDialog.show();
            }
        });

        moduleFilter = (TextView) mainView.findViewById(R.id.activity_points_module_filter);
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
                callRefresh();
                loadWebView();
            });
            ((MainActivity) getActivity()).showProgressBar2("");
            dialog.show();
        });

        return mainView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
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

        if(sum == 0){
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        activityPointsValue.setText(String.valueOf(sum));
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

    private void setUpDatePicker() {
        datePickerStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                startDatePicked.set(Calendar.YEAR, year);
                startDatePicked.set(Calendar.MONTH, monthOfYear);
                startDatePicked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                startDate.setText(dateFormat.format(startDatePicked.getTime()));
                if (startDatePicked.after(Calendar.getInstance())) {
                    startDatePicked = Calendar.getInstance();
                    Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.start_date_in_future_hint, Snackbar.LENGTH_LONG).show();
                    startDate.setText("Start");
                    return;
                }
                if (!endDate.getText().toString().equals("End")) {
                    if (startDatePicked.after(endDatePicked)) {
                        startDatePicked = Calendar.getInstance();
                        Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.start_date_after_end_date_hint, Snackbar.LENGTH_LONG).show();
                        startDate.setText("Start");
                    } else {
                        callRefresh();
                        loadWebView();
                    }
                }
            }
        };

        datePickerEnd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                endDatePicked.set(Calendar.YEAR, year);
                endDatePicked.set(Calendar.MONTH, monthOfYear);
                endDatePicked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                endDate.setText(dateFormat.format(endDatePicked.getTime()));
                if (endDatePicked.after(Calendar.getInstance())) {
                    endDatePicked = Calendar.getInstance();
                    Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.end_date_in_future_hint, Snackbar.LENGTH_LONG).show();
                    endDate.setText("End");
                    return;
                }
                if (!endDate.getText().toString().equals("Start")) {
                    if (endDatePicked.before(startDatePicked)) {
                        endDatePicked = Calendar.getInstance();
                        Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.end_date_before_start_date_hint, Snackbar.LENGTH_LONG).show();
                        endDate.setText("End");
                    } else {
                        callRefresh();
                        loadWebView();
                    }
                }
            }
        };
    }
}
