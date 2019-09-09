package ru.excalc.vk282.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataDbHelper extends SQLiteOpenHelper{
    private static final int DB_VERSION = 1;
    static final String DB_NAME = "data.db";

    public DataDbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + DbValues.PostsEntry.TABLE_NAME + " (" +
                DbValues.PostsEntry.COLUMN_POST_ID + " INTEGER NOT NULL, " +
                DbValues.PostsEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                DbValues.PostsEntry.COLUMN_TYPE + " INTEGER, " +
                DbValues.PostsEntry.COLUMN_FROM_ID + " INTEGER NOT NULL" +

                ");";

        final String SQL_CREATE_PUBLICS_TABLE = "CREATE TABLE " + DbValues.PublicsEntry.TABLE_NAME + " (" +
                DbValues.PublicsEntry.COLUMN_PUBLIC_ID + " INTEGER NOT NULL, " +
                DbValues.PublicsEntry.COLUMN_PUBLIC_NAME + " TEXT NOT NULL, " +
                DbValues.PublicsEntry.COLUMN_QUANTITY + " INTEGER NOT NULL" +

                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PUBLICS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + "wallposts");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + "wallq");

        onCreate(sqLiteDatabase);
    }
}
