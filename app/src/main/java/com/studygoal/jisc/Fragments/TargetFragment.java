package com.studygoal.jisc.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.TargetAdapter;
import com.studygoal.jisc.Adapters.ToDoTasksAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Targets;
import com.studygoal.jisc.Models.ToDoTasks;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.databinding.TargetFragmentBinding;

import org.apache.http.params.CoreConnectionPNames;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TargetFragment extends BaseFragment {
    private static final String TAG = TargetFragment.class.getSimpleName();

    private TargetAdapter mAdapterTarget;
    private ToDoTasksAdapter mAdapterToDo;

    private View mRootView;
    private View mTutorialMessage;
    private SwipeRefreshLayout mLayout;

    private TargetFragmentBinding mBinding = null;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.target));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(4);

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateTargetsMain);
        loadData(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.target_fragment, container, false);
        mRootView = mBinding.getRoot();

        mBinding.targetSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.target_recurring) {
                mBinding.list.setVisibility(View.VISIBLE);
                mBinding.listTodo.setVisibility(View.GONE);
            } else {
                mBinding.list.setVisibility(View.GONE);
                mBinding.listTodo.setVisibility(View.VISIBLE);
            }

            updateTutorialMessage();
        });

        mTutorialMessage = mRootView.findViewById(R.id.tutorial_message);
        mLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipelayout);

        mAdapterTarget = new TargetAdapter(getActivity(), new TargetAdapter.TargetAdapterListener() {
            @Override
            public void onDelete(Targets target, int finalPosition) {
                if(ConnectionHandler.isConnected(getContext())) {
                    deleteTarget(target, finalPosition);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }

            @Override
            public void onEdit(Targets targets) {
                if(ConnectionHandler.isConnected(getContext())) {
                    editTarget(targets);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        mAdapterToDo = new ToDoTasksAdapter(getActivity(), new ToDoTasksAdapter.ToDoTasksAdapterListener() {
            @Override
            public void onDelete(ToDoTasks target, int finalPosition) {
                if(ConnectionHandler.isConnected(getContext())) {
                    deleteToDoTasks(target, finalPosition);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }

            @Override
            public void onEdit(ToDoTasks targets) {
                if(ConnectionHandler.isConnected(getContext())){
                    editToDoTasks(targets);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }

            @Override
            public void onDone(ToDoTasks target) {
                if(ConnectionHandler.isConnected(getContext())) {
                    completeToDoTask(target);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        mBinding.list.setAdapter(mAdapterTarget);
        mBinding.listTodo.setAdapter(mAdapterToDo);

        mBinding.list.setOnItemClickListener((parent, v, position, id) -> {
            TargetDetails fragment = new TargetDetails();
            fragment.list = mAdapterTarget.list;
            fragment.position = position;
            DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        mBinding.listTodo.setOnItemClickListener((parent, v, position, id) -> {
            ToDoTasks item = mAdapterToDo.getItem(position);

            if (item != null) {
                if (item.fromTutor != null && item.isAccepted != null && item.fromTutor.toLowerCase().equals("yes") && item.isAccepted.equals("0")) {
                    showAcceptTaskDialog(item);
                }
            }
        });

        mBinding.list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (mBinding.list == null || mBinding.list.getChildCount() == 0) ? 0 : mBinding.list.getChildAt(0).getTop();
                mLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        mBinding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.swipe_for_edit_delete, Snackbar.LENGTH_LONG).show();
            }
        });

        mBinding.listTodo.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (mBinding.listTodo == null || mBinding.listTodo.getChildCount() == 0) ? 0 : mBinding.listTodo.getChildAt(0).getTop();
                mLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        mBinding.listTodo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.swipe_for_done_edit_delete, Snackbar.LENGTH_LONG).show();
            }
        });

        mLayout.setColorSchemeResources(R.color.colorPrimary);
        mLayout.setOnRefreshListener(() -> loadData(false));

        return mRootView;
    }

    private void deleteTarget(final Targets target, final int finalPosition) {
        if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TargetFragment.this.getActivity());
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_deletetarget) + "</font>"));
            alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        final HashMap<String, String> params = new HashMap<>();
        params.put("student_id", DataManager.getInstance().user.id);
        params.put("target_id", target.target_id);
        DataManager.getInstance().mainActivity.showProgressBar(null);

        new Thread(() -> {
            if (NetworkManager.getInstance().deleteTarget(params)) {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    target.delete();
                    mAdapterTarget.list.remove(finalPosition);
                    if (mAdapterTarget.list.size() == 0)
                        mTutorialMessage.setVisibility(View.VISIBLE);
                    else
                        mTutorialMessage.setVisibility(View.GONE);
                    mAdapterTarget.notifyDataSetChanged();
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(mRootView.findViewById(R.id.parent), R.string.target_deleted_successfully, Snackbar.LENGTH_LONG).show();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(mRootView.findViewById(R.id.parent), R.string.fail_to_delete_target_message, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void deleteToDoTasks(final ToDoTasks task, final int finalPosition) {
        if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TargetFragment.this.getActivity());
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_deletetarget) + "</font>"));
            alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        final HashMap<String, String> params = new HashMap<>();
        params.put("student_id", DataManager.getInstance().user.id);
        params.put("record_id", task.taskId);
        DataManager.getInstance().mainActivity.showProgressBar(null);

        new Thread(() -> {
            if (NetworkManager.getInstance().deleteToDoTask(params)) {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    task.delete();
                    mAdapterToDo.deleteItem(finalPosition);
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    updateTutorialMessage();
                    Snackbar.make(mRootView.findViewById(R.id.parent), R.string.target_deleted_successfully, Snackbar.LENGTH_LONG).show();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(mRootView.findViewById(R.id.parent), R.string.fail_to_delete_target_message, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void editTarget(Targets item) {
        AddTarget fragment = new AddTarget();
        fragment.isInEditMode = true;
        fragment.isSingleTarget = false;
        fragment.item = item;

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void editToDoTasks(ToDoTasks item) {
        AddTarget fragment = new AddTarget();
        fragment.isInEditMode = true;
        fragment.isSingleTarget = true;
        fragment.itemToDo = item;

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void acceptToDoTask(ToDoTasks item){
        showAcceptTaskDialog(item);
    }

    private void completeToDoTask(ToDoTasks item) {
        processCompletionTask(item);
    }

    private void loadData(boolean showProgress) {
        if (showProgress) {
            runOnUiThread(() -> DataManager.getInstance().mainActivity.showProgressBar(null));
        }

        new Thread(() -> {
            NetworkManager.getInstance().getStretchTargets(DataManager.getInstance().user.id);
            NetworkManager.getInstance().getTargets(DataManager.getInstance().user.id);
            NetworkManager.getInstance().getToDoTasks(DataManager.getInstance().user.id);

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                mAdapterTarget.list = new Select().from(Targets.class).execute();
                mAdapterTarget.notifyDataSetChanged();

                //mAdapterToDo.updateList(new Select().from(ToDoTasks.class).execute());

                List<ToDoTasks> currentTaskList = new Select().from(ToDoTasks.class).execute();
                Iterator iterator = currentTaskList.iterator();

                while (iterator.hasNext()){
                    ToDoTasks currentTask = (ToDoTasks)iterator.next();

                    if(currentTask.status.equals("1")) {
                        iterator.remove();
                    } else if (currentTask.isAccepted.equals("2")) {
                        iterator.remove();
                    }
                }

                /*Collections.sort(currentTaskList, new Comparator<ToDoTasks>() {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    @Override
                    public int compare(ToDoTasks taskA, ToDoTasks taskB)
                    {
                        Date date1 = new Date();
                        try {
                            date1 = dateFormat.parse(taskA.endDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Date date2 = new Date();
                        try {
                            date2 = dateFormat.parse(taskB.endDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        return Long.valueOf(date1.getTime()).compareTo(Long.valueOf(date2.getTime()));
                    }
                });*/

                mAdapterToDo.updateList(currentTaskList);
                mAdapterToDo.notifyDataSetChanged();

                if (showProgress) {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                } else {
                    mLayout.setRefreshing(false);
                }
            });

            updateTutorialMessage();
        }).start();
    }

    private void updateTutorialMessage() {
        runOnUiThread(() -> {
            if (mBinding.targetRecurring.isChecked()) {
                if (mAdapterTarget != null && mAdapterTarget.list.size() > 0) {
                    mTutorialMessage.setVisibility(View.GONE);
                } else {
                    mTutorialMessage.setVisibility(View.VISIBLE);
                }
            } else if (mBinding.targetSingle.isChecked()) {
                if (mAdapterToDo != null && mAdapterToDo.getCount() > 0) {
                    mTutorialMessage.setVisibility(View.GONE);
                } else {
                    mTutorialMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showAcceptTaskDialog(ToDoTasks item) {
        if(ConnectionHandler.isConnected(getContext())) {
            final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_accept_task);
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
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.to_do_task_accept_dialog_title);

            ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.to_do_task_accept_dialog_message);

            ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

            ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

            ((TextView) dialog.findViewById(R.id.dialog_cancel_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView) dialog.findViewById(R.id.dialog_cancel_text)).setText(R.string.cancel);

            dialog.findViewById(R.id.dialog_ok).setOnClickListener(v1 -> {
                dialog.dismiss();
                processAcceptTask(item);
            });

            dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> {
                dialog.dismiss();
                showDeclineTaskDialog(item);
            });

            dialog.findViewById(R.id.dialog_cancel).setOnClickListener(v12 -> {
                dialog.dismiss();
            });

            dialog.show();
        } else {
            ConnectionHandler.showNoInternetConnectionSnackbar();
        }
    }

    private void showDeclineTaskDialog(ToDoTasks item) {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_decline_task);
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
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.to_do_task_decline_dialog_title);

        ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.to_do_task_decline_dialog_message);

        ((TextView) dialog.findViewById(R.id.reason)).setTypeface(DataManager.getInstance().myriadpro_regular);
        ((TextView) dialog.findViewById(R.id.reason_title)).setTypeface(DataManager.getInstance().myriadpro_regular);

        ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
        ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

        ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
        ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

        final EditText reason = ((EditText) dialog.findViewById(R.id.reason));
        dialog.findViewById(R.id.dialog_ok).setOnClickListener(v1 -> {
            dialog.dismiss();
            processDeclineTask(item, reason.getText().toString());
        });

        dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void processAcceptTask(ToDoTasks item) {
        runOnUiThread(() -> DataManager.getInstance().mainActivity.showProgressBar(null));

        new Thread(() -> {
            ActiveAndroid.beginTransaction();
            item.isAccepted = "2";
            item.save();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();

            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", item.endDate);
            params.put("record_id", item.taskId);
            params.put("module", item.module);
            params.put("description", item.description);
            params.put("reason", item.reason);
            params.put("is_accepted", item.isAccepted);

            if (NetworkManager.getInstance().editToDoTask(params)) {
                loadData(true);
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(mRootView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void processDeclineTask(ToDoTasks item, String declineReason) {
        runOnUiThread(() -> DataManager.getInstance().mainActivity.showProgressBar(null));

        new Thread(() -> {
            ActiveAndroid.beginTransaction();
            item.isAccepted = "2";

            if (declineReason != null) {
                item.reasonForIgnoring = declineReason;
            } else {
                item.reasonForIgnoring = "";
            }

            item.save();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();

            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", item.endDate);
            params.put("record_id", item.taskId);
            params.put("module", item.module);
            params.put("description", item.description);
            params.put("reason", item.reason);
            params.put("is_accepted ", item.isAccepted);
            params.put("reason_for_ignoring ", item.reasonForIgnoring);

            if (NetworkManager.getInstance().editToDoTask(params)) {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(mRootView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void processCompletionTask(ToDoTasks item) {
        if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TargetFragment.this.getActivity());
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_completetarget) + "</font>"));
            alertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }
        runOnUiThread(() -> DataManager.getInstance().mainActivity.showProgressBar(null));

        new Thread(() -> {
            final HashMap<String, String> params = new HashMap<>();
            params.put("student_id", DataManager.getInstance().user.id);
            params.put("end_date", item.endDate);
            params.put("record_id", item.taskId);
            params.put("module", item.module);
            params.put("description", item.description);
            params.put("reason", item.reason);
            params.put("is_completed", "1");

            if (NetworkManager.getInstance().editToDoTask(params)) {
                loadData(true);
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(mRootView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
