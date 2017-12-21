package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activeandroid.util.Log;
import com.studygoal.jisc.Managers.LinguisticManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.News;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.GlideConfig.GlideApp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Log Adapter
 * <p>
 * Handles log items.
 *
 * @author Therapy Box - Marjana Karzek
 * @version 1.5
 * @date 30/11/17
 */
public class PushAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private static final String TAG = "PushAdapter";

    private Context context;
    public List<News> newsList = new ArrayList<>();

    public PushAdapter(Context context) {
        this.context = context;
        newsList = new ArrayList<>();
    }

    @Override
    public FeedAdapter.FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.
                from(parent.getContext()).inflate(R.layout.list_item_feed, parent, false);
        return new FeedAdapter.FeedViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FeedAdapter.FeedViewHolder feedViewHolder, int position) {
        final News item = newsList.get(position);
        Log.e(getClass().getCanonicalName(), item.toString());

        feedViewHolder.body.setBackgroundColor(Color.parseColor("#ffbad8f7"));
        feedViewHolder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkManager.getInstance().markNewsAsRead(item);
                removeItem(feedViewHolder.getAdapterPosition());
            }
        });

        GlideApp.with(context).load(R.drawable.notification_image).into(feedViewHolder.profilePic);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        long currentTime = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            calendar.setTime(dateFormat.parse(item.created_date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long createdDate = calendar.getTimeInMillis();
        long difference = (currentTime - createdDate) / 60000;

        if (difference <= 1)
            feedViewHolder.timeAgo.setText(context.getString(R.string.just_a_moment_ago));
        else if (difference < 59)
            feedViewHolder.timeAgo.setText(difference + " " + context.getString(R.string.minutes_ago));
        else if (difference < 120)
            feedViewHolder.timeAgo.setText("1 " + context.getString(R.string.hour_ago));
        else if (difference < 1440)
            feedViewHolder.timeAgo.setText((difference / 60) + " " + context.getString(R.string.hours_ago));
        else
            feedViewHolder.timeAgo.setText(
                    context.getString(R.string.on) + " "
                            + item.created_date.split(" ")[0].split("-")[2] + " "
                            + LinguisticManager.getInstance().getShortMonth(item.created_date.split(" ")[0].split("-")[1]) + " " + item.created_date.split(" ")[0].split("-")[0]);

        feedViewHolder.feed.setText(item.message);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /**
     * Deletes item at given position from current list.
     *
     * @param position position of the item that is going to be deleted
     */
    private void removeItem(int position) {
        newsList.remove(position);
        notifyItemRemoved(position);
    }
}
