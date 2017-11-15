package com.vivo.hdp.sleep;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.vivo.hdp.sleep.R.id.start_test;
import static com.vivo.hdp.sleep.R.id.text_end_time;
import static com.vivo.hdp.sleep.R.id.text_start_time;
import static com.vivo.hdp.sleep.R.id.text_state_report;
import static com.vivo.hdp.sleep.R.id.text_total_count;

public class MainActivity extends AppCompatActivity {

    private EditText brightScreenTime;
    private EditText blackScreenTime;
    private TextView testTips;
    private EditText totalTestCount;
    private TextView textStartTime;
    private TextView textEndTime;
    private TextView textStateReport;
    private TextView textTotalCount;
    private Button sets;
    private Button startTest;
    private CheckBox allCheck;
    private CheckBox noCheck;

    private ContentValues values;
    AlertDialog dialog;
    ListView appInfoListView = null;
    List<AppInfo> appInfos = null;
    MyAdapter infosAdapter = null;
    ArrayList<String> mData = new ArrayList<>();

    private MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appInfoListView = (ListView)this.findViewById(R.id.list_view);
        appInfos = getAppInfos();
        updateUI(appInfos);
        initView();
        final RelativeLayout rlContainer = (RelativeLayout) findViewById(R.id.rl_container);
        final LinearLayout llWait = (LinearLayout) findViewById(R.id.ll_wait);

        new Thread() {
            @Override
            public void run() {
                super.run();
                String testPkg = "com.vivo.hdp.sleeptest.test";
                String daemonPkg = "com.test.daemon";
                String appPkg = "com.vivo.hdp.sleeptest";
                copyAndInstall("testApp.apk", appPkg);
                copyAndInstall("sleepTest.apk", testPkg);
                copyAndInstall("daemon.apk", daemonPkg);

//                 && isApkInstalled(testPkg) && isApkInstalled(appPkg))
                int count = 0;
                while (!(isApkInstalled(daemonPkg) && count < 40)) {
                    SystemClock.sleep(500);
                    count++;
                }
                if (count == 40) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "所需应用未安装成功，请退出重新安装", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            llWait.setVisibility(View.GONE);
                            rlContainer.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }.start();
    }

    public void updateUI(List<AppInfo> appInfos){
        if(null != appInfos){
            infosAdapter = new MyAdapter(getApplication(),appInfos,mData);
            appInfoListView.setAdapter(infosAdapter);
        }
    }

//    public List<AppInfo> getAppInfos(){
//        PackageManager pm = getApplication().getPackageManager();
//        List<PackageInfo>  packgeInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
//        appInfos = new ArrayList<AppInfo>();
//        /* 获取应用程序的名称，不是包名，而是清单文件中的labelname
//            String str_name = packageInfo.applicationInfo.loadLabel(pm).toString();
//            appInfo.setAppName(str_name);
//         */
//        for(PackageInfo packgeInfo : packgeInfos){
//            String appName = packgeInfo.applicationInfo.loadLabel(pm).toString();
//            String packageName = packgeInfo.packageName;
//            Drawable drawable = packgeInfo.applicationInfo.loadIcon(pm);
//            AppInfo appInfo = new AppInfo(appName, packageName,drawable);
//            appInfos.add(appInfo);
//        }
//        return appInfos;
//    }

