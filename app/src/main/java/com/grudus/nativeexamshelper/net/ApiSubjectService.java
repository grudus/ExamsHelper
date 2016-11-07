package com.grudus.nativeexamshelper.net;

import com.grudus.nativeexamshelper.pojos.JsonSubject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

import static com.grudus.nativeexamshelper.net.ApiUserService.HEADER_TOKEN;


public interface ApiSubjectService {

    String BASE_URL = com.grudus.nativeexamshelper.net.ApiUserService.BASE_URL + "/subjects";

    @POST(BASE_URL)
    Observable<Response<Void>> insertSubjects(@Path("username") String username, @Header(HEADER_TOKEN) String token,
                                              @Body ArrayList<JsonSubject> subjects);

    @PUT(BASE_URL + "/{id}")
    Observable<Response<Void>> updateSubject(@Path("username") String username, @Path("id") Long id,
                                             @Header(HEADER_TOKEN) String token, @Body JsonSubject subject);

    @POST(BASE_URL + "/add")
    Observable<Response<Void>> createSubject(@Path("username") String username, @Header(HEADER_TOKEN) String token, @Body JsonSubject jsonSubject);

    @GET(BASE_URL)
    Observable<Response<List<JsonSubject>>> getSubjectsModifiedAfter(@Path("username") String username, @Header(HEADER_TOKEN) String token, @Query("lastModified") long lastModified);
}
