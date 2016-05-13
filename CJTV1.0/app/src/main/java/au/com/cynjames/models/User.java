package au.com.cynjames.models;

import java.io.Serializable;
import au.com.cynjames.models.Vehicles.Vehicle;

public class User implements Serializable {
    private String userFirstName;
    private String userLastName;
    private String userid;
    private Vehicle vehicle;

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getUserid() {
        return this.userid;
    }

    public String getUserFirstName() {
        return this.userFirstName;
    }

    public String getUserLastName() {
        return this.userLastName;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }
}
