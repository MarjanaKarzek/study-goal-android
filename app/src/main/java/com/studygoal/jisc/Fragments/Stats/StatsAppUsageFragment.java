package com.studygoal.jisc.Fragments.Stats;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.studygoal.jisc.Adapters.AppUsageAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Stats VLE Activity Fragment
 * <p>
 * Displays the VLE activity of the user.
 *
 * @author Therapy Box - Marjana Karzek
 * @version 1.5
 * @date 07/09/17
 */
public class StatsAppUsageFragment extends Fragment {

    private TextView startDate;
    private TextView endDate;
    private Calendar startDatePicked = Calendar.getInstance();
    private Calendar endDatePicked = Calendar.getInstance();
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DatePickerDialog.OnDateSetListener datePickerEnd;
    private DatePickerDialog.OnDateSetListener datePickerStart;

    private ArrayList<String> list = new ArrayList<>(Arrays.asList("targets", "setTarget", "activities", "sessions"));
    private HashMap<String, String> data = new HashMap<>();
    private AppUsageAdapter adapter;

    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.app_usage));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateAppUsage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.layout_stats_app_usage, container, false);

        setUpDatePicker();

        DatePickerDialog startDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerStart, startDatePicked
                .get(Calendar.YEAR), startDatePicked.get(Calendar.MONTH),
                startDatePicked.get(Calendar.DAY_OF_MONTH));

        startDate = (TextView) mainView.findViewById(R.id.app_usage_start);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateDatePickerDialog.show();
            }
        });

        DatePickerDialog endDateDatePickerDialog = new DatePickerDialog(getActivity(), datePickerEnd, endDatePicked
                .get(Calendar.YEAR), endDatePicked.get(Calendar.MONTH),
                endDatePicked.get(Calendar.DAY_OF_MONTH));

        endDate = (TextView) mainView.findViewById(R.id.app_usage_end);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDateDatePickerDialog.show();
            }
        });

        loadData(null, null);
        adapter = new AppUsageAdapter(getContext());
        adapter.list = list;
        adapter.data = data;
        ListView listView = (ListView) mainView.findViewById(R.id.app_usage_data_list);
        listView.setAdapter(adapter);

        return mainView;
    }

    /**
     * Calls the data from the server according to the settings.
     *
     * @param startDate period beginning
     * @param endDate period ending
     */
    private void loadData(String startDate, String endDate) {
        DataManager.getInstance().mainActivity.showProgressBar("");
        NetworkManager.getInstance().getAppUsage(startDate, endDate);
        data.clear();
        data.put("sessions", DataManager.getInstance().appUsageData.sessions);
        data.put("activities", DataManager.getInstance().appUsageData.activities);
        data.put("set_targets", DataManager.getInstance().appUsageData.setTargets);
        data.put("met_targets", DataManager.getInstance().appUsageData.metTargets);
        data.put("failed_targets", DataManager.getInstance().appUsageData.failedTargets);
        if (adapter != null)
            adapter.notifyDataSetChanged();
        DataManager.getInstance().mainActivity.hideProgressBar();
    }

    // Settings

    /**
     * Sets up the date pickers for the settings.
     */
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
                        loadData(apiDateFormat.format(startDatePicked.getTime()), apiDateFormat.format(endDatePicked.getTime()));
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
                        loadData(apiDateFormat.format(startDatePicked.getTime()), apiDateFormat.format(endDatePicked.getTime()));
                    }
                }
            }
        };
    }
}
