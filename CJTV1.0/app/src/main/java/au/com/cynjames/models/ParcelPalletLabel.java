package au.com.cynjames.models;

import java.io.Serializable;

/**
 * Created by rajithvithanage on 5/1/18.
 */

public class ParcelPalletLabel implements Serializable {
    public int getLabelId() {
        return labelId;
    }

    public void setLabelId(int labelId) {
        this.labelId = labelId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public boolean isSacanned() {
        return isSacanned;
    }

    public void setSacanned(boolean sacanned) {
        isSacanned = sacanned;
    }

    private int labelId;
    private String barcode;
    private int bookingId;
    private boolean isSacanned;
}
