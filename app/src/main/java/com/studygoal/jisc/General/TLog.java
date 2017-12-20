package com.studygoal.jisc.General;

import android.util.Log;

import com.studygoal.jisc.BuildConfig;

/**
 * TLog class
 * <p>
 * Provides logging methods which give more information.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class TLog {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.i(tag, message, throwable);
        }
    }

    public static void i(String tag, String message) {
        i(tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.e(tag, message, throwable);
        }
    }

    public static void e(String tag, String message) {
        e(tag, message, null);
    }

    public static void e(String tag, Throwable throwable) {
        e(tag, null, throwable);
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.d(tag, message, throwable);
        }
    }

    public static void d(String tag, String message) {
        d(tag, message, null);
    }

    public static void v(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.v(tag, message, throwable);
        }
    }

    public static void v(String tag, String message) {
        v(tag, message, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.w(tag, message, throwable);
        }
    }

    public static void w(String tag, String message) {
        w(tag, message, null);
    }
}

