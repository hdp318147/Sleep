package com.vivo.hdp.sleep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Created by 10957084 on 2017/9/18.
 */
public class MyDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME_SLEEP = "sleep";
    public static final String COLUMN_NAME_BRIGHT_TIME = "brightTime";
    public static final String COLUMN_NAME_BLACK_TIME = "blackTime";
    public static final String COLUMN_NAME_TOTAL_COUNT = "totalCount";
    public static final String COLUMN_NAME_EXECUTED_COUNTS = "executedCounts";
    public static final String COLUMN_NAME_START_TIME = "startTime";
    public static final String COLUMN_NAME_END_TIME = "endTime";
    public static final String COLUMN_NAME_PACKAGE_NAME = "packageName";
    public static final String COLUMN_NAME_CHECKED = "checked";

    private static MyDBHelper dbHelper;
    private static final String DB_NAME = "sleep.db";
    private static final int VERSION = 1;
    private static final String TOTAL_TIME_INFO = "create table "+TABLE_NAME_SLEEP+"("+COLUMN_NAME_BRIGHT_TIME+" integer, "
            +COLUMN_NAME_BLACK_TIME+" integer, "+COLUMN_NAME_TOTAL_COUNT+" integer, "+COLUMN_NAME_EXECUTED_COUNTS+" integer, "+COLUMN_NAME_START_TIME+" text, "+COLUMN_NAME_END_TIME+" text, "+COLUMN_NAME_PACKAGE_NAME+" text)";
//    private static final String TOTAL_COUNT_INFO = "create table sleep(brightTime integer, "
//            + "blackTime integer, totalCount integer)";
    private static final String SQL_DROP = "drop table if exists sleep";
    private static final String SQL_DROP_CATEGORY = "drop table if exists Category";

    MyDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    /**
     * 使用单例模式获取DBHelper
     */
    public static MyDBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new MyDBHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TOTAL_TIME_INFO);
//        sqLiteDatabase.execSQL(TOTAL_COUNT_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//        sqLiteDatabase.execSQL(SQL_DROP);
//        sqLiteDatabase.execSQL(SQL_DROP_CATEGORY);
//        onCreate(sqLiteDatabase);
    }

    /**
     * Cursor cursor = db.query("stu_table", new String[]{"id","sname","sage","ssex"}, "id=?", new String[]{"1"}, null, null, null);
     while(cursor.moveToNext()){
     String name = cursor.getString(cursor.getColumnIndex("sname"));
     String age = cursor.getString(cursor.getColumnIndex("sage"));
     String sex = cursor.getString(cursor.getColumnIndex("ssex"));
     System.out.println("query------->" + "姓名："+name+" "+"年龄："+age+" "+"性别："+sex);
     }
     * @param packageName
     */

//    public void checkPackage(String packageName){
//        SQLiteDatabase db = getWritableDatabase();
//        Cursor cursor = db.query(TABLE_NAME_SLEEP, new String[]{COLUMN_NAME_PACKAGE_NAME, COLUMN_NAME_CHECKED}, COLUMN_NAME_PACKAGE_NAME+"=?", new String[]{packageName}, null, null, null);
//        while (cursor.moveToNext()){
//            String pkg = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PACKAGE_NAME));
//            Boolean checked = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CHECKED))>0;
//        }
//    }
//
//    public void uncheckPackage(String packageName){
//
//    }

    public SleepInfo findSleepInfoByPackageName(String packageName){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME_SLEEP, new String[]{COLUMN_NAME_PACKAGE_NAME}, COLUMN_NAME_PACKAGE_NAME+"=?", new String[]{packageName}, null, null, null);
        while (cursor.moveToNext()){
            String pkg = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PACKAGE_NAME));
            if (pkg!=null&&pkg.equals(packageName)){
                SleepInfo sleepInfo = new SleepInfo();
                sleepInfo.setPackageName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PACKAGE_NAME)));
                return sleepInfo;
            }
        }
        cursor.close();
        return null;
    }

    public List<SleepInfo> findSleepInfosByChecked(Boolean checked){
        return null;
    }


    public void save(String packageName){
        SQLiteDatabase db =dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (findSleepInfoByPackageName(packageName) ==null){
            cv.put(COLUMN_NAME_PACKAGE_NAME, packageName);
            db.insert(TABLE_NAME_SLEEP, null, cv);
            db.close();
        }
    }

    public void update(SleepInfo sleepInfo){
        SQLiteDatabase db =dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_PACKAGE_NAME, sleepInfo.getPackageName());
        String whereClause = COLUMN_NAME_PACKAGE_NAME+"=?";
        String[] whereArgs = new String[]{};
        db.update(TABLE_NAME_SLEEP, cv, whereClause, whereArgs);
    }

    public void delete(SleepInfo sleepInfo){
        findSleepInfoByPackageName(sleepInfo.getPackageName());

    }

    public void deleteByPackageName(String packageName){
        if (findSleepInfoByPackageName(packageName) !=null){
            SQLiteDatabase db =dbHelper.getWritableDatabase();
            db.delete(TABLE_NAME_SLEEP,COLUMN_NAME_PACKAGE_NAME+"=?",new String[]{packageName});
        }
    }
}
