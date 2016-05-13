package au.com.cynjames.Utils;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.widget.Toast;

/**
 * Created by eleos on 5/13/2016.
 */
public class GenericMethods {

    public static boolean isConnectedToInternet(Context con) {
        ConnectivityManager connectivity = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()){
                        return true;
                }
        }
        return false;
    }

    public static void showToast(Context con, String message) {
        Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
    }

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please Wait...");
        return progressDialog;
    }

    public static void showNoInternetDialog(Context con) {
        Builder build = new Builder(con);
        build.setTitle("No Internet Connection");
        build.setMessage("Please check your connection.");
        build.setCancelable(false);
        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        build.create().show();
    }

}
