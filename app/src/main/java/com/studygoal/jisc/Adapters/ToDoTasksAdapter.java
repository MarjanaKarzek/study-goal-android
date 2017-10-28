package com.studygoal.jisc.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.ToDoTasks;
import com.studygoal.jisc.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ToDoTasksAdapter extends BaseAdapter {
    private static final String TAG = ToDoTasksAdapter.class.getSimpleName();

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Context context;
    private ArrayList<ToDoTasks> list;
    private ToDoTasksAdapterListener listener;

    public interface ToDoTasksAdapterListener {
        void onDelete(ToDoTasks target, int finalPosition);
        void onEdit(ToDoTasks targets);
        void onDone(ToDoTasks target);
    }

    public ToDoTasksAdapter(Context context, ToDoTasksAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        list = new ArrayList<>();
    }

    public void updateList(List<ToDoTasks> list) {
        if (this.list != null) {
            this.list.clear();
        } else {
            this.list = new ArrayList<>();
        }

        if (list != null) {
            this.list.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void deleteItem(int position) {
        if (list != null && position < list.size()) {
            list.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ToDoTasks getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ToDoTasks item = list.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_todo_tasks, parent, false);
        }

        ImageView activity_icon = (ImageView) convertView.findViewById(R.id.activity_icon);

        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.HOUR_OF_DAY, 0);
        currentTime.set(Calendar.MINUTE, 0);
        currentTime.set(Calendar.SECOND, 0);
        currentTime.set(Calendar.MILLISECOND, 0);

        boolean isToday = false;
        boolean isTomorrow = false;

        Date currentDate = currentTime.getTime();
        Date itemDate = null;
        try {
            itemDate = simpleDateFormat.parse(item.endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int difference = (int)((currentDate.getTime() - itemDate.getTime()) / (1000 * 60 * 60 * 24)) ;

        String overdueText = "";
        if (difference == 0) {
            activity_icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.watch_time_due_today));
            isToday = true;
        } else if (difference == -1 || difference == -2) {
            if(difference == -1)
                isTomorrow = true;
            activity_icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.watch_time_2_left));
        } else if (difference <= -3 && difference > -7) {
            activity_icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.watch_time_7_left));
        } else if (difference <= -7) {
            activity_icon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.watch_time_idle));
        } else if (difference > 0){
            activity_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.watch_time_overdue));
            if(difference == 1)
                overdueText = "1 " + context.getString(R.string.day_overdue);
            else
                overdueText = difference + " "+ context.getString(R.string.days_overdue);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.target_item_text);
        textView.setTypeface(DataManager.getInstance().myriadpro_regular);

        String text = "";
        text += overdueText + item.description;
        if(!item.module.equals("no_module"))
            text += " " + context.getString(R.string._for) + " " + item.module;
        text  += " " + context.getString(R.string.by).toLowerCase() + " ";

        if (isToday) {
            text += context.getString(R.string.today);
        } else if (isTomorrow){
            text += context.getString(R.string.tomorrow);
        } else {
            text += getDateFromEndDateTag(item.endDate);
        }

        if (item.reason != null && !item.reason.isEmpty()) {
            text += " " + context.getString(R.string.because) + " " + item.reason;
        }

        textView.setText(text);

        final com.daimajia.swipe.SwipeLayout swipeLayout = (com.daimajia.swipe.SwipeLayout) convertView.findViewById(R.id.swipelayout);
        convertView.findViewById(R.id.edit).setOnClickListener(v -> {
            swipeLayout.close(true);

            if (listener != null) {
                listener.onEdit(item);
            }
        });

        convertView.findViewById(R.id.done).setOnClickListener(v -> {
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
            ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.confirm_todo_task_done_message);

            ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

            ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

            dialog.findViewById(R.id.dialog_ok).setOnClickListener(v1 -> {
                dialog.dismiss();
                swipeLayout.close(true);

                if (listener != null) {
                    listener.onDone(item);
                }
            });
            dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> dialog.dismiss());
            dialog.show();
        });

        View mainLayout = convertView.findViewById(R.id.mainLayout);

        if (item.fromTutor != null && item.isAccepted != null && item.fromTutor.toLowerCase().equals("yes") && item.isAccepted.equals("0")) {
            mainLayout.setBackgroundColor(context.getResources().getColor(R.color.to_do_item_tutor_background));
        } else {
            mainLayout.setBackgroundColor(context.getResources().getColor(R.color.to_do_item_general_background));
        }

        final int finalPosition = position;
        convertView.findViewById(R.id.delete).setOnClickListener(v -> {
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

            dialog.findViewById(R.id.dialog_ok).setOnClickListener(v1 -> {
                dialog.dismiss();
                swipeLayout.close(true);

                if (listener != null) {
                    listener.onDelete(item, finalPosition);
                }
            });
            dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> dialog.dismiss());
            dialog.show();
        });

        convertView.setTag(item.taskId);
        return convertView;
    }

    private String getDateFromEndDateTag(String dateTag){
        String[] date = dateTag.split("-");
        int day = Integer.valueOf(date[2]);
        int month = Integer.valueOf(date[1]);

        String returnDate = "";
        switch(day){
            case 1:
            case 21:
            case 31:
                returnDate += day + "st";
                break;
            case 2:
            case 22:
                returnDate += day + "nd";
                break;
            case 3:
            case 23:
                returnDate += day + "rd";
                break;
            default:
                returnDate += day + context.getString(R.string._th);
        }

        switch(month){
            case 1:
                returnDate += " " + context.getString(R.string.january);
                break;
            case 2:
                returnDate += " " + context.getString(R.string.february);
                break;
            case 3:
                returnDate += " " + context.getString(R.string.march);
                break;
            case 4:
                returnDate += " " + context.getString(R.string.april);
                break;
            case 5:
                returnDate += " " + context.getString(R.string.may);
                break;
            case 6:
                returnDate += " " + context.getString(R.string.june);
                break;
            case 7:
                returnDate += " " + context.getString(R.string.july);
                break;
            case 8:
                returnDate += " " + context.getString(R.string.august);
                break;
            case 9:
                returnDate += " " + context.getString(R.string.september);
                break;
            case 10:
                returnDate += " " + context.getString(R.string.october);
                break;
            case 11:
                returnDate += " " + context.getString(R.string.november);
                break;
            default:
                returnDate += " " + context.getString(R.string.december);
        }

        returnDate += " " + date[0];

        return returnDate;
    }
}
