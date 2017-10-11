package com.studygoal.jisc.Managers.xApi;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.xApi.entity.LogActivityEvent;
import com.studygoal.jisc.Managers.xApi.entity.attendance.AttendanceStatement;
import com.studygoal.jisc.Managers.xApi.response.ResponseAttendance;
import com.studygoal.jisc.Managers.xApi.response.ResponseSetting;
import com.studygoal.jisc.Models.Event;

import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Eugene Krasnopolskiy (eugene.krasnopolskiy@gmail.com) on 14.08.2017.
 */
public class XApiManager {
    private static final String TAG = XApiManager.class.getSimpleName();

    private static String SERVER_BASE = "https://api.datax.jisc.ac.uk";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private static final String SETTING_ATTENDANCE_DATA = "attendanceData";
    private static final String SETTING_ATTAINMENT_DATA = "attainmentData";
    private static final String SETTING_STUDY_GOAL_ATTENDANCE = "studyGoalAttendance";

    private static XApiManager sInstance = null;
    private boolean developerMode = false;

    public static XApiManager getInstance() {
        if (sInstance == null) {
            sInstance = new XApiManager();
        }

        return sInstance;
    }

    public boolean getSettingAttendanceData() {
        boolean result = true;

        try {
            String value = getSetting(SETTING_ATTENDANCE_DATA);

            if (value != null) {
                result = Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public boolean getSettingStudyGoalAttendance() {
        boolean result = true;

        try {
            String value = getSetting(SETTING_STUDY_GOAL_ATTENDANCE);

            if (value != null) {
                result = Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public boolean getSettingAttainmentData() {
        boolean result = true;

        try {
            String value = getSetting(SETTING_ATTAINMENT_DATA);

            if (value != null) {
                result = Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public boolean getAttendance(int skip, int limit, boolean reset) {
        boolean result = true;

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            XApi api = retrofit.create(XApi.class);
            String token = TOKEN_PREFIX + DataManager.getInstance().get_jwt();
            Call<List<ResponseAttendance>> call = api.getAttendance(token, skip, limit);
            Response<List<ResponseAttendance>> response = call.execute();

            if (response != null && response.code() == 200) {
                List<ResponseAttendance> list = response.body();

                if (list != null && list.size() > 0) {
                    ActiveAndroid.beginTransaction();

                    try {
                        if (reset) {
                            new Delete().from(Event.class).execute();
                        }

                        for (ResponseAttendance aitem : list) {
                            AttendanceStatement item = aitem.getStatement();
                            String dateString = "";
                            String activityInfo = "";
                            String moduleName = "";
                            long time = 0;
                            String[] dataInfo = null;

                            if (item.getObject() != null && item.getObject().getDefinition() != null
                                    && item.getObject().getDefinition().getName() != null && item.getObject().getDefinition().getName().getEn() != null) {
                                dataInfo = item.getObject().getDefinition().getName().getEn().split(" ");
                            }

                            if (item.getContext() != null && item.getContext().getExtensions() != null
                                    && item.getContext().getExtensions().getCourseArea() != null) {
                                activityInfo = item.getContext().getExtensions().getActivityTypeId();
                            }

                            if (dataInfo != null && dataInfo.length > 1) {
                                dateString = dataInfo[dataInfo.length - 1] + " " + dataInfo[dataInfo.length - 2];
                                if(dataInfo.length > 2){
                                    for(int i = 0; i < dataInfo.length - 2; i++){
                                        moduleName += dataInfo[i] + " ";
                                    }
                                }
                            }

                            try {
                                time = sDateFormat.parse(dateString).getTime();
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                            }

                            Event event = new Event();
                            event.setDate(dateString);
                            event.setActivity(activityInfo);
                            event.setModule(moduleName);
                            event.setTime(time);
                            event.save();
                        }

                        ActiveAndroid.setTransactionSuccessful();
                        result = true;
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public void sendLogActivityEvent(LogActivityEvent event) {
        sendLogActivityEvent(event, null);
    }

    public void sendLogActivityEvent(LogActivityEvent event, String modId) {
        if (event != null) {
            switch (event) {
                case SuccessfulLogin: {
                    sendLogActivity("loggedin");
                    break;
                }
                case NavigateActivityFeed: {
                    sendLogActivity("viewed", "feed-main", "MainFeed");
                    break;
                }
                case NavigateFriends: {
                    sendLogActivity("viewed", "feed-friends", "friends");
                    break;
                }
                case NavigateStatsAllActivity: {
                    sendLogActivity("viewed", "stats-main", "MainStats");
                    break;
                }
                case NavigateAttainment: {
                    sendLogActivity("viewed", "stats-attainment", "attainment");
                    break;
                }
                case NavigatePoints: {
                    sendLogActivity("viewed", "stats-points", "points");
                    break;
                }
                case NavigateLeaderboard: {
                    sendLogActivity("viewed", "stats-leaderboard", "leaderboard");
                    break;
                }
                case NavigateEventsAttended: {
                    sendLogActivity("viewed", "stats-events", "eventsAttended");
                    break;
                }
                case NavigateAttendanceGraph: {
                    sendLogActivity("viewed", "stats-attendance-summary", "attendanceGraph");
                    break;
                }
                case NavigateAppUsage: {
                    sendLogActivity("viewed", "app-usage", "appUsage");
                    break;
                }
                case NavigateLog: {
                    sendLogActivity("viewed", "logs-main", "MainLogsPage");
                    break;
                }
                case NavigateTargetsMain: {
                    sendLogActivity("viewed", "targets-main", "MainTargetsPage");
                    break;
                }
                case NavigateTargetsGraphs: {
                    sendLogActivity("viewed", "targets-specific", "TargetsDonutCharts");
                    break;
                }
                case ViewedGraphModule: {
                    sendLogActivity("viewed", "stats-main-module", "MainStatsFilteredByModule");
                    break;
                }
                case FilterLeaderboardByModule: {
                    sendLogActivity("viewed", "stats-leaderboard-module", "leaderboardFilteredByModule", modId);
                    break;
                }
                case FilterEventsAttendedByModule: {
                    sendLogActivity("viewed", "stats-events-module", "eventsAttendedFilteredByModule", modId);
                    break;
                }
                case FilterAttendanceGraph: {
                    sendLogActivity("viewed", "stats-attendance-summary-module", "attendanceGraphFilteredByModule", modId);
                    break;
                }
                case AddTimedLog: {
                    sendLogActivity("viewed", "logs-timed", "logTimed", modId);
                    break;
                }
                case AddReportedLog: {
                    sendLogActivity("viewed", "logs-untimed", "logReport", modId);
                    break;
                }
                case AddTarget: {
                    if (modId == null) {
                        sendLogActivity("viewed", "targets-add", "newTarget");
                    } else {
                        sendLogActivity("viewed", "targets-add", "newTarget", modId);
                    }
                    break;
                }
            }
        }
    }

    private void sendLogActivity(String verb) {
        Observable<Boolean> observable = Observable.create(subscriber -> {
            boolean result = false;

            try {
                if (verb != null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(SERVER_BASE)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    XApi api = retrofit.create(XApi.class);
                    String token = TOKEN_PREFIX + DataManager.getInstance().get_jwt();
                    Call<ResponseBody> call = api.getLogActivity(token, verb);
                    Response<ResponseBody> response = call.execute();

                    if (response != null && response.code() == 200) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            subscriber.onNext(result);
            subscriber.onCompleted();
        });

        observable.subscribeOn(Schedulers.io()).retry(1).subscribe();
    }

    private void sendLogActivity(String verb, String contentId, String contentName) {
        Observable<Boolean> observable = Observable.create(subscriber -> {
            boolean result = false;

            try {
                if (verb != null && contentId != null && contentName != null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(SERVER_BASE)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    XApi api = retrofit.create(XApi.class);
                    String token = TOKEN_PREFIX + DataManager.getInstance().get_jwt();
                    Call<ResponseBody> call = api.getLogActivity(token, verb, contentId, contentName);
                    Response<ResponseBody> response = call.execute();

                    if (response != null && response.code() == 200) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            subscriber.onNext(result);
            subscriber.onCompleted();
        });

        observable.subscribeOn(Schedulers.io()).retry(1).subscribe();
    }

    private void sendLogActivity(String verb, String contentId, String contentName, String modId) {
        Observable<Boolean> observable = Observable.create(subscriber -> {

            boolean result = false;

            try {
                if (verb != null && contentId != null && contentName != null && modId != null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(SERVER_BASE)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    XApi api = retrofit.create(XApi.class);
                    String token = TOKEN_PREFIX + DataManager.getInstance().get_jwt();
                    Call<ResponseBody> call = api.getLogActivity(token, verb);
                    Response<ResponseBody> response = call.execute();

                    if (response != null && response.code() == 200) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            subscriber.onNext(result);
            subscriber.onCompleted();
        });

        observable.subscribeOn(Schedulers.io()).retry(1).subscribe();
    }

    private String getSetting(String settingName) {
        String result = null;

        try {
            if (settingName != null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(SERVER_BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                XApi api = retrofit.create(XApi.class);
                String token = TOKEN_PREFIX + DataManager.getInstance().get_jwt();
                Call<ResponseSetting> call = api.getSetting(token, settingName);
                Response<ResponseSetting> response = call.execute();

                if (response != null && response.code() == 200) {
                    result = response.body().getValue();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public void changeDeveloperMode() {
        if(developerMode){
            SERVER_BASE = "https://api.datax.jisc.ac.uk";
        } else {
            SERVER_BASE = "https://api.x-dev.data.alpha.jisc.ac.uk";
        }
        developerMode = !developerMode;
    }
}
