package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatement
 * <p>
 * Provides the Retrofit Gson object for the attendance statement.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatement {

    @SerializedName("actor")
    AttendanceStatementActor actor;
    @SerializedName("object")
    AttendanceStatementObject object;
    @SerializedName("context")
    AttendanceStatementContext context;

    /**
     * Gets the attendance's actor.
     *
     * @return attendance actor
     */
    public AttendanceStatementActor getActor() {
        return actor;
    }

    /**
     * Gets the attendance's object.
     *
     * @return attendance object
     */
    public AttendanceStatementObject getObject() {
        return object;
    }

    /**
     * Gets the attendance's context.
     *
     * @return attendance context
     */
    public AttendanceStatementContext getContext() {
        return context;
    }

}
