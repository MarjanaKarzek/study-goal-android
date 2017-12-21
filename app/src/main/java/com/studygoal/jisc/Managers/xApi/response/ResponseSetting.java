package com.studygoal.jisc.Managers.xApi.response;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - ResponseSetting
 * <p>
 * Provides the Retrofit Gson object for the settings response.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class ResponseSetting {

    @SerializedName("value")
    String statement;

    /**
     * Gets the statement value.
     *
     * @return value
     */
    public String getValue() {
        return statement;
    }
}
