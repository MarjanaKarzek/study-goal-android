package com.studygoal.jisc.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.EventsAttendedAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Event;
import com.studygoal.jisc.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StatsEventAttendance extends BaseFragment {
    private static final String TAG = StatsEventAttendance.class.getSimpleName();

    private static final int PAGE_SIZE = 10;

    private ListView mListView;
    private int mPreviousLast;
    private EventsAttendedAdapter mAdapter;
    private ArrayList<Event> mEvents = new ArrayList<>();
    private boolean mIsLoading = false;

    private WebView mWebView;
    ArrayList<String> dates = new ArrayList<>();
    ArrayList<String> count = new ArrayList<>();

    private View mainView;
    private boolean allSelected = true;
    private TextView all;
    private TextView summary;
    private Switch viewSwitch;
    private ViewFlipper viewFlipper;

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
        mainView = inflater.inflate(R.layout.stats_event_attendance, container, false);
        mAdapter = new EventsAttendedAdapter(getContext());

        mListView = (ListView) mainView.findViewById(R.id.event_attendance_listView);
        //LayoutInflater inflater2 = getActivity().getLayoutInflater();
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if (lastItem == totalItemCount) {
                    if (mPreviousLast != lastItem) {
                        //to avoid multiple calls for last item
                        if (!mIsLoading) {
                            new Thread(() -> {
                                if (!mIsLoading) {
                                    mPreviousLast = lastItem;
                                    mIsLoading = true;
                                    loadData(lastItem, PAGE_SIZE, false);
                                    runOnUiThread(() -> {
                                        mAdapter.updateList(mEvents);
                                        mAdapter.notifyDataSetChanged();
                                        ((MainActivity) getActivity()).hideProgressBar();
                                        mIsLoading = false;
                                    });
                                }
                            }).start();
                        }
                    }
                }
            }
        });

        mListView.setOnItemClickListener((adapterView, view, i12, l) -> {
            return;
        });

        ((MainActivity) getActivity()).showProgressBar(null);

        new Thread(() -> {
            if (!mIsLoading) {
                mIsLoading = true;
                loadData(0, PAGE_SIZE * 2, true);
                runOnUiThread(() -> {
                    mAdapter.updateList(mEvents);
                    mAdapter.notifyDataSetChanged();
                    ((MainActivity) getActivity()).hideProgressBar();
                    mIsLoading = false;
                });
            }
        }).start();

        all = (TextView) mainView.findViewById(R.id.segment_button_all_events);
        summary = (TextView) mainView.findViewById(R.id.segment_button_attendance_summary);

        SegmentClickListener l = new SegmentClickListener();
        all.setOnClickListener(l);
        summary.setOnClickListener(l);

        viewFlipper = (ViewFlipper) mainView.findViewById(R.id.viewFlipperEvents);

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

    private void loadData(int skip, int limit, boolean reset) {
        if (XApiManager.getInstance().getAttendance(skip, limit, reset)) {
            mEvents.clear();
            List<Event> events = new Select().from(Event.class).execute();
            mEvents.addAll(events);
        }
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

    private class SegmentClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            allSelected = !allSelected;

            if (allSelected) {
                Drawable activeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.round_corners_segmented_active);
                all.setBackground(activeDrawable);
                all.setTextColor(Color.WHITE);

                summary.setBackground(null);
                summary.setBackgroundColor(Color.TRANSPARENT);
                summary.setTextColor(Color.parseColor("#3792ef"));
                viewFlipper.showNext();
            } else {
                Drawable activeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.round_corners_segmented_active_right);
                summary.setBackground(activeDrawable);
                summary.setTextColor(Color.WHITE);

                all.setBackground(null);
                all.setBackgroundColor(Color.TRANSPARENT);
                all.setTextColor(Color.parseColor("#3792ef"));
                viewFlipper.showPrevious();
            }
        }
    }
}
