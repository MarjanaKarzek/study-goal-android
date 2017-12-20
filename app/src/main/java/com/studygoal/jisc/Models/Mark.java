package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Mark Model
 * <p>
 * Provides model "Mark" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "Mark")
public class Mark extends Model {

    @Column(name = "student_id")
    public String id;
    @Column(name = "assignment")
    public String assigment;
    @Column(name = "module_instance")
    public String module_instance;
    @Column(name = "module")
    public String module;
    @Column(name = "mark")
    public String mark;

    public Mark() {
        super();
    }

}
