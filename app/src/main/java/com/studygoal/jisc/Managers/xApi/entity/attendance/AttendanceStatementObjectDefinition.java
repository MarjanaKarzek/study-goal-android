package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementObjectDefinition
 * <p>
 * Provides the Retrofit Gson object for the attendance statement object.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementObjectDefinition {

    @SerializedName("name")
    AttendanceStatementObjectDefinitionName name;

    /**
     * Gets the name of the definition.
     *
     * @return name
     */
    public AttendanceStatementObjectDefinitionName getName() {
        return name;
    }

}
