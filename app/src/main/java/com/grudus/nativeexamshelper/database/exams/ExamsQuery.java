package com.grudus.nativeexamshelper.database.exams;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.grudus.nativeexamshelper.activities.ExamsMainActivity;
import com.grudus.nativeexamshelper.database.QueryHelper;
import com.grudus.nativeexamshelper.helpers.normal.DateHelper;
import com.grudus.nativeexamshelper.pojos.Exam;
import com.grudus.nativeexamshelper.pojos.JsonExam;
import com.grudus.nativeexamshelper.pojos.grades.Grades;

import java.util.Calendar;
import java.util.List;

import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.AND;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.AVG;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.COUNT;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.EQ;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.GT;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.IS_NULL;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.LT;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.MONTH;
import static com.grudus.nativeexamshelper.database.SqliteQueryNamespace.YEAR;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.ALL_COLUMNS;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.DATE_COLUMN;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.DELETED_COLUMN;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.GRADE_COLUMN;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.INFO_COLUMN;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.MODIFIED_COLUMN;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.SUBJECT_ID_COLUMN;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry.TABLE_NAME;
import static com.grudus.nativeexamshelper.database.exams.ExamEntry._ID;

public class ExamsQuery {

    public static Cursor getAllIncomingExamsWithoutDeleteChangeAndSortByDate(SQLiteDatabase db) {
        long time = System.currentTimeMillis();

        return db.query(
                TABLE_NAME,
                ALL_COLUMNS,
                DATE_COLUMN + GT + AND + DELETED_COLUMN + EQ,
                new String[] {Long.toString(time), "0"},
                null,
                null,
                DATE_COLUMN
                );
    }

    public static Cursor getAllExamsWithoutDeleteChangeOlderThan(SQLiteDatabase db, long time) {

        return db.query(
                TABLE_NAME,
                ALL_COLUMNS,
                DATE_COLUMN + LT + AND + DELETED_COLUMN + EQ,
                new String[] {Long.toString(time), "0"},
                null,
                null,
                null
        );
    }

    public static Cursor getCountOfOldExamsPerMonth(SQLiteDatabase db) {
        return db.query(
                TABLE_NAME,
                new String[] {YEAR, MONTH, COUNT},
                DELETED_COLUMN + EQ + AND + GRADE_COLUMN + GT,
                new String[] {"0", "0"},
                YEAR + ", " + MONTH,
                null,
                YEAR
        );

    }

    public static Cursor getCountOfOldExamsPerMonth(SQLiteDatabase db, Long subjectId) {
        return db.query(
                TABLE_NAME,
                new String[]{YEAR, MONTH, COUNT},
                DELETED_COLUMN + EQ + AND + GRADE_COLUMN + GT + AND + SUBJECT_ID_COLUMN + EQ,
                new String[]{"0", "0", subjectId.toString()},
                YEAR + ", " + MONTH,
                null,
                YEAR
        );
    }


    public static Cursor getAllRecordsAndSortByDate(SQLiteDatabase db) {
        return QueryHelper.getAllRecordsAndSortBy(db, TABLE_NAME
                , ALL_COLUMNS, DATE_COLUMN);
    }

    public static Cursor getAllRecords(SQLiteDatabase db) {
        return QueryHelper.getAllRecordsAndSortBy(db, TABLE_NAME,
                ALL_COLUMNS, null);
    }


    public static Cursor findGradesWithoutDeleteChangeAndSortBy(SQLiteDatabase db, @NonNull Long subjectId, @Nullable String sort) {
        final String WHERE = SUBJECT_ID_COLUMN + EQ
                + AND + DELETED_COLUMN + EQ
                + AND + GRADE_COLUMN + GT;

        return db.query(
                TABLE_NAME,
                ALL_COLUMNS,
                WHERE,
                new String[] {subjectId.toString(), "0", Double.toString(Grades.EMPTY)},
                null,
                null,
                sort
        );
    }

    public static long insert(SQLiteDatabase db, Exam exam) {
        ContentValues contentValues = new ContentValues(6);
        contentValues.put(SUBJECT_ID_COLUMN, exam.getSubjectId());
        contentValues.put(INFO_COLUMN, exam.getInfo());
        contentValues.put(DATE_COLUMN, DateHelper.getLongFromDate(exam.getDate()));
        contentValues.put(DELETED_COLUMN, "0");
        contentValues.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        if (exam.getDate().getTime() > Calendar.getInstance().getTime().getTime())
            contentValues.put(GRADE_COLUMN, (String)null);
        else
        contentValues.put(GRADE_COLUMN, Grades.EMPTY);

        return db.insert(TABLE_NAME, null, contentValues);
    }

    public static int remove(SQLiteDatabase db, Long id) {
        return db.delete(TABLE_NAME,
                _ID + EQ,
                new String[] {id.toString()});
    }


    public static int removeAll(SQLiteDatabase db) {
        return db.delete(TABLE_NAME,
                null,
                null);
    }

    public static int removeSubjectExams(SQLiteDatabase db, Long subjectId) {
        return db.delete(TABLE_NAME,
                SUBJECT_ID_COLUMN + EQ,
                new String[] {subjectId.toString()});
    }


    public static Integer updateSetGrade(SQLiteDatabase database, Exam exam, double grade) {
        ContentValues contentValues = new ContentValues(2);
        contentValues.put(GRADE_COLUMN, grade);
        contentValues.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return database.update(
                TABLE_NAME,
                contentValues,
                _ID + EQ,
                new String[] {exam.getId().toString()}
        );
    }

