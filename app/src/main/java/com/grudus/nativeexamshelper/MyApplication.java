package com.grudus.nativeexamshelper;

import android.app.Application;
import android.util.Log;

import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.helpers.ExamsHelper;
import com.grudus.nativeexamshelper.helpers.normal.CursorHelper;
import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import rx.schedulers.Schedulers;


public class MyApplication extends Application {

    private static final String TAG = "@@@" + MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        initializeContexts();

        printDatabase(ExamsDbHelper.getInstance(this));

        new ExamsHelper(this).setEmptyGradeToPastExams();
        new ExamsHelper(this).removeTrash();

    }


    private void initializeContexts() {
        ThemeHelper.init(this);
        Grades.init(this);
        DateHelper.setDateFormat(getResources().getString(R.string.date_format));
    }



    private void printDatabase(ExamsDbHelper helper) {
        helper.openDB();
        helper.getAllSubjectsSortByTitle()
                .subscribeOn(Schedulers.io())
                .subscribe(cursor -> {
                    Log.d(TAG, "onCreate: getAllSubjects " + cursor);
                    if (cursor.moveToFirst()) {
                        do {
                            CursorHelper.printSubject(cursor);
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                });

        helper.getAllExams()
                .subscribeOn(Schedulers.io())
                .subscribe(cursor -> {
                    Log.d(TAG, "onCreate: getAllExams " + cursor);
                    if (cursor.moveToFirst()) {
                        do {
                            CursorHelper.printExam(cursor);
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                });
    }

}