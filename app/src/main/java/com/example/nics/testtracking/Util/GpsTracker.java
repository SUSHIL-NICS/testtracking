package com.example.nics.testtracking.Util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by sushil on 13-07-2017.
 */

public class GpsTracker extends Service implements LocationListener{

    // saving the context for later use
    private final Context mContext;
    //	 if GPS is enabled
    boolean isGPSEnabled = false;
    //boolean NetworkInfo = false;
    //	 if Network is enabled
    boolean isNetworkEnabled = false;
    //	 if Location co-ordinates are available using GPS or Network
    public boolean canGetLocation = false;
    //	 Location and co-ordinates coordinates
    Location mLocation;
    double mLatitude;
    double mLongitude;
  /*  public static String startlat;
    public static String startlong;*/
    //Minimum time fluctuation for next update (in milliseconds)
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 30;
    //Minimum distance fluctuation for next update (in meters)
    private static final long MIN_TIME_BW_UPDATE = 1000 * 60 * 1;
    private PreferenceHelper preferenceHelper;

    //	 Declaring a Location Manager
    protected LocationManager mlocationManager;
    private String result;
   // for ConnectivityManager manager
    private android.support.v7.app.AlertDialog mInternetDialog;
    private static final int WIFI_ENABLE_REQUEST = 0x1006;

    public GpsTracker(Context context) {
        this.mContext = context;
        preferenceHelper=new PreferenceHelper(mContext);
        getLocation(mContext);
        checkInternetConnection();
    }

    /**
     * Returs the Location
     *
     * @return Location or null if no location is found
     * @param mContext
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocation(Context mContext) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this.mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.mContext, android.Manifest.permission.ACCESS_NETWORK_STATE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.mContext, android.Manifest.permission.ACCESS_WIFI_STATE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.mContext, android.Manifest.permission.CHANGE_NETWORK_STATE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.mContext, android.Manifest.permission.BATTERY_STATS ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        try {
            mlocationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
           // manager=(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//			 Getting GPS status
            isGPSEnabled = mlocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

//			 If we are reaching this part, it means GPS was not able to fetch
//		     any location
//			 Getting network status
            isNetworkEnabled = mlocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                this.canGetLocation = false;
            } else {
                if (isNetworkEnabled) {
                    mlocationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                    Log.d("Network", "Network");
                    if (mlocationManager != null) {
                        mLocation = mlocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null) {
                            this.canGetLocation = true;
                            mLatitude = mLocation.getLatitude();
                            mLongitude = mLocation.getLongitude();
                        }
                    }
                }
                // First get location from Network Provider
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (mLocation == null) {
                        mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mlocationManager != null) {
                            mLocation = mlocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
                                this.canGetLocation = true;
                                mLatitude = mLocation.getLatitude();
                                mLongitude = mLocation.getLongitude();
                            }
                        }
                    }
                }
            }
            // If reaching here means, we were not able to get location neither
            // from GPS not Network,
            if (!isGPSEnabled) {
                // so asking user to open GPS
                askUserToOpenGPS();
            }
            preferenceHelper.saveValueToSharedPreference("latitude",""+ mLatitude );
            preferenceHelper.saveValueToSharedPreference("longitude",""+ mLongitude );
            /*startlat = preferenceHelper.getValueFromSharedPreference("latitude");
            startlong = preferenceHelper.getValueFromSharedPreference("longitude");*/

        } catch (Exception e) {

        }

        return mLocation;

    }

    /**
     *
     * show settings to open GPS
     */
    public void askUserToOpenGPS() {
        // TODO Auto-generated method stub
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog Title
               mAlertDialog.setTitle("Location not available, Open GPS?").setCancelable(false)
                       //.setCanceledOnTouchOutside(false)
                .setMessage("Activate GPS to use location services?")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       /*   this is should be work to close an activity but in my case it is showing app crash
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        getApplicationContext().startActivity(intent);
                        ((Activity)getApplicationContext()).finish();*/
                        dialog.dismiss();
                        /*
                        * used to close an activity in side a service class*/
                        ((Activity)mContext).finish();
                    }
                }).show();
    }

    private void checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {

        } else {
            showNoInternetDialog();
        }
    }

    /**
     *
     * show settings to open GPS
     */
    private void showNoInternetDialog() {

        if (mInternetDialog != null && mInternetDialog.isShowing()) {
            return;
        }
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("Internet Disabled!");
        builder.setMessage("No active Internet connection found.");
        builder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent gpsOptionsIntent = new Intent(Settings.ACTION_SETTINGS);
               // startActivityForResult(gpsOptionsIntent, WIFI_ENABLE_REQUEST);
                mContext.startActivity(gpsOptionsIntent);
            }
        }).setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mInternetDialog = builder.create();
        mInternetDialog.show();
    }

    /**
     * get latitude
     *
     * @return latitude in double
     */
    public double getLatitude() {
        if (mLocation != null) {
            mLatitude = mLocation.getLatitude();
        }
        return mLatitude;
    }

    /**
     * get longitude
     *
     * @return longitude in double
     */
    public double getLongitude() {
        if (mLocation != null) {
            mLongitude = mLocation.getLongitude();
        }
        return mLongitude;
    }

    /**
     * close GPS to save battery
     */
    public void closeGPS() {
        if(mlocationManager != null){
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mlocationManager.removeUpdates(GpsTracker.this);
        }
    }

    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    /**
     * Updating the location when location changes
     */
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
//            updateGPSCoordinates(location);
        }
    }

    public void updateGPSCoordinates(Location location) {
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            preferenceHelper.saveValueToSharedPreference("latitude",""+mLatitude);
            preferenceHelper.saveValueToSharedPreference("longitude",""+mLongitude);
//            LogFile.RecordLogFile("Inside gps location change"+"lat :" +mLatitude + " , long :" +mLongitude+ " , time :"+ new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).format(new Date().getTime())+"");

        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public String getLocationAddress(double latitude, double longitude) {
        // TODO Auto-generated method stub
        if (canGetLocation ) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
				/*
				 * Return 1 address.
				 */
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e1) {
                e1.printStackTrace();
//                ("IO Exception trying to get address:" + e1);
                return "errorString";
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments "
                        + Double.toString(mLatitude) + " , "
                        + Double.toString(mLongitude)
                        + " passed to address service";
                e2.printStackTrace();
                return "errorString";
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
							/*
							 * Format the first line of address (if available), city, and
							 * country name.
							 */
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName()).append("\n");
//                sb.append(address.getAdminArea());
                result = sb.toString();
                // Return the text
                return result;
            } else {
                return "No address found by the service: Note to the developers, If no address is found by google itself, there is nothing you can do about it.";
            }
        } else {
            return "Location Not available";
        }


    }



}
