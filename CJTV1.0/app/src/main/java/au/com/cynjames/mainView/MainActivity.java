package au.com.cynjames.mainView;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
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

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import au.com.cynjames.CJT;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ConceptBookingLog;
import au.com.cynjames.models.DriverStatus;
import au.com.cynjames.models.User;
import au.com.cynjames.models.Vehicles.Vehicle;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.LocationService;
import au.com.cynjames.utils.NewJobsUpdateService;
import au.com.cynjames.utils.SQLiteHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    public static final long INTERACTION_TIMEOUT = 8000;
    Context context;
    Menu menu;
    Editor prefsEditor;
    SharedPreferences mPrefs;
    User user;
    Gson gson;
    TextView pendingCount;
    TextView deliverReadyCount;
    TextView adhocPendingCount;
    TextView adhocDeliverReadyCount;
    TextView messagesCount;
    TextView tab_count_concept;
    TextView tab_count_adhoc;
    TextView tab_count_msgs;
    int unreadMessages;
    SQLiteHelper db;
    List<ConceptBooking> pendingJobs;
    List<ConceptBooking> deliverReadyJobs;
    List<ConceptBooking> adhocPendingJobs;
    List<ConceptBooking> adhocDeliverReadyJobs;
    Location mLastLocation;
    ImageView pendingIcon;
    Vehicle vehicle;
    CJT myApp;
    boolean logoutClicked;
    String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "CJT-AppData" + File.separator;
    int prelogCount = 0;
    int postlogCount = 0;
    int preStatusCount = 0;
    int postStatusCount = 0;
    int prejobsCount = 0;
    int postjobsCount = 0;
    int preImagesCount = 0;
    int postImagesCount = 0;
    boolean firstStatus;
    boolean logoutTimer = false;
    boolean isConcept;
    CustomPagerAdapter adapter;
    int conceptNewJobsCount = 0;
    Timer timer;
    boolean notificationSent = false;
    boolean isUploading = false;
    boolean isRefreshing = false;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("Location");
            mLastLocation = b.getParcelable("Location");
            if (mLastLocation != null) {
                if (firstStatus) {
                    updateDriverStauts("Driver has logged in");
                    firstStatus = false;
                } else {
                    updateDriverStauts("Locating Driver");
                }
            }
        }
    };
    private Handler interactionHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private Runnable interactionCallback = new Runnable() {
        @Override
        public void run() {
            if (GenericMethods.isConnectedToInternet(MainActivity.this)) {
                uploadDatatoServer();
            }
        }
    };
    private Handler logOutHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private Runnable logOutCallback = new Runnable() {
        @Override
        public void run() {
            sendNotification();
            notificationSent = true;
            logoutUser();
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myApp = (CJT) this.getApplication();
        mPrefs = getApplicationContext().getSharedPreferences("AppData", 0);
        prefsEditor = mPrefs.edit();
        context = MainActivity.this;
        pendingJobs = new ArrayList<>();
        deliverReadyJobs = new ArrayList<>();
        adhocPendingJobs = new ArrayList<>();
        adhocDeliverReadyJobs = new ArrayList<>();
        gson = new Gson();
        db = new SQLiteHelper(this);
        firstStatus = true;
        getUser();
        setActionBar();
        init();
        if (user == null) {
            //finish();
            GenericMethods.showMessage(context, "Error", "Please Login again!");
        }
        if (GenericMethods.isConnectedToInternet(context)) {
            loadData();
        } else {
            GenericMethods.showNoInternetDialog(context);
            GenericMethods.showMessage(context, "Error", "Please Login again!");
            //logoutUser();
        }
        startLocationService();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
        setTimer();
        uploadFirebaseToken();
        FirebaseCrash.log("MainActivity - onCreate");
    }

    public void setActionBar(){
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new CustomPagerAdapter(this, getLayoutInflater());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getSupportActionBar().setSelectedNavigationItem(position);
                    }
                });


        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        actionBar.setLogo(R.mipmap.app_icon);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("   Hi " + user.getUserFirstName() + "!");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
                TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab_view_id);
                title.setTypeface(Typeface.DEFAULT_BOLD);
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab_view_id);
                title.setTypeface(Typeface.DEFAULT);
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E0425")));
        ActionBar.Tab conceptTab = actionBar.newTab();
        conceptTab.setCustomView(R.layout.tab_item);
        conceptTab.setTag("1");
        conceptTab.setTabListener(tabListener);
        TextView tab_title_c = (TextView) conceptTab.getCustomView().findViewById(R.id.tab_view_id);
        tab_title_c.setText("CONCEPT");
        tab_count_concept = (TextView) conceptTab.getCustomView().findViewById(R.id.tab_view_count);
        tab_count_concept.setTypeface(tab_count_concept.getTypeface(), Typeface.BOLD);

        ActionBar.Tab adhocTab = actionBar.newTab();
        adhocTab.setCustomView(R.layout.tab_item);
        adhocTab.setTag("2");
        adhocTab.setTabListener(tabListener);
        TextView tab_title_a = (TextView) adhocTab.getCustomView().findViewById(R.id.tab_view_id);
        tab_title_a.setText("ADHOC");
        tab_count_adhoc = (TextView) adhocTab.getCustomView().findViewById(R.id.tab_view_count);
        tab_count_adhoc.setTypeface(tab_count_adhoc.getTypeface(), Typeface.BOLD);

        ActionBar.Tab msgsTab = actionBar.newTab();
        msgsTab.setCustomView(R.layout.tab_item);
        msgsTab.setTag("3");
        msgsTab.setTabListener(tabListener);
        TextView tab_title_m = (TextView) msgsTab.getCustomView().findViewById(R.id.tab_view_id);
        tab_title_m.setText("MESSAGES");
        tab_count_msgs = (TextView) msgsTab.getCustomView().findViewById(R.id.tab_view_count);
        tab_count_msgs.setTypeface(tab_count_msgs.getTypeface(), Typeface.BOLD);

        actionBar.addTab(conceptTab,true);
        actionBar.addTab(adhocTab);
        actionBar.addTab(msgsTab);
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
                if (GenericMethods.isConnectedToInternet(this)) {
                    loadNewJobs();
                } else {
                    GenericMethods.showNoInternetDialog(context);
                }
                break;
            case R.id.action_logout:
                logoutUser();
                break;
