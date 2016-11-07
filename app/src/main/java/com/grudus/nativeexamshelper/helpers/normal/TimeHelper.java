package com.grudus.nativeexamshelper.helpers.normal;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    private static final String DEFAULT_TIME_FORMAT = "HH:mm";
    private static final Locale DEFAULT_LOCALE = Locale.GERMANY;

    public static String getFormattedTime(Date date) {
        return new SimpleDateFormat(DEFAULT_TIME_FORMAT, DEFAULT_LOCALE)
                .format(date);
    }

    public static String getFormattedTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return getFormattedTime(calendar.getTime());
    }

    public static String getFormattedTime(long millis) {
        return getFormattedTime(new Date(millis));
    }

}
