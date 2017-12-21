package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementActorAccount
 * <p>
 * Provides the Retrofit Gson object for the attendance statement actor account.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementActorAccount {

    @SerializedName("name")
    String name;

    /**
     * Gets the name of the actor.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

}
