package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementObject
 * <p>
 * Provides the Retrofit Gson object for the attendance statement object.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementObject {

    @SerializedName("definition")
    AttendanceStatementObjectDefinition definition;

    /**
     * Gets the definition of the object.
     *
     * @return definition
     */
    public AttendanceStatementObjectDefinition getDefinition() {
        return definition;
    }

}
