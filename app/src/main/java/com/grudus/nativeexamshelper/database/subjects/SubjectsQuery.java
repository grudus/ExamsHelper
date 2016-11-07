package com.grudus.nativeexamshelper.database.subjects;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.grudus.nativeexamshelper.activities.ExamsMainActivity;
import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.database.QueryHelper;
import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.pojos.JsonSubject;
import com.grudus.nativeexamshelper.pojos.Subject;

import java.util.Calendar;
import java.util.List;

import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.AND;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.EQ;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.GT;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.LT;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.ALL_COLUMNS;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.COLOR_COLUMN;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.COLOR_COLUMN_INDEX;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.DELETED_COLUMN;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.HAS_GRADE_COLUMN;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.INDEX_COLUMN_INDEX;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.MODIFIED_COLUMN;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.TABLE_NAME;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.TITLE_COLUMN;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry.TITLE_COLUMN_INDEX;
import static com.grudus.nativeexamshelper.database.subjects.SubjectEntry._ID;



public final class SubjectsQuery {

    private static String[] defaultSubjects = new String[0];
    private static String[] defaultColors = new String[0];

    public static void setDefaultSubjects(@NonNull String[] defaultSubjects) {
        SubjectsQuery.defaultSubjects = defaultSubjects;
    }

    public static void setDefaultColors(@NonNull String[] defaultColors) {
        SubjectsQuery.defaultColors = defaultColors;
    }

    public static int firstInsert(SQLiteDatabase db) {
        ContentValues[] firstValues = new ContentValues[defaultSubjects.length];
        final long time = Calendar.getInstance().getTime().getTime();
        for (int i = 0; i < firstValues.length; i++) {

            firstValues[i] = new ContentValues(5);
            firstValues[i].put(TITLE_COLUMN, defaultSubjects[i]);
            firstValues[i].put(COLOR_COLUMN,
                    defaultColors[i]);
            firstValues[i].put(HAS_GRADE_COLUMN, "0");
            firstValues[i].put(DELETED_COLUMN, "0");
            firstValues[i].put(MODIFIED_COLUMN, time);

            Log.i(ExamsMainActivity.TAG, "Color is " + defaultColors[i]);
        }
        int counter = 0;

        try {
            db.beginTransaction();
            for (ContentValues val : firstValues) {
                long _id = db.insertWithOnConflict(TABLE_NAME, null, val, SQLiteDatabase.CONFLICT_REPLACE);

                if (_id != -1) counter++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        Log.d(ExamsMainActivity.TAG, "Inserted " + counter + " values");
        return firstValues.length;
    }


    public static int deleteAll(SQLiteDatabase db) {
        return db.delete(TABLE_NAME, null, null);
    }

    public static Cursor getAllRecords(SQLiteDatabase db) {
        return QueryHelper.getAllRecordsAndSortBy(db, TABLE_NAME,
                ALL_COLUMNS, null);
    }

    public static Cursor getAllRecordsAndSortByTitle(SQLiteDatabase db) {
        return QueryHelper.getAllRecordsAndSortBy(db, TABLE_NAME,
                ALL_COLUMNS, TITLE_COLUMN);
    }

    @Nullable
    public static Subject findById(SQLiteDatabase db, Long id) {
        Cursor c = db
                .query(
                        TABLE_NAME,
                        ALL_COLUMNS,
                        _ID + EQ,
                        new String[] {id.toString()},
                        null,
                        null,
                        null
                );

        if (!c.moveToFirst()) {
            c.close();
            throw new NullPointerException("Cannot find subject with id " + id);
        }

        Subject subject = new Subject(c.getLong(INDEX_COLUMN_INDEX),
                c.getString(TITLE_COLUMN_INDEX),
                c.getString(COLOR_COLUMN_INDEX)
                );

        c.close();

        return subject;

    }

    public static long insert(SQLiteDatabase db, Subject subject) {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(TITLE_COLUMN, subject.getTitle());
        contentValues.put(COLOR_COLUMN, subject.getColor());
        contentValues.put(DELETED_COLUMN, "0");
        contentValues.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return db.insert(TABLE_NAME, null, contentValues);
    }


    public static int update(SQLiteDatabase db, Subject old, Subject _new) {
        if (findById(db, old.getId()) == null) {
            Log.e(ExamsDbHelper.TAG, "update: " + old + " doesn't exists");
            return -1;
        }

        ContentValues cv = new ContentValues(3);
        cv.put(TITLE_COLUMN, _new.getTitle());
        cv.put(COLOR_COLUMN, _new.getColor());
        cv.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return db.update(TABLE_NAME,
                cv,
                TITLE_COLUMN + EQ,
                new String[] {old.getTitle()});
    }

    public static Cursor findSubjectsWithGradesAndSortBy(SQLiteDatabase db, @Nullable String sort) {
        return db.query(
                TABLE_NAME,
                ALL_COLUMNS,
                HAS_GRADE_COLUMN + EQ + AND + DELETED_COLUMN + EQ,
                new String[] {"1", "0"},
                null,
                null,
                sort
        );
    }

    public static int setSubjectHasGrade(SQLiteDatabase db, Subject subject, boolean hasGrade) {
        ContentValues cv = new ContentValues(4);
        cv.put(TITLE_COLUMN, subject.getTitle());
        cv.put(COLOR_COLUMN, subject.getColor());
        cv.put(HAS_GRADE_COLUMN, hasGrade ? 1 : 0);
        cv.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());
        return db.update(
                TABLE_NAME,
                cv,
                TITLE_COLUMN + EQ,
                new String[] {subject.getTitle()}
        );
    }


    public static int removeSubjectPermanently(SQLiteDatabase db, String subjectTitle) {
        return db.delete(
                TABLE_NAME,
                TITLE_COLUMN + EQ,
                new String[] {subjectTitle}
        );
    }



    public static Cursor getAllNotDeletedRecordsSortByTitle(SQLiteDatabase database) {
        return database.query(
                TABLE_NAME,
                ALL_COLUMNS,
                DELETED_COLUMN + EQ,
                new String[] {"0"},
                null,
                null,
                TITLE_COLUMN
        );
    }

    public static Integer removeDeletedSubjectsOlderThan(SQLiteDatabase database, long time) {
        return database.delete(
                TABLE_NAME,
                DELETED_COLUMN + EQ + AND + MODIFIED_COLUMN + LT,
                new String[] {"1", Long.toString(time)}
        );
    }

    public static Subject findByTitle(SQLiteDatabase database, String title) {
        Cursor cursor = database.query(
                TABLE_NAME,
                ALL_COLUMNS,
                TITLE_COLUMN + EQ,
                new String[] {title},
                null,
                null,
                null
        );

        if (cursor == null)
            return Subject.empty();

        cursor.moveToFirst();
        Subject subject = new Subject(
                cursor.getLong(INDEX_COLUMN_INDEX),
                cursor.getString(TITLE_COLUMN_INDEX),
                cursor.getString(COLOR_COLUMN_INDEX)
        );

        cursor.close();
        return subject;
    }

    public static Integer updateAllChangesToDelete(SQLiteDatabase database) {
        ContentValues cv = new ContentValues(2);
        cv.put(DELETED_COLUMN, 1);
        cv.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());


        return database.update(
                TABLE_NAME,
                cv,
                null,
                null
        );
    }

