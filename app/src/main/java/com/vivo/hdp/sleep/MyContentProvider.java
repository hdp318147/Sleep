package com.vivo.hdp.sleep;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MyContentProvider extends ContentProvider {


    private MyDBHelper myDBHelper;

    @Override
    public boolean onCreate() {
        myDBHelper = MyDBHelper.getInstance(getContext());
        return true;
    }

    public MyContentProvider() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        return db.delete("sleep",selection,selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        long id = db.insert("sleep",null,values);
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        return db.query("sleep",projection,selection,selectionArgs,null,null,null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        return db.update("sleep",values,selection,selectionArgs);
    }
}
