package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementContextExtensionsCourseArea
 * <p>
 * Provides the Retrofit Gson object for the attendance statement context extensions course area.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementContextExtensionsCourseArea {

    @SerializedName("http://xapi.jisc.ac.uk/uddModInstanceID")
    String uddModInstanceID;

    /**
     * Gets the uudModInstanceID of the course area.
     *
     * @return id
     */
    public String getUddModInstanceID() {
        return uddModInstanceID;
    }

}
