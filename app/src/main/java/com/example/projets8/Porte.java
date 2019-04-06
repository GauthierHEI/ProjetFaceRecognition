package com.example.projets8;

import android.location.Location;

public class Porte {

    private String name;
    private Location location;

    public Porte(String name) {
        this.name = name;
    }

    public Porte(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() { return location; }
}
