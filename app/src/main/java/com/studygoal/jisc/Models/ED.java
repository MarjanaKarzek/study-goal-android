package com.studygoal.jisc.Models;

import java.util.Date;

public class ED {
    public String date;
    public String student_id;
    public String position;
    public String year;
    public String month;
    public String week;
    public String day;
    public String hour;
    public Date realDate;
    public Integer activity_points;
    public Integer other_activity_points;


    public ED(String day, Integer activity_points){
        this.day = day;
        this.activity_points = activity_points;
    }

    public ED(){

    }

    public String toString(){
        return "" + date + " " + student_id + " " + position + " " + month + " " + week + " " + day + " " + realDate + " " + activity_points;
    }
}
