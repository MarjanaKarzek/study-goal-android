package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementObjectDefinitionName
 * <p>
 * Provides the Retrofit Gson object for the attendance statement object.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementObjectDefinitionName {

    @SerializedName("en")
    String en;

    /**
     * GEts the definitions name.
     *
     * @return name
     */
    public String getEn() {
        return en;
    }
}
