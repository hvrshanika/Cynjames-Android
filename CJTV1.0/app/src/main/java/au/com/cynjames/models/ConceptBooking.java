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
    private String conceptBookingPickupAddress;
    private String conceptBookingPickupSuburb;
    private String specialNotes;
    private String date;
    private String conceptBookingTimeFor;
    private int pallets;
    private int parcels;
    private int conceptBookingStatus;
    private int conceptBookingTailLift;
    private String arrivedClient;
    private String conceptDeliveryDate;
    private String conceptPickupSignature;
    private String conceptPickupName;
    private String conceptDeliverySignature;
    private String conceptDeliveryName;
    private String arrivedConcept;
    private String conceptBookingPickupDate;
    private int conceptBookingHandUnload;
    private int conceptBookingUrgent;
    private String deliveryImages;
    private String pickupImages;
    private String conceptClientsName;
    private String conceptBookingPickupClientName;
    private int customerType;
    private int no_pallets;
    private int no_parcels;
    private String jobno;

    public int getCustomerType() {
        return customerType;
    }

    public void setCustomerType(int customerType) {
        this.customerType = customerType;
    }

    public String getConceptClientsName() {
        return conceptClientsName;
    }

    public void setConceptClientsName(String conceptClientsName) {
        this.conceptClientsName = conceptClientsName;
    }

    public String getConceptBookingPickupClientName() {
        return conceptBookingPickupClientName;
    }

    public void setConceptBookingPickupClientName(String conceptBookingPickupClientName) {
        this.conceptBookingPickupClientName = conceptBookingPickupClientName;
    }

    public String getDeliveryImages() {
        return deliveryImages;
    }

    public void setDeliveryImages(String deliveryImages) {
        this.deliveryImages = deliveryImages;
    }

    public String getPickupImages() {
        return pickupImages;
    }

    public void setPickupImages(String pickupImages) {
        this.pickupImages = pickupImages;
    }

    public String getArrivedClient() {
        return arrivedClient;
    }

    public void setArrivedClient(String arrivedClient) {
        this.arrivedClient = arrivedClient;
    }

    public String getConceptDeliveryDate() {
        return conceptDeliveryDate;
    }

    public void setConceptDeliveryDate(String conceptDeliveryDate) {
        this.conceptDeliveryDate = conceptDeliveryDate;
    }

    public String getConceptPickupSignature() {
        return conceptPickupSignature;
    }

    public void setConceptPickupSignature(String conceptPickupSignature) {
        this.conceptPickupSignature = conceptPickupSignature;
    }

    public String getConceptPickupName() {
        return conceptPickupName;
    }

    public void setConceptPickupName(String conceptPickupName) {
        this.conceptPickupName = conceptPickupName;
    }

    public String getArrivedConcept() {
        return arrivedConcept;
    }

    public void setArrivedConcept(String arrivedConcept) {
        this.arrivedConcept = arrivedConcept;
    }

    public String getConceptDeliverySignature() {
        return conceptDeliverySignature;
    }

    public void setConceptDeliverySignature(String conceptDeliverySignature) {
        this.conceptDeliverySignature = conceptDeliverySignature;
    }

    public String getConceptDeliveryName() {
        return conceptDeliveryName;
    }

    public void setConceptDeliveryName(String conceptDeliveryName) {
        this.conceptDeliveryName = conceptDeliveryName;
    }

    public String getConceptBookingPickupDate() {
        return conceptBookingPickupDate;
    }

    public void setConceptBookingPickupDate(String conceptBookingPickupDate) {
        this.conceptBookingPickupDate = conceptBookingPickupDate;
    }

    public int getConceptBookingTailLift() {
        return conceptBookingTailLift;
    }

    public void setConceptBookingTailLift(int conceptBookingTailLift) {
        this.conceptBookingTailLift = conceptBookingTailLift;
    }

    public int getConceptBookingHandUnload() {
        return conceptBookingHandUnload;
    }

    public void setConceptBookingHandUnload(int conceptBookingHandUnload) {
        this.conceptBookingHandUnload = conceptBookingHandUnload;
    }

    public int getConceptBookingUrgent() {
        return conceptBookingUrgent;
    }

    public void setConceptBookingUrgent(int conceptBookingUrgent) {
        this.conceptBookingUrgent = conceptBookingUrgent;
    }

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

    public String getConceptBookingPickupSuburb() {
        return conceptBookingPickupSuburb;
    }

    public void setConceptBookingPickupSuburb(String conceptBookingPickupSuburb) {
        this.conceptBookingPickupSuburb = conceptBookingPickupSuburb;
    }

    public String getConceptBookingPickupAddress() {
        return conceptBookingPickupAddress;
    }

    public void setConceptBookingPickupAddress(String conceptBookingPickupAddress) {
        this.conceptBookingPickupAddress = conceptBookingPickupAddress;
    }

    public int getNo_pallets() {
        return no_pallets;
    }

    public void setNo_pallets(int no_pallets) {
        this.no_pallets = no_pallets;
    }

    public int getNo_parcels() {
        return no_parcels;
    }

    public void setNo_parcels(int no_parcels) {
        this.no_parcels = no_parcels;
    }

    public String getJobno() {
        return jobno;
    }

    public void setJobno(String jobno) {
        this.jobno = jobno;
    }


}
