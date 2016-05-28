package au.com.cynjames.mainView;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.ActionBar;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.DriverStatus;
import au.com.cynjames.models.User;
import au.com.cynjames.models.Vehicles;
import au.com.cynjames.models.Vehicles.Vehicle;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.LocationService;
import au.com.cynjames.utils.SQLiteHelper;

public class MainActivity extends AppCompatActivity{
    Context context;
    Menu menu;
    Editor prefsEditor;
    SharedPreferences mPrefs;
    User user;
    Gson gson;
    TextView pendingCount;
    TextView deliverReadyCount;
    TextView messagesCount;
    int unreadMessages;
    SQLiteHelper db;
    List<ConceptBooking> pendingJobs;
    List<ConceptBooking> deliverReadyJobs;
    Location mLastLocation;
    ImageView pendingIcon;
    Vehicle vehicle;

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
        db = new SQLiteHelper(this);
        getUser();
        init();
        if (user == null) {
            finish();
        }
        if (GenericMethods.isConnectedToInternet(this)) {
            loadData();
        } else {
            GenericMethods.showNoInternetDialog(context);
            logoutUser();
        }
        startLocationService();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        this.menu = menu;
        updateMenuTitles();
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
            case R.id.action_log:
                logsBtnClicked();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        stopLocationService();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        logoutUser();
    }

    private void updateMenuTitles() {
        MenuItem arriveConcept = menu.findItem(R.id.action_ariive_concept);
        MenuItem arriveClient = menu.findItem(R.id.action_arrive_client);
        MenuItem timeDifference = menu.findItem(R.id.action_difference);
        if (user.getUserArriveConcept() != null) {
            arriveConcept.setTitle(user.getUserArriveConcept());
        }
        if (user.getUserArriveClient() != null) {
            arriveClient.setTitle(user.getUserArriveClient());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        getUser();
        invalidateOptionsMenu();
        updateLabels();
    }

    private void init() {
        TextView welcome = (TextView) findViewById(R.id.main_welcome_user_text);
        welcome.setText("Welcome " + user.getUserFirstName());
        pendingCount = (TextView) findViewById(R.id.main_concept_count);
        deliverReadyCount = (TextView) findViewById(R.id.main_concept_ready_count);
        messagesCount = (TextView) findViewById(R.id.main_messages_count);
        pendingIcon = (ImageView) findViewById(R.id.main_truck_icon);
        TextView pendingJobsViewBtn = (TextView) findViewById(R.id.main_concept_view);
        pendingJobsViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingJobsViewBtnClicked();
            }
        });

    }

    private void loadData() {
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid", userId);
        HTTPHandler.post("cjt-concept-pending-jobs.php", params, new HTTPHandler.ResponseManager(new PendingListLoader(), context, "Updating..."));

    }

    public void logoutUser() {
        if (user.getUserArriveConcept() != null) {
            GenericMethods.showToast(context, "You need to depart Concept before logging out");
        }
        if (user.getUserArriveClient() != null) {
            GenericMethods.showToast(context, "You need to depart Client before logging out");
        }
        if (user.getUserArriveConcept() == null && user.getUserArriveClient() == null) {
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
    }

    private void getUser() {
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
        String jsonVehicle = mPrefs.getString("Vehicle", "");
        vehicle = gson.fromJson(jsonVehicle, Vehicle.class);
    }

    public class PendingListLoader implements ResponseListener{
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                JSONArray objs = jSONObject.getJSONArray("joblist");
                db.clearTable("conceptBooking");
                for(int i = 0; i<objs.length(); i++){
                    JSONObject obj = objs.getJSONObject(i);
                    ConceptBooking job = gson.fromJson(obj.toString(),ConceptBooking.class);
                    db.addJob(job);

                }
                updateLabels();
                if(pendingJobs.size()>0){
                    pendingIcon.setVisibility(View.VISIBLE);
                    playSound();
                }
            }
        }
    }

    public void updateLabels(){
        pendingJobs = db.getPendingJobs();
        deliverReadyJobs = db.getReadyJobs();
        pendingCount.setText(String.valueOf(pendingJobs.size()));
        deliverReadyCount.setText(String.valueOf(deliverReadyJobs.size()));

    }

    private void updateDriverStauts(){
        Calendar c = Calendar.getInstance();
        DriverStatus status = new DriverStatus();
        status.setDriverStatusDate(String.valueOf(c.get(Calendar.DATE)));
        status.setDriverStatusTime(c.getTime().toString());
        status.setDriverStatus_driverId(user.getDriverId());
        status.setDriverStatusDescription("Locating Driver");
        status.setDriverStatus_vehicleId(vehicle.getVehicleId());
        status.setDriverStatusLatitude(mLastLocation.getLatitude());
        status.setDriverStatusLongitude(mLastLocation.getLongitude());
        db.addDriverStatus(status);
    }

    public void startLocationService() {
        startService(new Intent(getBaseContext(), LocationService.class));
    }

    public void stopLocationService() {
        stopService(new Intent(getBaseContext(), LocationService.class));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("Location");
            mLastLocation = (Location) b.getParcelable("Location");
            if (mLastLocation != null) {
                updateDriverStauts();
            }
        }
    };

    private void playSound(){
        MediaPlayer mp;
        mp = MediaPlayer.create(context, R.raw.notification_sound);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                mp=null;
            }

        });
        mp.start();
    }

    private void pendingJobsViewBtnClicked(){
        Intent intent = new Intent(context, JobsListActivity.class);
        intent.putExtra("type", "JobsPending");
        startActivity(intent);
    }

    private void logsBtnClicked(){
        List<DriverStatus> statusList = db.getAllStatus();
        GenericMethods.showToast(context, "driverStatus Table count:" + statusList.size());
    }

}
