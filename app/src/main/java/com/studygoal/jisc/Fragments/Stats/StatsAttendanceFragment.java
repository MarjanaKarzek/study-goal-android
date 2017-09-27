package com.studygoal.jisc.Fragments.Stats;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StatsAttendanceFragment extends Fragment {
    private static final String TAG = StatsAttendanceFragment.class.getSimpleName();

    private WebView webView;

    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<String> count = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.attendance));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.layout_stats_attendance, container, false);

        webView = (WebView) mainView.findViewById(R.id.webview_graph);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setOnTouchListener(new View.OnTouchListener() {
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
                String[] dateInfo = jsonObject.getString("date").substring(0, 10).split("-");
                String date = dateInfo[2]+ "/" + dateInfo[1];
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
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);

        try {
            InputStream is = getContext().getAssets().open("stats_attendance_high_chart.html");
            int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            webView.post(new Runnable() {
                @Override
                public void run() {
                    double density = getActivity().getResources().getDisplayMetrics().density;
                    int height = (int) (webView.getHeight() / density) - 20;
                    int width = (int) (webView.getWidth() / density) - 20;

                    String dataCount = "";
                    String dataDate = "";
                    for (int i = 0; i < dates.size(); i++) {
                        dataCount += "" + count.get(i) + ", ";
                        dataDate += "'" + dates.get(i) + "', \n";
                    }


                    String rawHTML = new String(buffer);
                    rawHTML = rawHTML.replace("280px", width + "px");
                    rawHTML = rawHTML.replace("220px", height + "px");
                    rawHTML = rawHTML.replace("DATA", dataCount);
                    rawHTML = rawHTML.replace("DATES", dataDate);
                    webView.loadDataWithBaseURL("", rawHTML, "text/html", "UTF-8", "");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
