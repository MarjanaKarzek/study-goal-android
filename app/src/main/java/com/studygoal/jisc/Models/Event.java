package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Event Model
 * <p>
 * Provides model "Event" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "Event")
public class Event extends Model {

    @Column(name = "activity")
    private String activity;
    @Column(name = "module")
    private String module;
    @Column(name = "date")
    private String date;
    @Column(name = "time")
    private long time = 0;

    public Event() {
        super();
    }

    /**
     * Gets the activity of the current event.
     *
     * @return event's activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * Gets the module of the current event.
     *
     * @return event's module
     */
    public String getModule() {
        return module;
    }

    /**
     * Gets the date of the current event.
     *
     * @return event's date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the time of the current event.
     *
     * @return event's time
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the activity of the current event.
     *
     * @param activity activity to be set
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * Sets the module of the current event.
     *
     * @param module module to be set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Sets the date of the current event.
     *
     * @param date date to be set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the time of the current event.
     *
     * @param time time to be set
     */
    public void setTime(long time) {
        this.time = time;
    }

}

