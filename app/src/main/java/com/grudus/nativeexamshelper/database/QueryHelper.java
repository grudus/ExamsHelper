package com.grudus.nativeexamshelper.database;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

abstract public class QueryHelper {

    public static Cursor getAllRecordsAndSortBy(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull String[] allColumns, @Nullable String sort) {
        Cursor c = db
                .query(
                    tableName,
                        allColumns,
                        null,
                        null,
                        null,
                        null,
                        sort
        );
        return c;
    }

    public static Cursor findBy(@NonNull SQLiteDatabase db, @NonNull String tableName,
                                @NonNull String[] allColumns, @NonNull String[] columnsToFind,
                                @NonNull String[] columnToFindValues, @Nullable String sort) {
        StringBuilder where = new StringBuilder();

        for (int i = 0; i < columnsToFind.length; i++) {
            where.append(columnsToFind[i]);
            where.append("=?");
            if (i < columnsToFind.length - 1) where.append(" AND ");
        }

        Cursor c = db.query(
                tableName,
                allColumns,
                where.toString(),
                columnToFindValues,
                null,
                null,
                sort
        );

        if (c == null)
            return null;
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;

    }

//    public abstract long insert(SQLiteDatabase db, Insertable object);



}
