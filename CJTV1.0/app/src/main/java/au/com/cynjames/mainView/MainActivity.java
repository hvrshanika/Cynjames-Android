package au.com.cynjames.mainView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;

public class MainActivity extends AppCompatActivity {
    Context context;
    Editor prefsEditor;
    SharedPreferences mPrefs;
    User user;
    Gson gson;
    TextView pendingCount;
    TextView deliverReadyCount;
    TextView messagesCount;
    int unreadMessages;
    ArrayList<ConceptBooking> pendingJobs;
    ArrayList<ConceptBooking> deliverReadyJobs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#bec2cb")));
        actionBar.setLogo(R.mipmap.logo_red);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        mPrefs = getApplicationContext().getSharedPreferences("AppData", 0);
        prefsEditor = mPrefs.edit();
        context = this;
        pendingJobs = new ArrayList<>();
        deliverReadyJobs = new ArrayList<>();
        gson = new Gson();
        getUser();
        init();
        if(user == null){
            finish();
        }
        if(GenericMethods.isConnectedToInternet(this)) {
            loadData();
        }
        else {
            GenericMethods.showNoInternetDialog(context);
            logoutUser();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:

                break;
            case R.id.action_logout:
                logoutUser();
                break;
            default:
                break;
        }

        return true;
    }

    private void init(){
        TextView welcome = (TextView) findViewById(R.id.main_welcome_user_text);
        welcome.setText("Welcome " + user.getUserFirstName());
        pendingCount = (TextView) findViewById(R.id.main_concept_count);
        deliverReadyCount = (TextView) findViewById(R.id.main_concept_ready_count);
        messagesCount = (TextView) findViewById(R.id.main_messages_count);
    }

    private void loadData(){
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid",  userId);
        HTTPHandler.post("concept-ready-for-delivery-list.php", params, new HTTPHandler.ResponseManager(new DeliveryListLoader(), context));


    }

    public void logoutUser() {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setMessage("Are you sure you want to Logout?");
        build.setCancelable(false);
        build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                prefsEditor.clear();
                finish();
            }
        });
        build.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        build.create().show();
    }

    private void getUser(){
        String jsonUser = mPrefs.getString("User", "");
        user = gson.fromJson(jsonUser, User.class);
    }

    public class DeliveryListLoader implements ResponseListener{

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                String userId = String.valueOf(user.getUserid());
                RequestParams params = new RequestParams();
                params.add("userid", userId);
                HTTPHandler.post("concept-pending-jobs-list.php", params, new HTTPHandler.ResponseManager(new PendingListLoader(), context));
                JSONArray objs = jSONObject.getJSONArray("joblist");
                for(int i = 0; i<objs.length(); i++){
                    JSONObject obj = objs.getJSONObject(i);
                    deliverReadyJobs.add(gson.fromJson(obj.toString(),ConceptBooking.class));
                }
                deliverReadyCount.setText(String.valueOf(deliverReadyJobs.size()));
            }
        }
    }

    public class PendingListLoader implements ResponseListener{
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                HTTPHandler.post("unread-messages.php", null, new HTTPHandler.ResponseManager(new MessagesLoader(), context));
                JSONArray objs = jSONObject.getJSONArray("joblist");
                for(int i = 0; i<objs.length(); i++){
                    JSONObject obj = objs.getJSONObject(i);
                    pendingJobs.add(gson.fromJson(obj.toString(),ConceptBooking.class));
                }
                pendingCount.setText(String.valueOf(pendingJobs.size()));
            }
        }
    }

    public class MessagesLoader implements ResponseListener{
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                unreadMessages = jSONObject.getInt("unreadmessage");
                messagesCount.setText(String.valueOf(unreadMessages));
            }
        }
    }


}
