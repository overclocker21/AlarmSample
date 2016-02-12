package com.androbro.alarmsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by user on 2/11/2016.
 */
public class AlarmReceiver extends BroadcastReceiver{


    //everytime the alarmreceiver fires its gonna start the Backgroundservice
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent background = new Intent(context, BackgroundService.class);
        context.startService(background);
    }
}
