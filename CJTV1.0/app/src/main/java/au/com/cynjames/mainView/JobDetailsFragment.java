package au.com.cynjames.mainView;


import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.cynjames.cjtv10.R;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.SQLiteHelper;

public class JobDetailsFragment extends DialogFragment {
    ConceptBooking job;
    Context context;
    WindowManager.LayoutParams params;
    User user;
    SQLiteHelper db;
    JobsDetailsFragmentListener listener;
    TextView pallets;
    TextView parcels;

    public JobDetailsFragment() {

    }

    public JobDetailsFragment(Context context, ConceptBooking job, WindowManager.LayoutParams params) {
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
        rootView = inflater.inflate(R.layout.fragment_job_details, container, false);
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
        TextView suburb = (TextView) viewRef.findViewById(R.id.list_item_suburb);
        TextView orderNo = (TextView) viewRef.findViewById(R.id.list_item_order_no);
        TextView orderNoType = (TextView) viewRef.findViewById(R.id.list_item_order_no_type);
        TextView clientName = (TextView) viewRef.findViewById(R.id.list_item_client_name);
        TextView bookingTime = (TextView) viewRef.findViewById(R.id.list_item_booking_time);
        pallets = (TextView) viewRef.findViewById(R.id.list_item_pallets);
        parcels = (TextView) viewRef.findViewById(R.id.list_item_parcels);
        TextView address = (TextView) viewRef.findViewById(R.id.list_item_address);
        TextView notes = (TextView) viewRef.findViewById(R.id.list_item_notes);
        TextView btnProcess = (TextView) viewRef.findViewById(R.id.list_item_process_button);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnProcessClicked();
            }
        });

        suburb.setText(job.getConceptBookingDeliverySuburb());
        orderNo.setText(job.getOrderno());
        if(job.getConceptBookingTailLift() == 1){
            orderNoType.setText("[TL]");
        }else if(job.getConceptBookingHandUnload() == 1){
            orderNoType.setText("[HU]");
        }else if(job.getConceptBookingUrgent() == 1){
            orderNoType.setText("[U]");
        }

        clientName.setText(job.getClient());
        if(job.getConceptBookingTimeFor() == null){
            bookingTime.setVisibility(View.GONE);
            viewRef.findViewById(R.id.list_item_booking_time_label).setVisibility(View.GONE);
        }
        else{
            bookingTime.setText(job.getConceptBookingTimeFor());
        }
        pallets.setText(String.valueOf(job.getPallets()));
        parcels.setText(String.valueOf(job.getParcels()));
        address.setText(job.getAddress());
        notes.setText(job.getSpecialNotes());

        if(user.getUserArriveConcept() != null){
            if(job.getConceptBookingStatus() == 7) {
                btnProcess.setVisibility(View.VISIBLE);
            }
            else if(job.getConceptBookingStatus() == 2){
                btnProcess.setVisibility(View.VISIBLE);
                btnProcess.setText("Cancel");
            }
        }
    }

    private void btnBackClicked(){
        dismiss();
    }

    public void setListener(JobsDetailsFragmentListener closeListener){
        this.listener = closeListener;
    }

    private void BtnProcessClicked(){
        if(job.getConceptBookingStatus() == 7) {
            job.setConceptBookingStatus(2);
            job.setPallets(Integer.parseInt(pallets.getText().toString()));
            job.setParcels(Integer.parseInt(parcels.getText().toString()));
            job.setConceptBookingPickupDate(GenericMethods.getDisplayDate(new Date()));
        }
        else if(job.getConceptBookingStatus() == 2) {
            job.setConceptBookingStatus(7);
        }
        db.updateJob(job);
        listener.handleDialogClose();
        dismiss();
    }

}
