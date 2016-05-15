package au.com.cynjames.models;

import java.io.Serializable;

/**
 * Created by eleos on 5/14/2016.
 */
public class DriverStatus implements Serializable {
    private String driverStatusDate;
    private String driverStatusTime;
    private String driverStatusDescription;

    public String getDriverStatusDate() {
        return driverStatusDate;
    }

    public void setDriverStatusDate(String driverStatusDate) {
        this.driverStatusDate = driverStatusDate;
    }

    public String getDriverStatusTime() {
        return driverStatusTime;
    }

    public void setDriverStatusTime(String driverStatusTime) {
        this.driverStatusTime = driverStatusTime;
    }

    public String getDriverStatusDescription() {
        return driverStatusDescription;
    }

    public void setDriverStatusDescription(String driverStatusDescription) {
        this.driverStatusDescription = driverStatusDescription;
    }

    public String getDriverStatusLongitude() {
        return driverStatusLongitude;
    }

    public void setDriverStatusLongitude(String driverStatusLongitude) {
        this.driverStatusLongitude = driverStatusLongitude;
    }

    public String getDriverStatusLatitude() {
        return driverStatusLatitude;
    }

    public void setDriverStatusLatitude(String driverStatusLatitude) {
        this.driverStatusLatitude = driverStatusLatitude;
    }

    public int getDriverStatus_driverId() {
        return driverStatus_driverId;
    }

    public void setDriverStatus_driverId(int driverStatus_driverId) {
        this.driverStatus_driverId = driverStatus_driverId;
    }

    public int getDriverStatus_vehicleId() {
        return driverStatus_vehicleId;
    }

    public void setDriverStatus_vehicleId(int driverStatus_vehicleId) {
        this.driverStatus_vehicleId = driverStatus_vehicleId;
    }

    private String driverStatusLongitude;
    private String driverStatusLatitude;
    private int driverStatus_driverId;
    private int driverStatus_vehicleId;

}
