package com.grudus.nativeexamshelper.net;


import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.converters.CursorToArrayConverter;
import com.grudus.nativeexamshelper.helpers.exceptions.ExceptionsHelper;
import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.pojos.JsonUser;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import java.util.Calendar;

import rx.Observable;
import rx.schedulers.Schedulers;

public class ServerTransporter {

    public static final String TAG = "@@@" + ServerTransporter.class.getSimpleName();

    public static void tryToShareDataWithServer(Context context) {
        final long lastModified = getLastModifiedTime(context);


        RetrofitMain retrofit = new RetrofitMain(context);
        ExamsDbHelper helper = ExamsDbHelper.getInstance(context);
        helper.openDB();

        retrofit.getUserInfo()
                .flatMap(response -> {
                    ExceptionsHelper.checkResponse(response);
                    JsonUser user = response.body();

                    Log.d(TAG, "onCreate: last modified user " + DateHelper.getReadableDataFromLong(user.getLastModified()));
                    Log.d(TAG, "onCreate: last modified system: " + DateHelper.getReadableDataFromLong(lastModified));

                    if (areEquals(user.getLastModified(), lastModified)) {
                        Log.d(TAG, "onCreate: modified equals");
                        return Observable.empty();
                    }

                    if (user.getLastModified() > lastModified) {
                        return retrofit.getModifiedSubjects()
                                .flatMap(arrayResponse -> {
                                    ExceptionsHelper.checkResponse(arrayResponse);
                                    return helper.insertSubjects(arrayResponse.body());
                                }).flatMap(howMany -> retrofit.getModifiedExam())
                                .flatMap(arrayResponse -> {
                                    ExceptionsHelper.checkResponse(arrayResponse);
                                    return helper.insertExams(arrayResponse.body());
                                });
                    } else {
                        CursorToArrayConverter converter = new CursorToArrayConverter(context);
                        return converter.getModifiedSubjectsAsJson(user.getLastModified())
                                .flatMap(retrofit::sendSubjectsToServer)
                                .flatMap(voidResponse -> {
                                    ExceptionsHelper.checkResponse(voidResponse);
                                    return converter.getModifiedExamsAsJson(user.getLastModified());
                                }).flatMap(retrofit::insertExams);
                    }
                })
                .flatMap(i -> helper.removeSubjectWithDeleteChange())
                .flatMap(i -> helper.removeExamsWithChangeDelete())
                .subscribeOn(Schedulers.io())
                .subscribe(i -> Log.e(TAG, "retro onNext: removed " + i),
                        error -> Log.e(TAG, "retro on: error", error),
                        () -> {
                            Log.e(TAG, "retro: on completed");
                            new UserPreferences(context).changeLastModifiedToNow();
                        });

    }


    private static long getLastModifiedTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.key_last_modified), Calendar.getInstance().getTime().getTime());
    }

    private static boolean areEquals(long lastModified, long lastModified1) {
        return Math.abs(lastModified - lastModified1) < (1000);
    }


}