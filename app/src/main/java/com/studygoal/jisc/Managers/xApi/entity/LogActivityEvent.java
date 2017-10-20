package com.studygoal.jisc.Managers.xApi.entity;

/**
 * Created by Eugene Krasnopolskiy (eugene.krasnopolskiy@gmail.com) on 14.08.2017.
 */
public enum LogActivityEvent {
    SuccessfulLogin,
    NavigateActivityFeed,
    NavigateFriends,
    NavigateStatsAllActivity,
    NavigateAttainment,
    NavigatePoints,
    NavigateLeaderboard,
    NavigateEventsAttended,
    NavigateAttendanceGraph,
    NavigateAppUsage,
    NavigateLog,
    NavigateTargetsMain,
    NavigateTargetsGraphs,
    ViewedGraphModule,
    FilterLeaderboardByModule,
    FilterEventsAttendedByModule,
    FilterAttendanceGraph,
    AddTimedLog,
    AddReportedLog,
    AddTarget,
    AcceptSingleTarget,
    DeclineSingleTarget,
    AddSingleTarget,
    AddRecurringTarget,
    DeleteSingleTarget,
    DeleteRecurringTarget,
    CompleteSingleTarget,
    CompleteOverdueSingleTarget
}
