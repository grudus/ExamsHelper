package com.grudus.nativeexamshelper.database;

public final class SqliteQueryNamespace {
    
    public static final String YEAR = "strftime('%Y', date / 1000, 'unixepoch')";
    public static final String MONTH = "strftime('%m', date / 1000, 'unixepoch')";
    public static final String AVG = "avg";
    public static final String COUNT = " COUNT(*) ";

    public static final String AND = " AND ";
    public static final String EQ = " = ? ";
    public static final String GT = " > ? ";
    public static final String LT = " < ? ";
    public static final String IS_NULL = " IS NULL ";
    public static final String IS_NOT_NULL = " IS NOT NULL ";
    
}
