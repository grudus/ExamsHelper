package com.grudus.nativeexamshelper.helpers.normal;

import android.database.Cursor;
import android.util.Log;

import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.database.subjects.SubjectEntry;

import java.util.Date;


public class CursorHelper {
    public static final String TAG = "@@@" + CursorHelper.class.getSimpleName();
    
    public static void printExam(Cursor cursor) {
        Log.d(TAG, "printExam: printExams: " + cursor.getLong(ExamEntry.INDEX_COLUMN_INDEX) + ", id: "
                + (cursor.getLong(ExamEntry.SUBJECT_ID_COLUMN_INDEX)) + ", info: "
                + cursor.getString(ExamEntry.INFO_COLUMN_INDEX) + ", date: "
                + DateHelper.getReadableDataFromLong(cursor.getLong(ExamEntry.DATE_COLUMN_INDEX)) + ", grade: "
                + cursor.getString(ExamEntry.GRADE_COLUMN_INDEX) + ", modified: "
                +  DateHelper.getReadableDataFromLong(cursor.getLong(ExamEntry.MODIFIED_COLUMN_INDEX)) + ", deleted: "
                + cursor.getString(ExamEntry.DELETED_COLUMN_INDEX)
        );
    }

    public static void printSubject(Cursor cursor) {
        Log.d(TAG, "printSubject: id: " + cursor.getLong(SubjectEntry.INDEX_COLUMN_INDEX) + ", title: "
                + cursor.getString(SubjectEntry.TITLE_COLUMN_INDEX) + ", deleted: "
                + cursor.getString(SubjectEntry.DELETED_COLUMN_INDEX) + ", color: "
                + cursor.getString(SubjectEntry.COLOR_COLUMN_INDEX) + ", hasgrade: "
                + cursor.getString(SubjectEntry.HAS_GRADE_COLUMN_INDEX) + ", mod: "
                + "Last modified: " + new Date(cursor.getLong(SubjectEntry.MODIFIED_COLUMN_INDEX)));
    }

    public static void printCursor(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            builder.append(cursor.getColumnName(i))
                    .append(": ")
                    .append(cursor.getString(i));
        }
        Log.d(TAG, "printCursor: " + builder.toString());
    }
}
