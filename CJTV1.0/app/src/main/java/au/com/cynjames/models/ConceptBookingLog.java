package au.com.cynjames.models;

/**
 * Created by eleos on 5/26/2016.
 */
public class ConceptBookingLog {

    private int conceptBookingLog_bookingId;
    private String conceptBookingLogOrderNo;
    private String conceptBookingLogBarcode;
    private int conceptBookingLogUserId;
    private String conceptBookingLogComments;
    private String conceptBookingLogDate;
    private int conceptBookingLogStatus;
    private int hasDeparted;

    public int getHasDeparted() {
        return hasDeparted;
    }

    public void setHasDeparted(int hasDeparted) {
        this.hasDeparted = hasDeparted;
    }

    public int getConceptBookingLog_bookingId() {
        return conceptBookingLog_bookingId;
    }

    public void setConceptBookingLog_bookingId(int conceptBookingLog_bookingId) {
        this.conceptBookingLog_bookingId = conceptBookingLog_bookingId;
    }

    public String getConceptBookingLogOrderNo() {
        return conceptBookingLogOrderNo;
    }

    public void setConceptBookingLogOrderNo(String conceptBookingLogOrderNo) {
        this.conceptBookingLogOrderNo = conceptBookingLogOrderNo;
    }

    public String getConceptBookingLogBarcode() {
        return conceptBookingLogBarcode;
    }

    public void setConceptBookingLogBarcode(String conceptBookingLogBarcode) {
        this.conceptBookingLogBarcode = conceptBookingLogBarcode;
    }

    public int getConceptBookingLogUserId() {
        return conceptBookingLogUserId;
    }

    public void setConceptBookingLogUserId(int conceptBookingLogUserId) {
        this.conceptBookingLogUserId = conceptBookingLogUserId;
    }

    public String getConceptBookingLogComments() {
        return conceptBookingLogComments;
    }

    public void setConceptBookingLogComments(String conceptBookingLogComments) {
        this.conceptBookingLogComments = conceptBookingLogComments;
    }

    public String getConceptBookingLogDate() {
        return conceptBookingLogDate;
    }

    public void setConceptBookingLogDate(String conceptBookingLogDate) {
        this.conceptBookingLogDate = conceptBookingLogDate;
    }

    public int getConceptBookingLogStatus() {
        return conceptBookingLogStatus;
    }

    public void setConceptBookingLogStatus(int conceptBookingLogStatus) {
        this.conceptBookingLogStatus = conceptBookingLogStatus;
    }

}
