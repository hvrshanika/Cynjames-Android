package au.com.cynjames.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.mainView.MainActivity;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.User;
import au.com.cynjames.models.Vehicles;

public class NewJobsUpdateService extends Service {

    private int mInterval = 300000;
    private Handler mHandler;
    SQLiteHelper db;
    Gson gson;
    User user;
    SharedPreferences mPrefs;
    Bitmap icon;

    public NewJobsUpdateService() {
    }

    @Override
    public void onCreate() {

        super.onCreate();
        gson = new Gson();
        db = new SQLiteHelper(this);
        mHandler = new Handler();
        mPrefs = getApplicationContext().getSharedPreferences("AppData", 0);
        getUser();
        startRepeatingTask();
        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.truck_icon_resized);
    }

    private void getUser() {
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                loadNewJobs();
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void loadNewJobs() {
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid", userId);
        HTTPHandler.post("cjt-concept-pending-jobs.php", params, new HTTPHandler.ResponseManagerWithOutProgress(new NewJobsLoader()));
    }

    private void sendNotification(){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification noti = new Notification.Builder(this)
                .setContentTitle("Cynjames Notification")
                .setContentText("You have New Jobs")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(icon)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }

    public class NewJobsLoader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                ArrayList<ConceptBooking> newJobs = new ArrayList<>();
                JSONArray objs = jSONObject.getJSONArray("joblist");
                for (int i = 0; i < objs.length(); i++) {
                    JSONObject obj = objs.getJSONObject(i);
                    ConceptBooking job = gson.fromJson(obj.toString(), ConceptBooking.class);
                    if (!db.jobExist(job.getId())) {
                        db.addJob(job);
                        newJobs.add(job);
                    }

                }
                if (newJobs.size() > 0) {
                    sendNotification();
                }
            }
        }
    }


}