    public static Integer removeExamsWithChangeDelete(SQLiteDatabase database) {
        return database.delete(
                TABLE_NAME,
                DELETED_COLUMN + EQ,
                new String[] {"1"}
        );
    }

    public static Cursor findGradesWithoutGradesAndWithoutDeleteChange(SQLiteDatabase database) {
        final String WHERE = DELETED_COLUMN + EQ + AND + GRADE_COLUMN + EQ;

        return database.query(
                TABLE_NAME,
                ALL_COLUMNS,
                WHERE,
                new String[] {"0", Double.toString(Grades.EMPTY)},
                null,
                null,
                null
        );
    }

    public static Integer updateExamsSetDelete(SQLiteDatabase database) {
        ContentValues cv = new ContentValues(2);
        cv.put(DELETED_COLUMN, "1");
        cv.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return database.update(
                TABLE_NAME,
                cv,
                null,
                null
        );
    }

    public static Cursor getAllExams(SQLiteDatabase database) {
        return database.query(
                TABLE_NAME,
                ALL_COLUMNS,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Integer removeAllOldExamsRelatedWithSubject(long id, SQLiteDatabase database) {
        return database.delete(
                TABLE_NAME,
                SUBJECT_ID_COLUMN + EQ + AND + DATE_COLUMN + LT,
                new String[] {Long.toString(id), Long.toString(Calendar.getInstance().getTime().getTime())}
        );
    }

    public static Integer insertExams(SQLiteDatabase db, List<JsonExam> jsonExams) {
        ContentValues[] values = new ContentValues[jsonExams.size()];

        for (int i = 0; i < jsonExams.size(); i++) {
            ContentValues cv = new ContentValues(6);
            JsonExam exam = jsonExams.get(i);

            cv.put(SUBJECT_ID_COLUMN, exam.getSubjectId());
            cv.put(INFO_COLUMN, exam.getExamInfo());
            cv.put(DATE_COLUMN, exam.getDate().getTime());
            cv.put(GRADE_COLUMN, exam.getGrade());
            cv.put(MODIFIED_COLUMN, exam.getLastModified());
            cv.put(DELETED_COLUMN, exam.isDeleted() ? 1 : 0);

            values[i] = cv;
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

    public static Integer updateExamSetDelete(SQLiteDatabase database, Long id) {
        ContentValues cv = new ContentValues(2);
        cv.put(DELETED_COLUMN, 1);
        cv.put(MODIFIED_COLUMN, Calendar.getInstance().getTime().getTime());

        return database.update(
                TABLE_NAME,
                cv,
                _ID + EQ,
                new String[] {id.toString()}
        );
    }

    public static Cursor getGrades(SQLiteDatabase database) {
        return database.query(
                TABLE_NAME,
                new String[] {GRADE_COLUMN},
                GRADE_COLUMN + GT + AND + DELETED_COLUMN + EQ,
                new String[] {"0", "0"},
                null,
                null,
                null
        );
    }

    public static Cursor getGradesPerMonth(SQLiteDatabase database) {
        return database.query(
                TABLE_NAME,
                new String[] {YEAR, MONTH, AVG + "(" + GRADE_COLUMN + ")"},
                DELETED_COLUMN + EQ + AND + GRADE_COLUMN + GT,
                new String[] {"0", "0"},
                YEAR + ", " + MONTH,
                null,
                YEAR
        );
    }

    public static Cursor getGradesPerMonth(SQLiteDatabase database, Long id) {
        return database.query(
                TABLE_NAME,
                new String[] {YEAR, MONTH, AVG + "(" + GRADE_COLUMN + ")"},
                SUBJECT_ID_COLUMN + EQ + AND + DELETED_COLUMN + EQ + AND + GRADE_COLUMN + GT,
                new String[] {id.toString(), "0", "0"},
                YEAR + ", " + MONTH,
                null,
                YEAR
        );
    }

    public static Cursor getRoundedGradesPerMonth(SQLiteDatabase database, Long id) {
        final boolean withId = id > -1;
        return database.query(
                TABLE_NAME,
                new String[] {YEAR, MONTH, AVG + "(ROUND(" + GRADE_COLUMN + "))"},
                (withId ? SUBJECT_ID_COLUMN + EQ + AND : "") + DELETED_COLUMN + EQ + AND + GRADE_COLUMN + GT,
                (withId ? new String[] {id.toString(), "0", "0"} : new String[] {"0", "0"}),
                YEAR + ", " + MONTH,
                null,
                YEAR
        );
    }


    public static Integer updateExamsFromPastWithoutGrade(SQLiteDatabase database) {
        long now = Calendar.getInstance().getTime().getTime();

        ContentValues values = new ContentValues(2);
        values.put(MODIFIED_COLUMN, now);
        values.put(GRADE_COLUMN, Grades.EMPTY);

        return database.update(
                TABLE_NAME,
                values,
                DATE_COLUMN + LT + AND + GRADE_COLUMN + IS_NULL,
                new String[] {Long.toString(now)}
        );

    }

    public static Integer removeDeletedExamsOlderThan(SQLiteDatabase database, long time) {
        return database.delete(
                TABLE_NAME,
                DELETED_COLUMN + EQ + AND + MODIFIED_COLUMN + LT,
                new String[] {"1", Long.toString(time)}
        );
    }
}
