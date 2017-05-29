package au.com.cynjames.mainView;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.cynjames.CJT;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ConceptBookingLog;
import au.com.cynjames.models.DriverStatus;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.SQLiteHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class JobsListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, JobsDetailsFragmentListener {
    public static final long INTERACTION_TIMEOUT = 8000;
    List<ConceptBooking> jobsList;
    List<ConceptBooking> sortedJobsList;
    SQLiteHelper db;
    User user;
    SharedPreferences mPrefs;
    Gson gson;
    ListArrayAdapter adapter;
    ListView jobsListView;
    boolean isDepart = false;
    TextView btnDepart;
    String type;
    String view;
    TextView btnCancel;
    boolean statusTwoJobs = false;
    boolean statusNineJobs = false;
    CJT myApp;
    String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "CJT-AppData" + File.separator;
    Context context;
    boolean isConcept;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_list);
        myApp = (CJT) this.getApplication();
        context = JobsListActivity.this;
        type = getIntent().getExtras().getString("type");
        view = getIntent().getExtras().getString("view");
        db = new SQLiteHelper(this);
        sortedJobsList = new ArrayList<>();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        actionBar.setLogo(R.mipmap.logo_red);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        mPrefs = getApplicationContext().getSharedPreferences("AppData", 0);
        gson = new Gson();
        getData();
        getUser();
        setButtonListeners();
        if (type.equals("JobsPending")) {
            checkUserArriveConcept();
        } else if (type.equals("DeliveryJobs")) {
            checkUserArriveClient();
        }
        adapter = new ListArrayAdapter(this, sortedJobsList, isConcept);
        init();
        checkDepartBtn();
        showCancelButton();
        FirebaseCrash.log("ListActivity - onCreate");
    }

    private Handler interactionHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private Runnable interactionCallback = new Runnable() {
        @Override
        public void run() {
            if (GenericMethods.isConnectedToInternet(JobsListActivity.this)) {
                uploadDatatoServer();
            }
        }
    };

    public void resetDisconnectTimer() {
        interactionHandler.removeCallbacks(interactionCallback);
        interactionHandler.postDelayed(interactionCallback, INTERACTION_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        interactionHandler.removeCallbacks(interactionCallback);
    }

    @Override
    public void onUserInteraction() {
        resetDisconnectTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myApp.startActivityTransitionTimer();
        FirebaseCrash.log("ListActivity - onPause");
    }

    private void init() {
        jobsListView = (ListView) findViewById(R.id.fragment_jobs_list_jobs_list);
        jobsListView.setAdapter(adapter);
        jobsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        jobsListView.setOnItemClickListener(this);

        TextView header = (TextView) findViewById(R.id.fragment_jobs_list_header_text);
        String headerText;
        if(isConcept){
            headerText = "Concept Jobs";
        }
        else {
            headerText = "Adhoc Jobs";
        }
        header.setText(headerText);

        if(sortedJobsList.size() == 0){
            findViewById(R.id.fragment_jobs_list_no_jobs_view).setVisibility(View.VISIBLE);
        }
    }

    private void setButtonListeners() {
        TextView backBtn = (TextView) findViewById(R.id.fragment_jobs_list_header_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backBtnClicked();
            }
        });
        btnDepart = (TextView) findViewById(R.id.fragment_jobs_list_header_depart_button);
        btnDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDepartClicked();
            }
        });
        btnCancel = (TextView) findViewById(R.id.fragment_jobs_list_header_cancel_button);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButtonClicked();
            }
        });
    }

    private void getData() {
        if(view.equals("Concept")) {
            isConcept = true;
            if (type.equals("JobsPending")) {
                jobsList = db.getPendingJobs(true);
            } else if (type.equals("DeliveryJobs")) {
                jobsList = db.getReadyJobs(true);
            }
        }
        if(view.equals("Adhoc")) {
            isConcept = false;
            if (type.equals("JobsPending")) {
                jobsList = db.getPendingJobs(false);
            } else if (type.equals("DeliveryJobs")) {
                jobsList = db.getReadyJobs(false);
            }
        }
    }

    private void getUser() {
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
    }

    private void showCancelButton() {
        if (type.equals("JobsPending")) {
            if (user.getUserArriveConcept() != null && !statusTwoJobs) {
                btnCancel.setVisibility(View.VISIBLE);
            } else {
                btnCancel.setVisibility(View.INVISIBLE);
            }
        } else if (type.equals("DeliveryJobs")) {
            if (user.getUserArriveClient() != null && !statusNineJobs) {
                btnCancel.setVisibility(View.VISIBLE);
            } else {
                btnCancel.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void cancelButtonClicked() {
        if (type.equals("JobsPending")) {
            user.setUserArriveConcept(null);
            db.updateUser(user);
            finish();
        } else if (type.equals("DeliveryJobs")) {
            user.setUserArriveClient(null);
            db.updateUser(user);
            finish();
        }
    }

    private void backBtnClicked() {
        finish();
    }

    private void checkUserArriveConcept() {
        boolean isStatusSeven = false;
        for (ConceptBooking job : jobsList) {
            if (job.getConceptBookingStatus() == 7) {
                isStatusSeven = true;
                sortedJobsList.add(job);
            }
        }
        for (ConceptBooking job : jobsList) {
            if (job.getConceptBookingStatus() == 2) {
                sortedJobsList.add(job);
                statusTwoJobs = true;
            }
        }
        if (user.getUserArriveConcept() == null && isStatusSeven && user.getUserArriveClient() == null) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            if(isConcept){
                build.setMessage("Arrived at Concept?");
            }
            else{
                build.setMessage("Arrived at Pickup Location?");
            }
            build.setCancelable(false);
            build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    user.setUserArriveConcept(GenericMethods.getDisplayDate(new Date()));
                    db.updateUser(user);
                    dialog.dismiss();
                    showCancelButton();
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

    @Override
    public void onResume() {
        super.onResume();
        myApp.stopActivityTransitionTimer();
        resetDisconnectTimer();
        FirebaseCrash.log("ListActivity - onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDisconnectTimer();
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (GenericMethods.isConnectedToInternet(JobsListActivity.this)) {
                    Log.d("stopped", "ONSTOP");
                    if (myApp.wasInBackground) {
                        Log.d("inBackground", "ONSTOP");
                        uploadDatatoServer();
                    }

                }
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 3000);
        FirebaseCrash.log("ListActivity - onStop");

    }

    private void checkUserArriveClient() {
        boolean isStatusEight = false;
        for (ConceptBooking job : jobsList) {
            if (job.getConceptBookingStatus() == 8) {
                isStatusEight = true;
                sortedJobsList.add(job);
            }
        }
        for (ConceptBooking job : jobsList) {
            if (job.getConceptBookingStatus() == 9) {
                sortedJobsList.add(job);
                statusNineJobs = true;
            }
        }
        if (user.getUserArriveClient() == null && user.getUserArriveConcept() == null && isStatusEight) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            if(isConcept){
                build.setMessage("Arrived at Client?");
            }
            else{
                build.setMessage("Arrived at Drop off Location?");
            }
            build.setCancelable(false);
            build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    user.setUserArriveClient(GenericMethods.getDisplayDate(new Date()));
                    db.updateUser(user);
                    dialog.dismiss();
                    showCancelButton();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        FragmentManager fm = getFragmentManager();
        JobDetailsFragment jobFragment = new JobDetailsFragment(this, sortedJobsList.get(position), params, type, isConcept);
        jobFragment.setListener(this);
        jobFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar);
        jobFragment.show(fm, "job_fragment");
    }

    @Override
    public void handleDialogClose() {
        resetDisconnectTimer();
        statusTwoJobs = false;
        statusNineJobs = false;
        runOnUiThread(new Runnable() {
            public void run() {
                sortedJobsList.clear();
                getData();
                for (ConceptBooking job : jobsList) {
                    if (job.getConceptBookingStatus() == 7 || job.getConceptBookingStatus() == 8) {
                        sortedJobsList.add(job);
                    }
                }
                for (ConceptBooking job : jobsList) {
                    if (job.getConceptBookingStatus() == 2) {
                        sortedJobsList.add(job);
                        statusTwoJobs = true;
                    }
                    if (job.getConceptBookingStatus() == 9) {
                        sortedJobsList.add(job);
                        statusNineJobs = true;
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                checkDepartBtn();
                showCancelButton();
            }
        });
    }

    @Override
    public void handleDialogCloseDepart() {
        resetDisconnectTimer();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDisconnectTimer();
    }

    @Override
    public void handleDialogCloseImage(String image) {

    }

    private void checkDepartBtn() {
        for (ConceptBooking job : sortedJobsList) {
            if (job.getConceptBookingStatus() == 2 || job.getConceptBookingStatus() == 9) {
                isDepart = true;
            }
        }
        if (isDepart) {
            btnDepart.setVisibility(View.VISIBLE);
            isDepart = false;
        } else {
            btnDepart.setVisibility(View.GONE);
        }
    }

    private void btnDepartClicked() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        FragmentManager fm = getFragmentManager();
        DepartFragment departFragment = new DepartFragment(this, params, type, isConcept);
        departFragment.setListener(this);
        departFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar);
        departFragment.show(fm, "depart_fragment");
    }

    private void uploadDatatoServer() {
        List<ConceptBookingLog> conceptLogs = db.getAllLogs(true);
        List<ConceptBookingLog> adhocLogs = db.getAllLogs(false);
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
        for (DriverStatus status : statuses) {
            RequestParams params = new RequestParams();
            params.add("date", status.getDriverStatusDate());
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
        for(int i=0; i<2; i++) {
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
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file:///" + FILE_PATH + "" + jobStatusEight.getConceptPickupSignature() + ""));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap != null) {
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
                        String encoded = getStringImage(bitmap);
                        RequestParams paramsImg = new RequestParams();
                        paramsImg.add("image", encoded);
                        paramsImg.add("name", jobStausTen.getConceptDeliverySignature());
                        HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(jobStausTen.getConceptDeliverySignature()), context, "Uploading Image..."));
                    }
                    uploadPhotos(jobStausTen.getDeliveryImages());
                }
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
                RequestParams params = new RequestParams();
                params.add("id", String.valueOf(jobStausYwo.getId()));
                params.add("conceptBookingStatus", String.valueOf(jobStausYwo.getConceptBookingStatus()));
                HTTPHandler.post(phpFile29, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausYwo.getId(), concept), context, "Updating..."));
            }
            for (ConceptBooking jobStausNine : arr9) {
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
                            String encoded = getStringImage(bitmap);
                            RequestParams paramsImg = new RequestParams();
                            paramsImg.add("image", encoded);
                            paramsImg.add("name", jobStausNine.getConceptPickupSignature());
                            HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(jobStausNine.getConceptPickupSignature()), context, "Uploading Image..."));
                        }
                        HTTPHandler.post(phpFile8, params, new HTTPHandler.ResponseManager(new ConceptUploader(jobStausNine.getId(), concept), context, "Updating..."));
                        uploadPhotos(jobStausNine.getPickupImages());
                        jobStausNine.setConceptPickupSignature("uploads/" + jobStausNine.getConceptPickupSignature());
                        db.updateJob(jobStausNine, true);
                    }
                }
            }
        }
        stopDisconnectTimer();
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
                String encoded = getStringPhoto(bitmap);
                RequestParams paramsImg = new RequestParams();
                paramsImg.add("image", encoded);
                paramsImg.add("name", image);
                HTTPHandler.post("cjt-upload-image.php", paramsImg, new HTTPHandler.ResponseManager(new ImageUploader(image), context, "Uploading Image..."));
            }
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

    public class LogUploader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                db.clearTable("conceptBookingLog");
            }
        }
    }

    public class DriverStatusUploader implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                db.clearTable("driverStatus");
            }
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
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                if (jSONObject.getInt("status") == 10) {
                    db.clearConcept(id, concept);
                }
            }
        }
    }

    public class ImageUploader implements ResponseListener {
        String name;

        public ImageUploader(String name) {
            this.name = name;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                File file = new File(FILE_PATH, name);
                file.delete();
            }
        }
    }
}
