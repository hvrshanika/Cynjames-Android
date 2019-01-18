package au.com.cynjames.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.cynjames.cjtv20.R;
import au.com.cynjames.mainView.MainActivity;
import au.com.cynjames.models.AdhocDimensions;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ParcelPalletLabel;

public class CynFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessage";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            SQLiteHelper db = new SQLiteHelper(this);
            Gson gson = new Gson();
            boolean isConcept = false;

            String str = remoteMessage.getData().get("isConcept");
            if(str.equals("true"))
                isConcept = true;

            ConceptBooking job = gson.fromJson(remoteMessage.getData().get("job"), ConceptBooking.class);
            if(job != null) {
                if (job.getConceptBookingStatus() == 1 || job.getConceptBookingStatus() == -1 || job.getConceptBookingStatus() == 6) {
                    db.clearConcept(job.getId(), isConcept);
                } else if (job.getConceptBookingStatus() == 10) {
                    db.clearConcept(job.getId(), isConcept);
                } else {
                    db.updateJob(job, isConcept);
                }

                if (!isConcept) {
                    String dStr = remoteMessage.getData().get("dimens");
                    if (!dStr.equals("[]")) {
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(dStr);
                        JsonArray dimens = element.getAsJsonArray();
                        if (dimens != null) {
                            for (int i = 0; i < dimens.size(); i++) {
                                JsonObject obj = dimens.get(i).getAsJsonObject();
                                AdhocDimensions dimen = gson.fromJson(obj.toString(), AdhocDimensions.class);
                                if (!db.dimenExist(dimen.getId())) {
                                    db.addDimension(dimen);
                                }
                            }
                        }
                    }
                    String lStr = remoteMessage.getData().get("labels");
                    if (!lStr.equals("[]")) {
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(lStr);
                        JsonArray labels = element.getAsJsonArray();
                        if (labels != null) {
                            for (int i = 0; i < labels.size(); i++) {
                                JsonObject obj = labels.get(i).getAsJsonObject();
                                ParcelPalletLabel label = gson.fromJson(obj.toString(), ParcelPalletLabel.class);
                                if (!db.labelExist(label.getLabelId())) {
                                    db.addLabel(label);
                                }
                            }
                        }
                    }
                }
                refreshActivity();
            }
        }

        if (remoteMessage.getNotification() != null) {
//            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }

    }

    private void refreshActivity() {
        Intent intent = new Intent("RefreshUI");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendNotification(String messageBody, String title){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification noti = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSmallIcon(R.mipmap.app_icon)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }
}
