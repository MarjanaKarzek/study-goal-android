package com.studygoal.jisc.Managers.xApi.response;

import com.google.gson.annotations.SerializedName;

public class AttendanceStatementObject {
    @SerializedName("definition")
    AttendanceStatementObjectDefinition definition;

    public AttendanceStatementObjectDefinition getDefinition() {
        return definition;
    }
}
