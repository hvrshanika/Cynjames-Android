package au.com.cynjames.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Vehicles implements Serializable {
    private ArrayList<Vehicle> vehicles;

    public ArrayList<Vehicle> getVehicles() {
        return this.vehicles;
    }

    public Vehicle getAVehicle(String name, String id) {
        return new Vehicle(name, id);
    }

    public class Vehicle {
        private String vehicleId;
        private String vehicleName;

        public Vehicle(String name, String id) {
            this.vehicleName = name;
            this.vehicleId = id;
        }

        public String getVehicleId() {
            return this.vehicleId;
        }

        public String toString() {
            return this.vehicleName;
        }
    }
}
