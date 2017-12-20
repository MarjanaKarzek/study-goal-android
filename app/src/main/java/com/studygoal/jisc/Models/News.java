package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * News Model
 * <p>
 * Provides model "News" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "News")
public class News extends Model {

    @Column(name = "news_id")
    public String id;
    @Column(name = "message_from")
    public String message_from;
    @Column(name = "read")
    public String read;
    @Column(name = "message")
    public String message;
    @Column(name = "created_date")
    public String created_date;

    public News() {
        super();
    }

}
