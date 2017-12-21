package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementContext
 * <p>
 * Provides the Retrofit Gson object for the attendance statement context.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementContext {

    @SerializedName("extensions")
    AttendanceStatementContextExtensions extensions;

    /**
     * Gets the extension of the context.
     *
     * @return extension
     */
    public AttendanceStatementContextExtensions getExtensions() {
        return extensions;
    }

}
