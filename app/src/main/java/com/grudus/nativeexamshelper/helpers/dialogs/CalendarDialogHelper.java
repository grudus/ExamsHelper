package com.grudus.nativeexamshelper.helpers.dialogs;


import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class CalendarDialogHelper implements DatePickerDialog.OnDateSetListener {

    private Calendar calendar;
    private Context context;
    private AfterDateSetListener actionAfterDateSet;

    public CalendarDialogHelper(Context context, AfterDateSetListener actionAfterDateSet) {
        this.calendar = Calendar.getInstance();
        this.context = context;
        this.actionAfterDateSet = actionAfterDateSet;
        cleanTime();

    }

    private void cleanTime() {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);

        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        actionAfterDateSet.afterDateSet();
    }

    public void showDialog() {
        new DatePickerDialog(context, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    public Date getDate() {
        return calendar.getTime();
    }

    public interface AfterDateSetListener {
        void afterDateSet();
    }

    public Calendar getCalendar() {
        return calendar;
    }
}
