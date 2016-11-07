package com.grudus.nativeexamshelper;

import android.app.Application;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.database.subjects.SubjectEntry;
import com.grudus.nativeexamshelper.helpers.ExamsHelper;
import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.helpers.normal.ThemeHelper;
import com.grudus.nativeexamshelper.net.ServerTransporter;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import java.util.Date;

import rx.schedulers.Schedulers;


public class MyApplication extends Application {

    private static final String TAG = "@@@" + MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        initializeContexts();

        if (!sharingDataWithServer())
            return;

        ServerTransporter.tryToShareDataWithServer(this);
        printDatabase(ExamsDbHelper.getInstance(this));

        new ExamsHelper(this).setEmptyGradeToPastExams();
        new ExamsHelper(this).removeTrash();

    }

    /**
     * TODO: 17.10.16 clean this code
     * <p>
     * 1) get information about last modification on server and compare with last modification on phone
     * 1a) equals - do nothin
     * 1b) server's data is newer - get fresh subjects from server and save on device
     * 1c) phone's data is newer - send modified data to server
     */


    private void initializeContexts() {
        ThemeHelper.init(this);
        Grades.init(this);
        DateHelper.setDateFormat(getResources().getString(R.string.date_format));
    }

    private boolean sharingDataWithServer() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_user_is_logged), false);
    }


    private void printDatabase(ExamsDbHelper helper) {
        helper.openDB();
        helper.getAllSubjectsSortByTitle()
                .subscribeOn(Schedulers.io())
                .subscribe(cursor -> {
                    Log.d(TAG, "onCreate: getAllSubjects " + cursor);
                    if (cursor.moveToFirst()) {
                        do {
                            printSubject(cursor);
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
                            printExam(cursor);
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                });
    }


    private void printExam(Cursor cursor) {
        Log.d(TAG, "printExam: printExams: " + cursor.getLong(ExamEntry.INDEX_COLUMN_INDEX) + ", id: "
                + (cursor.getLong(ExamEntry.SUBJECT_ID_COLUMN_INDEX)) + ", info: "
                + cursor.getString(ExamEntry.INFO_COLUMN_INDEX) + ", date: "
                + DateHelper.getReadableDataFromLong(cursor.getLong(ExamEntry.DATE_COLUMN_INDEX)) + ", grade: "
                + cursor.getString(ExamEntry.GRADE_COLUMN_INDEX) + ", modified: "
                +  DateHelper.getReadableDataFromLong(cursor.getLong(ExamEntry.MODIFIED_COLUMN_INDEX)) + ", deleted: "
                + cursor.getString(ExamEntry.DELETED_COLUMN_INDEX)
        );
    }

    private void printSubject(Cursor cursor) {
        Log.d(TAG, "printSubject: id: " + cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX) + ", title: "
                + cursor.getString(SubjectEntry.TITLE_COLUMN_INDEX) + ", deleted: "
                + cursor.getString(SubjectEntry.DELETED_COLUMN_INDEX) + ", color: "
                + cursor.getString(SubjectEntry.COLOR_COLUMN_INDEX) + ", hasgrade: "
                + cursor.getString(SubjectEntry.HAS_GRADE_COLUMN_INDEX) + ", mod: "
                + "Last modified: " + new Date(cursor.getLong(SubjectEntry.MODIFIED_COLUMN_INDEX)));
    }

}