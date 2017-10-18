package com.studygoal.jisc.Fragments.Stats;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.GenericAdapter;
import com.studygoal.jisc.Adapters.ModuleAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.Courses;
import com.studygoal.jisc.Models.ED;
import com.studygoal.jisc.Models.Friend;
import com.studygoal.jisc.Models.Module;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.SegmentController.SegmentClickListener;
import com.studygoal.jisc.Utils.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class StatsVLEActivityFragment extends Fragment {
    private static final String TAG = StatsVLEActivityFragment.class.getSimpleName();

    private View mainView;
    private TextView moduleFilter;
    private TextView compareTo;
    private WebView webView;

    private List<ED> list;
    private int[] offlineDemoData = {22,0,0,21,4,5,23,6,16,10,3,4,6,1,7,0,0,0,0,3,5,7,12,24,1,0,0,12,13,21};
    private float webViewHeight;
    private boolean isBar = true;
    private boolean isSevenDays = true;
    private boolean isOverall = false;

    private SegmentClickListener segmentClickListener;
    private TextView segmentButtonBarGraph;
    private TextView segmentButtonLineGraph;

    private TextView startDate;
    private TextView endDate;
    private Calendar startDatePicked = Calendar.getInstance();
    private Calendar endDatePicked = Calendar.getInstance();
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DatePickerDialog.OnDateSetListener datePickerEnd;
    private DatePickerDialog.OnDateSetListener datePickerStart;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.engagement_graph));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.layout_stats_vle_activity, container, false);

        webView = (WebView) mainView.findViewById(R.id.chart_web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setPadding(0, 0, 0, 0);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        webView.getViewTreeObserver().addOnGlobalLayoutListener(() -> webViewHeight = Utils.pxToDp(webView.getHeight() - 40));

        webView.loadDataWithBaseURL("", "<html><head></head><body><div style=\"height:100%;width:100%;background:white;\"></div></body></html>", "text/html", "UTF-8", "");

        setUpDatePicker();

        DatePickerDialog startDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerStart, startDatePicked
                .get(Calendar.YEAR), startDatePicked.get(Calendar.MONTH),
                startDatePicked.get(Calendar.DAY_OF_MONTH));

        startDate = (TextView) mainView.findViewById(R.id.vle_activity_start);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateDatePickerDialog.show();
            }
        });

        DatePickerDialog endDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerEnd, endDatePicked
                .get(Calendar.YEAR), endDatePicked.get(Calendar.MONTH),
                endDatePicked.get(Calendar.DAY_OF_MONTH));

        endDate = (TextView) mainView.findViewById(R.id.vle_activity_end);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDateDatePickerDialog.show();
            }
        });

        moduleFilter = (TextView) mainView.findViewById(R.id.vle_activity_module_filter);

        segmentButtonBarGraph = (TextView) mainView.findViewById(R.id.segment_button_bar_graph);
        segmentButtonLineGraph = (TextView) mainView.findViewById(R.id.segment_button_line_graph);

        ArrayList<TextView> segments = new ArrayList<>();
        segments.add(segmentButtonBarGraph);
        segments.add(segmentButtonLineGraph);

        segmentClickListener = new SegmentClickListener(null, segments, getContext(), 0){
            @Override
            public void onClick(View view){
                loadData();
                isBar = !isBar;
                refreshUi();
            }
        };

        segmentButtonBarGraph.setOnClickListener(segmentClickListener);
        segmentButtonLineGraph.setOnClickListener(segmentClickListener);

        compareTo = (TextView) mainView.findViewById(R.id.activity_points_compare_to);

        final View.OnClickListener compareToListener = v -> {
            if (!moduleFilter.getText().toString().equals(DataManager.getInstance().mainActivity.getString(R.string.anymodule))) {
                final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.snippet_custom_spinner);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(dialog1 -> {
                    dialog1.dismiss();
                    runOnUiThread(() -> ((MainActivity) getActivity()).hideProgressBar());
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
                ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_student);

                ArrayList<String> items = new ArrayList<>();
                items.add(getString(R.string.no_one));
//                    items.add(getString(R.string.top10));
                items.add(getString(R.string.average));
                List<Friend> friendList;
                friendList = new Select().from(Friend.class).execute();
                for (int i = 0; i < friendList.size(); i++)
                    items.add(friendList.get(i).name);
                final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
                listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, compareTo.getText().toString(), items));
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    compareTo.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
                    dialog.dismiss();
                    loadData();
                });

                ((MainActivity) getActivity()).showProgressBar2("");
                dialog.show();
            } else {
                final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.snippet_custom_spinner);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(dialog12 -> {
                    dialog12.dismiss();
                    runOnUiThread(() -> ((MainActivity) getActivity()).hideProgressBar());
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
                ((TextView) dialog.findViewById(R.id.dialog_title)).setText("");

                ArrayList<String> items = new ArrayList<>();
                items.add(getString(R.string.no_one));
//                    items.add(getString(R.string.top10));
//                    items.add(getString(R.string.average));
                List<Friend> friendList;
                friendList = new Select().from(Friend.class).execute();
                for (int i = 0; i < friendList.size(); i++)
                    items.add(friendList.get(i).name);
                final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
                listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, compareTo.getText().toString(), items));
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    compareTo.setText(((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString());
                    dialog.dismiss();
                    loadData();
                });
                ((MainActivity) getActivity()).showProgressBar2("");
                dialog.show();
            }
        };

        //Setting the moduleFilter
        moduleFilter.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.snippet_custom_spinner);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(dialog13 -> {
                dialog13.dismiss();
                runOnUiThread(() -> ((MainActivity) getActivity()).hideProgressBar());
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
                ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_module);

            final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
            listView.setAdapter(new ModuleAdapter(DataManager.getInstance().mainActivity, moduleFilter.getText().toString()));
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String titleText = ((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString();
                List<Courses> coursesList = new Select().from(Courses.class).execute();

                    for (int j = 0; j < coursesList.size(); j++) {
                        String courseName = coursesList.get(j).name;
                        if (courseName.equals(titleText)) {
                            return;
                        }
                    }

                dialog.dismiss();
                moduleFilter.setText(titleText);

                if (!moduleFilter.getText().toString().equals(getString(R.string.anymodule))) {
                    compareTo.setOnClickListener(compareToListener);
                    compareTo.setAlpha(1.0f);
                } else {
                    compareTo.setOnClickListener(null);
                    compareTo.setAlpha(0.5f);
                    compareTo.setText(getString(R.string.compare_to));
                }

                if (moduleFilter.getText().toString().replace(" -", "").equals(getString(R.string.anymodule)) && (compareTo.getText().toString().equals(getString(R.string.average)) || compareTo.getText().toString().equals(getString(R.string.top10)))) {
                    compareTo.setText(R.string.compare_to);
                }

                    loadData();
                });
                ((MainActivity) getActivity()).showProgressBar2("");
                dialog.show();
            });

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            loadData();
        }, 100);

        refreshUi();

        return mainView;
    }

    private void loadData() {
        runOnUiThread(() -> ((MainActivity) getActivity()).showProgressBar2(""));

        new Thread(() -> {
            if (DataManager.getInstance().user.isStaff) {
                list = new ArrayList<>();

                if (compareTo.getText().toString().equals(getString(R.string.compare_to))) {
                    if (isSevenDays) {
                        for (int i = 0; i < 7; i++) {
                            ED item = new ED();
                            item.day = "" + (i + 1);
                            item.activity_points = Math.abs(new Random().nextInt()) % 100;

                            list.add(item);
                        }

                        Collections.sort(list, (s1, s2) -> s1.day.compareToIgnoreCase(s2.day));
                    } else if (!isSevenDays) {

                        for (int i = 0; i < 30; i++) {
                            ED item = new ED();
                            item.day = "" + (i + 1);
                            item.activity_points = Math.abs(new Random().nextInt()) % 100;

                            list.add(item);
                        }

                        Collections.sort(list, (s1, s2) -> s1.day.compareToIgnoreCase(s2.day));
                    } else if (isOverall) {

                        try {

                            Calendar calendar = Calendar.getInstance();

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date startDate = dateFormat.parse("01/01/2017");
                            Date now = new Date();

                            while (now.after(startDate)) {
                                int numberOfDays = Math.abs(new Random().nextInt()) % 5 + 1;
                                calendar.setTime(startDate);
                                calendar.add(Calendar.DATE, numberOfDays);

                                startDate = calendar.getTime();

                                ED item = new ED();
                                item.day = dateFormat.format(startDate);
                                item.realDate = startDate;
                                item.activity_points = Math.abs(new Random().nextInt()) % 100;

                                list.add(item);
                            }

                            Collections.sort(list, (s1, s2) -> s1.realDate.compareTo(s2.realDate));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (isSevenDays) {
                        for (int i = 0; i < 7; i++) {
                            ED item = new ED();
                            item.day = "" + (i + 1);
                            item.activity_points = Math.abs(new Random().nextInt()) % 100;
                            item.student_id = DataManager.getInstance().user.jisc_student_id;
                            list.add(item);

                            ED item1 = new ED();
                            item1.day = "" + (i + 1);
                            item1.activity_points = Math.abs(new Random().nextInt()) % 100;
                            item1.student_id = "";
                            list.add(item1);
                        }

                        Collections.sort(list, (s1, s2) -> s1.day.compareToIgnoreCase(s2.day));
                    } else if (!isSevenDays) {

                        for (int i = 0; i < 30; i++) {
                            ED item = new ED();
                            item.day = "" + (i + 1);
                            item.activity_points = Math.abs(new Random().nextInt()) % 100;
                            item.student_id = DataManager.getInstance().user.jisc_student_id;
                            list.add(item);

                            ED item1 = new ED();
                            item1.day = "" + (i + 1);
                            item1.activity_points = Math.abs(new Random().nextInt()) % 100;
                            item1.student_id = "";
                            list.add(item1);
                        }

                        Collections.sort(list, (s1, s2) -> s1.day.compareToIgnoreCase(s2.day));
                    } else if (isOverall) {

                        try {

                            Calendar calendar = Calendar.getInstance();

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date startDate = dateFormat.parse("01/01/2017");
                            Date now = new Date();

                            while (now.after(startDate)) {
                                int numberOfDays = Math.abs(new Random().nextInt()) % 5 + 1;
                                calendar.setTime(startDate);
                                calendar.add(Calendar.DATE, numberOfDays);

                                startDate = calendar.getTime();

                                ED item = new ED();
                                item.day = dateFormat.format(startDate);
                                item.realDate = startDate;
                                item.activity_points = Math.abs(new Random().nextInt()) % 100;
                                item.student_id = DataManager.getInstance().user.jisc_student_id;
                                list.add(item);

                                ED item1 = new ED();
                                item1.day = dateFormat.format(startDate);
                                item1.realDate = startDate;
                                item1.activity_points = Math.abs(new Random().nextInt()) % 100;
                                item1.student_id = "";
                                list.add(item1);
                            }

                            Collections.sort(list, (s1, s2) -> s1.realDate.compareTo(s2.realDate));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                runOnUiThread(() -> {
                    refreshUi();
                    DataManager.getInstance().mainActivity.hideProgressBar();
                });
                return;
            }

            String filterType;
            String filterValue;
            boolean isCourse = false;

            String moduleTitleName = moduleFilter.getText().toString().replace(" -", "");
            if (new Select().from(Module.class).where("module_name LIKE ?", "%" + moduleTitleName + "%").exists()) {
                filterType = "moduleFilter";
                filterValue = ((Module) new Select().from(Module.class).where("module_name = ?", moduleTitleName).executeSingle()).id;
            } else {
                filterType = "course";
                if (new Select().from(Courses.class).where("course_name LIKE ?", "%" + moduleTitleName + "%").exists()) {
                    filterValue = ((Courses) new Select().from(Courses.class).where("course_name = ?", moduleTitleName).executeSingle()).id;
                    isCourse = true;
                } else {
                    filterValue = "";
                }
            }

            String compareValue;
            String compareType;
//        if (compareTo.getText().toString().contains("Top")) {
//            compareValue = "10";
//            compareType = "top";
//        } else
            if (!compareTo.getText().toString().equals(getString(R.string.compare_to))
//                && !compareTo.getText().toString().equals(getString(R.string.top10))
                    && !compareTo.getText().toString().equals(getString(R.string.average))) {
                compareValue = ((Friend) new Select().from(Friend.class).where("name = ?", compareTo.getText().toString()).executeSingle()).jisc_student_id.replace("[", "").replace("]", "").replace("\"", "");
                compareType = "friend";
            } else if (compareTo.getText().toString().equals(getString(R.string.average))) {
                compareValue = "";
                compareType = "average";
            } else {
                compareType = "";
                compareValue = "";
            }

            String period;
            if (isSevenDays)
                period = getString(R.string.last_7_days).toLowerCase();
            else
                period = getString(R.string.last_30_days).toLowerCase();
            //String scope = DataManager.getInstance().api_values.get(period.getText().toString().toLowerCase()).replace(" ", "_").toLowerCase();
            String scope = DataManager.getInstance().api_values.get(period).replace(" ", "_").toLowerCase();

            list = NetworkManager.getInstance().getEngagementGraph(
                    scope,
                    compareType,
                    compareValue,
                    filterType,
                    filterValue,
                    isCourse
            );

            if((list.size() == 0 || list == null) && DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk") && !ConnectionHandler.isConnected(getContext())){
                SimpleDateFormat logDate = new SimpleDateFormat("MM/dd");
                Calendar currentDate = Calendar.getInstance();
                if(!isSevenDays) {
                    for(int i = 28; i > 0; i--) {
                        currentDate.add(Calendar.DATE, -1);
                        list.add(new ED(logDate.format(currentDate.getTime()), offlineDemoData[i]));
                    }
                } else {
                    for(int i = 28; i > 21; i--) {
                        currentDate.add(Calendar.DATE, -1);
                        list.add(new ED(logDate.format(currentDate.getTime()), offlineDemoData[i]));
                    }
                }
            }

            runOnUiThread(() -> {
                refreshUi();
                DataManager.getInstance().mainActivity.hideProgressBar();
            });
        }).start();
    }

    private void refreshUi() {
        if (list == null) {
            list = new ArrayList<>();
        }

        ArrayList<ED> tempList = new ArrayList<>();
        tempList.addAll(list);

        if (compareTo.getText().toString().equals(getString(R.string.compare_to))) {
            if (isSevenDays) {

                final ArrayList<String> xVals = new ArrayList<>();
                ArrayList<String> vals1 = new ArrayList<>();

                String name = getString(R.string.me);

                Date date = new Date();
                date.setTime(date.getTime() - 6 * 86400000);
                Collections.reverse(tempList);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

                for (int i = 0; i < tempList.size(); i++) {
                    String day = dateFormat.format(date);
                    date.setTime(date.getTime() + 86400000);
                    vals1.add("" + tempList.get(i).activity_points + "");
                    xVals.add("\'" + day + "\'");
                }

                String webData = "xAxis: { title: {text:null}, categories:[";
                webData += TextUtils.join(",", xVals);
                webData += "]}, series:[{name:\'" + name + "\',data: [" + TextUtils.join(",", vals1) + "]}]";

                String html = getHighCartsString();
                html = html.replace("<<<REPLACE_DATA_HERE>>>", webData);
                html = html.replace("height:1000px", "height:" + webViewHeight + "px");

                webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
            } else if (!isSevenDays) {
                ArrayList<String> vals1 = new ArrayList<>();
                ArrayList<String> xVals = new ArrayList<>();

                Integer val1 = 0;

                Collections.reverse(tempList);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -27);

                String day;
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

                for (int i = 0; i < tempList.size(); i++) {
                    val1 = val1 + tempList.get(i).activity_points;

                    if (i == 6 || i == 13 || i == 20 || i == 27) {
                        vals1.add("" + val1);
                        calendar.add(Calendar.DATE, 6);
                        day = dateFormat.format(calendar.getTime());
                        calendar.add(Calendar.DATE, 1);
                        xVals.add("\'" + day + "\'");
                        val1 = 0;
                    }
                }

                String name = getString(R.string.me);

                String webData = "xAxis: { title: {text:null}, categories:[";
                webData += TextUtils.join(",", xVals);
                webData += "]}, series:[{name:\'" + name + "\',data: [" + TextUtils.join(",", vals1) + "]}]";

                String html = getHighCartsString();
                html = html.replace("<<<REPLACE_DATA_HERE>>>", webData);
                html = html.replace("height:1000px", "height:" + webViewHeight + "px");

                Log.e("JISC", "HTML: " + html);
                webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
            }
        } else {
            if (isSevenDays) {
                final ArrayList<String> xVals = new ArrayList<>();

                ArrayList<Integer> vals3 = new ArrayList<>();
                ArrayList<Integer> vals4 = new ArrayList<>();

                ArrayList<String> vals1 = new ArrayList<>();
                ArrayList<String> vals2 = new ArrayList<>();

                String name = getString(R.string.me);
                String id = DataManager.getInstance().user.jisc_student_id;
                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    id = "demouser";
                }

                Integer value1;
                Integer value2;

                String day;
                Calendar c = Calendar.getInstance();
                Long curr = c.getTimeInMillis() - 518400000;
                c.setTimeInMillis(curr);

                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    for (int i = 0; i < tempList.size(); i++) {
                        if (tempList.get(i).student_id.equals(id)) {
                            value1 = tempList.get(i).activity_points;
                            vals3.add(value1);
                        } else {
                            value2 = tempList.get(i).activity_points;
                            vals4.add(value2);
                        }
                    }
                } else {
                    for (int i = 0; i < tempList.size(); i++) {
                        if (tempList.get(i).student_id.contains(id)) {
                            value1 = tempList.get(i).activity_points;
                            vals3.add(value1);
                        } else {
                            value2 = tempList.get(i).activity_points;
                            vals4.add(value2);
                        }
                    }
                }

                Collections.reverse(vals3);
                Collections.reverse(vals4);

                Date date = new Date();
                date.setTime(date.getTime() - 6 * 86400000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

                for (int i = 0; i < vals3.size(); i++) {
                    day = dateFormat.format(date);
                    date.setTime(date.getTime() + 86400000);
                    vals1.add("" + vals3.get(i));
                    vals2.add("" + vals4.get(i));
                    xVals.add("\'" + day + "\'");
                }

                String webData = "xAxis: { title: {text:null}, categories:[";
                webData += TextUtils.join(",", xVals);
                webData += "]}, series:[{name:\'" + name + "\',data: [" + TextUtils.join(",", vals1) + "]},{name:\'" + compareTo.getText().toString() + "\',data: [" + TextUtils.join(",", vals2) + "]}]";

                String html = getHighCartsString();
                html = html.replace("<<<REPLACE_DATA_HERE>>>", webData);
                html = html.replace("height:1000px", "height:" + webViewHeight + "px");

                webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");

            } else if (!isSevenDays) {
                final ArrayList<String> xVals = new ArrayList<>();

                ArrayList<Integer> vals3 = new ArrayList<>();
                ArrayList<Integer> vals4 = new ArrayList<>();

                ArrayList<String> vals1 = new ArrayList<>();
                ArrayList<String> vals2 = new ArrayList<>();

                String name = getString(R.string.me);

                String id = DataManager.getInstance().user.jisc_student_id;
                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    id = "demouser";
                }

                Integer value1;
                Integer value2;
                String label;

                Calendar c = Calendar.getInstance();
                Long curr = c.getTimeInMillis() - (3 * 518400000);
                c.setTimeInMillis(curr);

                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    for (int i = 0; i < tempList.size(); i++) {
                        if (tempList.get(i).student_id.equals(id)) {
                            value1 = tempList.get(i).activity_points;
                            vals3.add(value1);
                        } else {
                            value2 = tempList.get(i).activity_points;
                            vals4.add(value2);
                        }
                    }
                } else {
                    for (int i = 0; i < tempList.size(); i++) {
                        if (tempList.get(i).student_id.contains(id)) {
                            value1 = tempList.get(i).activity_points;
                            vals3.add(value1);
                        } else {
                            value2 = tempList.get(i).activity_points;
                            vals4.add(value2);
                        }
                    }
                }

                Collections.reverse(vals3);
                Collections.reverse(vals4);

                Integer val1 = 0;
                Integer val2 = 0;

                Date date = new Date();
                long time = date.getTime() - 21 * 86400000;
                date.setTime(time);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                for (int i = 0; i < vals3.size(); i++) {
                    val1 = val1 + vals3.get(i);
                    val2 = val2 + vals4.get(i);

                    if (i == 6 || i == 13 || i == 20 || i == 27) {

                        label = dateFormat.format(date);
                        date.setTime(date.getTime() + 7 * 86400000);

                        vals1.add("" + val1);
                        vals2.add("" + val2);

                        xVals.add("\'" + label + "\'");

                        val1 = 0;
                        val2 = 0;
                    }
                }

                String webData = "xAxis: { title: {text:null}, categories:[";
                webData += TextUtils.join(",", xVals);
                webData += "]}, series:[{name:\'" + name + "\',data: [" + TextUtils.join(",", vals1) + "]},{name:\'" + compareTo.getText().toString() + "\',data: [" + TextUtils.join(",", vals2) + "]}]";

                String html = getHighCartsString();
                html = html.replace("<<<REPLACE_DATA_HERE>>>", webData);
                html = html.replace("height:1000px", "height:" + webViewHeight + "px");

                webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
            }
        }
    }

    private String getHighCartsString() {
        try {
            String path;

            if (isBar) {
                path = "highcharts/bargraph.html";
            } else {
                path = "highcharts/linegraph.html";
            }

            StringBuilder buf = new StringBuilder();
            InputStream json = DataManager.getInstance().mainActivity.getAssets().open(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
            return buf.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void runOnUiThread(Runnable run) {
        final Activity activity = getActivity();

        if (activity != null && run != null) {
            activity.runOnUiThread(run);
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
                        //load Data
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
                        //load Data
                    }
                }
            }
        };
    }
}
