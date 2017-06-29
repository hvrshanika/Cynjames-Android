package au.com.cynjames.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import au.com.cynjames.CJT;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.mainView.MainActivity;

public class LogoutService extends Service {
    public LogoutService() {

    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        startService();
    }

    private void startService() {
        setAlarm(this);
    }

    public void onDestroy() {
        super.onDestroy();
        cancelAlarm(this);
        cancelTaskAlarm(this);
    }

    public void setAlarm(Context context)
    {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR, 7);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.AM_PM, Calendar.PM);

        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LogoutAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, LogoutAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void cancelTaskAlarm(Context context)
    {
        Intent intent = new Intent(context, LogoutTaskAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
