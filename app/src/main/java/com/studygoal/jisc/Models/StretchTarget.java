package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Stretch Target Model
 * <p>
 * Provides model "Stretch Target" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "StretchTarget")
public class StretchTarget extends Model {

    @Column(name = "stretch_id")
    public String id;
    @Column(name = "target_id")
    public String target_id;
    @Column(name = "stretch_time")
    public String stretch_time;
    @Column(name = "status")
    public String status;
    @Column(name = "created_date")
    public String created_date;
    @Column(name = "student_id")
    public String student_id;

    public StretchTarget() {
        super();
    }

}
