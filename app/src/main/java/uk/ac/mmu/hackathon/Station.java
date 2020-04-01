package uk.ac.mmu.hackathon;

import androidx.annotation.NonNull;

public class Station {

    private String name;
    private double lat, lng, distanceToUser;

    public Station(String name, double lat, double lng){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getDistanceToUser() {
        return distanceToUser;
    }

    public void setDistanceToUser(double distanceToUser) {
        this.distanceToUser = distanceToUser;
    }

    @NonNull
    @Override
    public String toString() {
        return name+" "+distanceToUser;
    }
}
