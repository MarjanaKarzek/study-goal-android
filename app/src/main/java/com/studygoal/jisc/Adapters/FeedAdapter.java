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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.swipe.SwipeLayout;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.LinguisticManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Managers.SocialManager;
import com.studygoal.jisc.Models.Feed;
import com.studygoal.jisc.Models.Friend;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.CircleTransform;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private static final String TAG = FeedAdapter.class.getSimpleName();

    public List<Feed> feedList = new ArrayList<>();
    private Context context;
    private SwipeRefreshLayout layout;

    public FeedAdapter(Context context, SwipeRefreshLayout layout) {
        this.context = context;
        this.layout = layout;
        feedList = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    private void removeItem(int position) {
        feedList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(final FeedViewHolder feedViewHolder, final int position) {
        final Feed item = feedList.get(position);
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

        if (item.activity_type.toLowerCase().equals("friend_request"))
            feedViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager.getInstance().mainActivity.friend.setTag("from_list");
                    DataManager.getInstance().mainActivity.friend.callOnClick();
                }
            });
        else if (item.activity_type.toLowerCase().equals("push_notification"))
            GlideApp.with(context).load(R.drawable.notification_image).into(feedViewHolder.profilePic);
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View itemView;
        itemView = LayoutInflater.
                from(parent.getContext()).inflate(R.layout.list_item_feed, parent, false);
        return new FeedViewHolder(itemView);
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        protected TextView message;
        public ImageView profilePic;
        protected TextView feed;
        public TextView timeAgo;
        public View menu;
        public View bottomBar;
        public View body;
        public View facebookButton, twitterButton, mailButton;
        public View selfPost;

        public SwipeLayout swipelayout;
        public RelativeLayout deleteButton;
        public RelativeLayout shareButton;

        protected View share;

        public View view;

        public FeedViewHolder(View view) {
            super(view);
            try {
                message = (TextView) view.findViewById(R.id.message);
                message.setTypeface(DataManager.getInstance().myriadpro_regular);
            } catch (Exception ignored) {
            }
            this.view = view;
            try {
                swipelayout = (SwipeLayout) view.findViewById(R.id.swipelayout);
                deleteButton = (RelativeLayout) view.findViewById(R.id.delete);
                shareButton = (RelativeLayout) view.findViewById(R.id.share);
                profilePic = (ImageView) view.findViewById(R.id.feed_item_profile);
                feed = (TextView) view.findViewById(R.id.feed_item_feed);
                timeAgo = (TextView) view.findViewById(R.id.feed_item_time_ago);
                bottomBar = view.findViewById(R.id.feed_item_bottom_bar);
                body = view.findViewById(R.id.feed_item_body);
                facebookButton = view.findViewById(R.id.facebook_btn);
                twitterButton = view.findViewById(R.id.twitter_btn);
                mailButton = view.findViewById(R.id.mail_btn);

                feed.setTypeface(DataManager.getInstance().myriadpro_regular);
                timeAgo.setTypeface(DataManager.getInstance().myriadpro_regular);
            } catch (Exception ignored) { }
        }
    }
}
