package com.grudus.nativeexamshelper.database.converters;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.database.subjects.SubjectEntry;
import com.grudus.nativeexamshelper.pojos.JsonExam;
import com.grudus.nativeexamshelper.pojos.JsonSubject;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import java.util.ArrayList;
import java.util.Date;

import rx.Observable;

public class CursorToArrayConverter {

    public static final String TAG = "@@@" + CursorToArrayConverter.class.getSimpleName();

    private final Context context;
    private final ExamsDbHelper examsDbHelper;
    private final Long userId;

    public CursorToArrayConverter(Context context) {
        this.context = context;
        examsDbHelper = ExamsDbHelper.getInstance(context);
        userId = new UserPreferences(context).getLoggedUser().getId();
    }

    public <T> Observable<ArrayList<T>> getObjectsAsJson(Observable<Cursor> cursorObservable, Jsonable<T> converter) {
        return cursorObservable
                .flatMap(cursor -> {
                    ArrayList<T> jsonObjects = new ArrayList<T>(cursor.getCount());

                    Log.d(TAG, "getObjectsAsJson: find " + jsonObjects.size() + " elements");

                    if (cursor.moveToFirst()) {
                        do {
                            T json = converter.getJson(cursor);
                            Log.d(TAG, "getObjectsAsJson: " + json);
                            jsonObjects.add(json);
                        } while (cursor.moveToNext());

                    }

                    Log.d(TAG, "getObjectsAsJson: after cursor loop");
                    cursor.close();
                    return Observable.create(subscriber -> {
                        subscriber.onNext(jsonObjects);
                        subscriber.onCompleted();
                    });
                });
    }

    private Observable<ArrayList<JsonSubject>> getSubjectsAsJson(Observable<Cursor> cursorObservable) {
        return getObjectsAsJson(cursorObservable, getSubject());
    }

    private Jsonable<JsonSubject> getSubject() {
        return cursor ->
            new JsonSubject(
                    cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX),
                    userId,
                    cursor.getString(SubjectEntry.TITLE_COLUMN_INDEX),
                    cursor.getString(SubjectEntry.COLOR_COLUMN_INDEX),
                    cursor.getInt(SubjectEntry.DELETED_COLUMN_INDEX) != 0,
                    cursor.getLong(SubjectEntry.MODIFIED_COLUMN_INDEX),
                    cursor.getInt(SubjectEntry.HAS_GRADE_COLUMN_INDEX) != 0);
    }

    public Jsonable<JsonExam> getExam() {
        return cursor ->
                new JsonExam(
                        cursor.getLong(ExamEntry.INDEX_COLUMN_INDEX),
                        cursor.getLong(ExamEntry.SUBJECT_ID_COLUMN_INDEX),
                        userId,
                        cursor.getString(ExamEntry.INFO_COLUMN_INDEX),
                        new Date(cursor.getLong(ExamEntry.DATE_COLUMN_INDEX)),
                        cursor.getDouble(ExamEntry.GRADE_COLUMN_INDEX),
                        cursor.getLong(ExamEntry.MODIFIED_COLUMN_INDEX),
                        cursor.getInt(ExamEntry.DELETED_COLUMN_INDEX) != 0);


    }

    public Observable<ArrayList<JsonSubject>> getModifiedSubjectsAsJson(long lastModified) {
        return getSubjectsAsJson(examsDbHelper.getSubjectsModifiedAfter(lastModified));
    }

    public Observable<ArrayList<JsonExam>> getModifiedExamsAsJson(long lastModified) {
        return getObjectsAsJson(examsDbHelper.getExamsModifiedAfter(lastModified), getExam());
    }


    interface Jsonable<T> {
        T getJson(Cursor cursor);
    }


}
