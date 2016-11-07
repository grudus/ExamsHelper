package com.grudus.nativeexamshelper.database.subjects;


import android.provider.BaseColumns;

public abstract class SubjectEntry implements BaseColumns {


    public static final String TABLE_NAME = "subjects";
    public static final String TITLE_COLUMN = "title";
    public static final String COLOR_COLUMN = "color";
    public static final String HAS_GRADE_COLUMN = "has_grade";
    public static final String DELETED_COLUMN = "deleted";
    public static final String MODIFIED_COLUMN = "modified";

    public static final int INDEX_COLUMN_INDEX = 0;
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int COLOR_COLUMN_INDEX = 2;
    public static final int HAS_GRADE_COLUMN_INDEX = 3;
    public static final int DELETED_COLUMN_INDEX = 4;
    public static final int MODIFIED_COLUMN_INDEX = 5;

    public static final String[] ALL_COLUMNS = {_ID, TITLE_COLUMN, COLOR_COLUMN, HAS_GRADE_COLUMN, DELETED_COLUMN, MODIFIED_COLUMN};

    public static final String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME +
            " ( " +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TITLE_COLUMN + " TEXT NOT NULL, " +
            COLOR_COLUMN + " TEXT NOT NULL, " +
            HAS_GRADE_COLUMN + " INTEGER, " +
            DELETED_COLUMN + " INTEGER,  " +
            MODIFIED_COLUMN + " INTEGER "
            + ")";


}
