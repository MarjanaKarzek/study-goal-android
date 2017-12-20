package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Feed Model
 * <p>
 * Provides model "Event" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "Feed")
public class Feed extends Model {

    @Column(name = "feed_id")
    public String id;
    @Column(name = "message_from")
    public String message_from;
    @Column(name = "message_to")
    public String message_to;
    @Column(name = "message")
    public String message;
    @Column(name = "activity_type")
    public String activity_type;
    @Column(name = "is_hidden")
    public String is_hidden;
    @Column(name = "created_date")
    public String created_date;

    public Feed() {
        super();
    }

    @Override
    public String toString() {
        return "Feed{" +
                "id='" + id + '\'' +
                ", message_from='" + message_from + '\'' +
                ", message_to='" + message_to + '\'' +
                ", message='" + message + '\'' +
                ", activity_type='" + activity_type + '\'' +
                ", is_hidden='" + is_hidden + '\'' +
                ", created_date='" + created_date + '\'' +
                '}';
    }
}
