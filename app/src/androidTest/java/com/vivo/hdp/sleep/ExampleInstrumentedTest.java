package com.vivo.hdp.sleep;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.ContentValues.TAG;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    Context appContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void testNoPasswardCase() throws Exception {
        startAPP();
    }

    public void startAPP(){
        try{
//            Intent intent = appContext.getPackageManager().getLaunchIntentForPackage(appPackageName);
//            appContext.startActivity(intent);

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.test.daemon",
                    "com.test.daemon.DaemonService"));
            appContext.startActivity(intent);
        }catch(Exception e){
            Log.e(TAG, "startAPP: 0000000000000000000000000");
        }
    }
}
