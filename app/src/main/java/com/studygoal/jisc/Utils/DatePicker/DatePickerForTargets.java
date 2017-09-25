package com.studygoal.jisc.Utils.DatePicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerForTargets extends DialogFragment {
    private DatePicker.OnDateChangedListener mListener = null;

    public void setListener(DatePicker.OnDateChangedListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> mListener.onDateChanged(view, year1, month1, dayOfMonth), year, month, day);
        dialog.getDatePicker().init(year, month, day, mListener);
        dialog.getDatePicker().setMinDate(new Date().getTime());
        return dialog;
    }
}