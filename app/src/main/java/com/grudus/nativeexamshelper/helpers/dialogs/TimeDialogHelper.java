package com.grudus.nativeexamshelper.helpers.dialogs;


import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimeDialogHelper implements TimePickerDialog.OnTimeSetListener {

    private Context context;
    private AfterTimeSetListener actionAfterTimeSet;
    private int hour, minute;

    public TimeDialogHelper(Context context, AfterTimeSetListener actionAfterTimeSet) {
        Calendar calendar = Calendar.getInstance();
        this.context = context;
        this.actionAfterTimeSet = actionAfterTimeSet;
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }


    public void showDialog() {
        new TimePickerDialog(context, this, hour, minute, true).show();
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        actionAfterTimeSet.afterTimeSet();
    }

    public interface AfterTimeSetListener {
        void afterTimeSet();
    }
}
