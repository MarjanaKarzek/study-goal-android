package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementActor
 * <p>
 * Provides the Retrofit Gson object for the attendance statement actor.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementActor {

    @SerializedName("account")
    AttendanceStatementActorAccount account;

    /**
     * Gets the attendance actor account.
     *
     * @return account
     */
    public AttendanceStatementActorAccount getAccount() {
        return account;
    }

}
