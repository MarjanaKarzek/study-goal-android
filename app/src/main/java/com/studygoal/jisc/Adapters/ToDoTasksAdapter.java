package com.studygoal.jisc.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.ToDoTasks;
import com.studygoal.jisc.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ToDoTasksAdapter extends BaseAdapter {
    private static final String TAG = ToDoTasksAdapter.class.getSimpleName();

    public interface ToDoTasksAdapterListener {
        void onDelete(ToDoTasks target, int finalPosition);

        void onEdit(ToDoTasks targets);

        void onDone(ToDoTasks target);
    }

    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-mm-dd");

    private Context mContext;

    private ArrayList<ToDoTasks> mList;

    private ToDoTasksAdapterListener mListener;

    public ToDoTasksAdapter(Context context, ToDoTasksAdapterListener listener) {
        mContext = context;
        mListener = listener;
        mList = new ArrayList<>();
    }

    public void updateList(List<ToDoTasks> list) {
        if (mList != null) {
            mList.clear();
        } else {
            mList = new ArrayList<>();
        }

        if (list != null) {
            mList.addAll(list);
            notifyDataSetChanged();
        }

        Collections.sort(mList, (o1, o2) -> {
            int result = 0;

            if (o1 != null && o2 != null) {
                if (o1.fromTutor != null && o2.fromTutor != null) {
                    Boolean tutor1 = o1.fromTutor.toLowerCase().equals("yes");
                    Boolean tutor2 = o2.fromTutor.toLowerCase().equals("yes");
                    result = tutor2.compareTo(tutor1);
                }

                if (result == 0 && o1.isAccepted != null && o2.isAccepted != null) {
                    Boolean accepted1 = o1.isAccepted.toLowerCase().equals("0");
                    Boolean accepted2 = o2.isAccepted.toLowerCase().equals("0");
                    result += accepted2.compareTo(accepted1);
                }

                if (result == 0 && o1.description != null && o2.description != null) {
                    result = o1.description.toLowerCase().compareTo(o2.description.toLowerCase());
                }
            }

            return result;
        });
    }

    public void deleteItem(int position) {
        if (mList != null && position < mList.size()) {
            mList.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ToDoTasks getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ToDoTasks item = mList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_todo_tasks, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.target_item_text);
        textView.setTypeface(DataManager.getInstance().myriadpro_regular);

        String text = "";
        text += item.description + " " + mContext.getString(R.string._for) + " ";
        text += item.module + " " + mContext.getString(R.string.by).toLowerCase() + " ";

        boolean isToday = false;

        try {
            Date today = new Date();
            today.setTime(Calendar.getInstance().getTimeInMillis());
            Date data = sDateFormat.parse(item.endDate);
            isToday = data.getYear() == today.getYear()
                    && data.getMonth() == today.getMonth()
                    && data.getDay() == today.getDay();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (isToday) {
            text += mContext.getString(R.string.today);
        } else {
            text += item.endDate;
        }

        if (item.reason != null && !item.reason.isEmpty()) {
            text += " " + mContext.getString(R.string.because) + " " + item.reason;
        }

        textView.setText(text);

        final com.daimajia.swipe.SwipeLayout swipeLayout = (com.daimajia.swipe.SwipeLayout) convertView.findViewById(R.id.swipelayout);
        convertView.findViewById(R.id.edit).setOnClickListener(v -> {
            swipeLayout.close(true);

            if (mListener != null) {
                mListener.onEdit(item);
            }
        });

        convertView.findViewById(R.id.done).setOnClickListener(v -> {
            swipeLayout.close(true);

            if (mListener != null) {
                mListener.onDone(item);
            }
        });

        View mainLayout = convertView.findViewById(R.id.mainLayout);

        if (item.fromTutor != null && item.isAccepted != null && item.fromTutor.toLowerCase().equals("yes") && item.isAccepted.equals("0")) {
            mainLayout.setBackgroundColor(mContext.getResources().getColor(R.color.to_do_item_tutor_background));
        } else {
            mainLayout.setBackgroundColor(mContext.getResources().getColor(R.color.to_do_item_general_background));
        }

        final int finalPosition = position;
        convertView.findViewById(R.id.delete).setOnClickListener(v -> {
            final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_confirmation);
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

                if (mListener != null) {
                    mListener.onDelete(item, finalPosition);
                }
            });
            dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> dialog.dismiss());
            dialog.show();
        });

        convertView.setTag(item.taskId);
        return convertView;
    }
}
