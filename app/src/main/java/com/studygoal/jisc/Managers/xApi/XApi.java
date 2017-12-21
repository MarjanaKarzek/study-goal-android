package com.studygoal.jisc.Managers.xApi;

import com.studygoal.jisc.Managers.xApi.response.ResponseAttendance;
import com.studygoal.jisc.Managers.xApi.response.ResponseSetting;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Retrofit - XApi HTTP API Interface
 *
 * Provides the Retrofit interface for the API calls.
 *
 * @author Therapy Box - Eugene Krasnopolskiy
 * @version 1.5
 * @date 14/08/17
 */
public interface XApi {
    @GET("/sg/log")
    Call<ResponseBody> getLogActivity(@Header("authorization") String token,
                                      @Query("verb") String verb);

    @GET("/sg/log")
    Call<ResponseBody> getLogActivity(@Header("authorization") String token,
                                      @Query("verb") String verb,
                                      @Query("contentID") String contentId,
                                      @Query("contentName") String contentName);

    @GET("/sg/log")
    Call<ResponseBody> getLogActivity(@Header("authorization") String token,
                                      @Query("verb") String verb,
                                      @Query("contentID") String contentId,
                                      @Query("contentName") String contentName,
                                      @Query("modid") String modId);

    @GET("/sg/attendance")
    Call<List<ResponseAttendance>> getAttendance(@Header("authorization") String token,
                                                 @Query("skip") int skip,
                                                 @Query("limit") int limit);

    @GET("/sg/setting")
    Call<ResponseSetting> getSetting(@Header("authorization") String token,
                                     @Query("setting") String settingName);
}
