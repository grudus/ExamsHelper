package com.grudus.nativeexamshelper.helpers.normal;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;

import com.grudus.nativeexamshelper.R;

import java.util.Date;
import java.util.TimeZone;

public class CalendarHelper {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;
    private final AppCompatActivity activity;
    private static final String TAG = "@@@" + CalendarHelper.class.getSimpleName();

    public CalendarHelper(AppCompatActivity activity) {
        this.activity = activity;
    }



    public void addToCalendar(String subjectTitle, String info, Date when) {

        ContentResolver contentResolver = activity.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, when.getTime());
        values.put(CalendarContract.Events.TITLE, String.format(activity.getString(R.string.calendar_subject), subjectTitle));
        values.put(CalendarContract.Events.DESCRIPTION, info);

        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());

        // default calendar
        values.put(CalendarContract.Events.CALENDAR_ID, 1);

        //for one hour
        values.put(CalendarContract.Events.DURATION, "+P1H");

        values.put(CalendarContract.Events.HAS_ALARM, 0);

        Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
    }
}
