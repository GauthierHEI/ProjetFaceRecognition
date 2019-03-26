package com.example.projets8;

import java.util.ArrayList;

public class Porte {

    private String name;
    private double latitude;
    private double longitude;

    public Porte(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
