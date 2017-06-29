package au.com.cynjames;

import android.app.Application;
import android.location.Location;

import java.util.Timer;
import java.util.TimerTask;

import au.com.cynjames.cjtv10.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by eleos on 6/3/2016.
 */
public class CJT extends Application {

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean wasInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 3000;
    public Location lastLocation;
    public boolean logoutTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                CJT.this.wasInBackground = true;
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }

}
