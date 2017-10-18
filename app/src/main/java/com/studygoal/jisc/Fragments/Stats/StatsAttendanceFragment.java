package com.studygoal.jisc.Fragments.Stats;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.EventsAttendedAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Adapters.ModuleAdapter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsAttendanceFragment extends BaseFragment {
    private static final String TAG = StatsAttendanceFragment.class.getSimpleName();

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
    private TextView moduleFilterSummary;
    private TextView moduleFilterAll;

    private TextView startDateSummary;
    private TextView endDateSummary;
    private TextView startDateAll;
    private TextView endDateAll;
    private Calendar startDatePicked = Calendar.getInstance();
    private Calendar endDatePicked = Calendar.getInstance();
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DatePickerDialog.OnDateSetListener datePickerEnd;
    private DatePickerDialog.OnDateSetListener datePickerStart;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.attendance_menu));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateEventsAttended);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.layout_stats_attendance, container, false);
        adapter = new EventsAttendedAdapter(getContext());

        setUpDatePicker();

        DatePickerDialog startDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerStart, startDatePicked
                .get(Calendar.YEAR), startDatePicked.get(Calendar.MONTH),
                startDatePicked.get(Calendar.DAY_OF_MONTH));

        startDateSummary = (TextView) mainView.findViewById(R.id.attendance_start_summary);
        startDateSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateDatePickerDialog.show();
            }
        });

        if(!DataManager.getInstance().mainActivity.isLandscape) {
            startDateAll = (TextView) mainView.findViewById(R.id.attendance_start_all);
            startDateAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startDateDatePickerDialog.show();
                }
            });
        }

        DatePickerDialog endDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerEnd, endDatePicked
                .get(Calendar.YEAR), endDatePicked.get(Calendar.MONTH),
                endDatePicked.get(Calendar.DAY_OF_MONTH));

        endDateSummary = (TextView) mainView.findViewById(R.id.attendance_end_summary);
        endDateSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDateDatePickerDialog.show();
            }
        });

        if(!DataManager.getInstance().mainActivity.isLandscape) {
            endDateAll = (TextView) mainView.findViewById(R.id.attendance_end_all);
            endDateAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    endDateDatePickerDialog.show();
                }
            });
        }
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

        listView.setEmptyView(mainView.findViewById(R.id.events_attended_list_emptyView));

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

        if(!DataManager.getInstance().mainActivity.isLandscape) {
            summary = (TextView) mainView.findViewById(R.id.segment_button_attendance_summary);
            all = (TextView) mainView.findViewById(R.id.segment_button_all_events);

            viewFlipper = (ViewFlipper) mainView.findViewById(R.id.viewFlipperEvents);

            ArrayList<TextView> segments = new ArrayList<>();
            segments.add(summary);
            segments.add(all);

            segmentClickListener = new SegmentClickListener(viewFlipper, segments, getContext(), 0);
            summary.setOnClickListener(segmentClickListener);
            all.setOnClickListener(segmentClickListener);
        }

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

        moduleFilterSummary = (TextView) mainView.findViewById(R.id.attendance_module_filter_summary);
        moduleFilterSummary.setOnClickListener(v -> {
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
            listView1.setAdapter(new ModuleAdapter(DataManager.getInstance().mainActivity, moduleFilterSummary.getText().toString()));
            listView1.setOnItemClickListener((parent, view, position, id) -> {
                String titleText = ((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString();

                dialog.dismiss();
                if (!titleText.equals("All Activity")) {
                    moduleFilterSummary.setText(titleText);
                    if(!DataManager.getInstance().mainActivity.isLandscape) {
                        moduleFilterAll.setText(titleText);
                    }
                } else {
                    moduleFilterSummary.setText(R.string.filter_modules);
                    if(!DataManager.getInstance().mainActivity.isLandscape) {
                        moduleFilterAll.setText(R.string.filter_modules);
                    }
                }
                ((MainActivity) getActivity()).hideProgressBar();
                new Thread(() -> {
                    loadData(0, PAGE_SIZE * 2, true);
                    runOnUiThread(() -> {
                        adapter.updateList(events);
                        adapter.notifyDataSetChanged();
                        ((MainActivity) getActivity()).hideProgressBar();
                        isLoading = false;
                    });
                }).start();
            });
            ((MainActivity) getActivity()).showProgressBar2("");
            dialog.show();
        });

        if(!DataManager.getInstance().mainActivity.isLandscape) {
            moduleFilterAll = (TextView) mainView.findViewById(R.id.attendance_module_filter_all);
            moduleFilterAll.setOnClickListener(v -> {
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
                listView1.setAdapter(new ModuleAdapter(DataManager.getInstance().mainActivity, moduleFilterSummary.getText().toString()));
                listView1.setOnItemClickListener((parent, view, position, id) -> {
                    String titleText = ((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString();

                    dialog.dismiss();
                    if (!titleText.equals("All Activity")) {
                        moduleFilterSummary.setText(titleText);
                        moduleFilterAll.setText(titleText);
                    } else {
                        moduleFilterSummary.setText(R.string.filter_modules);
                        moduleFilterAll.setText(R.string.filter_modules);
                    }
                    ((MainActivity) getActivity()).hideProgressBar();
                    new Thread(() -> {
                        loadData(0, PAGE_SIZE * 2, true);
                        runOnUiThread(() -> {
                            adapter.updateList(events);
                            adapter.notifyDataSetChanged();
                            ((MainActivity) getActivity()).hideProgressBar();
                            isLoading = false;
                        });
                    }).start();
                });
                ((MainActivity) getActivity()).showProgressBar2("");
                dialog.show();
            });
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
        if(dates.size() == 0){
            if(!DataManager.getInstance().mainActivity.isLandscape) {
                mainView.findViewById(R.id.events_attended_graph_emptyView).setVisibility(View.VISIBLE);
            }
            webView.setVisibility(View.GONE);
        } else {
            mainView.findViewById(R.id.events_attended_graph_emptyView).setVisibility(View.GONE);
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

    private void setUpDatePicker() {
        datePickerStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                startDatePicked.set(Calendar.YEAR, year);
                startDatePicked.set(Calendar.MONTH, monthOfYear);
                startDatePicked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                startDateSummary.setText(dateFormat.format(startDatePicked.getTime()));
                if(!DataManager.getInstance().mainActivity.isLandscape) {
                    startDateAll.setText(dateFormat.format(startDatePicked.getTime()));
                }
                if (startDatePicked.after(Calendar.getInstance())) {
                    startDatePicked = Calendar.getInstance();
                    Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.start_date_in_future_hint, Snackbar.LENGTH_LONG).show();
                    startDateSummary.setText("Start");
                    if(!DataManager.getInstance().mainActivity.isLandscape) {
                        startDateAll.setText("Start");
                    }
                    return;
                }
                if (!endDateSummary.getText().toString().equals("End")) {
                    if (startDatePicked.after(endDatePicked)) {
                        startDatePicked = Calendar.getInstance();
                        Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.start_date_after_end_date_hint, Snackbar.LENGTH_LONG).show();
                        startDateSummary.setText("Start");
                        if(!DataManager.getInstance().mainActivity.isLandscape) {
                            startDateAll.setText("Start");
                        }
                    } else {
                        new Thread(() -> {
                            loadData(0, PAGE_SIZE * 2, true);
                            runOnUiThread(() -> {
                                adapter.updateList(events);
                                adapter.notifyDataSetChanged();
                                ((MainActivity) getActivity()).hideProgressBar();
                                isLoading = false;
                            });
                        }).start();
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
                endDateSummary.setText(dateFormat.format(endDatePicked.getTime()));
                if(!DataManager.getInstance().mainActivity.isLandscape) {
                    endDateAll.setText(dateFormat.format(endDatePicked.getTime()));
                }
                if (endDatePicked.after(Calendar.getInstance())) {
                    endDatePicked = Calendar.getInstance();
                    Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.end_date_in_future_hint, Snackbar.LENGTH_LONG).show();
                    endDateSummary.setText("End");
                    if(!DataManager.getInstance().mainActivity.isLandscape) {
                        endDateAll.setText("End");
                    }
                    return;
                }
                if (!endDateSummary.getText().toString().equals("Start")) {
                    if (endDatePicked.before(startDatePicked)) {
                        endDatePicked = Calendar.getInstance();
                        Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.end_date_before_start_date_hint, Snackbar.LENGTH_LONG).show();
                        endDateSummary.setText("End");
                        if(!DataManager.getInstance().mainActivity.isLandscape) {
                            endDateAll.setText("End");
                        }
                    } else {
                        new Thread(() -> {
                            loadData(0, PAGE_SIZE * 2, true);
                            runOnUiThread(() -> {
                                adapter.updateList(events);
                                adapter.notifyDataSetChanged();
                                ((MainActivity) getActivity()).hideProgressBar();
                                isLoading = false;
                            });
                        }).start();
                    }
                }
            }
        };
    }
}
