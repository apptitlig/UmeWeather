package com.matilda.umeweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Matilda", "on recieve 2");
        Weather.UpdateWidget(context);
    }


}