    public List<AppInfo> getAppInfos(){
        PackageManager pm = this.getPackageManager();
        appInfos = new ArrayList<AppInfo>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);

        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo resolveInfo : resolveInfos) {

            String appName = resolveInfo.loadLabel(pm).toString();
            Drawable drawable = resolveInfo.loadIcon(packageManager);
            String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.contains("com.vivo.hdp.sleep")){
                AppInfo appInfo = new AppInfo(appName, packageName,drawable);
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }


    private void initView() {
        dbHelper = new MyDBHelper(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        values = new ContentValues();

        textStartTime = (TextView) findViewById(text_start_time);
        textStateReport = (TextView) findViewById(text_state_report);
        textEndTime = (TextView) findViewById(text_end_time);
        textTotalCount = (TextView) findViewById(text_total_count);
        startTest = (Button) findViewById(start_test);
        sets = (Button) findViewById(R.id.sets);
        allCheck = (CheckBox) findViewById(R.id.all_check);
        noCheck = (CheckBox) findViewById(R.id.no_all_check);


        startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textEndTime.setText("结束时间：");
                values.put("startTime", "开始时间：" + getCurrentTime());
                db.insert("sleep", null, values);
                for (int i = 0; i < mData.size(); i++) {
                    dbHelper.save(mData.get(i));
                }
                setDisplayTime(30*60*1000);

                Intent intent = new Intent();
                intent.setClassName("com.test.daemon","com.test.daemon.DaemonService");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(intent);
                enableBgStart(new String[]{"com.vivo.hdp.sleep","com.test.daemon","com.vivo.hdp.sleeptest"});

                pressHome();
                Intent startIntent = new Intent(getApplication(), MyService.class);
                startService(startIntent);
            }
        });

        allCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    noCheck.setChecked(false);
                    selectAll(true);
                }
            }
        });

        noCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    allCheck.setChecked(false);
                    selectAll(false);
                }
            }
        });

        sets.setOnClickListener(new View.OnClickListener() {

            Button btOk;
            Button btCancel;
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("播放压力测试");
                View view1 = View.inflate(MainActivity.this, R.layout.dialog, null);
                mBuilder.setView(view1);
                dialog = mBuilder.create();
                btOk = view1.findViewById(R.id.s_ok);

                Log.e("hey"," "+dialog+"》》》》》》"+btOk);
                btCancel = view1.findViewById(R.id.s_cancel);
                brightScreenTime = view1.findViewById(R.id.bright_screen_time);
                blackScreenTime = view1.findViewById(R.id.black_screen_time);
                testTips = view1.findViewById(R.id.test_tips);
                totalTestCount = view1.findViewById(R.id.total_test_count);
                dialog.show();
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("hey",">>>>>>>>>cancel");
                        dialog.cancel();
                    }
                });

                totalTestCount.addTextChangedListener(watcher);

                btOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("hahaha",">>>>>>>>>");
                        String brightInput = brightScreenTime.getText().toString();
                        String blackInput = blackScreenTime.getText().toString();
                        String totalCountInput = totalTestCount.getText().toString();

                        if (TextUtils.isEmpty(brightInput) || TextUtils.isEmpty(blackInput) || TextUtils.isEmpty(totalCountInput)) {
                            testTips.setText("tis:请正确输入内容");
                            Toast.makeText(getApplication(), "请正确输入内容", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        long brightTime = Long.parseLong(brightInput);
                        long blackTime = Long.parseLong(blackInput);
                        long totalCount = Long.parseLong(totalCountInput);
                        if (brightTime < 10 || brightTime > Integer.MAX_VALUE
                                || blackTime < 1 || blackTime > Integer.MAX_VALUE
                                ||totalCount > Integer.MAX_VALUE) {
                            Toast.makeText(getApplication(), "亮屏时间不能低于10s或输入框输入值过大,请重新输入!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        db.delete("sleep", null, null);
                        values.put("brightTime", brightTime);
                        Log.e("hahaha","brightTime"+brightTime);
                        values.put("blackTime", blackTime);
                        Log.e("hahaha","blackTime"+blackTime);
                        values.put("totalCount", totalCount);
                        Log.e("hahaha","totalCount"+totalCount);
                        values.put("executedCounts", 0);
                        dialog.cancel();
                    }
                });

            }
        });

    }


    private void pressHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            String brightTimeInput = brightScreenTime.getText().toString();
            String blackTimeInput = blackScreenTime.getText().toString();
            long brightTime = 0 ;
            long blackTime = 0 ;
            if (!TextUtils.isEmpty(blackTimeInput)&&!TextUtils.isEmpty(brightTimeInput)){
                brightTime = Long.parseLong(brightTimeInput);
                blackTime = Long.parseLong(blackTimeInput);
            }

            String input = s.toString();
            if (!TextUtils.isEmpty(input)) {
                long size = Long.parseLong(input);
                if (size > Integer.MAX_VALUE ) {
                    testTips.setText("已输入最大值,请重新输入" + String.valueOf(Integer.MAX_VALUE));
                }
                long totalCount = Long.parseLong(input);
                float totalTime = (brightTime + blackTime) * totalCount;
                float t = totalTime/60/60;
                BigDecimal b = new BigDecimal(t);
                float t1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                testTips.setText("tis:约测试" + String.valueOf(t1) + "小时");
                Log.e("tis:约测试", "afterTextChanged: "+String.valueOf(t));
            }
        }
    };

    @Override
    protected void onStart() {
        Log.e("hdpTest", "onStart: 11111111111111111111111");
        ContentResolver resolver = this.getContentResolver();
        Uri uri = Uri.parse("content://com.vivo.hdp.provider");
        Cursor cursor = resolver.query(uri, new String[]{"brightTime","blackTime","totalCount", "executedCounts", "startTime", "endTime"}, null, null, null);

        int totalCount = 0;
        int executedCounts = 0;
        String startTime = "开始时间：";
        String endTime = "结束时间：";

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) == 0) {
                    break;
                }
                totalCount = cursor.getInt(cursor.getColumnIndex("totalCount"));
                executedCounts = cursor.getInt(cursor.getColumnIndex("executedCounts"));
                startTime = cursor.getString(cursor.getColumnIndex("startTime"));
                endTime = cursor.getString(cursor.getColumnIndex("endTime"));
                executedCounts = executedCounts+1;
            }
            cursor.close();
            textStartTime.setText(startTime);
            String text = "测试状态：第 " + String.valueOf(executedCounts) + " 次测试ing";
            if (totalCount == executedCounts-1 && totalCount != 0) {
                text = "测试状态： 已测试完成";
                textEndTime.setText(endTime);
                Log.e("hdp", "onStart: " + String.valueOf(totalCount) + "dddddd" + endTime + "dddddd" + String.valueOf(executedCounts));
            }
            textTotalCount.setText("总次数： " + String.valueOf(totalCount) + " 次");
            textStateReport.setText(text);

        }
        super.onStart();
    }


    public void setDisplayTime(int times){

       Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, times);


    }

    /**
     * apk是否存在
     *
     * @return
     */
    private boolean isApkInstalled(String pkg) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 不存在就复制安装apk
     */
    private void copyAndInstall(String appName, String pkg) {
        if (!isApkInstalled(pkg)) {
            try {
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/" + appName);
                if (!file.exists()) {
                    InputStream in = getAssets().open(appName);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
                ApplicationManager manager = new ApplicationManager(this);
                manager.installPackage(file);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void enableBgStart(String[] packages) {
        Uri uri = Uri.parse("content://com.vivo.permissionmanager.provider.permission/bg_start_up_apps");
        ContentValues values = new ContentValues();
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);

        if (cursor == null) {
            uri = Uri.parse("content://com.iqoo.secure.provider.secureprovider/forbidbgstartappslist");
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor == null) {
                return;
            }
            for (String pkgname : packages
                    ) {
                values.put("pkgname", pkgname);
                resolver.insert(uri, values);
            }
        } else {
            values.put("setbyuser", 1);
            values.put("currentstate", 0);
            for (String pkgname : packages
                    ) {
                resolver.update(uri, values, "pkgname=?", new String[]{pkgname});
            }
        }
        cursor.close();
    }


    private void selectAll(boolean x){
        for (int i=0; i<appInfos.size(); i++){
            infosAdapter.getIsSelected().put(i, x);
        }
        infosAdapter.notifyDataSetChanged();
    }

}
