package com.studygoal.jisc.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Select;
import com.studygoal.jisc.Fragments.Log.LogNewActivityFragment;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.Models.RunningActivity;
import com.studygoal.jisc.Models.TrophyMy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Synchronize class
 * <p>
 * Provides the intent for an intent service.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class Syncronize extends IntentService {

    Timer timer;

    public Syncronize() {
        super("SyncronizeService");
        timer = new Timer();
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     *
     * @param intent intent that handles the call
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!Cache.isInitialized()) {
                    ActiveAndroid.initialize(getApplicationContext());
                }

                List<TrophyMy> currentList = new Select().from(TrophyMy.class).execute();
                List<String> listOfIds = new ArrayList<>();
                for (TrophyMy trophyMy : currentList)
                    listOfIds.add(trophyMy.trophy_id);
                if (NetworkManager.getInstance().getMyTrophies()) {
                    List<TrophyMy> newList = new Select().from(TrophyMy.class).execute();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    Set<String> trophiesShown = prefs.getStringSet("trophies", new HashSet<String>());
                    boolean alwaysShowTrophy = true;
                    // only show new trophy
                    if (trophiesShown.isEmpty()) {
                        alwaysShowTrophy = false;
                    }
                    for (final TrophyMy trophyMy : newList)
                        if (!listOfIds.contains(trophyMy.trophy_id)) {
                            trophiesShown.add(trophyMy.trophy_id);
                            if (alwaysShowTrophy) { //  show new trophy only
                                DataManager.getInstance().showTrophyNotification(trophyMy);
                            }
                            editor.putStringSet("trophies", trophiesShown);
                            editor.apply();
                        }
                }

                if (new Select().from(RunningActivity.class).count() > 0) {
                    SharedPreferences saves = getSharedPreferences("jisc", Context.MODE_PRIVATE);
                    Long timestamp = saves.getLong("timer", 0);
                    RunningActivity activity = new Select().from(RunningActivity.class).executeSingle();
                    timestamp = System.currentTimeMillis() - timestamp;
                    if (timestamp / 60000 >= 180) {//180) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("student_id", DataManager.getInstance().user.id);
                        params.put("module_id", activity.module_id);
                        params.put("activity_type", DataManager.getInstance().api_values.get(activity.activity_type));
                        params.put("activity", DataManager.getInstance().api_values.get(activity.activity));
                        params.put("activity_date", activity.activity_date);
                        params.put("time_spent", "180");

                        if (NetworkManager.getInstance().addActivity(params).equals("200")) {
                            activity.delete();
                            NetworkManager.getInstance().getActivityHistory(DataManager.getInstance().user.id);
                        }
                        if (DataManager.getInstance().mainActivity != null) {
                            LogNewActivityFragment fragment = (LogNewActivityFragment) DataManager.getInstance().mainActivity.getSupportFragmentManager().findFragmentByTag("newActivity");
                            if (fragment != null)
                                DataManager.getInstance().mainActivity.onBackPressed();
                        }
                        saves.edit().putLong("timer", 0).apply();
                    }
                }
                if (new Select().from(com.studygoal.jisc.Models.Activity.class).count() > 0) {
                    List<com.studygoal.jisc.Models.Activity> list = new Select().from(com.studygoal.jisc.Models.Activity.class).execute();
                    for (int i = 0; i < list.size(); i++) {
                        com.studygoal.jisc.Models.Activity activity = list.get(i);
                        HashMap<String, String> params = new HashMap<>();
                        params.put("student_id", activity.student_id);
                        params.put("module_id", activity.module_id);
                        params.put("activity_type", DataManager.getInstance().api_values.get(activity.activity_type));
                        params.put("activity", DataManager.getInstance().api_values.get(activity.activity));
                        params.put("activity_date", activity.activity_date);
                        params.put("time_spent", activity.time_spent);

                        if (NetworkManager.getInstance().addActivity(params).equals("200"))
                            activity.delete();
                    }
                }
            }
        }, 0, 15000);

    }

    /**
     * Starts the command.
     *
     * @param intent intent service
     * @param flags flags to be set
     * @param startId starting id
     * @return result id
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
