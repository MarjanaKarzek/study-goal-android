package com.studygoal.jisc.Models;

/**
 * Activity Points Model
 * <p>
 * Provides model "Activity Points" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class ActivityPoints {
    public String activity;
    public String points;
    public String id;
    public String key;

    public ActivityPoints() {

    }

    public ActivityPoints(String activity, String points, String id, String key) {
        this.activity = activity;
        this.points = points;
        this.id = id;
        this.key = key;
    }
}
