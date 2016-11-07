package com.grudus.nativeexamshelper.helpers.normal;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.grudus.nativeexamshelper.helpers.exceptions.ExceptionsHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    private static String dateFormat = DEFAULT_DATE_FORMAT;

    public static void setDateFormat(@NonNull String dateFormat) {
        if (ExceptionsHelper.stringsAreEmpty(dateFormat)) return;
        DateHelper.dateFormat = dateFormat;
    }

    @Nullable
    public static Date tryToGetDateFromString(@NonNull String parsedDate) {
        Date date = null;
        try {
            date = getDateFromString(parsedDate);
        } catch (ParseException | IllegalArgumentException e) {
            Log.e("@@@DateFormat", "tryToGetDateFromString: ", e);
        }
        return date;
    }

    public static Date getDateFromString(@NonNull String date) throws ParseException {
        if (ExceptionsHelper.stringsAreEmpty(date)) throw new IllegalArgumentException("date is null");
        DateFormat format = new SimpleDateFormat(
                dateFormat, Locale.ENGLISH);
        return format.parse(date);
    }

    public static String getStringFromDate(@NonNull Date date) {
        DateFormat format = new SimpleDateFormat(
                dateFormat, Locale.ENGLISH);
        return format.format(date);
    }

    public static long getLongFromDate(@NonNull Date date) {
        return date.getTime();
    }

    public static String getReadableDataFromLong(long milliseconds) {
        return getReadableDataFromLong(milliseconds, null);
    }

    public static String getReadableDataFromLong(long milliseconds, @Nullable String format) {
        Date date = new Date(milliseconds);
        return new SimpleDateFormat(format == null ? dateFormat : format, Locale.ENGLISH).format(date);
    }
}