    public static Integer updateSetDeleted(SQLiteDatabase database, Subject subject) {
        ContentValues cv = new ContentValues(2);
        cv.put(DELETED_COLUMN, 1);
        cv.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return database.update(
                TABLE_NAME,
                cv,
                _ID + " = ?",
                new String[] {String.valueOf(subject.getId())}
        );
    }

    public static Cursor getAllModifiedAfter(SQLiteDatabase database, long lastModified) {
        return database.query(
                TABLE_NAME,
                ALL_COLUMNS,
                MODIFIED_COLUMN + GT,
                new String[] {Long.toString(lastModified)},
                null,
                null,
                null
        );
    }

    public static Integer insertSubjects(SQLiteDatabase db, List<JsonSubject> jsonSubjects) {
        ContentValues[] values = new ContentValues[jsonSubjects.size()];

        for (int i = 0; i < jsonSubjects.size(); i++) {
            ContentValues cv = new ContentValues(6);
            JsonSubject subject = jsonSubjects.get(i);
            cv.put(_ID, subject.getId());
            cv.put(TITLE_COLUMN, subject.getTitle());
            cv.put(COLOR_COLUMN, subject.getColor());
            cv.put(HAS_GRADE_COLUMN, "0");
            cv.put(DELETED_COLUMN, "0");
            cv.put(MODIFIED_COLUMN, subject.getModified());
            values[i] = cv;

            Log.d("@@@" + SubjectsQuery.class.getSimpleName(), "insertSubjects: " + cv);
        }

        int counter = 0;

        try {
            db.beginTransaction();
            for (ContentValues val : values) {
                long _id = db.insertWithOnConflict(TABLE_NAME, null, val, SQLiteDatabase.CONFLICT_REPLACE);

                if (_id != -1) counter++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        Log.d(ExamsMainActivity.TAG, "Inserted " + counter + " values");
        return counter;
    }

    public static Integer updateAllSetHasGrade(SQLiteDatabase database, boolean hasGrade) {
        ContentValues values = new ContentValues(2);
        values.put(HAS_GRADE_COLUMN, hasGrade ? 1 : 0);
        values.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return database.update(
                TABLE_NAME,
                values,
                HAS_GRADE_COLUMN + EQ,
                new String[] {hasGrade ? "0" : "1"}
        );
    }

    public static Cursor getSubjectsExamsCount(SQLiteDatabase database) {
        final String SELECT = "SELECT " + TABLE_NAME + "." + TITLE_COLUMN + ", "
                + TABLE_NAME + "." + COLOR_COLUMN + ", "
                + "COUNT(" + TABLE_NAME + "." + _ID + ")";
        final String FROM = "FROM " + TABLE_NAME;
        final String INNER_JOIN = " INNER JOIN " + ExamEntry.TABLE_NAME + " ON " + TABLE_NAME + "." + _ID
                + " = " + ExamEntry.TABLE_NAME + "." + ExamEntry.SUBJECT_ID_COLUMN;
        final String WHERE = " WHERE " + TABLE_NAME + "." + DELETED_COLUMN + " = 0 "
                + AND + ExamEntry.TABLE_NAME + "." + ExamEntry.DELETED_COLUMN + " = 0"
                + AND + ExamEntry.TABLE_NAME + "." + ExamEntry.GRADE_COLUMN + " > 0";

        final String GROUP_BY = " GROUP BY " + TABLE_NAME + "." + TITLE_COLUMN;

        return database.rawQuery(SELECT + FROM + INNER_JOIN + WHERE + GROUP_BY, null);
    }

    public static Cursor getSubjectWithGradesTitles(SQLiteDatabase database) {
        return database.query(TABLE_NAME,
                new String[] {TITLE_COLUMN},
                DELETED_COLUMN + EQ + AND + HAS_GRADE_COLUMN + EQ,
                new String[] {"0", "1"},
                null,
                null,
                TITLE_COLUMN
        );
    }
}
