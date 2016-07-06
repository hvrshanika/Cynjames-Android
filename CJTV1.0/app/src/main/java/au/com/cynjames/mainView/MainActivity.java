package au.com.cynjames.mainView;

import android.app.AlertDialog;
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
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    TextView messagesCount;
    int unreadMessages;
    SQLiteHelper db;
    List<ConceptBooking> pendingJobs;
    List<ConceptBooking> deliverReadyJobs;
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("Location");
            mLastLocation = b.getParcelable("Location");
            if (mLastLocation != null) {
                if(firstStatus){
                    updateDriverStauts("Driver has logged in");
                    firstStatus = false;
                }
                else {
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
            prefsEditor.clear();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myApp = (CJT) this.getApplication();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#bec2cb")));
        actionBar.setLogo(R.mipmap.logo_red);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        mPrefs = getApplicationContext().getSharedPreferences("AppData", 0);
        prefsEditor = mPrefs.edit();
        context = MainActivity.this;
        pendingJobs = new ArrayList<>();
        deliverReadyJobs = new ArrayList<>();
        gson = new Gson();
        db = new SQLiteHelper(this);
        firstStatus = true;
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
        setTimer();
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
                loadNewJobs();
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
    protected void onResume() {
        super.onResume();
        getUser();
        invalidateOptionsMenu();
        updateLabels();
        if (myApp.wasInBackground) {
            Log.d("inBackground", "ONRESUME");
            stopNotificationService();
            loadNewJobs();
        }
        myApp.stopActivityTransitionTimer();
        resetDisconnectTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myApp.startActivityTransitionTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDisconnectTimer();
        stopLogOutTimer();
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (GenericMethods.isConnectedToInternet(MainActivity.this)) {
                    Log.d("stopped", "ONSTOP");
                    if (myApp.wasInBackground) {
                        Log.d("inBackground", "ONSTOP");
                        uploadDatatoServer();
                        startNotificationService();
                    }
                }
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 3000);

        Runnable logoutRunnable = new Runnable() {

            @Override
            public void run() {
                if(logoutTimer) {
                    prefsEditor.clear();
                    finish();
                }
            }
        };

        Handler logoutTask = new Handler();
        logoutTask.postDelayed(logoutRunnable, 1800000);
//        logoutTask.postDelayed(logoutRunnable, 10000);

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
        TextView deliveryJobsViewBtn = (TextView) findViewById(R.id.main_concept_ready_view);
        deliveryJobsViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deliveryJobsViewBtnClicked();
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
                    initVariables();
                    logoutClicked = true;
                    if(mLastLocation != null){
                        updateDriverStauts("Driver has logged out");
                    }
                    uploadDatatoServer();
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

    public void updateLabels() {
        pendingJobs = db.getPendingJobs();
        deliverReadyJobs = db.getReadyJobs();
        pendingCount.setText(String.valueOf(pendingJobs.size()));
        deliverReadyCount.setText(String.valueOf(deliverReadyJobs.size()));

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
        startActivity(intent);
    }

    private void deliveryJobsViewBtnClicked() {
        Intent intent = new Intent(context, JobsListActivity.class);
        intent.putExtra("type", "DeliveryJobs");
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
        logOutHandler.postDelayed(logOutCallback, 1800000);
    }

    public void stopLogOutTimer() {
        logOutHandler.removeCallbacks(logOutCallback);
    }

    @Override
    public void onUserInteraction() {
        resetDisconnectTimer();
        if(logoutTimer) {
            resetLogOutTimer();
        }
    }

    private void setTimer(){
        Timer timer = new Timer();
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
        timer.schedule(task, date.getTime(), 1000 * 60 * 60 * 24 * 7);
    }


    private void uploadDatatoServer() {
        List<ConceptBookingLog> logs = db.getAllLogs();
        prelogCount = logs.size();
        for (ConceptBookingLog log : logs) {
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
        List<ConceptBooking> jobsStausEight = db.getPendingJobsWithStatus("8");
        List<ConceptBooking> jobsStausTen = db.getPendingJobsWithStatus("10");
        for (ConceptBooking jobStatusEight : jobsStausEight) {
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
                    HTTPHandler.post("cjt-update-jobs-status-8.php", params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStatusEight.getId()), context, "Updating..."));
                    uploadPhotos(jobStatusEight.getPickupImages());
                    jobStatusEight.setConceptPickupSignature("uploads/" + jobStatusEight.getConceptPickupSignature());
                    db.updateJob(jobStatusEight);
                    updateLabels();
                }
            }
        }
        for (ConceptBooking jobStausTen : jobsStausTen) {
            RequestParams params = new RequestParams();
            params.add("id", String.valueOf(jobStausTen.getId()));
            params.add("arrivedClient", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausTen.getArrivedClient())));
            params.add("conceptBookingStatus", String.valueOf(jobStausTen.getConceptBookingStatus()));
            params.add("conceptDeliveryName", jobStausTen.getConceptDeliveryName());
            params.add("conceptDeliveryDate", GenericMethods.getDBDate(GenericMethods.getDatefromString(jobStausTen.getConceptDeliveryDate())));
            params.add("conceptDeliverySignature", "uploads/" + jobStausTen.getConceptDeliverySignature());
            params.add("conceptPickupName", jobStausTen.getConceptPickupName());
            params.add("conceptBookingPallets", String.valueOf(jobStausTen.getPallets()));
            params.add("conceptBookingParcels", String.valueOf(jobStausTen.getParcels()));
            params.add("conceptBookingTailLift", String.valueOf(jobStausTen.getConceptBookingTailLift()));
            params.add("conceptBookingHandUnload", String.valueOf(jobStausTen.getConceptBookingHandUnload()));
            if (jobStausTen.getConceptPickupSignature().contains("uploads")) {
                params.add("conceptPickupSignature", jobStausTen.getConceptPickupSignature());
                params.add("arrivedConcept", jobStausTen.getArrivedConcept());
                params.add("conceptBookingPickupDate", jobStausTen.getConceptBookingPickupDate());
            } else {
                if (jobStausTen.getConceptPickupSignature() != null) {
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
                    uploadPhotos(jobStausTen.getPickupImages());
                }
            }

            if (jobStausTen.getConceptDeliverySignature() != null) {
                prejobsCount++;
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
                HTTPHandler.post("cjt-update-jobs-status-10.php", params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausTen.getId()), context, "Updating..."));
                uploadPhotos(jobStausTen.getDeliveryImages());
            }
        }
        List<ConceptBooking> jobsStausTwo = db.getPendingJobsWithStatus("2");
        List<ConceptBooking> jobsStausNine = db.getPendingJobsWithStatus("9");
        for (ConceptBooking jobStausYwo : jobsStausTwo) {
            RequestParams params = new RequestParams();
            params.add("id", String.valueOf(jobStausYwo.getId()));
            params.add("conceptBookingStatus", String.valueOf(jobStausYwo.getConceptBookingStatus()));
            HTTPHandler.post("cjt-update-jobs-status-2-9.php", params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausYwo.getId()), context, "Updating..."));
        }
        for (ConceptBooking jobStausNine : jobsStausNine) {
            RequestParams params = new RequestParams();
            params.add("id", String.valueOf(jobStausNine.getId()));
            params.add("conceptBookingStatus", String.valueOf(jobStausNine.getConceptBookingStatus()));
            HTTPHandler.post("cjt-update-jobs-status-2-9.php", params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausNine.getId()), context, "Updating..."));
        }
        stopDisconnectTimer();
        isUpdatefinished();
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
            prefsEditor.clear();
            finish();
        } else if (prelogCount == postlogCount && preStatusCount == postStatusCount && prejobsCount == postjobsCount && preImagesCount == postImagesCount) {
            initVariables();
            loadNewJobs();
        }
    }

    private void loadNewJobs() {
        String userId = String.valueOf(user.getUserid());
        RequestParams params = new RequestParams();
        params.add("userid", userId);
        HTTPHandler.post("cjt-concept-pending-jobs.php", params, new HTTPHandler.ResponseManager(new NewJobsLoader(), context, "Updating..."));
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
//            Bitmap bitmap = null;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file:///" + FILE_PATH + "" + image + ""));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
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

    private void checkJobsAssigned(){
        List<ConceptBooking> statusSevenJobs = db.getPendingJobsWithStatus("7");

        for(ConceptBooking statusSevenJob: statusSevenJobs){
            RequestParams params = new RequestParams();
            params.add("userid", String.valueOf(user.getUserid()));
            params.add("bookingId", String.valueOf(statusSevenJob.getId()));
            HTTPHandler.post("cjt-concept-check-assigned.php", params, new HTTPHandler.ResponseManager(new AssignedJobsChecker(statusSevenJob.getId()), context, "Updating..."));
        }
    }

    public class PendingListLoader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                JSONArray objs = jSONObject.getJSONArray("joblist");
                db.clearTable("conceptBooking");
                for (int i = 0; i < objs.length(); i++) {
                    JSONObject obj = objs.getJSONObject(i);
                    ConceptBooking job = gson.fromJson(obj.toString(), ConceptBooking.class);
                    db.addJob(job);

                }
                updateLabels();
                if (pendingJobs.size() > 0) {
                    pendingIcon.setVisibility(View.VISIBLE);
                    playSound();
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
                    db.clearConcept(id);
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
                if(!logoutClicked) {
                    GenericMethods.showToast(MainActivity.this, "Log data upload successful.");
                }
                db.clearTable("conceptBookingLog");
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
                if(!logoutClicked) {
                    GenericMethods.showToast(MainActivity.this, "Driver data upload successful.");
                }
                db.clearTable("driverStatus");
            }
            isUpdatefinished();
        }
    }

    public class ConceptUploader implements ResponseListener {
        int id;

        public ConceptUploader(int id) {
            this.id = id;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            postjobsCount++;
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                if(!logoutClicked) {
                    GenericMethods.showToast(MainActivity.this, "Concept data upload successful.");
                }
                if (jSONObject.getInt("status") == 10) {
                    db.clearConcept(id);
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
                if(!logoutClicked) {
                    GenericMethods.showToast(MainActivity.this, "Image upload successful.");
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
                    if (!db.jobExist(job.getId())) {
                        db.addJob(job);
                        newJobs.add(job);
                    }

                }
                updateLabels();
                if (newJobs.size() > 0) {
                    pendingIcon.setVisibility(View.VISIBLE);
                    playSound();
                }
            }
            checkJobsAssigned();
        }
    }
}
