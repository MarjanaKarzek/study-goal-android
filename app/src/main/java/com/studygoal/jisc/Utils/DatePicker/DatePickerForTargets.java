package com.studygoal.jisc.Utils.DatePicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Date Picker for Targets Dialog Fragment
 * <p>
 * Provides possibility to display a dialog fragment to pick the date specified for targets.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class DatePickerForTargets extends DialogFragment {

    private DatePicker.OnDateChangedListener listener = null;

    /**
     * Set listener for date picker.
     *
     * @param listener listener to be set
     */
    public void setListener(DatePicker.OnDateChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Creates date picker dialog with a maximum date of one year into the future.
     *
     * @param savedInstanceState saved instance state of the environment
     * @return created dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> listener.onDateChanged(view, year1, month1, dayOfMonth), year, month, day);
        dialog.getDatePicker().init(year, month, day, listener);
        dialog.getDatePicker().setMinDate(new Date().getTime());
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        dialog.getDatePicker().setMaxDate(nextYear.getTimeInMillis());
        return dialog;
    }

}