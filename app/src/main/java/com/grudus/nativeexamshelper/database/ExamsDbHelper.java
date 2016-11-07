package com.grudus.nativeexamshelper.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.grudus.nativeexamshelper.R;
import com.grudus.nativeexamshelper.database.exams.ExamEntry;
import com.grudus.nativeexamshelper.database.exams.ExamsQuery;
import com.grudus.nativeexamshelper.database.subjects.SubjectEntry;
import com.grudus.nativeexamshelper.database.subjects.SubjectsQuery;
import com.grudus.nativeexamshelper.pojos.Exam;
import com.grudus.nativeexamshelper.pojos.JsonExam;
import com.grudus.nativeexamshelper.pojos.JsonSubject;
import com.grudus.nativeexamshelper.pojos.Subject;

import java.util.Calendar;
import java.util.List;

import rx.Observable;

public class ExamsDbHelper extends SQLiteOpenHelper {

    public static final String TAG = "@@@ Main DB HELPER @@@";

    public static final String DATABASE_NAME = "ExamsHelper.db";
    public static final int DATABASE_VERSION = 19;

    private SQLiteDatabase database;
    private Context context;

    private static ExamsDbHelper instance;


    @Deprecated // test only
    public ExamsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        instance = this;
        Log.d(TAG, "ExamsDbHelper() constructor");
    }

    public static ExamsDbHelper getInstance(Context context) {
        if (instance == null)
            instance = new ExamsDbHelper(context.getApplicationContext());
        return instance;
    }

    @Deprecated //test only
    public static void setInstance(ExamsDbHelper examsDbHelper) {
        instance = examsDbHelper;
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate() ExamsDbHelper");
        db.execSQL(ExamEntry.CREATE_TABLE_QUERY);
        db.execSQL(SubjectEntry.CREATE_TABLE_QUERY);

        SubjectsQuery.setDefaultSubjects(context.getResources().getStringArray(R.array.default_subjects));
        SubjectsQuery.setDefaultColors(context.getResources().getStringArray(R.array.defaultSubjectsColors));
        SubjectsQuery.firstInsert(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ExamEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SubjectEntry.TABLE_NAME);

        onCreate(db);
    }

    public void openDBIfClosed() {
        if (database == null || !database.isOpen())
            database = this.getWritableDatabase();
    }

    public void openDB() {
        database = this.getWritableDatabase();
//        Log.d(TAG, "Database is opened");
    }

    public void openDBReadOnly() {
        database = this.getReadableDatabase();
//        Log.d(TAG, "openDBReadOnly: opened");
    }

    public void closeDB() {
        if (database != null && database.isOpen())
            database.close();
        super.close();
//        Log.d(TAG, "Database is closed");
    }


//    Subjects part ******************************

    public Observable<Cursor> getAllSubjectsSortByTitle() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.getAllRecordsAndSortByTitle(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getAllSubjectsWithoutDeleteChangeSortByTitle() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.getAllNotDeletedRecordsSortByTitle(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Long> insertSubject(Subject subject) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.insert(database, subject));
            subscriber.onCompleted();
        });
    }


    public Observable<Integer> updateSubject(Subject old, Subject _new) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.update(database, old, _new));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> setSubjectHasGrade(Subject subject, boolean hasGrade) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.setSubjectHasGrade(database, subject, hasGrade));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> removeSubject(String subjectTitle) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.removeSubjectPermanently(database, subjectTitle));
            subscriber.onCompleted();
        });
    }


    public Observable<Subject> findSubjectById(Long id) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.findById(database, id));
            subscriber.onCompleted();
        });
    }

    public Observable<Subject> findSubjectByTitle(String title) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.findByTitle(database, title));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> updateSubjectSetDelete(Subject subject) {
        return Observable.create(subscriber -> {
            subscriber.onNext(SubjectsQuery.updateSetDeleted(database, subject));
            subscriber.onCompleted();
        });
    }


    public Observable<Integer> removeAllExamsRelatedWithSubject(Long subjectId) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.removeSubjectExams(database, subjectId));
            subscriber.onCompleted();
        });
    }




    public Observable<Integer> removeSubjectWithDeleteChange() {
        return Observable.create(subscriber -> {
           openDBIfClosed();
            subscriber.onNext(SubjectsQuery.removeDeletedSubjectsOlderThan(database, Calendar.getInstance().getTime().getTime()));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getSubjectsWithGrade() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.findSubjectsWithGradesAndSortBy(database, null));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> firstSubjectsInsert() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.firstInsert(database));
            subscriber.onCompleted();
        });
    }
