package com.vivo.hdp.sleep;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;
import static com.vivo.hdp.sleep.MainActivity.getCurrentTime;

/**
 * Created by 10957084 on 2017/9/18.
 */

public class MyService extends Service {

    int totalCount = 0;
    int brightTime = 0;
    int blackTime = 0;
    int executedCounts = 0;
    private static String packageName = "";
    private MyDBHelper dbHelper;
    public static boolean flag = false;
    private MyHandler mHandler ;
    private PowerManager.WakeLock wakeLock = null;



    @Override
    public void onCreate() {
        makeText(this, "开始测试咯", LENGTH_LONG).show();
        killMonkey();
        Log.e(TAG, "run2:`777777777777777777777777777777777777777777777777777777777777");
        flag = true;
        mHandler = new MyHandler();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "onStartCommand: ---------------executed");
        ContentResolver resolver = this.getContentResolver();
        Uri uri = Uri.parse("content://com.vivo.hdp.provider");
        Cursor cursor = resolver.query(uri, new String[]{"brightTime", "blackTime", "totalCount", "executedCounts", "endTime","packageName"}, null, null, null);

        if (cursor != null) {

            if(cursor.moveToFirst()){
                brightTime = cursor.getInt(cursor.getColumnIndex("brightTime"));
                blackTime = cursor.getInt(cursor.getColumnIndex("blackTime"));
                totalCount = cursor.getInt(cursor.getColumnIndex("totalCount"));
                executedCounts = cursor.getInt(cursor.getColumnIndex("executedCounts"));
            }

            while (cursor.moveToNext()){
                packageName = cursor.getString(cursor.getColumnIndex("packageName"));
            }
            Log.e(TAG, "onStartCommand: >>>>>>>>>>>>>>>>>>"+packageName);
            cursor.close();
        }

        final String command = "am instrument -w -r   -e debug false -e class com.vivo.hdp.sleeptest.ExampleInstrumentedTest#testNoPasswardCase com.vivo.hdp.sleeptest.test/android.support.test.runner.AndroidJUnitRunner";
        final String commandBrightTest = "am instrument -w -r   -e debug false -e class com.vivo.hdp.sleeptest.ExampleInstrumentedTest#brightTest com.vivo.hdp.sleeptest.test/android.support.test.runner.AndroidJUnitRunner";
        new Thread() {
            @Override
            public void run() {

                while (totalCount > executedCounts) {
                    acquireWakeLock();
                    int time = (brightTime + blackTime) * 1000;
                    Message msg = mHandler.obtainMessage();
                    msg.what = time;
                    mHandler.sendMessage(msg);
                    try {

                        killMonkey();
                        Log.e(TAG, "run: 1111111111111111111111111111111111111111111"+isScreenLocked());
                        Runtime.getRuntime().exec(command);
                        Log.e(TAG,"COMMAND EXECUTED!!!!!!!!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "run: 222222222222222222222222222222222222222222222222"+isScreenLocked());
                    Log.e(TAG, "run: " + SystemClock.elapsedRealtimeNanos());
                    SystemClock.sleep((blackTime) * 1000);

                    try {
                        Runtime.getRuntime().exec(commandBrightTest);
//                        writer(getCurrentTime(),0,"commandUnLock1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "run: 3333333333333333333333333333333333333333333333333"+isScreenLocked());
                    SystemClock.sleep(8 * 1000);
                    if (!isScreenLocked()) {
                        killMonkey();
                        Log.e(TAG, "run: 444444444444444444444444444444444444444444444" + isScreenLocked());
//                        writer(getCurrentTime(), 0, "commandUnLock2");
                        wakeUp();
//                            myBind.exces("input keyevent 26");
                        try {
                            Runtime.getRuntime().exec(commandBrightTest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    SystemClock.sleep((brightTime-8) * 1000);
                    Log.e(TAG, "run2:`5555555555555555555555555555555555555555555555555555555555 " + SystemClock.elapsedRealtimeNanos());
                    executedCounts++;
                    upExecutedCountsData();
                    pressHome();

                    Handler handler = new Handler(Looper.getMainLooper());
                    final int finalCount = executedCounts;
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "已完成睡眠唤醒apk第 " + finalCount + " 次测试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                killMonkey();
                releaseWakeLock();
                Log.e(TAG, "run2:`66666666666666666666666666666666666666666666666666666666666666666");
                dbHelper = new MyDBHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("endTime", "测试结束时间：" + getCurrentTime());
                db.update("sleep", values, null, null);
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Binder myBinder = new MyBind();
        return myBinder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void upExecutedCountsData() {
        dbHelper = new MyDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("executedCounts", executedCounts);
        db.update("sleep", values, null, null);
    }

    int cn = 1;
    private void killMonkey() {
        Log.e(TAG,"KILL MONKEY");
        cn = cn++;
//        writer(String.valueOf(cn)+"第次",0,"killMonkeyid");
        try {
            java.lang.Process process = Runtime.getRuntime().exec("ps");
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            String pidStr ="";
            while ((line = reader.readLine()) != null) {
//                writer(String.valueOf(reader.readLine())+"line",0,"killMonkeyid");
                if (line.contains("com.android.commands.monkey")) {
                    Log.e(TAG, "killMonkey: " + line);
                    line = line.replaceAll(" +", " ");
                    String[] str = line.split(" ");
                    pidStr = str[1];
                    Log.e(TAG, "killMonkey: " + pidStr);
//                    writer(pidStr, 0, "killMonkeyid");
                    break;
                }
            }
            Log.e(TAG, "killMonkey: +++"+pidStr);
            if (!pidStr.equals("")){
                int pid = Integer.parseInt(pidStr);
                Process.killProcess(pid);
                Log.e(TAG, "killMonkey: haha"+pid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wakeUp(){
        // 键盘管理器
        KeyguardManager mKeyguardManager;
        // 键盘锁
        KeyguardManager.KeyguardLock mKeyguardLock;
        // 电源管理器
        PowerManager mPowerManager;
        // 唤醒锁
        PowerManager.WakeLock mWakeLock;
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock
        (PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Tag");
        mWakeLock.acquire();
        mWakeLock.release();
        Log.e(TAG, "wakeUp: **********************");
    }

    public  boolean isScreenLocked() {

        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) this.getSystemService(this.KEYGUARD_SERVICE);
        return !mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    private void pressHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int time = msg.what;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    SystemClock.sleep(time-1000);
                    if (packageName != null){
                        Log.e(TAG, "run: ********************"+packageName);
                        killMonkey();
                    }

                }
            }.start();
        }
    }

    private void writer(final String strr, final int time, final String ph){
        new Thread(){
            @Override
            public void run() {
                super.run();
                String path = "sdcard/+"+ph+".txt";
                File file = new File(path);
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
                    Thread.sleep(time*1000);//SystemClock.sleep(5000);
                    writer.write(strr);//当前时间
                    writer.newLine();
                    writer.flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                Log.i(TAG, "call acquireWakeLock");
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            Log.i(TAG, "call releaseWakeLock");
            wakeLock.release();
            wakeLock = null;
        }
    }
}
