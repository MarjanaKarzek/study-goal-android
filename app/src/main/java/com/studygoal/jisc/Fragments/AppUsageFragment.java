package com.studygoal.jisc.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.studygoal.jisc.Adapters.AppUsageListAdapter;
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
 * Created by Marjana-Tbox on 07/09/17.
 */

public class AppUsageFragment extends Fragment {

    private TextView startDate;
    private TextView endDate;
    private Calendar pickedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private DatePickerDialog.OnDateSetListener datePickerEnd;
    private DatePickerDialog.OnDateSetListener datePickerStart;

    private ArrayList<String> list = new ArrayList<>(Arrays.asList("targets", "setTarget", "activities", "sessions"));
    private HashMap<String,String> data = new HashMap<>();
    private AppUsageListAdapter adapter;

    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(getString(R.string.app_usage));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateAppUsage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.app_usage, container, false);

        setUpDatePicker();

        startDate = (TextView) mainView.findViewById(R.id.app_usage_start);
        //startDate.setText(dateFormat.format(pickedDate.getTime()));
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), datePickerStart, pickedDate
                        .get(Calendar.YEAR), pickedDate.get(Calendar.MONTH),
                        pickedDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endDate = (TextView) mainView.findViewById(R.id.app_usage_end);
        //endDate.setText(dateFormat.format(pickedDate.getTime()));
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), datePickerEnd, pickedDate
                        .get(Calendar.YEAR), pickedDate.get(Calendar.MONTH),
                        pickedDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        loadData(null,null);
        adapter = new AppUsageListAdapter(getContext());
        adapter.list = list;
        adapter.data = data;
        ListView listView = (ListView) mainView.findViewById(R.id.app_usage_data_list);
        listView.setAdapter(adapter);

        return mainView;
    }

    private void loadData(String startDate, String endDate){
        DataManager.getInstance().mainActivity.showProgressBar("");
        NetworkManager.getInstance().getAppUsage(startDate, endDate);
        data.clear();
        data.put("sessions",DataManager.getInstance().appUsageData.sessions);
        data.put("activities",DataManager.getInstance().appUsageData.activities);
        data.put("set targets",DataManager.getInstance().appUsageData.setTargets);
        data.put("met targets",DataManager.getInstance().appUsageData.metTargets);
        data.put("failed targets",DataManager.getInstance().appUsageData.failedTargets);
        adapter.notifyDataSetChanged();
        DataManager.getInstance().mainActivity.hideProgressBar();
    }

    private void setUpDatePicker(){
        datePickerStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                pickedDate.set(Calendar.YEAR, year);
                pickedDate.set(Calendar.MONTH, monthOfYear);
                pickedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                startDate.setText(dateFormat.format(pickedDate.getTime()));
                //refresh picker data
                pickedDate = Calendar.getInstance();
                if(!endDate.getText().toString().equals("End"))
                    loadData(startDate.getText().toString(),endDate.getText().toString());
            }
        };

        datePickerEnd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                pickedDate.set(Calendar.YEAR, year);
                pickedDate.set(Calendar.MONTH, monthOfYear);
                pickedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                endDate.setText(dateFormat.format(pickedDate.getTime()));
                //refresh picker data
                pickedDate = Calendar.getInstance();
                if(!endDate.getText().toString().equals("Start"))
                    loadData(startDate.getText().toString(),endDate.getText().toString());
            }
        };
    }
}
