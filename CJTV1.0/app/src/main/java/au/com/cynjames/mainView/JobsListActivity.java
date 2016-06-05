package au.com.cynjames.mainView;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.cynjames.CJT;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.SQLiteHelper;

public class JobsListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, JobsDetailsFragmentListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_list);
        type = getIntent().getExtras().getString("type");
        db = new SQLiteHelper(this);
        sortedJobsList = new ArrayList<>();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#bec2cb")));
        actionBar.setLogo(R.mipmap.logo_red);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        mPrefs = getApplicationContext().getSharedPreferences("AppData", 0);
        gson = new Gson();
        getData();
        getUser();
        if(type.equals("JobsPending")) {
            checkUserArriveConcept();
        }else if(type.equals("DeliveryJobs")){
            checkUserArriveClient();
        }
        adapter = new ListArrayAdapter(this, sortedJobsList);
        init();
        setButtonListeners();
        checkDepartBtn();

    }

    private void init(){
        jobsListView = (ListView) findViewById(R.id.fragment_jobs_list_jobs_list);
        jobsListView.setAdapter(adapter);
        jobsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        jobsListView.setOnItemClickListener(this);
    }

    private void setButtonListeners(){
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
    }

    private void getData(){
        if(type.equals("JobsPending")){
            jobsList = db.getPendingJobs();
        }
        else if(type.equals("DeliveryJobs")){
            jobsList = db.getReadyJobs();
        }
    }

    private void getUser() {
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
    }


    private void backBtnClicked(){
        finish();
    }

    private void checkUserArriveConcept(){
        boolean isStatusSeven = false;
        for(ConceptBooking job : jobsList){
            if(job.getConceptBookingStatus() == 7){
                isStatusSeven = true;
                sortedJobsList.add(job);
            }
        }
        for(ConceptBooking job : jobsList){
            if(job.getConceptBookingStatus() == 2){
                sortedJobsList.add(job);
            }
        }
        if(user.getUserArriveConcept() == null && isStatusSeven && user.getUserArriveClient() == null){
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setMessage("Arrived at Concept?");
            build.setCancelable(false);
            build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    user.setUserArriveConcept(GenericMethods.getDisplayDate(new Date()));
                    db.updateUser(user);
                    dialog.dismiss();
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
    public void onResume()
    {
        super.onResume();
        ProgressDialog progressDialog = GenericMethods.getProgressDialog(this, "Updating...");
        //progressDialog.show();
        ((CJT)this.getApplication()).stopActivityTransitionTimer();
    }

    private void checkUserArriveClient(){
        boolean isStatusEight = false;
        for(ConceptBooking job : jobsList){
            if(job.getConceptBookingStatus() == 8){
                isStatusEight = true;
                sortedJobsList.add(job);
            }
        }
        for(ConceptBooking job : jobsList){
            if(job.getConceptBookingStatus() == 9){
                sortedJobsList.add(job);
            }
        }
        if(user.getUserArriveClient() == null && user.getUserArriveConcept() == null && isStatusEight){
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setMessage("Arrived at Client?");
            build.setCancelable(false);
            build.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    user.setUserArriveClient(GenericMethods.getDisplayDate(new Date()));
                    db.updateUser(user);
                    dialog.dismiss();
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
        JobDetailsFragment jobFragment = new JobDetailsFragment(this, sortedJobsList.get(position), params, type);
        jobFragment.setListener(this);
        jobFragment.setStyle( DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar );
        jobFragment.show(fm, "job_fragment");
    }

    @Override
    public void handleDialogClose() {
        runOnUiThread(new Runnable() {
            public void run() {
                sortedJobsList.clear();
                getData();
                for(ConceptBooking job : jobsList){
                    if(job.getConceptBookingStatus() == 7 || job.getConceptBookingStatus() == 8){
                        sortedJobsList.add(job);
                    }
                }
                for(ConceptBooking job : jobsList){
                    if(job.getConceptBookingStatus() == 2 || job.getConceptBookingStatus() == 9){
                        sortedJobsList.add(job);
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                checkDepartBtn();
            }
        });
    }

    @Override
    public void handleDialogCloseDepart() {
        finish();
    }

    private void checkDepartBtn(){
        for(ConceptBooking job : sortedJobsList){
            if(job.getConceptBookingStatus() == 2 || job.getConceptBookingStatus() == 9){
                isDepart = true;
            }
        }
        if(isDepart){
            btnDepart.setVisibility(View.VISIBLE);
            isDepart = false;
        }
        else{
            btnDepart.setVisibility(View.GONE);
        }
    }

    private void btnDepartClicked(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        FragmentManager fm = getFragmentManager();
        DepartFragment departFragment = new DepartFragment(this, params, type);
        departFragment.setListener(this);
        departFragment.setStyle( DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar );
        departFragment.show(fm, "depart_fragment");
    }
}
