package com.studygoal.jisc;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * AppCore class
 * <p>
 * Used to provide the app core as a singleton.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AppCore extends MultiDexApplication {

    private static final String TAG = AppCore.class.getSimpleName();

    private static Context context = null;

    private Preferences preferences = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        MultiDex.install(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCore.context = getApplicationContext();
    }

    /**
     * Gets the singleton object of AppCore.
     *
     * @return instance of AppCore
     */
    public static AppCore getInstance() {
        return (AppCore) AppCore.context;
    }

    /**
     * Gets the application preferences.
     *
     * @return application preferences
     */
    public Preferences getPreferences() {
        if (preferences == null) {
            preferences = new Preferences(this);
        }

        return preferences;
    }

}
