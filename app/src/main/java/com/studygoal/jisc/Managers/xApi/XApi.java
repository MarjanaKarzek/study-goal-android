package com.studygoal.jisc.Managers.xApi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by Eugene Krasnopolskiy (eugene.krasnopolskiy@gmail.com) on 14.08.2017.
 */
interface XApi {
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
}
