package au.com.cynjames.models;

import java.io.Serializable;
import au.com.cynjames.models.Vehicles.Vehicle;

public class User implements Serializable {
    private String userFirstName;
    private String userLastName;
    private int userid;
    private String userRole;
    private int driverId;
    private String userArriveConcept;
    private String userArriveClient;
    private Vehicle vehicle;

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getUserFirstName() {
        return this.userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return this.userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getUserArriveConcept() {
        return userArriveConcept;
    }

    public void setUserArriveConcept(String userArriveConcept) {
        this.userArriveConcept = userArriveConcept;
    }

    public String getUserArriveClient() {
        return userArriveClient;
    }

    public void setUserArriveClient(String userArriveClient) {
        this.userArriveClient = userArriveClient;
    }

}
