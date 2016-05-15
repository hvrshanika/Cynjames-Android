package au.com.cynjames.models;

import java.io.Serializable;

/**
 * Created by eleos on 5/14/2016.
 */
public class ConceptBooking implements Serializable {
    private int id;
    private String orderno;
    private String barcode;
    private String conceptBookingDeliverySuburb;
    private String client;
    private String address;
    private String specialNotes;
    private String date;
    private String conceptBookingTimeFor;
    private int pallets;
    private int parcels;
    private int conceptBookingStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getConceptBookingDeliverySuburb() {
        return conceptBookingDeliverySuburb;
    }

    public void setConceptBookingDeliverySuburb(String conceptBookingDeliverySuburb) {
        this.conceptBookingDeliverySuburb = conceptBookingDeliverySuburb;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getConceptBookingTimeFor() {
        return conceptBookingTimeFor;
    }

    public void setConceptBookingTimeFor(String conceptBookingTimeFor) {
        this.conceptBookingTimeFor = conceptBookingTimeFor;
    }

    public int getPallets() {
        return pallets;
    }

    public void setPallets(int pallets) {
        this.pallets = pallets;
    }

    public int getParcels() {
        return parcels;
    }

    public void setParcels(int parcels) {
        this.parcels = parcels;
    }

    public int getConceptBookingStatus() {
        return conceptBookingStatus;
    }

    public void setConceptBookingStatus(int conceptBookingStatus) {
        this.conceptBookingStatus = conceptBookingStatus;
    }



}
