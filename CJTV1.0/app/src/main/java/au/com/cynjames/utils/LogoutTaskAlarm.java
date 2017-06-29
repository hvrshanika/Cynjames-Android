package au.com.cynjames.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import au.com.cynjames.CJT;

public class LogoutTaskAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        sendLogoutCallToActivity(context);

        wl.release();
    }

    private void sendLogoutCallToActivity(Context context) {
        Intent intent = new Intent("LogoutCall");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
