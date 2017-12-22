package com.studygoal.jisc.Utils.PushNotifications;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.studygoal.jisc.BuildConfig;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Instance ID Listener Service
 * <p>
 * Called if InstanceID token is updated. This may occur if the security of
 * the previous token had been compromised. This call is initiated by the
 * InstanceID provider.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class InstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Refreshes the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
//        sendRegistrationToServer(refreshedToken);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("push_token", refreshedToken).apply();
        if (DataManager.getInstance().user == null) { //If token refresh occurs befor any user id is stored locally on the device, or if user is wiped, it will set to demo mode.
            RequestBody formBody = new FormBody.Builder()
                    .add("student_id", "54")
                    .add("version", BuildConfig.VERSION_NAME)
                    .add("build", "" + BuildConfig.VERSION_CODE)
                    .add("bundle_identifier", BuildConfig.APPLICATION_ID)
                    .add("is_active", "0")
                    .add("is_social", "no")
                    .add("device_token", Build.SERIAL)
                    .add("platform", "android")
                    .build();
        } else { // if token refresh occurs after user has logged in, they will be available here. Tested with push notifications and the above assignment is overwritten as soon as user logs in and gives permission. Push notifications still function with this method// .
            NetworkManager.getInstance().updateDeviceDetails(); // update device token when the device token is changed.
        }
    }
}
