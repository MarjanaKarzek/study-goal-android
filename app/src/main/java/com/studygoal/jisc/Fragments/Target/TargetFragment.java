package com.studygoal.jisc.Fragments.Target;

import android.app.AlertDialog;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.RecurringTargetAdapter;
import com.studygoal.jisc.Adapters.SingleTargetAdapter;
import com.studygoal.jisc.Fragments.BaseFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Targets;
import com.studygoal.jisc.Models.ToDoTasks;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.databinding.TargetFragmentBinding;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Target Fragment class
 * <p>
 * Provides the handling of the view of "Target". Provides CRUD for target items.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class TargetFragment extends BaseFragment {
    private static final String TAG = TargetFragment.class.getSimpleName();

    private RecurringTargetAdapter recurringTargetAdapter;
    private SingleTargetAdapter singleTargetAdapter;

    private View rootView;
    private View tutorialMessage;
    private SwipeRefreshLayout layout;

    private TargetFragmentBinding binding = null;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.target));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(4);

        binding.targetSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.target_recurring) {
                binding.list.setVisibility(View.VISIBLE);
                binding.listTodo.setVisibility(View.GONE);
                DataManager.getInstance().mainActivity.displaySingleTarget = false;
                XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateTargetsMain);
            } else {
                binding.list.setVisibility(View.GONE);
                binding.listTodo.setVisibility(View.VISIBLE);
                DataManager.getInstance().mainActivity.displaySingleTarget = true;
                XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateTargetsMain);
            }

            updateTutorialMessage();
        });

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateTargetsMain);
        loadData(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boolean originalSingleTargetValue = DataManager.getInstance().mainActivity.displaySingleTarget;

        binding = DataBindingUtil.inflate(inflater, R.layout.target_fragment, container, false);
        rootView = binding.getRoot();

        tutorialMessage = rootView.findViewById(R.id.tutorial_message);
        layout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipelayout);

        NetworkManager.getInstance().getActivityHistory(DataManager.getInstance().user.id);

        recurringTargetAdapter = new RecurringTargetAdapter(getActivity(), new RecurringTargetAdapter.TargetAdapterListener() {
            @Override
            public void onDelete(Targets target, int finalPosition) {
                if (ConnectionHandler.isConnected(getContext())) {
                    deleteTarget(target, finalPosition);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }

            @Override
            public void onEdit(Targets targets) {
                if (ConnectionHandler.isConnected(getContext())) {
                    editTarget(targets);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        singleTargetAdapter = new SingleTargetAdapter(getActivity(), new SingleTargetAdapter.SingleTargetAdapterListener() {
            @Override
            public void onDelete(ToDoTasks target, int finalPosition) {
                if (ConnectionHandler.isConnected(getContext())) {
                    deleteSingleTarget(target, finalPosition);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }

            @Override
            public void onEdit(ToDoTasks targets) {
                if (ConnectionHandler.isConnected(getContext())) {
                    editSingleTarget(targets);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }

            @Override
            public void onDone(ToDoTasks target) {
                if (ConnectionHandler.isConnected(getContext())) {
                    completeSingleTarget(target);

                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    Date startDate;
                    try {
                        startDate = df.parse(target.endDate);
                        if (startDate.getTime() <= Calendar.getInstance().getTimeInMillis()) {
                            XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.CompleteSingleTarget);
                        } else {
                            XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.CompleteOverdueSingleTarget);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        binding.list.setAdapter(recurringTargetAdapter);
        binding.listTodo.setAdapter(singleTargetAdapter);

        binding.list.setOnItemClickListener((parent, v, position, id) -> {
            TargetDetailsFragment fragment = new TargetDetailsFragment();
            fragment.position = position;
            DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.listTodo.setOnItemClickListener((parent, v, position, id) -> {
            ToDoTasks item = singleTargetAdapter.getItem(position);

            if (item != null) {
                if (item.fromTutor != null && item.isAccepted != null && item.fromTutor.toLowerCase().equals("yes") && item.isAccepted.equals("0")) {
                    showAcceptSingleTargetDialog(item);
                } else {
                    Snackbar.make(DataManager.getInstance().mainActivity.findViewById(R.id.drawer_layout), R.string.swipe_for_done_edit_delete, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        binding.list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (binding.list == null || binding.list.getChildCount() == 0) ? 0 : binding.list.getChildAt(0).getTop();
                layout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        binding.listTodo.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (binding.listTodo == null || binding.listTodo.getChildCount() == 0) ? 0 : binding.listTodo.getChildAt(0).getTop();
                layout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        layout.setColorSchemeResources(R.color.colorPrimary);
        layout.setOnRefreshListener(() -> loadData(false));

        if (originalSingleTargetValue) {
            Log.d(TAG, "onCreateView: single target was originally selected");
            binding.targetSingle.setChecked(true);
            binding.targetRecurring.setChecked(false);
            binding.list.setVisibility(View.GONE);
            binding.listTodo.setVisibility(View.VISIBLE);
            DataManager.getInstance().mainActivity.displaySingleTarget = true;
        } else {
            Log.d(TAG, "onCreateView: single target was originally not selected");
            binding.list.setVisibility(View.VISIBLE);
            binding.listTodo.setVisibility(View.GONE);
            DataManager.getInstance().mainActivity.displaySingleTarget = false;
        }

        return rootView;
    }

    /**
     * Displays tutorial message if single or recurring targets is empty.
     */
    private void updateTutorialMessage() {
        runOnUiThread(() -> {
            if (binding.targetRecurring.isChecked()) {
                if (recurringTargetAdapter != null && recurringTargetAdapter.list.size() > 0) {
                    tutorialMessage.setVisibility(View.GONE);
                } else {
                    tutorialMessage.setVisibility(View.VISIBLE);
                }
            } else if (binding.targetSingle.isChecked()) {
                if (singleTargetAdapter != null && singleTargetAdapter.getCount() > 0) {
                    tutorialMessage.setVisibility(View.GONE);
                } else {
                    tutorialMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // Data

    /**
     * Loads the data of single and recurring targets.
     *
     * @param showProgress whether the progress bar should be shown or not
     */
    private void loadData(boolean showProgress) {
        if (showProgress) {
            runOnUiThread(() -> DataManager.getInstance().mainActivity.showProgressBar(null));
        }

        new Thread(() -> {
            NetworkManager.getInstance().getStretchTargets(DataManager.getInstance().user.id);
            NetworkManager.getInstance().getTargets(DataManager.getInstance().user.id);
            NetworkManager.getInstance().getToDoTasks(DataManager.getInstance().user.id);

            DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                recurringTargetAdapter.list = new Select().from(Targets.class).execute();
                recurringTargetAdapter.notifyDataSetChanged();

                List<ToDoTasks> currentTaskList = new Select().from(ToDoTasks.class).where("status != 1 and from_tutor = ? and is_accepted = 0", "yes").execute();
                currentTaskList.addAll(new Select().from(ToDoTasks.class).where("status != 1 and is_accepted != 2 and ((from_tutor = ? and is_accepted = 1) or from_tutor = ?)", "yes", "no").execute());

                singleTargetAdapter.updateList(currentTaskList);
                singleTargetAdapter.notifyDataSetChanged();

                if (showProgress) {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                } else {
                    layout.setRefreshing(false);
                }
            });

            updateTutorialMessage();
        }).start();
    }

    // Recurring targets

    /**
     * Deletes the recurring target selected.
     *
     * @param target        object to be deleted
     * @param finalPosition position in list
     */
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
                    recurringTargetAdapter.list.remove(finalPosition);
                    if (recurringTargetAdapter.list.size() == 0)
                        tutorialMessage.setVisibility(View.VISIBLE);
                    else
                        tutorialMessage.setVisibility(View.GONE);
                    recurringTargetAdapter.notifyDataSetChanged();
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(rootView.findViewById(R.id.parent), R.string.target_deleted_successfully, Snackbar.LENGTH_LONG).show();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(rootView.findViewById(R.id.parent), R.string.fail_to_delete_target_message, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Starts editing process for the selected target.
     *
     * @param item target selected
     */
    private void editTarget(Targets item) {
        AddTargetFragment fragment = new AddTargetFragment();
        fragment.isInEditMode = true;
        fragment.isSingleTarget = false;
        fragment.item = item;

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Single Targets

    /**
     * Deletes the selected single target.
     *
     * @param task
     * @param finalPosition
     */
    private void deleteSingleTarget(final ToDoTasks task, final int finalPosition) {
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
                    singleTargetAdapter.deleteItem(finalPosition);
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    updateTutorialMessage();
                    Snackbar.make(rootView.findViewById(R.id.parent), R.string.target_deleted_successfully, Snackbar.LENGTH_LONG).show();
                });
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(rootView.findViewById(R.id.parent), R.string.fail_to_delete_target_message, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Starts editing process for single target.
     *
     * @param item target to be edited
     */
    private void editSingleTarget(ToDoTasks item) {
        AddTargetFragment fragment = new AddTargetFragment();
        fragment.isInEditMode = true;
        fragment.isSingleTarget = true;
        fragment.itemToDo = item;

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Sets the selected target to completed.
     *
     * @param item target to be completed
     */
    private void completeSingleTarget(ToDoTasks item) {
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
                    Snackbar.make(rootView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // Single Targets from Tutor

    /**
     * Shows dialog to accept the single target from a tutor.
     *
     * @param item single target to be accepted
     */
    private void showAcceptSingleTargetDialog(ToDoTasks item) {
        if (ConnectionHandler.isConnected(getContext())) {
            final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_dialog_accept_task);
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
                processAcceptSingleTarget(item);
            });

            dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> {
                dialog.dismiss();
                showDeclineSingleTargetDialog(item);
            });

            dialog.findViewById(R.id.dialog_cancel).setOnClickListener(v12 -> {
                dialog.dismiss();
            });

            dialog.show();
        } else {
            ConnectionHandler.showNoInternetConnectionSnackbar();
        }
    }

    /**
     * Shows dialog to decline a single target from a tutor.
     *
     * @param item single target to be declined
     */
    private void showDeclineSingleTargetDialog(ToDoTasks item) {
        final Dialog dialog = new Dialog(DataManager.getInstance().mainActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_decline_task);
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
            processDeclineSingleTarget(item, reason.getText().toString());
        });

        dialog.findViewById(R.id.dialog_no).setOnClickListener(v12 -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Sets the single target from a tutor to accepted.
     *
     * @param item target to be accepted
     */
    private void processAcceptSingleTarget(ToDoTasks item) {
        runOnUiThread(() -> DataManager.getInstance().mainActivity.showProgressBar(null));

        new Thread(() -> {
            ActiveAndroid.beginTransaction();
            item.isAccepted = "1";
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
                XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.AcceptSingleTarget);
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(rootView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Sets the single target from a tutor to declined.
     *
     * @param item target to be declined
     */
    private void processDeclineSingleTarget(ToDoTasks item, String declineReason) {
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
            params.put("is_accepted", item.isAccepted);
            params.put("reason_for_ignoring", item.reasonForIgnoring);

            if (NetworkManager.getInstance().editToDoTask(params)) {
                loadData(true);
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                });
                XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.DeclineSingleTarget);
            } else {
                DataManager.getInstance().mainActivity.runOnUiThread(() -> {
                    DataManager.getInstance().mainActivity.hideProgressBar();
                    Snackbar.make(rootView, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
