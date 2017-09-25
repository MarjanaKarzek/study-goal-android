package com.studygoal.jisc.Fragments.Stats;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.ED;
import com.studygoal.jisc.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StatsAttedanceFragment extends Fragment {

    public LineChart lineChart;
    public BarChart barchart;
    AppCompatTextView module;
    WebView mWebView;

    RelativeLayout chart_layout;
    List<ED> list;
    String selectedPeriod;

    ArrayList<String> dates = new ArrayList<>();
    ArrayList<String> count = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.attendance));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.stats_attendance, container, false);


        mWebView = (WebView) mainView.findViewById(R.id.webview_graph);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            Log.e(getClass().getCanonicalName(), "attendance: " + preferences.getString(getString(R.string.attendance), null));
            JSONArray jsonArray = new JSONArray(preferences.getString(getString(R.string.attendance), null));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String[] dateinfo = jsonObject.getString("date").substring(0, 10).split("-");
                String date = dateinfo[2]+ "/" + dateinfo[1];
                dates.add(date);
                count.add(jsonObject.getString("count"));
                Log.e(dates.get(i), count.get(i));
            }

        } catch (Exception je) {
            je.printStackTrace();
        }

        loadWebView();
        return mainView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        WebSettings s = mWebView.getSettings();
        s.setJavaScriptEnabled(true);

        try {
            InputStream is = getContext().getAssets().open("stats_attendance_high_chart.html");
            int size = 0;
            size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    double d = getActivity().getResources().getDisplayMetrics().density;
                    int h = (int) (mWebView.getHeight() / d) - 20;
                    int w = (int) (mWebView.getWidth() / d) - 20;

                    String dataCount = "";
                    String dataDate = "";
                    for (int i = 0; i < dates.size(); i++) {
                        dataCount += "" + count.get(i) + ", ";
                        dataDate += "'" + dates.get(i) + "', \n";
//                        data += "},";
                    }


                    String rawhtml = new String(buffer);
                    rawhtml = rawhtml.replace("280px", w + "px");
                    rawhtml = rawhtml.replace("220px", h + "px");
                    rawhtml = rawhtml.replace("DATA", dataCount);
                    rawhtml = rawhtml.replace("DATES", dataDate);
                    mWebView.loadDataWithBaseURL("", rawhtml, "text/html", "UTF-8", "");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
