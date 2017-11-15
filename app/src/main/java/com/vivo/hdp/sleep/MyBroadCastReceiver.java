package com.vivo.hdp.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by 10957084 on 2017/9/19.
 */

public class MyBroadCastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!MyService.flag){
            Intent startIntent = new Intent(context, MyService.class);
            context.startService(startIntent);
        }
    }
}
