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


    /**
     * TODO: 17.10.16 clean this code
     * <p>
     * 1) get information about last modification on server and compare with last modification on phone
     * 1a) equals - do nothin
     * 1b) server's data is newer - get fresh subjects from server and save on device
     * 1c) phone's data is newer - send modified data to server
     */

    public static Observable<?> tryToShareDataWithServer(Context context) {
        final long lastModified = getLastModifiedTime(context);


        RetrofitMain retrofit = new RetrofitMain(context);
        ExamsDbHelper helper = ExamsDbHelper.getInstance(context);
        helper.openDB();

        return retrofit.getUserInfo()
                .flatMap(response -> {
                    ExceptionsHelper.checkResponse(response);
                    JsonUser user = response.body();

                    Log.d(TAG, "onCreate: last modified user " + DateHelper.getReadableDataFromLong(user.getLastModified(), "dd/MM/yyyy HH:mm:ss"));
                    Log.d(TAG, "onCreate: last modified system: " + DateHelper.getReadableDataFromLong(lastModified, "dd/MM/yyyy HH:mm:ss"));

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
                .flatMap(i -> helper.removeExamsWithChangeDelete());
    }


    private static long getLastModifiedTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.key_last_modified), 2);
    }

    private static boolean areEquals(long lastModified, long lastModified1) {
        return Math.abs(lastModified - lastModified1) < (1000);
    }


}