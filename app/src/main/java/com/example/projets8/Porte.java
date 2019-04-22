package com.example.projets8;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Porte implements Parcelable {

    private String name;
    private Location location;

    protected Porte(Parcel in){
        name = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public Porte(String name) {
        this.name = name;
    }

    public Porte(String name, Location location) {
        this.name = name;
        this.location = location;
    }


    public static final Creator<Porte> CREATOR = new Creator<Porte>() {
        @Override
        public Porte createFromParcel(Parcel in) {
            return new Porte(in);
        }

        @Override
        public Porte[] newArray(int size) {
            return new Porte[size];
        }
    };

    public String getName() {
        return name;
    }

    public Location getLocation() { return location; }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(location, 1);
    }
}
