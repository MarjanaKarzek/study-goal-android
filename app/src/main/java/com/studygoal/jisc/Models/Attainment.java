package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Attainment Model
 * <p>
 * Provides model "Attainment" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "Attainmnet")
public class Attainment extends Model {

    @Column(name = "student_id")
    public String id;
    @Column(name = "date")
    public String date;
    @Column(name = "module")
    public String module;
    @Column(name = "percent")
    public String percent;

    public Attainment() {
        super();
    }

    public Attainment(String id, String date, String module, String percent) {
        this.id = id;
        this.date = date;
        this.module = module;
        this.percent = percent;
    }

}