//            case R.id.action_log:
//                logsBtnClicked();
//                break;
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
//        MenuItem timeDifference = menu.findItem(R.id.action_difference);
        if (user.getUserArriveConcept() != null) {
            arriveConcept.setTitle("Concept: " + user.getUserArriveConcept());
        }
        else{
            arriveConcept.setVisible(false);
        }
        if (user.getUserArriveClient() != null) {
            arriveClient.setTitle("Client: " + user.getUserArriveClient());
        }
        else{
            arriveClient.setVisible(false);
        }
    }

    private void openGMapsWithPosition() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q= &mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        else{
            GenericMethods.showToast(context, "No supported application found");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseCrash.log("MainActivity - onResume - start");
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (result != 0) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(this, result,
                        result).show();
            }
        }
        getUser();
        invalidateOptionsMenu();
        updateLabels();
        if (myApp.wasInBackground) {
            FirebaseCrash.log("MainActivity inBackground onResume");
            //stopNotificationService();
            if (GenericMethods.isConnectedToInternet(context)) {
                loadNewJobs();
            }
        }
        myApp.stopActivityTransitionTimer();
        resetDisconnectTimer();

        if(logoutTimer){
            if(notificationSent) {
                stopLogOutTimer();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(0);
            }
            else
                resetLogOutTimer();
        }
        FirebaseCrash.log("MainActivity - onResume - end");
    }

    @Override
    protected void onPause() {
        super.onPause();
        myApp.startActivityTransitionTimer();
        FirebaseCrash.log("MainActivity - onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseCrash.log("MainActivity - onStop - start");
        stopDisconnectTimer();
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (myApp.wasInBackground) {
                    FirebaseCrash.log("MainActivity inBackground ONSTOP");
                    if (GenericMethods.isConnectedToInternet(context)) {
                        uploadDatatoServer();
                    }
                    //startNotificationService();
                }
                else{
                    if(logoutTimer)
                        stopLogOutTimer();
                }
            }
        };

        if (!logoutClicked) {
            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 3000);
        }

        FirebaseCrash.log("MainActivity - onStop");

    }

    private void init() {
        TextView welcome = (TextView) findViewById(R.id.main_welcome_user_text);
        welcome.setText("Welcome " + user.getUserFirstName());

        pendingCount = adapter.getViewAtPosition(CustomPagerAdapter.CONCEPT_COUNT);
        deliverReadyCount = adapter.getViewAtPosition(CustomPagerAdapter.CONCEPT_DELIVERY_COUNT);
        adhocPendingCount = adapter.getViewAtPosition(CustomPagerAdapter.ADHOC_COUNT);
        adhocDeliverReadyCount = adapter.getViewAtPosition(CustomPagerAdapter.ADHOC_DELIVERY_COUNT);

        pendingIcon = (ImageView) findViewById(R.id.main_truck_icon);
        messagesCount = (TextView) findViewById(R.id.main_messages_count);

        pendingCount.setTypeface(pendingCount.getTypeface(), Typeface.BOLD);
        deliverReadyCount.setTypeface(deliverReadyCount.getTypeface(), Typeface.BOLD);
        adhocPendingCount.setTypeface(pendingCount.getTypeface(), Typeface.BOLD);
        adhocDeliverReadyCount.setTypeface(deliverReadyCount.getTypeface(), Typeface.BOLD);

        TextView pendingJobsViewBtn = adapter.getViewAtPosition(CustomPagerAdapter.CONCEPT_VIEW);
        pendingJobsViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConcept = true;
                pendingJobsViewBtnClicked();
            }
        });
        TextView deliveryJobsViewBtn = adapter.getViewAtPosition(CustomPagerAdapter.CONCEPT_DELIVERY_VIEW);
        deliveryJobsViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConcept = true;
                deliveryJobsViewBtnClicked();
            }
        });

        TextView adhocPendingJobsViewBtn = adapter.getViewAtPosition(CustomPagerAdapter.ADHOC_VIEW);
        adhocPendingJobsViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConcept = false;
                pendingJobsViewBtnClicked();
            }
        });
        TextView adhocDeliveryJobsViewBtn = adapter.getViewAtPosition(CustomPagerAdapter.ADHOC_DELIVERY_VIEW);
        adhocDeliveryJobsViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConcept = false;
                deliveryJobsViewBtnClicked();
            }
        });

    }

    private void uploadFirebaseToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && GenericMethods.isConnectedToInternet(context)) {
            Log.d("Token:", token);
            String userId = String.valueOf(user.getUserid());
            RequestParams params = new RequestParams();
            params.add("userId", userId);
            params.add("firebaseToken", token);
            HTTPHandler.post("cjt-updateFbToken.php", params, new HTTPHandler.ResponseManager(new TokenUploader(), context, "Updating..."));
        }
    }

    private void loadData() {
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid", userId);
        isConcept = true;
        HTTPHandler.post("cjt-concept-pending-jobs.php", params, new HTTPHandler.ResponseManager(new PendingListLoader(), context, "Updating..."));
    }

    private void loadAdhocData() {
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid", userId);
        isConcept = false;
        HTTPHandler.post("cjt-adhoc-pending-jobs.php", params, new HTTPHandler.ResponseManager(new PendingListLoader(), context, "Updating..."));
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
                    initVariables();
                    logoutClicked = true;
                    if(logoutTimer){
                        timer.cancel();
                        stopLogOutTimer();
                    }
                    if (mLastLocation != null) {
                        updateDriverStauts("Driver has logged out");
                    }
                    if (GenericMethods.isConnectedToInternet(context)) {
                        uploadDatatoServer();
                    } else {
                        GenericMethods.showNoInternetDialog(context);
                    }
                }
            });
            build.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            if(!((MainActivity) context).isFinishing()){
                build.create().show();
            }
        }
    }

    private void getUser() {
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
        String jsonVehicle = mPrefs.getString("Vehicle", "");
        vehicle = gson.fromJson(jsonVehicle, Vehicle.class);
    }

    public void updateLabels() {
        pendingJobs = db.getPendingJobs(true);
        deliverReadyJobs = db.getReadyJobs(true);
        adhocPendingJobs = db.getPendingJobs(false);
        adhocDeliverReadyJobs = db.getReadyJobs(false);
        pendingCount.setText(String.valueOf(pendingJobs.size()));
        deliverReadyCount.setText(String.valueOf(deliverReadyJobs.size()));
        adhocPendingCount.setText(String.valueOf(adhocPendingJobs.size()));
        adhocDeliverReadyCount.setText(String.valueOf(adhocDeliverReadyJobs.size()));

        int conceptTotal = pendingJobs.size() + deliverReadyJobs.size();
        int adhocTotal = adhocPendingJobs.size() + adhocDeliverReadyJobs.size();
        if(conceptTotal > 0){
            tab_count_concept.setVisibility(View.VISIBLE);
            tab_count_concept.setText(String.valueOf(pendingJobs.size() + deliverReadyJobs.size()));
        }
        else{
            tab_count_concept.setVisibility(View.GONE);
        }
        if(adhocTotal > 0){
            tab_count_adhoc.setVisibility(View.VISIBLE);
            tab_count_adhoc.setText(String.valueOf(adhocPendingJobs.size() + adhocDeliverReadyJobs.size()));
        }
        else{
            tab_count_adhoc.setVisibility(View.GONE);
        }
        tab_count_msgs.setVisibility(View.GONE);
    }

    private void initVariables() {
        prelogCount = 0;
        postlogCount = 0;
        preStatusCount = 0;
        postStatusCount = 0;
        prejobsCount = 0;
        postjobsCount = 0;
        preImagesCount = 0;
        postImagesCount = 0;
    }

    private void updateDriverStauts(String desc) {
        DriverStatus status = new DriverStatus();
        status.setDriverStatusDate(GenericMethods.getDBDateOnly(new Date()));
        status.setDriverStatusTime(GenericMethods.getDBTime(new Date()));
        status.setDriverStatus_driverId(user.getUserid());
        status.setDriverStatusDescription(desc);
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

    public void startNotificationService() {
        startService(new Intent(getBaseContext(), NewJobsUpdateService.class));
    }

    public void stopNotificationService() {
        stopService(new Intent(getBaseContext(), NewJobsUpdateService.class));
    }

    private void playSound() {
        MediaPlayer mp;
        mp = MediaPlayer.create(context, R.raw.notification_sound);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                mp = null;
            }

        });
        mp.start();
    }

    private void pendingJobsViewBtnClicked() {
        Intent intent = new Intent(context, JobsListActivity.class);
        intent.putExtra("type", "JobsPending");
        if(isConcept){
            intent.putExtra("view", "Concept");
        }
        else{
            intent.putExtra("view", "Adhoc");
        }
        startActivity(intent);
    }

    private void deliveryJobsViewBtnClicked() {
        Intent intent = new Intent(context, JobsListActivity.class);
        intent.putExtra("type", "DeliveryJobs");
        if(isConcept){
            intent.putExtra("view", "Concept");
        }
        else{
            intent.putExtra("view", "Adhoc");
        }
        startActivity(intent);
    }

    private void logsBtnClicked() {
        List<DriverStatus> statusList = db.getAllStatus();
        GenericMethods.showToast(context, "driverStatus Table count:" + statusList.size());
    }

    public void resetDisconnectTimer() {
        interactionHandler.removeCallbacks(interactionCallback);
        interactionHandler.postDelayed(interactionCallback, INTERACTION_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        interactionHandler.removeCallbacks(interactionCallback);
    }

    public void resetLogOutTimer() {
        logOutHandler.removeCallbacks(logOutCallback);
        logOutHandler.postDelayed(logOutCallback, 60000);
    }

    public void stopLogOutTimer() {
        logOutHandler.removeCallbacks(logOutCallback);
    }

    @Override
    public void onUserInteraction() {
        resetDisconnectTimer();
        if(logoutTimer && !notificationSent) {
            resetLogOutTimer();
        }
    }

    private void setTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                logoutTimer = true;
                resetLogOutTimer();
            }
        };
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR, 7);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.AM_PM, Calendar.PM);
        timer.schedule(task, date.getTime());//, 1000 * 60 * 60 * 24);
    }

    private void sendNotification(){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Cynjames Notification")
                .setContentText("You have been idle for 30 minutes.")
                .setSmallIcon(R.mipmap.app_icon)
                .setSound(defaultSoundUri)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }

    private void uploadDatatoServer() {
        if(!isUploading) {
            isUploading = true;
            List<ConceptBookingLog> conceptLogs = db.getAllLogs(true);
            List<ConceptBookingLog> adhocLogs = db.getAllLogs(false);
            prelogCount = conceptLogs.size();
            for (ConceptBookingLog log : conceptLogs) {
                RequestParams params = new RequestParams();
                params.add("conceptBookingLog_bookingId", String.valueOf(log.getConceptBookingLog_bookingId()));
                params.add("conceptBookingLogOrderNo", log.getConceptBookingLogOrderNo());
                params.add("conceptBookingLogBarcode", log.getConceptBookingLogBarcode());
                params.add("conceptBookingLogUserId", String.valueOf(log.getConceptBookingLogUserId()));
                params.add("conceptBookingLogComments", log.getConceptBookingLogComments());
                params.add("conceptBookingLogDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(log.getConceptBookingLogDate())));
                params.add("conceptBookingLogStatus", String.valueOf(log.getConceptBookingLogStatus()));
                params.add("hasDeparted", String.valueOf(log.getHasDeparted()));
                HTTPHandler.post("cjt-update-log.php", params, new HTTPHandler.ResponseManager(new LogUploader(), context, "Updating..."));
            }
            prelogCount += adhocLogs.size();
            for (ConceptBookingLog log : adhocLogs) {
                RequestParams params = new RequestParams();
                params.add("conceptBookingLog_bookingId", String.valueOf(log.getConceptBookingLog_bookingId()));
                params.add("conceptBookingLogOrderNo", log.getConceptBookingLogOrderNo());
                params.add("conceptBookingLogBarcode", log.getConceptBookingLogBarcode());
                params.add("conceptBookingLogUserId", String.valueOf(log.getConceptBookingLogUserId()));
                params.add("conceptBookingLogComments", log.getConceptBookingLogComments());
                params.add("conceptBookingLogDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(log.getConceptBookingLogDate())));
                params.add("conceptBookingLogStatus", String.valueOf(log.getConceptBookingLogStatus()));
                params.add("hasDeparted", String.valueOf(log.getHasDeparted()));
                HTTPHandler.post("cjt-adhoc-update-log.php", params, new HTTPHandler.ResponseManager(new LogUploader(), context, "Updating..."));
            }
            List<DriverStatus> statuses = db.getAllStatus();
            preStatusCount = statuses.size();
            for (DriverStatus status : statuses) {
                RequestParams params = new RequestParams();
                params.add("date", GenericMethods.getDBDate(GenericMethods.getDatefromString(status.getDriverStatusDate())));
                params.add("time", status.getDriverStatusTime());
                params.add("description", status.getDriverStatusDescription());
                params.add("longitude", String.valueOf(status.getDriverStatusLongitude()));
                params.add("latitude", String.valueOf(status.getDriverStatusLatitude()));
                params.add("driverid", String.valueOf(status.getDriverStatus_driverId()));
                params.add("vehicleid", status.getDriverStatus_vehicleId());
                HTTPHandler.post("cjt-update-driver-status.php", params, new HTTPHandler.ResponseManager(new DriverStatusUploader(), context, "Updating..."));
            }
            List<ConceptBooking> jobsStausEight = db.getPendingJobsWithStatus("8", true);
            List<ConceptBooking> jobsStausTen = db.getPendingJobsWithStatus("10", true);
            List<ConceptBooking> adhocJobsStausEight = db.getPendingJobsWithStatus("8", false);
            List<ConceptBooking> adhocJobsStausTen = db.getPendingJobsWithStatus("10", false);
            List<ConceptBooking> jobsStausTwo = db.getPendingJobsWithStatus("2", true);
            List<ConceptBooking> jobsStausNine = db.getPendingJobsWithStatus("9", true);
            List<ConceptBooking> adhocJobsStausTwo = db.getPendingJobsWithStatus("2", false);
            List<ConceptBooking> adhocJobsStausNine = db.getPendingJobsWithStatus("9", false);
            for (int i = 0; i < 2; i++) {
                List<ConceptBooking> arr8;
                List<ConceptBooking> arr10;
                List<ConceptBooking> arr2;
                List<ConceptBooking> arr9;
                String phpFile8;
                String phpFile10;
                String phpFile810;
                String phpFile29;
                boolean concept;
                if (i == 0) {
                    arr8 = jobsStausEight;
                    arr10 = jobsStausTen;
                    arr2 = jobsStausTwo;
                    arr9 = jobsStausNine;
                    concept = true;
                    phpFile8 = "cjt-update-jobs-status-8.php";
                    phpFile10 = "cjt-update-jobs-status-10.php";
                    phpFile810 = "cjt-update-jobs-status-8-10.php";
                    phpFile29 = "cjt-update-jobs-status-2-9.php";
                } else {
                    arr8 = adhocJobsStausEight;
                    arr10 = adhocJobsStausTen;
                    arr2 = adhocJobsStausTwo;
                    arr9 = adhocJobsStausNine;
                    concept = true;
                    phpFile8 = "cjt-adhoc-update-jobs-status-8.php";
                    phpFile10 = "cjt-adhoc-update-jobs-status-10.php";
                    phpFile810 = "cjt-adhoc-update-jobs-status-8-10.php";
                    phpFile29 = "cjt-adhoc-update-jobs-status-2-9.php";
                }
                for (ConceptBooking jobStatusEight : arr8) {
                    RequestParams params = new RequestParams();
                    params.add("id", String.valueOf(jobStatusEight.getId()));
                    params.add("arrivedConcept", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStatusEight.getArrivedConcept())));
                    params.add("conceptBookingStatus", String.valueOf(jobStatusEight.getConceptBookingStatus()));
                    params.add("conceptPickupName", jobStatusEight.getConceptPickupName());
                    params.add("conceptBookingPallets", String.valueOf(jobStatusEight.getPallets()));
                    params.add("conceptBookingParcels", String.valueOf(jobStatusEight.getParcels()));
                    if (jobStatusEight.getConceptPickupSignature().contains("uploads")) {
                        params.add("conceptPickupSignature", jobStatusEight.getConceptPickupSignature());
                        params.add("conceptBookingPickupDate", jobStatusEight.getConceptBookingPickupDate());
                    } else {
                        if (jobStatusEight.getConceptPickupSignature() != null) {
                            params.add("conceptBookingPickupDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStatusEight.getConceptBookingPickupDate())));
                            params.add("conceptPickupSignature", "uploads/" + jobStatusEight.getConceptPickupSignature());
                            prejobsCount++;
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file:///" + FILE_PATH + "" + jobStatusEight.getConceptPickupSignature() + ""));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null) {
                                preImagesCount += 1;
                                String encoded = getStringImage(bitmap);
                                RequestParams paramsImg = new RequestParams();
                                paramsImg.add("image", encoded);
                                paramsImg.add("name", jobStatusEight.getConceptPickupSignature());
                                HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(jobStatusEight.getConceptPickupSignature()), context, "Uploading Image..."));
                            }
                            HTTPHandler.post(phpFile8, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStatusEight.getId(), concept), context, "Updating..."));
                            uploadPhotos(jobStatusEight.getPickupImages());
                            jobStatusEight.setConceptPickupSignature("uploads/" + jobStatusEight.getConceptPickupSignature());
                            db.updateJob(jobStatusEight, true);
                        }
                    }
                }
                updateLabels();
                for (ConceptBooking jobStausTen : arr10) {
                    RequestParams params = new RequestParams();
                    params.add("id", String.valueOf(jobStausTen.getId()));
                    params.add("arrivedClient", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausTen.getArrivedClient())));
                    params.add("conceptBookingStatus", String.valueOf(jobStausTen.getConceptBookingStatus()));
                    params.add("conceptDeliveryName", jobStausTen.getConceptDeliveryName());
                    params.add("conceptDeliveryDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausTen.getConceptDeliveryDate())));
                    params.add("conceptDeliverySignature", "uploads/" + jobStausTen.getConceptDeliverySignature());
                    params.add("conceptBookingPallets", String.valueOf(jobStausTen.getPallets()));
                    params.add("conceptBookingParcels", String.valueOf(jobStausTen.getParcels()));
                    params.add("conceptBookingTailLift", String.valueOf(jobStausTen.getConceptBookingTailLift()));
                    params.add("conceptBookingHandUnload", String.valueOf(jobStausTen.getConceptBookingHandUnload()));

                    if (jobStausTen.getConceptDeliverySignature() != null) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file:///" + FILE_PATH + "" + jobStausTen.getConceptDeliverySignature() + ""));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap != null) {
                            preImagesCount += 1;
                            String encoded = getStringImage(bitmap);
                            RequestParams paramsImg = new RequestParams();
                            paramsImg.add("image", encoded);
                            paramsImg.add("name", jobStausTen.getConceptDeliverySignature());
                            HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(jobStausTen.getConceptDeliverySignature()), context, "Uploading Image..."));
                        }
                        uploadPhotos(jobStausTen.getDeliveryImages());
                    }
                    prejobsCount++;
                    if (jobStausTen.getConceptPickupSignature() != null) {
                        if (jobStausTen.getConceptPickupSignature().contains("uploads")) {
                            HTTPHandler.post(phpFile10, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausTen.getId(), concept), context, "Updating..."));
                        } else {
                            params.add("conceptPickupName", jobStausTen.getConceptPickupName());
                            params.add("conceptPickupSignature", "uploads/" + jobStausTen.getConceptPickupSignature());
                            params.add("arrivedConcept", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausTen.getArrivedConcept())));
                            params.add("conceptBookingPickupDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausTen.getConceptBookingPickupDate())));
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file:///" + FILE_PATH + "" + jobStausTen.getConceptPickupSignature() + ""));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null) {
                                preImagesCount += 1;
                                String encoded = getStringImage(bitmap);
                                RequestParams paramsImg = new RequestParams();
                                paramsImg.add("image", encoded);
                                paramsImg.add("name", jobStausTen.getConceptPickupSignature());
                                HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(jobStausTen.getConceptPickupSignature()), context, "Uploading Image..."));
                            }
                            HTTPHandler.post(phpFile810, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausTen.getId(), concept), context, "Updating..."));
                            uploadPhotos(jobStausTen.getPickupImages());
                        }
                    } else {
                        HTTPHandler.post(phpFile10, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausTen.getId(), concept), context, "Updating..."));
                    }
                }

                for (ConceptBooking jobStausYwo : arr2) {
                    prejobsCount++;
                    RequestParams params = new RequestParams();
                    params.add("id", String.valueOf(jobStausYwo.getId()));
                    params.add("conceptBookingStatus", String.valueOf(jobStausYwo.getConceptBookingStatus()));
                    HTTPHandler.post(phpFile29, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausYwo.getId(), concept), context, "Updating..."));
                }
                for (ConceptBooking jobStausNine : arr9) {
                    prejobsCount++;
                    RequestParams params = new RequestParams();
                    params.add("id", String.valueOf(jobStausNine.getId()));
                    params.add("conceptBookingStatus", String.valueOf(jobStausNine.getConceptBookingStatus()));
                    HTTPHandler.post(phpFile29, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausNine.getId(), concept), context, "Updating..."));
                    if (jobStausNine.getConceptPickupSignature() != null) {
                        if (!(jobStausNine.getConceptPickupSignature().contains("uploads"))) {
                            params.add("conceptPickupName", jobStausNine.getConceptPickupName());
                            params.add("conceptPickupSignature", "uploads/" + jobStausNine.getConceptPickupSignature());
                            params.add("conceptBookingPallets", String.valueOf(jobStausNine.getPallets()));
                            params.add("conceptBookingParcels", String.valueOf(jobStausNine.getParcels()));
                            params.add("arrivedConcept", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausNine.getArrivedConcept())));
                            params.add("conceptBookingPickupDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausNine.getConceptBookingPickupDate())));
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file:///" + FILE_PATH + "" + jobStausNine.getConceptPickupSignature() + ""));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null) {
                                preImagesCount += 1;
                                String encoded = getStringImage(bitmap);
                                RequestParams paramsImg = new RequestParams();
                                paramsImg.add("image", encoded);
                                paramsImg.add("name", jobStausNine.getConceptPickupSignature());
                                HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(jobStausNine.getConceptPickupSignature()), context, "Uploading Image..."));
                            }
                            prejobsCount++;
                            HTTPHandler.post(phpFile8, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausNine.getId(), concept), context, "Updating..."));
                            uploadPhotos(jobStausNine.getPickupImages());
                            jobStausNine.setConceptPickupSignature("uploads/" + jobStausNine.getConceptPickupSignature());
                            db.updateJob(jobStausNine, true);
                        }
                    }
                }
            }
            stopDisconnectTimer();
            isUpdatefinished();
        }
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private String getStringPhoto(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void isUpdatefinished() {
        if (logoutClicked && prelogCount == postlogCount && preStatusCount == postStatusCount && prejobsCount == postjobsCount && preImagesCount == postImagesCount) {
            isUploading = false;
            prefsEditor.clear();
            finish();
        } else if (prelogCount == postlogCount && preStatusCount == postStatusCount && prejobsCount == postjobsCount && preImagesCount == postImagesCount) {
            initVariables();
            if (GenericMethods.isConnectedToInternet(this)) {
                loadNewJobs();
            }
        }
    }

    private void loadNewJobs() {
        if(!isRefreshing) {
            isRefreshing = true;
            String userId = String.valueOf(user.getUserid());
            RequestParams params = new RequestParams();
            params.add("userid", userId);
            isConcept = true;
            HTTPHandler.post("cjt-concept-pending-jobs.php", params, new HTTPHandler.ResponseManager(new NewJobsLoader(), context, "Updating..."));
        }
    }

    private void loadNewAdhocJobs() {
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid", userId);
        isConcept = false;
        HTTPHandler.post("cjt-adhoc-pending-jobs.php", params, new HTTPHandler.ResponseManager(new NewJobsLoader(), context, "Updating..."));
    }

    private void uploadPhotos(String imageString) {
        ArrayList<String> images = new ArrayList<>();
        if (imageString != null) {
            String[] imagesArr = imageString.split(",");
            for (String image : imagesArr) {
                if (!image.equals("")) {
                    images.add(image);
                }
            }
        }
        for (String image : images) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            String strPath = FILE_PATH + "" + image + "";
            Bitmap bitmap = BitmapFactory.decodeFile(strPath, options);
            if (bitmap != null) {
                preImagesCount += 1;
                String encoded = getStringPhoto(bitmap);
                RequestParams paramsImg = new RequestParams();
                paramsImg.add("image", encoded);
                paramsImg.add("name", image);
                HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(image), context, "Uploading Image..."));
            }
        }
    }

    int preJobsAssignedCheck = 0;
    int postJobsAssignedCheck = 0;

    private void checkJobsAssigned() {
        List<ConceptBooking> statusSevenJobs = db.getPendingJobsWithStatus("7", true);
        preJobsAssignedCheck = statusSevenJobs.size();
        postJobsAssignedCheck = 0;

        for (ConceptBooking statusSevenJob : statusSevenJobs) {

            RequestParams params = new RequestParams();
            params.add("userid", String.valueOf(user.getUserid()));
            params.add("bookingId", String.valueOf(statusSevenJob.getId()));
            isConcept = true;
            HTTPHandler.post("cjt-concept-check-assigned.php", params, new HTTPHandler.ResponseManager(new AssignedJobsChecker(statusSevenJob.getId()), context, "Updating..."));
        }
    }

    private void checkAdhocJobsAssigned() {
        List<ConceptBooking> statusSevenJobs = db.getPendingJobsWithStatus("7", false);

        for (ConceptBooking statusSevenJob : statusSevenJobs) {
            RequestParams params = new RequestParams();
            params.add("userid", String.valueOf(user.getUserid()));
            params.add("bookingId", String.valueOf(statusSevenJob.getId()));
            isConcept = false;
            HTTPHandler.post("cjt-adhoc-check-assigned.php", params, new HTTPHandler.ResponseManager(new AssignedJobsChecker(statusSevenJob.getId()), context, "Updating..."));
        }
    }

    public class PendingListLoader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                JSONArray objs = jSONObject.getJSONArray("joblist");
                if(isConcept){
                    db.clearTable("conceptBooking");
                }
                else{
                    db.clearTable("adhocBooking");
                }
                for (int i = 0; i < objs.length(); i++) {
                    JSONObject obj = objs.getJSONObject(i);
                    ConceptBooking job = gson.fromJson(obj.toString(), ConceptBooking.class);
                    db.addJob(job, isConcept);
                }
                updateLabels();
                if(isConcept){
                    loadAdhocData();
                }
                else{
                    if (pendingJobs.size() > 0 || adhocPendingJobs.size() > 0) {
                        pendingIcon.setVisibility(View.VISIBLE);
                        playSound();
                    }
                }
            }
        }
    }

    public class AssignedJobsChecker implements ResponseListener {
        int id;

        public AssignedJobsChecker(int id) {
            this.id = id;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                if (jSONObject.getInt("concept_status") == -1 || jSONObject.getInt("concept_status") == 6) {
                    db.clearConcept(id, isConcept);
                }
                postJobsAssignedCheck ++;
                if(isConcept && preJobsAssignedCheck == postJobsAssignedCheck){
                    checkAdhocJobsAssigned();
                }
            }
        }
    }

    public class LogUploader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            postlogCount++;
            if (jSONObject.getInt("success") == 1) {
                if (!logoutClicked) {
//                    GenericMethods.showToast(MainActivity.this, "Log data upload successful.");
                }
                db.clearTable("conceptBookingLog");
                db.clearTable("adhocBookingLog");
            }
            isUpdatefinished();
        }
    }

    public class DriverStatusUploader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            postStatusCount++;
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                if (!logoutClicked) {
//                    GenericMethods.showToast(MainActivity.this, "Driver data upload successful.");
                }
                db.clearTable("driverStatus");
            }
            isUpdatefinished();
        }
    }

    public class ConceptUploader implements ResponseListener {
        int id;
        boolean concept;

        public ConceptUploader(int id, boolean concept) {
            this.id = id;
            this.concept = concept;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            postjobsCount++;
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                if (!logoutClicked) {
//                    GenericMethods.showToast(MainActivity.this, "Concept data upload successful.");
                }
                if (jSONObject.getInt("status") == 10) {
                    db.clearConcept(id, concept);
                }
            }
            isUpdatefinished();
        }
    }

    public class ImageUploader implements ResponseListener {
        String name;

        public ImageUploader(String name) {
            this.name = name;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            postImagesCount++;
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                if (!logoutClicked) {
//                    GenericMethods.showToast(MainActivity.this, "Image upload successful.");
                }
                File file = new File(FILE_PATH, name);
                file.delete();
            }
            isUpdatefinished();
        }
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
                    if (!db.jobExist(job.getId(), isConcept)) {
                        db.addJob(job, isConcept);
                        newJobs.add(job);
                    }

                }
                updateLabels();
                if(isConcept){
                    conceptNewJobsCount = newJobs.size();
                    loadNewAdhocJobs();
                }
                else{
                    if (conceptNewJobsCount > 0 || newJobs.size() > 0) {
                        pendingIcon.setVisibility(View.VISIBLE);
                        playSound();
                    }
                    conceptNewJobsCount = 0;

                    if (GenericMethods.isConnectedToInternet(context)) {
                        checkJobsAssigned();
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
                    String d=formatter.format(new Date());
                    GenericMethods.showToast(MainActivity.this, "Last Updated at " + d);
                    isUploading = false;
                    isRefreshing = false;
                }
            }
        }
    }

    public class TokenUploader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {

            } else {

            }
        }
    }
}
