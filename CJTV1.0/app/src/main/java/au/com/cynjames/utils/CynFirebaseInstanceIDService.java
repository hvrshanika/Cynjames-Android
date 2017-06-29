package au.com.cynjames.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.FirebaseInstanceId;

import static au.com.cynjames.utils.SQLiteHelper.DATABASE_VERSION;

public class CynFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseInstanceID";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //saveToken(refreshedToken);
    }

    private void saveToken(String token) {
        Log.d(TAG, "Token: " + token);
        SharedPreferences sharedpreferences = getSharedPreferences("AppData" + DATABASE_VERSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("FireBaseToken", token);
        editor.commit();
    }
}
