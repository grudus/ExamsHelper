package com.grudus.nativeexamshelper.net;


import android.support.annotation.Nullable;

import com.grudus.nativeexamshelper.pojos.JsonUser;

import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiUserService {

    String HEADER_TOKEN = "X-AUTH-TOKEN";
    String BASE_URL = "/api/user/{username}";

    @GET("/api/user/{username}")
    Observable<Response<JsonUser>> getUser(@Path("username") String username, @Header(HEADER_TOKEN) String token);

    @POST("/login")
    @FormUrlEncoded
    Observable<Response<Void>> login(@Field("username") String username, @Field("password") String password);

    @POST("/auth/add/google")
    @FormUrlEncoded
    Observable<Response<Void>> loginUsingGoogle(@Field("email") String email, @Field("id") String id);

    @GET("/auth/check")
    Observable<Response<String>> checkLoginOrEmailAvailability(@Nullable @Query("login") String login, @Nullable @Query("email") String email);

    @POST("/auth/add")
    @FormUrlEncoded
    Observable<Response<Void>> addNewUser(@Field("username") String login, @Field("password") String password, @Field("email") String email);
}
