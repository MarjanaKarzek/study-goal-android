package com.studygoal.jisc.Receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;

/**
 * Notification Alarm Receiver
 * <p>
 * Generates a notification for the user when activated.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class NotificationAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String text =  context.getString(R.string.jisc_reminder_title) + " " + context.getString(R.string.dont_forget_break);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.log_icon_1)
                        .setContentTitle(context.getString(R.string.jisc_reminder_title))
                        .setContentText(context.getString(R.string.dont_forget_break))
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        DataManager.getInstance().mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        DataManager.getInstance().mNotificationManager.notify(1, mBuilder.build());
    }
}