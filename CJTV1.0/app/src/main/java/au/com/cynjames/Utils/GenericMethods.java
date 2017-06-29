package au.com.cynjames.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;

/**
 * Created by eleos on 5/13/2016.
 */
public class GenericMethods {
    int response = 0;
    static Context context;
    static ProgressDialog progressDialog;
    static AlertDialog builder;

    public static boolean isConnectedToInternet(Context con) {
        ConnectivityManager connectivity = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }

        }
        return false;
    }

    public static void showToast(Context con, String message) {
        Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
    }

    public static ProgressDialog getProgressDialog(Context con, String message) {
        if(context == null || context != con){
            context = con;
            progressDialog = new ProgressDialog(con);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
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

    public static void showMessage(Context con, String title, String message) {
        Builder build = new Builder(con);
        build.setTitle(title);
        build.setMessage(message);
        build.setCancelable(false);
        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        build.create().show();
    }

    public static void showServerError(Context con, String title, String message) {
        builder = new AlertDialog.Builder(con).create();
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setButton(AlertDialog.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static AlertDialog getBuilderObj(){
        return builder;
    }

    public static String getDisplayDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        String d=formatter.format(date);
        return d;

    }

    public static String getDBDate(Date date) {
        if (date == null) {
            date = new Date();

        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d=formatter.format(date);
        return d;

    }

    public static Date getDBDateForSave(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d= null;
        try {
            d = formatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static String getDBTime(Date date) {
        if (date == null) {
            date = new Date();

        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String d=formatter.format(date);
        return d;

    }

    public static String getDBDateOnly(Date date) {
        if (date == null) {
            date = new Date();

        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String d=formatter.format(date);
        return d;

    }

    public static Date getDatefromString(String dateString) {

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
            Date date = null;
        if (dateString != null) {
            try {
                date = format.parse(dateString);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            try {
                date = format.parse(getDisplayDate(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
            return date;
    }

}
