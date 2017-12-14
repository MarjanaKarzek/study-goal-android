package com.studygoal.jisc.Fragments.Target;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.studygoal.jisc.Adapters.GenericAdapter;
import com.studygoal.jisc.Fragments.Log.LogLogActivityFragment;
import com.studygoal.jisc.Fragments.Log.LogNewActivityFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.LinguisticManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.SocialManager;
import com.studygoal.jisc.Models.ActivityHistory;
import com.studygoal.jisc.Models.Module;
import com.studygoal.jisc.Models.StretchTarget;
import com.studygoal.jisc.Models.Targets;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TargetItemFragment extends Fragment {
    private static final String TAG = TargetItemFragment.class.getSimpleName();

    private View mainView;
    public Targets target;
    public TargetDetailsFragment reference;
    public int position;
    private int necessaryTime;
    private int spentTime;
    private boolean piChart;
    private TextView incompleteTextView;
    private List<ActivityHistory> activityHistoryList;
    private StretchTarget stretchTarget;
    private View.OnClickListener setStretchTarget;
    private WebView webView;

    private float webViewHeight;
    private float webViewWidth;

    public void showDialog() {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.snippet_custom_spinner);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (DataManager.getInstance().mainActivity.isLandscape) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels * 0.3);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            dialog.getWindow().setAttributes(params);
        }

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.add);

        ArrayList<String> items = new ArrayList<>();
        items.add(getString(R.string.report_activity));
        items.add(getString(R.string.log_recent_activity));
        final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, "", items));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    DataManager.getInstance().fromTargetItem = true;
                    DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, new LogNewActivityFragment(), "newActivity")
                            .addToBackStack(null)
                            .commit();
                } else {
                    DataManager.getInstance().fromTargetItem = true;
                    LogLogActivityFragment fragment = new LogLogActivityFragment();
                    fragment.isInEditMode = false;
                    DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.target_title));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.target_item, container, false);

        mainView.findViewById(R.id.main_all_content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        setStretchTarget = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_stretchtarget);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if (DataManager.getInstance().mainActivity.isLandscape) {
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = (int) (displaymetrics.widthPixels * 0.3);

                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = width;
                    dialog.getWindow().setAttributes(params);
                }

                final NumberPicker hourPicker = (NumberPicker) dialog.findViewById(R.id.hour_picker);
                hourPicker.setMinValue(0);
                hourPicker.setMaxValue(10);
                hourPicker.setFormatter(new NumberPicker.Formatter() {
                    @Override
                    public String format(int value) {
                        if (value < 10)
                            return "0" + value;
                        else
                            return value + "";
                    }
                });
                final NumberPicker minutePicker = (NumberPicker) dialog.findViewById(R.id.minute_picker);
                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(59);
                minutePicker.setFormatter(new NumberPicker.Formatter() {
                    @Override
                    public String format(int value) {
                        if (value < 10)
                            return "0" + value;
                        else
                            return value + "";
                    }
                });

                ((TextView) dialog.findViewById(R.id.timespent_save_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                dialog.findViewById(R.id.set_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String time = hourPicker.getValue() * 60 + minutePicker.getValue() + "";
                        final HashMap<String, String> map = new HashMap<>();
                        map.put("target_id", target.target_id);
                        map.put("stretch_time", time);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataManager.getInstance().mainActivity.showProgressBar("");
                                    }
                                });
                                if (NetworkManager.getInstance().addStretchTarget(map)) {
                                    DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            DataManager.getInstance().mainActivity.hideProgressBar();
                                            Snackbar.make(mainView.findViewById(R.id.container), R.string.successfully_set_stretch_target, Snackbar.LENGTH_LONG).show();
                                            mainView.findViewById(R.id.target_set_stretch_btn).setVisibility(View.GONE);
                                            NetworkManager.getInstance().getStretchTargets(DataManager.getInstance().user.id);

                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loadDataStretch();
                                                }
                                            }, 100);
                                            dialog.dismiss();
                                        }
                                    });
                                } else {
                                    DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            DataManager.getInstance().mainActivity.hideProgressBar();
                                            Snackbar.make(mainView.findViewById(R.id.container), R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
                ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);

                dialog.show();
            }
        };

        webView = mainView.findViewById(R.id.piechart);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setPadding(0, 0, 0, 0);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setBackgroundColor(getResources().getColor(R.color.background_color));
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/20 Safari/537.31");
        webView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                webViewHeight = Utils.pxToDp(webView.getHeight() - 40);
                webViewWidth = Utils.pxToDp(webView.getWidth() - 40);
            }
        });
        webView.loadDataWithBaseURL("", "<html><head></head><body><div style=\"height:100%;width:100%;background:#fbfbfb;\"></div></body></html>", "text/html", "UTF-8", "");

        piChart = true;

        stretchTarget = new Select().from(StretchTarget.class).where("target_id = ?", target.target_id).executeSingle();
        Module module = new Select().from(Module.class).where("module_id = ?", target.module_id).executeSingle();

        TextView textView = mainView.findViewById(R.id.target_item_text);
        textView.setTypeface(DataManager.getInstance().myriadpro_regular);

        if (module != null) {
            activityHistoryList = new Select().from(ActivityHistory.class).where("module_id = ?", target.module_id).and("activity = ?", target.activity).execute();
        } else {
            activityHistoryList = new Select().from(ActivityHistory.class).where("activity = ?", target.activity).execute();
        }

        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDate = dateFormat.format(date.getTime());

        boolean dueToday = false;

        SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = null;
        try {
            convertedDate = shortDateFormat.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(convertedDate);
        String shortCurrentDate = shortDateFormat.format(Calendar.getInstance().getTime());

        switch (target.time_span.toLowerCase()) {
            case "day": {
                String time = currentDate.split(" ")[0];
                List<ActivityHistory> tmp = new ArrayList<>();
                for (int i = 0; i < activityHistoryList.size(); i++) {
                    Log.d("", "getView: activity History all " + activityHistoryList.get(i).toString());
                    if (time.equals(activityHistoryList.get(i).created_date.split(" ")[0]))
                        tmp.add(activityHistoryList.get(i));
                }
                activityHistoryList.clear();
                activityHistoryList.addAll(tmp);
                dueToday = true;
                break;
            }
            case "week": {
                List<ActivityHistory> tmp = new ArrayList<>();
                for (int i = 0; i < activityHistoryList.size(); i++) {
                    Log.d("", "getView: activity History all " + activityHistoryList.get(i).toString());
                    if (Utils.isInSameWeek(activityHistoryList.get(i).created_date.split(" ")[0]))
                        tmp.add(activityHistoryList.get(i));
                }
                activityHistoryList.clear();
                activityHistoryList.addAll(tmp);
                if(dueDate.get(Calendar.DAY_OF_WEEK) == 7){
                    dueToday = true;
                }
                break;
            }
            case "month": {
                String time = currentDate.split(" ")[0].split("-")[0] + "-" + currentDate.split(" ")[0].split("-")[1];
                List<ActivityHistory> tmp = new ArrayList<>();
                for (int i = 0; i < activityHistoryList.size(); i++) {
                    Log.d("TargetAdapter", "getView: activity History all " + activityHistoryList.get(i).toString());
                    if (time.equals(activityHistoryList.get(i).created_date.split(" ")[0].split("-")[0] + "-" + activityHistoryList.get(i).created_date.split(" ")[0].split("-")[1]))
                        tmp.add(activityHistoryList.get(i));
                }
                activityHistoryList.clear();
                activityHistoryList.addAll(tmp);
                dueDate.set(Calendar.DAY_OF_MONTH, dueDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                String nextDueDate = shortDateFormat.format(dueDate.getTime());
                if(shortCurrentDate.equals(nextDueDate)){
                    dueToday = true;
                }
                break;
            }
        }

        necessaryTime = Integer.parseInt(target.total_time);
        spentTime = 0;

        for (int i = 0; i < activityHistoryList.size(); i++) {
            Log.d("TargetAdapter", "getView: activity History filtered " + activityHistoryList.get(i).toString());
            Log.d("TargetAdapter", "getView: time of that log" + Integer.parseInt(activityHistoryList.get(i).time_spent));
            spentTime += Integer.parseInt(activityHistoryList.get(i).time_spent);
        }

        if (dueToday && (spentTime < necessaryTime))
            mainView.findViewById(R.id.colorbar).setBackgroundColor(0xFFFF0000);
        else if (spentTime < necessaryTime)
            mainView.findViewById(R.id.colorbar).setBackgroundColor(0xFFFFCC00);
        else
            mainView.findViewById(R.id.colorbar).setBackgroundColor(0xFF00FF00);

        try {
            Glide.with(DataManager.getInstance().mainActivity).load(LinguisticManager.getInstance().images.get(target.activity)).into((ImageView) mainView.findViewById(R.id.activity_icon));
        } catch (Exception e) { }

        Log.d(TAG, "onCreateView: " + target.total_time);
        if (spentTime == 0 || spentTime < necessaryTime) {
            incompleteTextView = mainView.findViewById(R.id.target_item_incomplete_textview);
            incompleteTextView.setVisibility(View.VISIBLE);
            incompleteTextView.setTypeface(DataManager.getInstance().myriadpro_regular);

            int percentage = (int) (100.0 / (necessaryTime * 1.0) * (spentTime * 1.0));
            incompleteTextView.setText(percentage + "%");

            if (spentTime < necessaryTime) {
                View set_stretch = mainView.findViewById(R.id.target_set_stretch_btn);
                set_stretch.setVisibility(View.VISIBLE);
                ((TextView) mainView.findViewById(R.id.target_stretch_btn_text)).setText(DataManager.getInstance().mainActivity.getString(R.string.start_your_new_activity));
                set_stretch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                    }
                });
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadData();
                }
            }, 100);
        } else {
            TextView completeTextView = (TextView) mainView.findViewById(R.id.target_item_complete_textview);
            completeTextView.setVisibility(View.VISIBLE);
            completeTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
            completeTextView.setText("100%");

            if (stretchTarget != null) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadDataStretch();
                    }
                }, 100);


                if (spentTime < this.necessaryTime + Integer.parseInt(stretchTarget.stretch_time)) {
                    int time = this.necessaryTime + Integer.parseInt(stretchTarget.stretch_time) - spentTime;
                    TextView textView1 = (TextView) mainView.findViewById(R.id.target_reached_stretch_present);
                    String text = LinguisticManager.getInstance().present.get(target.activity);
                    if (text.contains(DataManager.getInstance().mainActivity.getString(R.string._for))) {
                        text += " " + DataManager.getInstance().mainActivity.getString(R.string.another) + " ";
                    } else {
                        text += " " + DataManager.getInstance().mainActivity.getString(R.string.for_text) + " " + DataManager.getInstance().mainActivity.getString(R.string.another) + " ";
                    }
                    int hour = time / 60;
                    int minute = time % 60;
                    text += (hour == 1) ? "1" + " " + DataManager.getInstance().mainActivity.getString(R.string.hour) + " " : hour + " " + DataManager.getInstance().mainActivity.getString(R.string.hours) + " ";
                    if (minute > 0)
                        text += ((minute == 1) ? DataManager.getInstance().mainActivity.getString(R.string.and) + " 1" + " " + DataManager.getInstance().mainActivity.getString(R.string.minute) + " " : DataManager.getInstance().mainActivity.getString(R.string.and) + " " + minute + " " + DataManager.getInstance().mainActivity.getString(R.string.minutes) + " ");
                    text += " " + DataManager.getInstance().mainActivity.getString(R.string.this_text) + " " + target.time_span.substring(0, target.time_span.length() - 2).toLowerCase() + " " + DataManager.getInstance().mainActivity.getString(R.string.to_meet_stretch_target);
                    textView1.setText(text);
                    textView1.setVisibility(View.VISIBLE);
                }
            } else {
                if (canStretchTarget()) {
                    View setStretch = mainView.findViewById(R.id.target_set_stretch_btn);
                    setStretch.setVisibility(View.VISIBLE);
                    ((TextView) mainView.findViewById(R.id.target_stretch_btn_text)).setText(DataManager.getInstance().mainActivity.getString(R.string.set_stretch_target));
                    ((TextView) mainView.findViewById(R.id.target_stretch_btn_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                    setStretch.setOnClickListener(setStretchTarget);
                }
            }

            mainView.findViewById(R.id.target_item_complete_imageview).setVisibility(View.VISIBLE);
            mainView.findViewById(R.id.target_reached_layout).setVisibility(View.VISIBLE);
        }

        HashMap<String,String> spans = new HashMap<>();
        spans.put("Day",getContext().getString(R.string.daily));
        spans.put("Week",getContext().getString(R.string.Weekly));
        spans.put("Month",getContext().getString(R.string.monthly));

        String text = "";
        text += LinguisticManager.getInstance().present.get(target.activity) + " ";
        int hour = Integer.parseInt(target.total_time) / 60;
        int minute = Integer.parseInt(target.total_time) % 60;
        text += (hour == 1) ? "1 " + DataManager.getInstance().mainActivity.getString(R.string.hour) + " " : hour + " " + DataManager.getInstance().mainActivity.getString(R.string.hours) + " ";
        if (minute > 0)
            text += ((minute == 1) ? " " + DataManager.getInstance().mainActivity.getString(R.string.and) + " 1 " + DataManager.getInstance().mainActivity.getString(R.string.minute) + " " : " " + DataManager.getInstance().mainActivity.getString(R.string.and) + " " + minute + " " + DataManager.getInstance().mainActivity.getString(R.string.minutes) + " ");
        text += spans.get(target.time_span);
        text += module == null ? "" : " " + DataManager.getInstance().mainActivity.getString(R.string.for_text) + " " + module.name;
        textView.setText(text);

        final String finalText = text;
        mainView.findViewById(R.id.target_reached_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocialManager.getInstance().shareOnIntent(getActivity().getString(R.string.target_reached_2) + " " + finalText);
            }
        });

        final com.daimajia.swipe.SwipeLayout swipeLayout = (com.daimajia.swipe.SwipeLayout) mainView.findViewById(R.id.swipelayout);

        mainView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout.close(true);
                AddTargetFragment fragment = new AddTargetFragment();
                fragment.isInEditMode = true;
                fragment.isSingleTarget = false;
                fragment.item = target;
                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        mainView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_dialog_confirmation);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if (DataManager.getInstance().mainActivity.isLandscape) {
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    DataManager.getInstance().mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = (int) (displaymetrics.widthPixels * 0.45);

                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = width;
                    dialog.getWindow().setAttributes(params);
                }

                ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(DataManager.getInstance().oratorstd_typeface);
                ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.confirmation);

                ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.confirm_delete_message);

                ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

                ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

                dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        swipeLayout.close(true);
                        reference.deleteTarget(target, position);
                    }
                });
                dialog.findViewById(R.id.dialog_no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        return mainView;
    }

    private boolean canStretchTarget() {
        boolean eligible = false;
        if (target.time_span.toLowerCase().equals(getString(R.string.daily).toLowerCase())) {

        } else if (target.time_span.toLowerCase().equals(getString(R.string.Weekly).toLowerCase())) {
            Calendar c = Calendar.getInstance();
            if (c.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY)
                eligible = true;
        } else if (target.time_span.toLowerCase().equals(getString(R.string.monthly).toLowerCase())) {
            Calendar c = Calendar.getInstance();
            if (c.getActualMaximum(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_MONTH) > 4)
                eligible = true;
        }

        return eligible;
    }

    protected void loadData() {
        String html = getHighChartsHTML(false);
        html = html.replace("Y_MAX_VALUE", "" + necessaryTime);
        html = html.replace("Y_VALUE", "" + spentTime);
        html = html.replace("height:1000px", "height:" + webViewHeight + "px !important");
        html = html.replace("width:1000px", "width:" + webViewWidth + "px !important");

        webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
    }

    protected void loadDataStretch() {
        String html = getHighChartsHTML(true);
        html = html.replace("Y_VALUE", "" + (spentTime - necessaryTime));
        html = html.replace("Y_MAX_VALUE", "" + stretchTarget.stretch_time);
        html = html.replace("height:1000px", "height:" + webViewHeight + "px !important");
        html = html.replace("width:1000px", "width:" + webViewWidth + "px !important");

        webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
    }

    public String getHighChartsHTML(boolean isStretch) {

        try {
            String path = "highcharts/piegraph.html";
            if (isStretch) {
                path = "highcharts/piestretchgraph.html";
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
            e.printStackTrace();
            return "";
        }

    }
}
