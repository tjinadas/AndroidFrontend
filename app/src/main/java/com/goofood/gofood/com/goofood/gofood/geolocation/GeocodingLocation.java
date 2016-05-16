package com.goofood.gofood.com.goofood.gofood.geolocation;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Thanuj on 5/15/2016.
 */
public class GeocodingLocation {

    private static final String TAG = "GeocodingLocation";
    private double latitude;
    private double longitude;
    private String Address;
    private Context Context;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public android.content.Context getContext() {
        return Context;
    }

    public void setContext(android.content.Context context) {
        Context = context;
    }

    public GeocodingLocation () {

                Geocoder geocoder = new Geocoder(Context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocationName(Address, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        latitude = address.getLatitude();
                        longitude = address.getLatitude();

                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to connect to Geocoder", e);
                }
            }








}
