package com.studygoal.jisc.Fragments.Stats;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.ModuleAdapter;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Fragments.BaseFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Courses;
import com.studygoal.jisc.R;

import java.util.List;

public class StatsLeaderBoardFragment extends BaseFragment {
    private static final String TAG = StatsLeaderBoardFragment.class.getSimpleName();

    private AppCompatTextView module;
    private String selectedPeriod;
    private ListView rankListView;

    static final String[] EVENTS = new String[]{"Calculate 101", "Calculate 102", "Calculate 103", "Calculate 101"};

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.leader_board));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateLeaderboard);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.stats_leaderboard, container, false);

        rankListView = (ListView) mainView.findViewById(R.id.stats_leaderBoard_listView);
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.stats_event_attendance_list_view_header, rankListView, false);
        rankListView.addHeaderView(header);
        rankListView.setAdapter(new ArrayAdapter<>(getContext(), R.layout.snippet_event_attendance_list, EVENTS));


        setUpModule(mainView);

//        ((MainActivity) getActivity()).showProgressBar(null);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        getData();
//                        ((MainActivity) getActivity()).hideProgressBar();
//                    }
//                });
//            }
//        }).start();

        return mainView;
    }

    private void setUpModule(View mainView) {
        module = (AppCompatTextView) mainView.findViewById(R.id.module_list);
        module.setSupportBackgroundTintList(ColorStateList.valueOf(0xFF8a63cc));
        module.setTypeface(DataManager.getInstance().myriadpro_regular);
        module.setText(R.string.anymodule);
        ((TextView) mainView.findViewById(R.id.module)).setTypeface(DataManager.getInstance().myriadpro_regular);
        module.setOnClickListener(v -> {
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
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.choose_module);

            final ListView listView = (ListView) dialog.findViewById(R.id.dialog_listview);
            listView.setAdapter(new ModuleAdapter(DataManager.getInstance().mainActivity, module.getText().toString()));
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
                module.setText(titleText);
            });
            ((MainActivity) getActivity()).showProgressBar2("");
            dialog.show();
        });

        final TextView last7DTextView = (TextView) mainView.findViewById(R.id.last_7d_text_view);
        final TextView last30DTextView = (TextView) mainView.findViewById(R.id.last_30d_text_view);
        last7DTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
        last30DTextView.setTypeface(DataManager.getInstance().myriadpro_regular);
        selectedPeriod = getString(R.string.last_7_days);

        LinearLayout period_segmented = (LinearLayout) mainView.findViewById(R.id.period_segmented);
        period_segmented.setOnClickListener(v -> {
            if (selectedPeriod.equals(getString(R.string.last_7_days))) {
                selectedPeriod = getString(R.string.last_30_days);
                last30DTextView.setTextColor(Color.parseColor("#ffffff"));
                last30DTextView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_blue_right));

                last7DTextView.setTextColor(Color.parseColor("#3691ee"));
                last7DTextView.setBackground(null);

            } else {
                selectedPeriod = getString(R.string.last_7_days);
                last30DTextView.setTextColor(Color.parseColor("#3691ee"));
                last30DTextView.setBackground(null);

                last7DTextView.setTextColor(Color.parseColor("#ffffff"));
                last7DTextView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_blue_left));
            }
        });
    }
}