package au.com.cynjames.mainView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import au.com.cynjames.cjtv20.R;
import au.com.cynjames.models.AdhocDimensions;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.utils.SQLiteHelper;

/**
 * Created by eleos on 5/25/2016.
 */
public class ListArrayAdapter extends ArrayAdapter<ConceptBooking> {
    private final Context context;
    private final List<ConceptBooking> jobsList;
    private final boolean isConcept;


    public ListArrayAdapter(Context context, List<ConceptBooking> jobsList, boolean isConcept) {
        super(context, -1, jobsList);
        this.context = context;
        this.jobsList = jobsList;
        this.isConcept = isConcept;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.jobs_list_item, parent, false);
        ConceptBooking job = jobsList.get(position);

        TextView orderNoLbl = (TextView) rowView.findViewById(R.id.list_item_order_no_label);
        TextView suburb = (TextView) rowView.findViewById(R.id.list_item_suburb);
        TextView orderNo = (TextView) rowView.findViewById(R.id.list_item_order_no);
        TextView orderNoType = (TextView) rowView.findViewById(R.id.list_item_order_no_type);
        TextView clientName = (TextView) rowView.findViewById(R.id.list_item_client_name);
        TextView clientNameLbl = (TextView) rowView.findViewById(R.id.list_item_client_label);
        TextView customerName = (TextView) rowView.findViewById(R.id.list_item_customer_name);
        TextView customerNameLbl = (TextView) rowView.findViewById(R.id.list_item_customer_label);
        TextView bookingTime = (TextView) rowView.findViewById(R.id.list_item_booking_time);
        TextView pallets = (TextView) rowView.findViewById(R.id.list_item_pallets);
        TextView palletsLbl = (TextView) rowView.findViewById(R.id.list_item_pallets_label);
        TextView parcels = (TextView) rowView.findViewById(R.id.list_item_parcels);
        TextView parcelsLbl = (TextView) rowView.findViewById(R.id.list_item_parcels_label);
        TextView type = (TextView) rowView.findViewById(R.id.list_item_type);
        TextView jobTypeMain = (TextView) rowView.findViewById(R.id.list_item_job_type_label);

        if (isConcept) {
            orderNo.setText(job.getOrderno());
            if (job.getConceptBookingStatus() == 7 || job.getConceptBookingStatus() == 8) {
                rowView.setBackgroundColor(context.getResources().getColor(R.color.list_red));
            } else if (job.getConceptBookingStatus() == 2 || job.getConceptBookingStatus() == 9) {
                rowView.setBackgroundColor(context.getResources().getColor(R.color.list_green));
            }

            jobTypeMain.setVisibility(View.GONE);
            suburb.setText(job.getConceptBookingDeliverySuburb());
            clientName.setText(job.getClient());
            pallets.setText(String.valueOf(job.getPallets()));
            parcels.setText(String.valueOf(job.getParcels()));
        } else {
            orderNoLbl.setText("Job No:");
            orderNo.setText(job.getJobno());

            if (job.getConceptBookingStatus() == 7) {
                jobTypeMain.setText("Pick up Job");
                rowView.setBackgroundColor(context.getResources().getColor(R.color.list_red));
            }
            else if (job.getConceptBookingStatus() == 8){
                jobTypeMain.setText("Ready for Delivery");
                rowView.setBackgroundColor(context.getResources().getColor(R.color.list_green));
            }
            else if (job.getConceptBookingStatus() == 2) {
                jobTypeMain.setText("Pick up in Progress");
                rowView.setBackgroundColor(context.getResources().getColor(R.color.orange));
            }
            else if (job.getConceptBookingStatus() == 9) {
                jobTypeMain.setText("Delivery in Progress");
                rowView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            }

            // Asked to remove
            customerName.setVisibility(View.GONE);
            customerNameLbl.setVisibility(View.GONE);
            type.setVisibility(View.VISIBLE);
            customerName.setText(job.getConceptClientsName());
            if (job.getConceptBookingStatus() == 7 || job.getConceptBookingStatus() == 2) {
                suburb.setText(job.getConceptBookingPickupSuburb());
                clientName.setText(job.getConceptBookingPickupClientName());
                clientNameLbl.setText("Pickup Client Name:");
            } else if (job.getConceptBookingStatus() == 8 || job.getConceptBookingStatus() == 9) {
                suburb.setText(job.getConceptBookingDeliverySuburb());
                clientName.setText(job.getClient());
                clientNameLbl.setText("Delivery Client Name:");
            }

            String jobType = "";
            String customerType = "";
            switch (job.getCustomerType()) {
                case 1:
                    customerType = "VIP";
                    break;
                case 2:
                    customerType = "Standard";
                    break;
                case 3:
                    customerType = "Next Day";
                    break;
            }

            List<AdhocDimensions> adhocDimensions = (new SQLiteHelper(context)).getDimenForJob(job.getId());
            int totQty = 0;
            for(AdhocDimensions dimen : adhocDimensions){
                totQty += dimen.getQty();
            }

            pallets.setText(String.valueOf(job.getNo_pallets()));
            parcels.setText(String.valueOf(job.getNo_parcels()));

            if (job.getPallets() == 1 || job.getParcels() == 0) {
                jobType = "Pallets";
                parcels.setVisibility(View.GONE);
                parcelsLbl.setVisibility(View.GONE);
            }
            if (job.getParcels() == 2 || job.getPallets() == 0) {
                jobType = "Parcels";
                pallets.setVisibility(View.INVISIBLE);
                palletsLbl.setVisibility(View.INVISIBLE);
            }
            if (job.getPallets() == 0 && job.getParcels() == 0) {
                jobType = "Tonnage";
                pallets.setVisibility(View.VISIBLE);
                palletsLbl.setVisibility(View.VISIBLE);
                pallets.setText(""+totQty);
                parcels.setVisibility(View.GONE);
                parcelsLbl.setVisibility(View.GONE);
            }

            String typeString = customerType + "|" + jobType;
            type.setText(typeString);
        }

        if (job.getConceptBookingTailLift() == 1) {
            orderNoType.setText("[TL]");
            if (job.getConceptBookingHandUnload() == 1) {
                orderNoType.setText("[TL][HU]");
                if (job.getConceptBookingUrgent() == 1) {
                    orderNoType.setText("[TL][HU][U]");
                }
            }
            else if (job.getConceptBookingUrgent() == 1) {
                orderNoType.setText("[TL][U]");
            }
        }else if (job.getConceptBookingHandUnload() == 1) {
            orderNoType.setText("[HU]");
            if (job.getConceptBookingUrgent() == 1) {
                orderNoType.setText("[HU][U]");
            }
        }else if (job.getConceptBookingUrgent() == 1) {
            orderNoType.setText("[U]");
        }

        if (job.getConceptBookingTimeFor() == null || job.getConceptBookingTimeFor().equals("")) {
            bookingTime.setVisibility(View.GONE);
            rowView.findViewById(R.id.list_item_booking_time_label).setVisibility(View.GONE);
            rowView.findViewById(R.id.list_item_timefor_line).setVisibility(View.GONE);
        } else {
            bookingTime.setText(job.getConceptBookingTimeFor());
        }

        return rowView;
    }
}
