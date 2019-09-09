package ru.excalc.vk282.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbCursor {
    private DataDbHelper mDbHelper;
    private SQLiteDatabase mDb;


    public DbCursor(Context context) {
        mDbHelper = new DataDbHelper(context);
        mDb = mDbHelper.getReadableDatabase();
    }

    public Cursor getByQuantity() {
        return mDb.query("publics", null, null, null, null, null, "QUANTITY");
    }
    public Cursor getAll() {
        return mDb.query(DbValues.PostsEntry.TABLE_NAME, null, null, null, null, null, null);
    }
    public Cursor getAllByDate(String selection_types) {
        return mDb.query(DbValues.PostsEntry.TABLE_NAME, null, selection_types, null, null, null, null);
    }
    public Cursor getOwn(int timeFrom, int timeTo) {
        String selection = "type = 1 AND ? <= time AND time <= ?";
        String[] selectionArgs = {String.valueOf(timeFrom),String.valueOf(timeTo)};
        return mDb.query(DbValues.PostsEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }
    public Cursor getReposts(int timeFrom, int timeTo) {
        String selection = "type = 0 AND ? <= time AND time <= ?";
        String[] selectionArgs = {String.valueOf(timeFrom),String.valueOf(timeTo)};
        return mDb.query(DbValues.PostsEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }
    public Cursor getAlien(int timeFrom, int timeTo) {
        String selection = "type = 2 AND ? <= time AND time <= ?";
        String[] selectionArgs = {String.valueOf(timeFrom),String.valueOf(timeTo)};
        return mDb.query(DbValues.PostsEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }

    public void close() {
        if (mDbHelper != null) mDbHelper.close();
        if (mDb != null) mDb.close();
    }
}
