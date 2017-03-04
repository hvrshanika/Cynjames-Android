package au.com.cynjames.mainView;


import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import au.com.cynjames.CJT;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ConceptBookingLog;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.SQLiteHelper;

public class DepartFragment extends DialogFragment {
    Context context;
    WindowManager.LayoutParams params;
    User user;
    SQLiteHelper db;
    EditText name;
    JobsDetailsFragmentListener listener;
    GestureOverlayView sign;
    String signtureId;
    String type;
    boolean isConcept;

    public DepartFragment() {

    }

    public DepartFragment(Context context, WindowManager.LayoutParams params, String type, boolean isConcept) {
        this.context = context;
        this.params = params;
        this.type = type;
        this.isConcept = isConcept;

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
    public void onDestroy() {
        super.onDestroy();
        if(getActivity().getClass() == JobsListActivity.class){
            ((JobsListActivity) getActivity()).resetDisconnectTimer();
        }
        else{
            ((MainActivity) getActivity()).resetDisconnectTimer();
        }
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

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity().getClass() == JobsListActivity.class){
            ((JobsListActivity) getActivity()).stopDisconnectTimer();
        }
        else{
            ((MainActivity) getActivity()).stopDisconnectTimer();
        }
        ((CJT) getActivity().getApplication()).stopActivityTransitionTimer();
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
        sign = (GestureOverlayView) viewRef.findViewById(R.id.list_item_sign);
        sign.setDrawingCacheEnabled(true);
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
        TextView arriveConceptLabel = (TextView) viewRef.findViewById(R.id.list_item_arrive_concept_label);
        TextView departConceptLabel = (TextView) viewRef.findViewById(R.id.list_item_depart_concept_label);
        if(type.equals("JobsPending")){
            arriveConceptLabel.setText("Arrive Concept");
            departConceptLabel.setText("Depart Concept");
            arriveConcept.setText(user.getUserArriveConcept());
        }else if(type.equals("DeliveryJobs")){
            arriveConceptLabel.setText("Arrive Client");
            departConceptLabel.setText("Depart Client");
            arriveConcept.setText(user.getUserArriveClient());
        }
        departConcept.setText(GenericMethods.getDisplayDate(new Date()));
    }

    private void btnBackClicked(){
        if(user.getUserArriveClient() != null){
            if(type.equals("JobsPending")) {
                GenericMethods.showToast(context, "Have not departed client premises yet");
            }else if(type.equals("DeliveryJobs")){{
                GenericMethods.showToast(context, "Have not departed concept premises yet");
            }}
        }
        listener.handleDialogClose();
        dismiss();
    }

    private void BtnProcessClicked(){
        if (name.getText().toString().isEmpty()) {
            GenericMethods.showToast(context, "Please enter Name");
        }
        else if (!(sign.isGestureVisible())) {
            GenericMethods.showToast(context, "Please input Signature");
        }
        else {
            saveSig();
            if(type.equals("JobsPending")) {
                List<ConceptBooking> jobs = db.getPendingJobsWithStatus("2", isConcept);
                for (ConceptBooking job : jobs) {
                    ConceptBookingLog log = new ConceptBookingLog();
                    log.setConceptBookingLog_bookingId(job.getId());
                    log.setConceptBookingLogOrderNo(job.getOrderno());
                    log.setConceptBookingLogBarcode("-");
                    log.setConceptBookingLogUserId(user.getDriverId());
                    log.setConceptBookingLogComments(user.getUserFirstName() + " has departed concept");
                    log.setConceptBookingLogDate(GenericMethods.getDisplayDate(new Date()));
                    log.setConceptBookingLogStatus(8);
                    log.setHasDeparted(1);

                    db.addLog(log, isConcept);

                    job.setConceptPickupSignature(signtureId);
                    job.setConceptBookingPickupDate(GenericMethods.getDisplayDate(new Date()));
                    job.setConceptPickupName(name.getText().toString());
                    job.setConceptBookingStatus(8);
                    job.setArrivedConcept(user.getUserArriveConcept());
                    db.updateJob(job, isConcept);
                }
                user.setUserArriveConcept(null);
            }else if(type.equals("DeliveryJobs")){
                List<ConceptBooking> jobs = db.getPendingJobsWithStatus("9", isConcept);
                for (ConceptBooking job : jobs) {
                    ConceptBookingLog log = new ConceptBookingLog();
                    log.setConceptBookingLog_bookingId(job.getId());
                    log.setConceptBookingLogOrderNo(job.getOrderno());
                    log.setConceptBookingLogBarcode("-");
                    log.setConceptBookingLogUserId(user.getDriverId());
                    log.setConceptBookingLogComments(user.getUserFirstName() + "has departed client");
                    log.setConceptBookingLogDate(GenericMethods.getDisplayDate(new Date()));
                    log.setConceptBookingLogStatus(10);
                    log.setHasDeparted(1);

                    db.addLog(log, isConcept);

                    job.setConceptDeliverySignature(signtureId);
                    job.setConceptDeliveryDate(GenericMethods.getDisplayDate(new Date()));
                    job.setConceptDeliveryName(name.getText().toString());
                    job.setConceptBookingStatus(10);
                    job.setArrivedClient(user.getUserArriveClient());
                    db.updateJob(job, isConcept);
                }
                user.setUserArriveClient(null);
            }
            db.updateUser(user);
            listener.handleDialogCloseDepart();
            if(!isConcept){
                dismiss();
            }
        }

    }

    public void setListener(JobsDetailsFragmentListener closeListener){
        this.listener = closeListener;
    }

    private void btnClearClicked(){
        name.setText("");
        sign.clear(false);
        sign.cancelClearAnimation();
    }

    public void saveSig() {
        long time= System.currentTimeMillis();
        try {
            Bitmap bm = Bitmap.createBitmap(sign.getDrawingCache());
            signtureId = "signature"+time+".png";
            File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "CJT-AppData" + File.separator);
            dir.mkdir();
            File f = new File(dir, signtureId);
            f.createNewFile();
            FileOutputStream os = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (Exception e) {
            Log.v("Gestures", e.getMessage());
            e.printStackTrace();
        }
    }

}
