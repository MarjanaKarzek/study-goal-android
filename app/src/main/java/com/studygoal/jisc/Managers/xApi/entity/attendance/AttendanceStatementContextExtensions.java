package com.studygoal.jisc.Managers.xApi.entity.attendance;

import com.google.gson.annotations.SerializedName;

/**
 * Retrofit Gson - AttendanceStatementContextExtensions
 * <p>
 * Provides the Retrofit Gson object for the attendance statement context extension.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class AttendanceStatementContextExtensions {

    @SerializedName("http://xapi.jisc.ac.uk/courseArea")
    AttendanceStatementContextExtensionsCourseArea courseArea;

    @SerializedName("http://xapi.jisc.ac.uk/activity_type_id")
    String activityTypeId;

    @SerializedName("http://xapi.jisc.ac.uk/starttime")
    String startTime;

    @SerializedName("http://xapi.jisc.ac.uk/recipeVersion")
    String recipeVersion;

    /**
     * Gets course area of extensions.
     *
     * @return course area
     */
    public AttendanceStatementContextExtensionsCourseArea getCourseArea() {
        return courseArea;
    }

    /**
     * Gets activity type id of extensions.
     *
     * @return activity type
     */
    public String getActivityTypeId() {
        return activityTypeId;
    }

    /**
     * Gets start time of extensions.
     *
     * @return start time
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Gets recipe version of extensions.
     *
     * @return recipe version
     */
    public String getRecipeVersion() {
        return recipeVersion;
    }

}
