package com.example.androidrealtimelocation2019;

import android.location.Location;

public class CLocation extends Location {
    private boolean bUseMetricUnis = false;

    public CLocation(Location location){
        this(location,true);
    }

    public CLocation(Location location, boolean bUseMetricUnis){
        super(location);
        this.bUseMetricUnis = bUseMetricUnis ;
    }

    public boolean getUseMetricUnits(){
        return this.bUseMetricUnis;
    }

    public void setUseMericUnits(boolean bUseMetricUnis){
        this.bUseMetricUnis = bUseMetricUnis;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);
        if (!this.getUseMetricUnits()){
            //convert meter
            nDistance = nDistance * 3.28083989501312f;

        }
        return nDistance;
    }

    @Override
    public double getAltitude() {
        double nAltitufe = super.getAltitude();
        if (!this.getUseMetricUnits()){
            //convert meter
            nAltitufe = nAltitufe * 3.28083989501312d;

        }
        return nAltitufe;
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed() * 3.6f;
        if (!this.getUseMetricUnits()){
            //convert meter / s to miels / h
            nSpeed = nSpeed * 2.23693629f;

        }
        return nSpeed;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();
        if (!this.getUseMetricUnits()){
            //convert meter to f
            nAccuracy = nAccuracy * 3.28083989501312f;

        }
        return nAccuracy;
    }
}
