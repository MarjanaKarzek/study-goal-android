package com.studygoal.jisc.Fragments.ActivityFeed;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.studygoal.jisc.Adapters.FeedAdapter;
import com.studygoal.jisc.Adapters.PushAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.XApiManager;
import com.studygoal.jisc.Models.Feed;
import com.studygoal.jisc.Models.News;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.EditTextCustom;

import java.util.HashMap;

public class FeedFragment extends Fragment {
    private static final String TAG = FeedFragment.class.getSimpleName();

    public View mainView, tutorialMessage;
    private PushAdapter adapterPush;
    private FeedAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    public void onResume() {
        super.onResume();

        DataManager.getInstance().mainActivity.setTitle(getString(R.string.feed));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(5);

        new Thread(new Runnable() {
            @Override
            public void run() {
                DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DataManager.getInstance().mainActivity.showProgressBar(null);
                    }
                });
                NetworkManager.getInstance().getFeed(DataManager.getInstance().user.id);
                adapter.feedList = new Select().from(Feed.class).where("is_hidden = 0").execute();

                DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        if (adapter.feedList.size() == 0)
                            tutorialMessage.setVisibility(View.VISIBLE);
                        else
                            tutorialMessage.setVisibility(View.INVISIBLE);
                    }
                });
                NetworkManager.getInstance().getNewsFeed();
                adapterPush.newsList = new Select().from(News.class).execute();

                DataManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterPush.notifyDataSetChanged();
                        DataManager.getInstance().mainActivity.hideProgressBar();
                    }
                });
            }
        }).start();

        XApiManager.getInstance().sendLogActivityEvent(LogActivityEvent.NavigateActivityFeed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.layout_feed, container, false);
        layout = (SwipeRefreshLayout) mainView.findViewById(R.id.swipelayout);
        tutorialMessage = mainView.findViewById(R.id.tutorial_message);

        final EditTextCustom myEditText = (EditTextCustom) mainView.findViewById(R.id.message);
        myEditText.fragment = FeedFragment.this;

        final View floating_btn = mainView.findViewById(R.id.floating_btn);
        floating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionHandler.isConnected(getContext())) {
                    DataManager.getInstance().mainActivity.feedFragment = FeedFragment.this;
                    DataManager.getInstance().mainActivity.hideAllButtons();
                    DataManager.getInstance().mainActivity.showCertainButtons(6);
                    floating_btn.setVisibility(View.GONE);
                    myEditText.setText("");
                    mainView.findViewById(R.id.message).requestFocus();
                    InputMethodManager keyboard = (InputMethodManager) DataManager.getInstance().mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(mainView.findViewById(R.id.message), 0);
                    mainView.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                    mainView.findViewById(R.id.overlay).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DataManager.getInstance().mainActivity.hideAllButtons();
                            DataManager.getInstance().mainActivity.showCertainButtons(5);
                            mainView.findViewById(R.id.overlay).setVisibility(View.GONE);
                            View view = mainView.findViewById(R.id.message);
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) DataManager.getInstance().mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            floating_btn.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        layout.setColorSchemeResources(R.color.colorPrimary);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!ConnectionHandler.isConnected(getContext())) {
                            ConnectionHandler.showNoInternetConnectionSnackbar();
                        }
                        if (NetworkManager.getInstance().getFeed(DataManager.getInstance().user.id)) {
                            adapter.feedList = new Select().from(Feed.class).where("is_hidden = 0").execute();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    layout.setRefreshing(false);
                                    if (adapter.feedList.size() == 0)
                                        tutorialMessage.setVisibility(View.VISIBLE);
                                    else
                                        tutorialMessage.setVisibility(View.INVISIBLE);
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

        RecyclerView recyclerViewPush = (RecyclerView) mainView.findViewById(R.id.recycler_view_push);
        recyclerViewPush.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManagerPush = new LinearLayoutManager(DataManager.getInstance().mainActivity);
        linearLayoutManagerPush.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewPush.setLayoutManager(linearLayoutManagerPush);

        RecyclerView recyclerViewFeed = (RecyclerView) mainView.findViewById(R.id.recycler_view_feed);
        recyclerViewFeed.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManagerFeed = new LinearLayoutManager(DataManager.getInstance().mainActivity);
        linearLayoutManagerFeed.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewFeed.setLayoutManager(linearLayoutManagerFeed);

        NetworkManager.getInstance().getFriends(DataManager.getInstance().user.id);

        adapterPush = new PushAdapter(DataManager.getInstance().mainActivity, layout);
        recyclerViewPush.setAdapter(adapterPush);
        recyclerViewPush.setScrollContainer(false);

        adapter = new FeedAdapter(DataManager.getInstance().mainActivity, layout);
        recyclerViewFeed.setAdapter(adapter);
        recyclerViewFeed.setScrollContainer(false);

        Log.e(TAG, "FeedFragment Opened!");
        return mainView;
    }

    public void post() {
        String message = ((EditText) mainView.findViewById(R.id.message)).getText().toString();
        HashMap<String, String> map = new HashMap<>();
        map.put("student_id", DataManager.getInstance().user.id);
        map.put("message", message);
        if (NetworkManager.getInstance().postFeedMessage(map)) {
            if (NetworkManager.getInstance().getFeed(DataManager.getInstance().user.id)) {
                adapter.feedList = new Select().from(Feed.class).where("is_hidden = 0").execute();
                adapter.notifyDataSetChanged();
            }
            Snackbar.make(layout, R.string.posted_message, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(layout, R.string.fail_to_post_message, Snackbar.LENGTH_LONG).show();
        }
        mainView.findViewById(R.id.overlay).callOnClick();
    }
}
