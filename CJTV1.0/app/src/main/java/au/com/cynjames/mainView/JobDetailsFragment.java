package au.com.cynjames.mainView;


import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import au.com.cynjames.CJT;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.User;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.utils.LocationService;
import au.com.cynjames.utils.SQLiteHelper;

public class JobDetailsFragment extends DialogFragment implements JobsDetailsFragmentListener {
    static final int REQUEST_TAKE_PHOTO = 1;
    ConceptBooking job;
    Context context;
    WindowManager.LayoutParams params;
    User user;
    SQLiteHelper db;
    JobsDetailsFragmentListener listener;
    TextView pallets;
    TextView parcels;
    String type;
    ArrayList<String> images;
    String imageFileName;
    boolean rejected = false;
    boolean isConcept;
    TextView eta;
    Location loc;
    String addressStr;

    public JobDetailsFragment() {

    }

    public JobDetailsFragment(Context context, ConceptBooking job, WindowManager.LayoutParams params, String type, boolean isConcept) {
        this.job = job;
        this.context = context;
        this.params = params;
        this.type = type;
        this.isConcept = isConcept;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHelper(context);
        images = new ArrayList<>();
        SharedPreferences mPrefs = context.getSharedPreferences("AppData", 0);
        Gson gson = new Gson();
        String jsonUser = mPrefs.getString("User", "");
        int id = gson.fromJson(jsonUser, Integer.TYPE);
        user = db.getUser(id);
        loadImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((JobsListActivity) getActivity()).stopDisconnectTimer();
        ((CJT) getActivity().getApplication()).stopActivityTransitionTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((JobsListActivity) getActivity()).resetDisconnectTimer();
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

        loc = ((CJT) getActivity().getApplication()).lastLocation;
//        addressStr = "307,Galle Road, Colombo 3";
        HTTPHandler.directionsRequest(loc,addressStr, new RequestParams(), new HTTPHandler.ResponseManager(new ETADownload(), context, "Updating..."));

        return rootView;
    }

