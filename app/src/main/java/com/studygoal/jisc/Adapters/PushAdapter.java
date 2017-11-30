package com.studygoal.jisc.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.LinguisticManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.SocialManager;
import com.studygoal.jisc.Models.Feed;
import com.studygoal.jisc.Models.Friend;
import com.studygoal.jisc.Models.News;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by therapybox on 30/11/17.
 */

public class PushAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private static final String TAG = "PushAdapter";

    private Context context;
    public List<News> newsList = new ArrayList<>();
    private SwipeRefreshLayout layout;

    public PushAdapter(Context context, SwipeRefreshLayout layout){
        this.context = context;
        newsList = new ArrayList<>();
        this.layout = layout;
    }

    @Override
    public FeedAdapter.FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.
                from(parent.getContext()).inflate(R.layout.list_item_feed, parent, false);
        return new FeedAdapter.FeedViewHolder(itemView);
    }

    private void removeItem(int position) {
        newsList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(FeedAdapter.FeedViewHolder feedViewHolder, int position) {
        final News item = newsList.get(position);
        Log.e(getClass().getCanonicalName(), item.toString());
        feedViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")){
                    Snackbar.make(layout, R.string.demo_mode_sharefeedlog, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if(ConnectionHandler.isConnected(context)) {
                    SocialManager.getInstance().shareOnIntent(item.message);
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        feedViewHolder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedViewHolder.bottomBar.setVisibility(View.VISIBLE);
                feedViewHolder.close.setVisibility(View.GONE);
                feedViewHolder.menu.setVisibility(View.GONE);
                feedViewHolder.feed.setVisibility(View.VISIBLE);
            }
        });

        if (feedViewHolder.close.getVisibility() == View.VISIBLE)
            feedViewHolder.close.callOnClick();

        feedViewHolder.hidePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")){
                    feedViewHolder.close.callOnClick();
                    removeItem(feedViewHolder.getAdapterPosition());
                    Snackbar.make(layout, R.string.post_hidden_message, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if(ConnectionHandler.isConnected(context)) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("feed_id", item.id);
                    map.put("student_id", DataManager.getInstance().user.id);
                    if (NetworkManager.getInstance().hidePost(map)) {
                        feedViewHolder.close.callOnClick();
                        removeItem(feedViewHolder.getAdapterPosition());
                        Snackbar.make(layout, R.string.post_hidden_message, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(layout, R.string.failed_to_hide_message, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        if (item.message_from.equals(DataManager.getInstance().user.id)) {
            //feedViewHolder.open.setVisibility(View.GONE);
            if(DataManager.getInstance().user.profile_pic != null) {
                if (!DataManager.getInstance().user.profile_pic.equals("")) {
                    Log.e("TEST", NetworkManager.getInstance().host + DataManager.getInstance().user.profile_pic);
                    GlideApp.with(feedViewHolder.itemView.getContext())
                            .load(NetworkManager.getInstance().host + DataManager.getInstance().user.profile_pic)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    e.printStackTrace();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .placeholder(R.drawable.profilenotfound)
                            .transform(new CircleTransform(context))
                            .into(feedViewHolder.profilePic);
                } else {
                    GlideApp.with(context).load(R.drawable.profilenotfound).into(feedViewHolder.profilePic);
                }
            }

            feedViewHolder.swipelayout.setSwipeEnabled(true);
            feedViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataManager.getInstance().mainActivity);
                        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + DataManager.getInstance().mainActivity.getString(R.string.demo_mode_deletefeedlog) + "</font>"));
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

                    if(ConnectionHandler.isConnected(context)) {
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
                        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(R.string.confirm);

                        ((TextView) dialog.findViewById(R.id.dialog_message)).setTypeface(DataManager.getInstance().myriadpro_regular);
                        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.confirm_delete_feed);

                        ((TextView) dialog.findViewById(R.id.dialog_no_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                        ((TextView) dialog.findViewById(R.id.dialog_no_text)).setText(R.string.no);

                        ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setTypeface(DataManager.getInstance().myriadpro_regular);
                        ((TextView) dialog.findViewById(R.id.dialog_ok_text)).setText(R.string.yes);

                        dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                feedViewHolder.swipelayout.close(true);

                                if (NetworkManager.getInstance().deleteFeed(item.id)) {
                                    removeItem(feedViewHolder.getAdapterPosition());
                                }
                            }
                        });
                        dialog.findViewById(R.id.dialog_no).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                feedViewHolder.swipelayout.close(true);
                            }
                        });
                        dialog.show();
                    } else {
                        ConnectionHandler.showNoInternetConnectionSnackbar();
                    }
                }
            });
        } else {
            feedViewHolder.swipelayout.setSwipeEnabled(false);
            feedViewHolder.deleteButton.setOnClickListener(null);
            Friend friend = new Select().from(Friend.class).where("friend_id = ?", item.message_from).executeSingle();
            String photo;
            if (friend != null) {
                photo = friend.profile_pic;
            } else
                photo = "";

            if (photo.equals(""))
                GlideApp.with(context).load(R.drawable.profilenotfound).into(feedViewHolder.profilePic);
            else
                GlideApp.with(context)
                        .load(NetworkManager.getInstance().host + photo)
                        .transform(new CircleTransform(context))
                        .into(feedViewHolder.profilePic);
        }

        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        long current_time = System.currentTimeMillis();

        c.set(Integer.parseInt(item.created_date.split(" ")[0].split("-")[0]),
                Integer.parseInt(item.created_date.split(" ")[0].split("-")[1]) - 1,
                Integer.parseInt(item.created_date.split(" ")[0].split("-")[2]),
                Integer.parseInt(item.created_date.split(" ")[1].split(":")[0]),
                Integer.parseInt(item.created_date.split(" ")[1].split(":")[1]));

        long created_date = c.getTimeInMillis();
        long diff = (current_time - created_date) / 60000;

        if (diff <= 1)
            feedViewHolder.timeAgo.setText(context.getString(R.string.just_a_moment_ago));
        else if (diff < 59)
            feedViewHolder.timeAgo.setText(diff + " " + context.getString(R.string.minutes_ago));
        else if (diff < 120)
            feedViewHolder.timeAgo.setText("1 " + context.getString(R.string.hour_ago));
        else if (diff < 1440)
            feedViewHolder.timeAgo.setText((diff / 60) + " " + context.getString(R.string.hours_ago));
        else
            feedViewHolder.timeAgo.setText(
                    context.getString(R.string.on) + " "
                            + item.created_date.split(" ")[0].split("-")[2] + " "
                            + LinguisticManager.getInstance().convertMonth(item.created_date.split(" ")[0].split("-")[1]) + " " + item.created_date.split(" ")[0].split("-")[0]);

        feedViewHolder.feed.setText(item.message);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }
}
