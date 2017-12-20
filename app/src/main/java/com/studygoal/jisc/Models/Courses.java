package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Courses Model
 * <p>
 * Provides model "Courses" for Active Android.
 *
 * @author Therapy Box - Bogdan
 * @version 1.5
 * @date 22/09/16
 */
@Table(name = "Courses")
public class Courses extends Model {

    @Column(name = "course_id")
    public String id;
    @Column(name = "course_name")
    public String name;

    public Courses() {
        super();
    }

}
