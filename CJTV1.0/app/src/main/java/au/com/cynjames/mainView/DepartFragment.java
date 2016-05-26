package au.com.cynjames.mainView;


import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

import au.com.cynjames.cjtv10.R;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ConceptBookingLog;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.SQLiteHelper;

public class DepartFragment extends DialogFragment {
    ConceptBooking job;
    Context context;
    WindowManager.LayoutParams params;
    User user;
    SQLiteHelper db;
    EditText name;
    EditText sign;
    JobsDetailsFragmentListener listener;

    public DepartFragment() {

    }

    public DepartFragment(Context context, WindowManager.LayoutParams params) {
        this.job = job;
        this.context = context;
        this.params = params;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHelper(context);
        SharedPreferences mPrefs = context.getSharedPreferences("AppData", 0);
        Gson gson = new Gson();
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = this.params.width;
        params.height = this.params.height;
        params.gravity = this.params.gravity;
        getDialog().getWindow().setAttributes(params);
        getDialog().setCanceledOnTouchOutside(false);
        rootView = inflater.inflate(R.layout.fragment_depart, container, false);
        setLabels(rootView);
        return rootView;
    }

    private void setLabels(View viewRef){
        TextView btnBack = (TextView) viewRef.findViewById(R.id.fragment_job_details_header_back_button);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBackClicked();
            }
        });
        TextView arriveConcept = (TextView) viewRef.findViewById(R.id.list_item_arrive_concept);
        TextView departConcept = (TextView) viewRef.findViewById(R.id.list_item_depart_concept);
        name = (EditText) viewRef.findViewById(R.id.list_item_name);
        sign = (EditText) viewRef.findViewById(R.id.list_item_sign);
        TextView btnProcess = (TextView) viewRef.findViewById(R.id.list_item_process_button);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnProcessClicked();
            }
        });
        TextView btnClear = (TextView) viewRef.findViewById(R.id.list_item_clear_button);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClearClicked();
            }
        });
        arriveConcept.setText(user.getUserArriveConcept());
        departConcept.setText(GenericMethods.getDisplayDate(new Date()));
    }

    private void btnBackClicked(){
        if(user.getUserArriveClient() != null){
            GenericMethods.showToast(context, "Have not departed client premises yet");
        }
        listener.handleDialogClose();
        dismiss();
    }

    private void BtnProcessClicked(){
        if (name.getText().toString().isEmpty()) {
            GenericMethods.showToast(context, "Please enter Name");
        } else if (sign.getText().toString().isEmpty()) {
            GenericMethods.showToast(context, "Please input Signature");
        }
        else {
            List<ConceptBooking> jobs = db.getPendingJobsStatusTwo();
            for (ConceptBooking job : jobs) {
                ConceptBookingLog log = new ConceptBookingLog();
                log.setConceptBookingLog_bookingId(job.getId());
                log.setConceptBookingLogOrderNo(job.getOrderno());
                log.setConceptBookingLogBarcode("");
                log.setConceptBookingLogUserId(user.getDriverId());
                log.setConceptBookingLogComments(user.getUserFirstName() + "has departed concept");
                log.setConceptBookingLogDate(GenericMethods.getDisplayDate(new Date()));
                log.setConceptBookingLogStatus(8);
                log.setHasDeparted(1);

                db.addLog(log);

                job.setConceptPickupSignature(sign.getText().toString());
                job.setConceptBookingPickupDate(GenericMethods.getDisplayDate(new Date()));
                job.setConceptPickupName(name.getText().toString());
                job.setConceptBookingStatus(8);
                job.setArrivedConcept(user.getUserArriveConcept());

                db.updateJob(job);
            }
            user.setUserArriveConcept("");
            db.updateUser(user);
        }

    }

    public void setListener(JobsDetailsFragmentListener closeListener){
        this.listener = closeListener;
    }

    private void btnClearClicked(){
        name.setText("");
        sign.setText("");
    }

}
