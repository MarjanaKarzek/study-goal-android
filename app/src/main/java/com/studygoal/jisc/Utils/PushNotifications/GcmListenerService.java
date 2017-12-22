package com.studygoal.jisc.Utils.PushNotifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.studygoal.jisc.Activities.MainActivity;

import java.util.Map;

/**
 * GCM Listener Service
 * <p>
 * Used to listen for the GCM message.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class GcmListenerService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param message RemoteMessage of the sender.
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.e("onMessageReceived", "onMessageReceived: " + message.toString());
        Map data = message.getData();

        String m = (String) data.get("message");

        Log.e("onMessageReceived", "Message: " + m);

        sendNotification(m);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param message FCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(com.studygoal.jisc.R.drawable.ic_notification)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(
                0 /* ID of notification */,
                notificationBuilder.build());
    }
}