    private void setLabels(View viewRef) {
        TextView btnBack = (TextView) viewRef.findViewById(R.id.fragment_job_details_header_back_button);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBackClicked();
            }
        });
        ImageView btnCamera = (ImageView) viewRef.findViewById(R.id.fragment_job_details_header_camera_button);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraBtnClicked();
            }
        });
        ImageView btnDirections = (ImageView) viewRef.findViewById(R.id.fragment_job_details_header_directions_button);
        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGMapsWithPosition();
            }
        });
        TextView btnViewImages = (TextView) viewRef.findViewById(R.id.fragment_job_details_header_images_view_button);
        btnViewImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImagesBtnClicked();
            }
        });
        TextView suburb = (TextView) viewRef.findViewById(R.id.list_item_suburb);
        TextView orderNo = (TextView) viewRef.findViewById(R.id.list_item_order_no);
        TextView orderNoTypeTL = (TextView) viewRef.findViewById(R.id.list_item_order_no_type_tl);
        TextView orderNoTypeHU = (TextView) viewRef.findViewById(R.id.list_item_order_no_type_hu);
        TextView clientName = (TextView) viewRef.findViewById(R.id.list_item_client_name);
        TextView bookingTime = (TextView) viewRef.findViewById(R.id.list_item_booking_time);
        pallets = (TextView) viewRef.findViewById(R.id.list_item_pallets);
        parcels = (TextView) viewRef.findViewById(R.id.list_item_parcels);
        TextView address = (TextView) viewRef.findViewById(R.id.list_item_address);
        TextView notes = (TextView) viewRef.findViewById(R.id.list_item_notes);
        eta = (TextView) viewRef.findViewById(R.id.list_item_eta);
        TextView etaLbl = (TextView) viewRef.findViewById(R.id.list_item_eta_label);
        TextView btnProcess = (TextView) viewRef.findViewById(R.id.list_item_process_button);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnProcessClicked();
            }
        });

        eta.setVisibility(View.VISIBLE);
        etaLbl.setVisibility(View.VISIBLE);

        if(isConcept){
            suburb.setText(job.getConceptBookingDeliverySuburb());
            address.setText(job.getAddress());
            addressStr = job.getAddress() + "," + job.getConceptBookingDeliverySuburb();
        }
        else{
            if (type.equals("JobsPending")){
                suburb.setText(job.getConceptBookingPickupSuburb());
                address.setText(job.getConceptBookingPickupAddress());
                addressStr = job.getConceptBookingPickupAddress() + "," + job.getConceptBookingPickupSuburb();
            }
            else{
                suburb.setText(job.getConceptBookingDeliverySuburb());
                address.setText(job.getAddress());
                addressStr = job.getAddress() + "," + job.getConceptBookingDeliverySuburb();
            }
        }

        orderNo.setText(job.getOrderno());
        if (job.getConceptBookingTailLift() == 1) {
            orderNoTypeTL.setVisibility(View.VISIBLE);
        }
        if (job.getConceptBookingHandUnload() == 1) {
            orderNoTypeHU.setVisibility(View.VISIBLE);
        }
        if (job.getConceptBookingUrgent() == 1) {

        }

        clientName.setText(job.getClient());
        if (job.getConceptBookingTimeFor() == null || job.getConceptBookingTimeFor().equals("")) {
            bookingTime.setVisibility(View.GONE);
            viewRef.findViewById(R.id.list_item_booking_time_label).setVisibility(View.GONE);
        } else {
            bookingTime.setText(job.getConceptBookingTimeFor());
        }
        pallets.setText(String.valueOf(job.getPallets()));
        parcels.setText(String.valueOf(job.getParcels()));

        if (job.getSpecialNotes() == null || job.getSpecialNotes().equals("")) {
            notes.setVisibility(View.GONE);
            viewRef.findViewById(R.id.list_item_notes_label).setVisibility(View.GONE);
        } else {
            notes.setText(job.getSpecialNotes());
        }

        if (type.equals("JobsPending") && user.getUserArriveConcept() != null) {
            if (job.getConceptBookingStatus() == 7) {
                btnProcess.setVisibility(View.VISIBLE);
            } else if (job.getConceptBookingStatus() == 2) {
                btnProcess.setVisibility(View.VISIBLE);
                btnProcess.setText("Cancel");
                btnCamera.setVisibility(View.GONE);
                btnViewImages.setVisibility(View.GONE);
            }
        }
        if (type.equals("DeliveryJobs") && user.getUserArriveClient() != null) {
            if (job.getConceptBookingStatus() == 8) {
                btnProcess.setVisibility(View.VISIBLE);
            } else if (job.getConceptBookingStatus() == 9) {
                btnProcess.setVisibility(View.VISIBLE);
                btnProcess.setText("Cancel");
                btnCamera.setVisibility(View.GONE);
                btnViewImages.setVisibility(View.GONE);
            }
        }
    }

    private void btnBackClicked() {
        listener.handleDialogClose();
        dismiss();
    }

    public void setListener(JobsDetailsFragmentListener closeListener) {
        this.listener = closeListener;
    }

    private void BtnProcessClicked() {
        if (type.equals("JobsPending")) {
            if (job.getConceptBookingStatus() == 7) {
                job.setConceptBookingStatus(2);
                job.setPallets(Integer.parseInt(pallets.getText().toString()));
                job.setParcels(Integer.parseInt(parcels.getText().toString()));
                job.setConceptBookingPickupDate(GenericMethods.getDisplayDate(new Date()));
                job.setPickupImages(createImageString());
            } else if (job.getConceptBookingStatus() == 2) {
                job.setConceptBookingStatus(7);
            }
            db.updateJob(job, isConcept);
            listener.handleDialogClose();
            dismiss();
        }
        if (type.equals("DeliveryJobs")) {
            if (job.getConceptBookingStatus() == 9) {
                job.setConceptBookingStatus(8);
                db.updateJob(job, isConcept);
                listener.handleDialogClose();
                dismiss();
            } else if (job.getConceptBookingStatus() == 8) {
                showProcessDialog().show();
            }
        }

    }

    private AlertDialog showProcessDialog() {
        CharSequence[] items = new CharSequence[3];
        items[0] = "Tail Lift";
        items[1] = "Hand Unloaded";
        items[2] = "Rejected";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(rejected){
                    sendEmail();
                }
                job.setConceptBookingStatus(9);
                job.setPallets(Integer.parseInt(pallets.getText().toString()));
                job.setParcels(Integer.parseInt(parcels.getText().toString()));
                job.setConceptDeliveryDate(GenericMethods.getDisplayDate(new Date()));
                job.setDeliveryImages(createImageString());
                db.updateJob(job, isConcept);
                listener.handleDialogClose();
                dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    switch (which) {
                        case 0:
                            job.setConceptBookingTailLift(1);
                            break;
                        case 1:
                            job.setConceptBookingHandUnload(1);
                            break;
                        case 2:
                            rejected = true;
                            break;
                    }
                }
                else{
                    switch (which) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            rejected = false;
                            break;
                    }
                }
            }
        });

        return builder.create();
    }

    private void sendEmail(){
        String subject = "Booking " + job.getId() + " has been rejected";
        String content = job.getId() + " has been rejected by client " + job.getClient() + ".";
        RequestParams params = new RequestParams();
        params.add("subject", subject);
        params.add("content", content);
        HTTPHandler.post("cjt-send-email.php", params, new HTTPHandler.ResponseManager(new EmailSender(), context, "Sending Email..."));
    }

    public void getEmailIntent() {
        String subject = "Booking " + job.getId() + " has been rejected";
        String content = job.getId() + " has been rejected by client [Client Name]";

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{
                "jlenette@gmail.com"
        });
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, content);

        startActivity(createEmailOnlyChooserIntent(i, "Send via email"));
    }

    public Intent createEmailOnlyChooserIntent(Intent source,
                                               CharSequence chooserTitle) {
        Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@domain.com", null));
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(i, 0);

        for (ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            target.setPackage(ri.activityInfo.packageName);
            intents.add(target);
        }

        if (!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new Parcelable[intents.size()]));

            return chooserIntent;
        } else {
            return Intent.createChooser(source, chooserTitle);
        }
    }

    public class ETADownload implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getString("status").equals("OK")) {
                JSONArray rows = jSONObject.getJSONArray("rows");
                JSONObject elements = rows.getJSONObject(0);
                JSONArray eleArr = elements.getJSONArray("elements");
                JSONObject row = eleArr.getJSONObject(0);
                JSONObject duration = row.getJSONObject("duration");
                String time = duration.getString("text");
                eta.setText(time);
            }
        }
    }

    private void cameraBtnClicked() {
        if (images.size() == 3) {
            GenericMethods.showToast(context, "Maximum of 3 photos are allowed.");
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {

                }
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    private void openGMapsWithPosition() {
        String destinationsF = addressStr.replace(" ", "+");
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+ destinationsF +"&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        else{
            GenericMethods.showToast(context, "No supported application found");
        }
    }

    private File createImageFile() throws IOException {
        long time = System.currentTimeMillis();
        imageFileName = job.getId() + "_" + time + ".jpg";

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "CJT-AppData" + File.separator);
        dir.mkdir();
        File image = new File(dir, imageFileName);
        image.createNewFile();

        return image;
    }

    private void viewImagesBtnClicked() {
        if (images.size() > 0) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.CENTER;
            FragmentManager fm = getFragmentManager();
            ImageFragment imageFragment = new ImageFragment(context, images, params);
            imageFragment.setListener(this);
            imageFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar);
            imageFragment.show(fm, "job_fragment");
        } else {
            GenericMethods.showToast(context, "No Images");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == -1) {
            images.add(imageFileName);
        }
    }


    @Override
    public void handleDialogClose() {

    }

    @Override
    public void handleDialogCloseDepart() {

    }

    @Override
    public void handleDialogCloseImage(String image) {
        images.remove(image);
    }

    private String createImageString() {
        StringBuilder sb = new StringBuilder();
        for (String imageName : images) {
            sb.append(imageName);
            sb.append(",");
        }
        return sb.toString();
    }

    private void loadImages() {
        if (type.equals("DeliveryJobs")) {
            String imageString = job.getDeliveryImages();
            if (imageString != null) {
                String[] imagesArr = imageString.split(",");
                for (String image : imagesArr) {
                    if (!image.equals("")) {
                        images.add(image);
                    }
                }
            }
        }
        if (type.equals("JobsPending")) {
            String imageString = job.getPickupImages();
            if (imageString != null) {
                String[] imagesArr = imageString.split(",");
                for (String image : imagesArr) {
                    if (!image.equals("")) {
                        images.add(image);
                    }
                }
            }
        }
    }

    public class EmailSender implements ResponseListener {
        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            Log.d("Response", jSONObject.toString());
            if (jSONObject.getInt("success") == 1) {
                GenericMethods.showToast(context, "Email Sent.");
            }
        }
    }
}
