package com.studygoal.jisc.Managers.xApi.response;

import com.google.gson.annotations.SerializedName;
import com.studygoal.jisc.Managers.xApi.entity.attendance.AttendanceStatement;

/**
 * Retrofit Gson - ResponseAttendance
 * <p>
 * Provides the Retrofit Gson object for the attendance response.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class ResponseAttendance {

    @SerializedName("statement")
    AttendanceStatement statement;

    /**
     * Gets the statement of the response.
     *
     * @return statement
     */
    public AttendanceStatement getStatement() {
        return statement;
    }

}
