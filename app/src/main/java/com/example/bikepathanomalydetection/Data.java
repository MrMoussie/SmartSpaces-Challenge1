package com.example.bikepathanomalydetection;

import java.util.ArrayList;

public class Data {

    private final double longitude;
    private final double latitude;
    private final ArrayList<Float> acc;
    private final ArrayList<Float> gyro;

    public Data (double longitude, double latitude, ArrayList<Float> acc, ArrayList<Float> gyro) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.acc = acc;
        this.gyro = gyro;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public ArrayList<Float> getAcc() {
        return acc;
    }

    public ArrayList<Float> getGyro() {
        return gyro;
    }
}
