package com.grudus.nativeexamshelper.net;


import com.grudus.nativeexamshelper.pojos.JsonExam;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

import static com.grudus.nativeexamshelper.net.ApiUserService.HEADER_TOKEN;

public interface ApiExamService {

    String BASE_URL = com.grudus.nativeexamshelper.net.ApiUserService.BASE_URL + "/exams";

    @GET("api/user/{username}/exams")
    Observable<Response<List<JsonExam>>> getUserExams(@Path("username") String username, @Header(HEADER_TOKEN) String token);

    @POST(BASE_URL)
    Observable<Response<Void>> insertSubjects(@Path("username") String username, @Header(HEADER_TOKEN) String token, @Body ArrayList<JsonExam> array);

    @GET("/api/user/{username}/exams")
    Observable<Response<List<JsonExam>>> getExamsModifiedAfter(@Path("username") String username,
                                                         @Header(HEADER_TOKEN) String token,
                                                         @Query("lastModified") long lastModified);
}
