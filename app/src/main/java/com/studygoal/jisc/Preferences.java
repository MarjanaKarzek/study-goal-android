package com.studygoal.jisc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.studygoal.jisc.General.TLog;

/**
 * Preferences class
 * <p>
 * Provides the functionality to interact with the shared preferences.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class Preferences {

    private static final String TAG = Preferences.class.getSimpleName();

    private static final String preferenceAttendanceData = "attendanceData";
    private static final boolean preferenceAttendanceDataDefault = false;

    private static final String preferenceAttainmentData = "attainmentData";
    private static final boolean preferenceAttainmentDataDefault = false;

    private static final String preferenceStudyGoalAttendance = "studyGoalAttendance";
    private static final boolean preferenceStudyGoalAttendanceDefault = false;

    private final SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Returns the attendance data from shared preferences.
     *
     * @return attendance data
     */
    public boolean getAttendanceData() {
        boolean value = preferenceAttendanceDataDefault;

        try {
            value = preferences.getBoolean(preferenceAttendanceData, preferenceAttendanceDataDefault);
        } catch (Exception e) {
            TLog.e(TAG, "Unable get preferences.", e);
        }

        return value;
    }

    /**
     * Sets the attendance data to shared preferences.
     *
     * @param value attendance data
     */
    public void setAttendanceData(boolean value) {
        setBooleanPreference(preferenceAttendanceData, value);
    }

    /**
     * Returns the attainment data from shared preferences.
     *
     * @return attainment data
     */
    public boolean getAttainmentData() {
        boolean value = preferenceAttainmentDataDefault;

        try {
            value = preferences.getBoolean(preferenceAttainmentData, preferenceAttainmentDataDefault);
        } catch (Exception e) {
            TLog.e(TAG, "Unable get preferences.", e);
        }

        return value;
    }

    /**
     * Sets the attainment data to shared preferences.
     *
     * @param value attainment data
     */
    public void setAttainmentData(boolean value) {
        setBooleanPreference(preferenceAttainmentData, value);
    }

    /**
     * Returns the study goal attendance data from shared preferences.
     *
     * @return study goal attendance data
     */
    public boolean getStudyGoalAttendance() {
        boolean value = preferenceStudyGoalAttendanceDefault;

        try {
            value = preferences.getBoolean(preferenceStudyGoalAttendance, preferenceStudyGoalAttendanceDefault);
        } catch (Exception e) {
            TLog.e(TAG, "Unable get preferences.", e);
        }

        return value;
    }

    /**
     * Sets the Study Goal attendance data to shared preferences.
     *
     * @param value Study Goal attendance data
     */
    public void setStudyGoalAttendance(boolean value) {
        setBooleanPreference(preferenceStudyGoalAttendance, value);
    }

    /**
     * Sets the boolean value to specified shared preference by the key.
     *
     * @param key   key for the preference
     * @param value boolean data
     */
    private void setBooleanPreference(String key, Boolean value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            editor.commit();
        } catch (Exception e) {
            TLog.e(TAG, "Unable set Boolean preferences: " + key, e);
        }
    }

    /**
     * Sets the string value to specified shared preference by the key.
     *
     * @param key   key for the preference
     * @param value string data
     */
    private void setStringPreference(String key, String value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.commit();
        } catch (Exception e) {
            TLog.e(TAG, "Unable set Boolean preferences: " + key, e);
        }
    }

    /**
     * Sets the int value to specified shared preference by the key.
     *
     * @param key   key for the preference
     * @param value int data
     */
    private void setIntPreference(String key, int value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            editor.commit();
        } catch (Exception e) {
            TLog.e(TAG, "Unable set Integer preferences: " + key, e);
        }
    }

    /**
     * Sets the long value to specified shared preference by the key.
     *
     * @param key   key for the preference
     * @param value long data
     */
    private void setLongPreference(String key, long value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            editor.commit();
        } catch (Exception e) {
            TLog.e(TAG, "Unable set Integer preferences: " + key, e);
        }
    }

    /**
     * Sets the float value to specified shared preference by the key.
     *
     * @param key   key for the preference
     * @param value float data
     */
    private void setFloatPreference(String key, float value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            editor.commit();
        } catch (Exception e) {
            TLog.e(TAG, "Unable set Float preferences: " + key, e);
        }
    }

    /**
     * Sets the bytes value to specified shared preference by the key.
     *
     * @param key   key for the preference
     * @param value bytes data
     */
    private void setBytesPreference(String key, byte[] value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            String base64Value = Base64.encodeToString(value, Base64.DEFAULT);
            editor.putString(key, base64Value);
            editor.commit();
        } catch (Exception e) {
            TLog.e(TAG, "Unable set byte[] preferences: " + key, e);
        }
    }

    /**
     * Returns byte array value for the given key.
     *
     * @param key key for which the value is asked for
     * @return byte[] value
     */
    private byte[] getBytesPreference(String key) {
        byte[] value = null;

        try {
            String base64Value = preferences.getString(key, null);

            if (base64Value != null) {
                value = Base64.decode(base64Value, Base64.DEFAULT);
            }
        } catch (Exception e) {
            TLog.e(TAG, "Unable get preferences.", e);
        }

        return value;
    }

}
