package com.studygoal.jisc.Fragments.Stats;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.EventsAttendedAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Fragments.BaseFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Event;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.SegmentController.SegmentClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StatsEventAttendanceFragment extends BaseFragment {
    private static final String TAG = StatsEventAttendanceFragment.class.getSimpleName();

    private static final int PAGE_SIZE = 10;

    private ListView listView;
    private int previousLast;
    private EventsAttendedAdapter adapter;
    private ArrayList<Event> events = new ArrayList<>();
    private boolean isLoading = false;

    private WebView webView;
    ArrayList<String> dates = new ArrayList<>();
    ArrayList<String> count = new ArrayList<>();

    private View mainView;
    private TextView all;
    private TextView summary;
    private ViewFlipper viewFlipper;
    private SegmentClickListener segmentClickListener;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.events_attended));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateEventsAttended);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.layout_stats_event_attendance, container, false);
        adapter = new EventsAttendedAdapter(getContext());

        listView = (ListView) mainView.findViewById(R.id.event_attendance_listView);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if (lastItem == totalItemCount) {
                    if (previousLast != lastItem) {
                        //to avoid multiple calls for last item
                        if (!isLoading) {
                            new Thread(() -> {
                                if (!isLoading) {
                                    previousLast = lastItem;
                                    isLoading = true;
                                    loadData(lastItem, PAGE_SIZE, false);
                                    runOnUiThread(() -> {
                                        adapter.updateList(events);
                                        adapter.notifyDataSetChanged();
                                        ((MainActivity) getActivity()).hideProgressBar();
                                        isLoading = false;
                                    });
                                }
                            }).start();
                        }
                    }
                }
            }
        });

        listView.setOnItemClickListener((adapterView, view, i12, l) -> {
            return;
        });

        ((MainActivity) getActivity()).showProgressBar(null);

        new Thread(() -> {
            if (!isLoading) {
                isLoading = true;
                loadData(0, PAGE_SIZE * 2, true);
                runOnUiThread(() -> {
                    adapter.updateList(events);
                    adapter.notifyDataSetChanged();
                    ((MainActivity) getActivity()).hideProgressBar();
                    isLoading = false;
                });
            }
        }).start();

        all = (TextView) mainView.findViewById(R.id.segment_button_all_events);
        summary = (TextView) mainView.findViewById(R.id.segment_button_attendance_summary);

        viewFlipper = (ViewFlipper) mainView.findViewById(R.id.viewFlipperEvents);

        ArrayList<TextView> segments = new ArrayList<>();
        segments.add(all);
        segments.add(summary);

        segmentClickListener = new SegmentClickListener(viewFlipper,segments,getContext(),0);
        all.setOnClickListener(segmentClickListener);
        summary.setOnClickListener(segmentClickListener);

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

    private void loadData(int skip, int limit, boolean reset) {
        if (XApiManager.getInstance().getAttendance(skip, limit, reset)) {
            events.clear();
            List<Event> events = new Select().from(Event.class).execute();
            this.events.addAll(events);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);

        try {
            InputStream is = getContext().getAssets().open("stats_attendance_high_chart.html");
            int size = 0;
            size = is.available();
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
