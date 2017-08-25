package com.studygoal.jisc.Managers.xApi;

import android.util.Log;

import com.studygoal.jisc.Managers.DataManager;

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

    private static final String SERVER_BASE = "https://api.x-dev.data.alpha.jisc.ac.uk";
    private static final String TOKEN_PREFIX = "Bearer ";

    private static XApiManager sInstance = null;

    public static XApiManager getInstance() {
        if (sInstance == null) {
            sInstance = new XApiManager();
        }

        return sInstance;
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
}
