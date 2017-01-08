package au.com.cynjames.mainView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import au.com.cynjames.cjtv10.R;
import au.com.cynjames.models.ConceptBooking;

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
        if(job.getConceptBookingStatus() == 7 || job.getConceptBookingStatus() == 8 ){
            rowView.setBackgroundColor(context.getResources().getColor(R.color.list_red));
        }else if(job.getConceptBookingStatus() == 2 || job.getConceptBookingStatus() == 9 ) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.list_green));
        }

        TextView suburb = (TextView) rowView.findViewById(R.id.list_item_suburb);
        TextView orderNo = (TextView) rowView.findViewById(R.id.list_item_order_no);
        TextView orderNoType = (TextView) rowView.findViewById(R.id.list_item_order_no_type);
        TextView clientName = (TextView) rowView.findViewById(R.id.list_item_client_name);
        TextView bookingTime = (TextView) rowView.findViewById(R.id.list_item_booking_time);
        TextView pallets = (TextView) rowView.findViewById(R.id.list_item_pallets);
        TextView parcels = (TextView) rowView.findViewById(R.id.list_item_parcels);

        if(isConcept){
            suburb.setText(job.getConceptBookingDeliverySuburb());
        }
        else{
            if(job.getConceptBookingStatus() == 7 || job.getConceptBookingStatus() == 2){
                suburb.setText(job.getConceptBookingPickupSuburb());
            }
            else if(job.getConceptBookingStatus() == 8 || job.getConceptBookingStatus() == 9){
                suburb.setText(job.getConceptBookingDeliverySuburb());
            }
        }

        orderNo.setText(job.getOrderno());
        if(job.getConceptBookingTailLift() == 1){
            orderNoType.setText("[TL]");
        }else if(job.getConceptBookingHandUnload() == 1){
            orderNoType.setText("[HU]");
        }else if(job.getConceptBookingUrgent() == 1){
            orderNoType.setText("[U]");
        }

        clientName.setText(job.getClient());
        if(job.getConceptBookingTimeFor() == null || job.getConceptBookingTimeFor().equals("")){
            bookingTime.setVisibility(View.GONE);
            rowView.findViewById(R.id.list_item_booking_time_label).setVisibility(View.GONE);
            rowView.findViewById(R.id.list_item_timefor_line).setVisibility(View.GONE);
        }
        else{
            bookingTime.setText(job.getConceptBookingTimeFor());
        }
        pallets.setText(String.valueOf(job.getPallets()));
        parcels.setText(String.valueOf(job.getParcels()));

        return rowView;
    }
}
