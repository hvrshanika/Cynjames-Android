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

    public double getDriverStatusLongitude() {
        return driverStatusLongitude;
    }

    public void setDriverStatusLongitude(double driverStatusLongitude) {
        this.driverStatusLongitude = driverStatusLongitude;
    }

    public double getDriverStatusLatitude() {
        return driverStatusLatitude;
    }

    public void setDriverStatusLatitude(double driverStatusLatitude) {
        this.driverStatusLatitude = driverStatusLatitude;
    }

    public int getDriverStatus_driverId() {
        return driverStatus_driverId;
    }

    public void setDriverStatus_driverId(int driverStatus_driverId) {
        this.driverStatus_driverId = driverStatus_driverId;
    }

    public String getDriverStatus_vehicleId() {
        return driverStatus_vehicleId;
    }

    public void setDriverStatus_vehicleId(String driverStatus_vehicleId) {
        this.driverStatus_vehicleId = driverStatus_vehicleId;
    }

    private double driverStatusLongitude;
    private double driverStatusLatitude;
    private int driverStatus_driverId;
    private String driverStatus_vehicleId;

}
