package com.grudus.nativeexamshelper.helpers;


import android.content.Context;

import com.grudus.nativeexamshelper.database.ExamsDbHelper;
import com.grudus.nativeexamshelper.pojos.UserPreferences;

import java.util.Calendar;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExamsHelper {

    private final Context context;

    private static final long TEN_DAYS_MILLIS = 10 * 24 * 3600 * 1000;

    public ExamsHelper(Context context) {
        this.context = context;
    }

    public void setEmptyGradeToPastExams() {
        ExamsDbHelper dbHelper = ExamsDbHelper.getInstance(context);
        dbHelper.openDBIfClosed();

        dbHelper.updateExamsFromPastWithoutGrade()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(howMany -> {
                    if (howMany > 0)
                        new UserPreferences(context).changeLastModifiedToNow();
                });
    }

    public void removeTrash() {
        ExamsDbHelper dbHelper = ExamsDbHelper.getInstance(context);
        dbHelper.openDBIfClosed();

        dbHelper.removeDeletedExamsOlderThan(Calendar.getInstance().getTime().getTime() - TEN_DAYS_MILLIS);
        dbHelper.removeDeletedSubjectsOlderThan(Calendar.getInstance().getTime().getTime() - TEN_DAYS_MILLIS);
    }
}
