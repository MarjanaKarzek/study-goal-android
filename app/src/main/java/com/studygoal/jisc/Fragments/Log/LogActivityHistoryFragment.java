package com.studygoal.jisc.Fragments.Log;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Adapters.ActivitiesHistoryAdapter;
import com.studygoal.jisc.Adapters.GenericAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.ActivityHistory;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class LogActivityHistoryFragment extends Fragment {
    private static final String TAG = LogActivityHistoryFragment.class.getSimpleName();

    private View mainView;
    private ListView list;
    private ActivitiesHistoryAdapter adapter;
    private SwipeRefreshLayout layout;
    private TextView message;

    @Override
    public void onResume() {
        super.onResume();

        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.activity_log));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(3);

        DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DataManager.getInstance().mainActivity.showProgressBar(null);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {

                NetworkManager.getInstance().getActivityHistory(DataManager.getInstance().user.id);

                DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.historyList = new Select().from(ActivityHistory.class).orderBy("activity_date DESC").execute();
                        adapter.notifyDataSetChanged();

                        if (adapter.historyList.size() == 0) {
                            message.setVisibility(View.VISIBLE);
                        } else {
                            message.setVisibility(View.GONE);
                        }
                        DataManager.getInstance().mainActivity.hideProgressBar();
                    }
                });
            }
        }).start();

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateLog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.layout_log_activity_history, container, false);
        layout = (SwipeRefreshLayout) mainView.findViewById(R.id.swipelayout);
        message = (TextView) mainView.findViewById(R.id.message);

        ((TextView) mainView.findViewById(R.id.activity_history_title)).setTypeface(DataManager.getInstance().myriadpro_regular);

        adapter = new ActivitiesHistoryAdapter(LogActivityHistoryFragment.this);
        list = (ListView) mainView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                String logId = v.getTag().toString().split(";")[0];
                String text = v.getTag().toString().split(";")[1];

                ActivityDetailsFragment fragment = new ActivityDetailsFragment();
                fragment.activityHistory = new Select().from(ActivityHistory.class).where("log_id=?", logId).executeSingle();
                fragment.title = text;

                DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, fragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

        layout.setColorSchemeResources(R.color.colorPrimary);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!ConnectionHandler.isConnected(getContext()))
                            ConnectionHandler.showNoInternetConnectionSnackbar();
                        if (NetworkManager.getInstance().getActivityHistory(DataManager.getInstance().user.id)) {
                            adapter.historyList = new Select().from(ActivityHistory.class).orderBy("activity_date DESC").execute();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    layout.setRefreshing(false);
                                    if (adapter.historyList.size() == 0)
                                        message.setVisibility(View.VISIBLE);
                                    else
                                        message.setVisibility(View.GONE);

                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout.setRefreshing(false);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        return mainView;
    }

    public void deleteLog(final ActivityHistory activityHistory, final int finalPosition) {

        if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LogActivityHistoryFragment.this.getActivity());
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_deleteactivitylog) + "</font>"));
            alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        if(ConnectionHandler.isConnected(getContext())) {
            final HashMap<String, String> params = new HashMap<>();
            params.put("log_id", activityHistory.id);
            DataManager.getInstance().mainActivity.showProgressBar(null);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (NetworkManager.getInstance().deleteActivity(params)) {
                        DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activityHistory.delete();
                                adapter.historyList.remove(finalPosition);
                                adapter.notifyDataSetChanged();
                                if (adapter.historyList.size() == 0)
                                    message.setVisibility(View.VISIBLE);
                                else
                                    message.setVisibility(View.GONE);

                                DataManager.getInstance().mainActivity.hideProgressBar();
                                Snackbar.make(mainView.findViewById(R.id.parent), R.string.record_deleted_successfully, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DataManager.getInstance().mainActivity.hideProgressBar();
                                Snackbar.make(mainView.findViewById(R.id.parent), R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();
        } else {
            ConnectionHandler.showNoInternetConnectionSnackbar();
        }
    }

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
                if(ConnectionHandler.isConnected(getContext())) {
                    if (position == 0) {
                        DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_fragment, new LogNewActivityFragment(), "newActivity")
                                .addToBackStack(null)
                                .commit();
                    } else {
                        LogLogActivityFragment fragment = new LogLogActivityFragment();
                        fragment.isInEditMode = false;
                        DataManager.getInstance().mainActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_fragment, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    dialog.dismiss();
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });
        dialog.show();
    }
}