//    Exams part ********************************

    public Observable<Cursor> getExamsWithoutDeleteChangeOlderThan(long time) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getAllExamsWithoutDeleteChangeOlderThan(database, time));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getCountOfOldExamsPerMonth() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getCountOfOldExamsPerMonth(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getCountOfOldExamsPerMonth(Long subjectId) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getCountOfOldExamsPerMonth(database, subjectId));
            subscriber.onCompleted();
        });
    }


    public Observable<Cursor> getSubjectGradesWithoutDeleteChange(@NonNull Long subjectId) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.findGradesWithoutDeleteChangeAndSortBy(database, subjectId, null));
            subscriber.onCompleted();
        });
    }


    public Observable<Cursor> getAllIncomingExamsWithoutDeleteChangeSortByDate() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getAllIncomingExamsWithoutDeleteChangeAndSortByDate(database));
            subscriber.onCompleted();
        });
    }


    public Observable<Integer> removeExamsWithChangeDelete() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.removeExamsWithChangeDelete(database));
            subscriber.onCompleted();
        });
    }


    public Observable<Double> getGradesFromOrderedSubjectGrades(Long subjectId) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            Cursor cursor = ExamsQuery.findGradesWithoutDeleteChangeAndSortBy(database, subjectId, ExamEntry.GRADE_COLUMN);
            cursor.moveToFirst();
            // first returned item - size of the items
            subscriber.onNext((double)cursor.getCount());
            do {
                double temp = cursor.getDouble(ExamEntry.GRADE_COLUMN_INDEX);
                subscriber.onNext(temp);
            } while (cursor.moveToNext());

            cursor.close();
            subscriber.onCompleted();
        });
    }

    public Observable<Long> insertExam(Exam exam) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.insert(database, exam));
            subscriber.onCompleted();
        });
    }


    public Observable<Integer> updateExamSetGrade(Exam exam, double grade) {
        openDBIfClosed();

        return findSubjectById(exam.getSubjectId())
                .flatMap(subject -> setSubjectHasGrade(subject, true))
                .flatMap((updatedRows) -> Observable.create(subscriber -> {
                    subscriber.onNext(ExamsQuery.updateSetGrade(database, exam, grade));
                    subscriber.onCompleted();
                }));
    }


    public Observable<Integer> removeExam(Long id) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.remove(database, id));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> removeAllExams() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.removeAll(database));
            subscriber.onCompleted();
        });
    }


    public Observable<Cursor> getExamsWithoutGradeWithoutDeleteChange() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.findGradesWithoutGradesAndWithoutDeleteChange(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> updateAllExamsSetDeleteChange() {
        return Observable.create(subscriber -> {
            subscriber.onNext(ExamsQuery.updateExamsSetDelete(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> updateAllSubjectsSetChangeDelete() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.updateAllChangesToDelete(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getAllExams() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getAllExams(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> removeAllSubjects() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.deleteAll(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getSubjectsModifiedAfter(long lastModified) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.getAllModifiedAfter(database, lastModified));
           subscriber.onCompleted();
        });
    }

    public Observable<Integer> insertSubjects(List<JsonSubject> jsonSubjects) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.insertSubjects(database, jsonSubjects));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> removeDeletedSubjectsOlderThan(long time) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.removeDeletedSubjectsOlderThan(database, time));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> removeAllOldSubjectExams(Subject subject) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.removeAllOldExamsRelatedWithSubject(subject.getId(), database));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> insertExams(List<JsonExam> jsonExams) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.insertExams(database, jsonExams));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getExamsModifiedAfter(long lastModified) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getAllModifiedAfter(database, lastModified));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> updateExamSetDeleteChange(Long id) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.updateExamSetDelete(database, id));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> updateAllSubjectsSetHasGrade(boolean hasGrade) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.updateAllSetHasGrade(database, hasGrade));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getSubjectsExamsQuantity() {
        return Observable.create(subscriber -> {
             openDBIfClosed();
            subscriber.onNext(SubjectsQuery.getSubjectsExamsCount(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getAllSubjectWithGradeTitles() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(SubjectsQuery.getSubjectWithGradesTitles(database));
            subscriber.onCompleted();
        });
    }



    public Observable<Cursor> getMonthlyExamsGrades() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getGradesPerMonth(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getMonthlyExamsGrades(Long id) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getGradesPerMonth(database, id));
            subscriber.onCompleted();
        });
    }

    public Observable<Cursor> getMonthlyRoundedExamsGrades(Long id) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.getRoundedGradesPerMonth(database, id));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> updateExamsFromPastWithoutGrade() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.updateExamsFromPastWithoutGrade(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> cleanDb() {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.updateExamsSetDelete(database));
            subscriber.onNext(SubjectsQuery.updateAllChangesToDelete(database));
            subscriber.onNext(SubjectsQuery.firstInsert(database));
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> removeDeletedExamsOlderThan(long time) {
        return Observable.create(subscriber -> {
            openDBIfClosed();
            subscriber.onNext(ExamsQuery.removeDeletedExamsOlderThan(database, time));
            subscriber.onCompleted();
        });
    }
}
