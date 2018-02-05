package com.example.nics.testtracking.Util;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.nics.testtracking.Constants;
import com.example.nics.testtracking.LogFile;
import com.example.nics.testtracking.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

/**
 * Created by subrat on 14-07-2017.
 */

public class BackgroundLocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



     private PreferenceHelper preferenceHelper;
     LocationRequest mLocationRequest;
     GoogleApiClient mGoogleApiClient;
     Location mCurrentLocation;
    static LocationDto locationDto;
     static double distance = 0;
     public static double latitude=0.0;
     public static double longitude=0.0;
     public  String  destinationlat;
     public String destinationlong;
     double speed;
    private Timer myTimer;
    private Timer showTimer;
    public ArrayList<LatLng> points;
    public Polyline line;
    private final IBinder mBinder = new LocalBinder();


     @Nullable
     @Override
     public IBinder onBind(Intent intent) {
          createLocationRequest();
          mGoogleApiClient = new GoogleApiClient.Builder(this)
                  .addApi(LocationServices.API)
                  .addConnectionCallbacks(this)
                  .addOnConnectionFailedListener(this)
                  .build();
          mGoogleApiClient.connect();
          return mBinder;
     }

     protected void createLocationRequest() {
          preferenceHelper = new PreferenceHelper(BackgroundLocationService.this);
         points=new ArrayList<LatLng>();
         locationDto=new LocationDto();
          mLocationRequest = new LocationRequest();
          mLocationRequest.setInterval(Constants.ACTIVE_UPDATE_INTERVALL);
          mLocationRequest.setFastestInterval(Constants.ACTIVE_FASTEST_INTERVAL);
          mLocationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT);
          mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
     }
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {

          return super.onStartCommand(intent, flags, startId);
     }
     @Override
     public void onConnected(Bundle bundle) {
          try {
               LocationServices.FusedLocationApi.requestLocationUpdates(
                       mGoogleApiClient, mLocationRequest, this);
          } catch (SecurityException e) {
          }
     }

     protected void stopLocationUpdates() {
          LocationServices.FusedLocationApi.removeLocationUpdates(
                  mGoogleApiClient, this);

     }

     @Override
     public void onConnectionSuspended(int i) {

     }
     @Override
     public void onLocationChanged(Location location) {
          mCurrentLocation = location;
          //Calling the method below updates the  live values of distance and speed to the TextViews.
          updateUI(mCurrentLocation);
         //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
         speed = mCurrentLocation.getSpeed() * 18 / 5;
        // redrawLine();
     }

     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {

     }

    public class LocalBinder extends Binder {

          public BackgroundLocationService getService() {
               return BackgroundLocationService.this;
          }


     }

     //The live feed of Distance and Speed are being set in the method below .
     private void updateUI(Location mCurrentLocation) {
          latitude = mCurrentLocation.getLatitude();
          longitude = mCurrentLocation.getLongitude();
          String msg = Double.toString(latitude) + "," + Double.toString(longitude);
         LatLng latLng=new LatLng(latitude,longitude);
         Log.d("debug", msg);
          if (speed >= 3.0) {
               Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
               LogFile.RecordLogFile("location changed Background services"+": "+msg+" time: "+getTime(),Constants.LOG_FILE);
               LogFile.RecordLogFile("coordList.add(new LatLng( "+ msg +"));",Constants.LOG_POINTS);
               preferenceHelper.saveValueToSharedPrefs("lat",""+ latitude);
               preferenceHelper.saveValueToSharedPrefs("long",""+ longitude);
               points.add(latLng);
               double speedD= Double.parseDouble(new DecimalFormat("#.##").format(speed));
               preferenceHelper.saveFloatValueToSharedPrefs("speed",speedD);
                }  else {
               double speedD = Double.parseDouble(new DecimalFormat("#.##").format(speed));
               preferenceHelper.saveFloatValueToSharedPrefs("speed", speedD);
               Toast.makeText(this.getApplicationContext(), "Current speed: " + speedD, Toast.LENGTH_SHORT).show();
             //LogFile.RecordLogFile("start coordList.add(new LatLng( "+ points + "));", Constants.LOG_POINTS);
          }
         redrawLine();
         destinationlat=preferenceHelper.getValueFromSharedPrefs("lat");
         destinationlong=preferenceHelper.getValueFromSharedPrefs("long");
     }


     public String getTime() {
          SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
          return mDateFormat.format(new Date());
     }

     @Override
     public boolean onUnbind(Intent intent) {
          stopLocationUpdates();
          if (mGoogleApiClient.isConnected())
               mGoogleApiClient.disconnect();
          return super.onUnbind(intent);
     }

  /*
* new methods for polyline
**/
    public void redrawLine(){
       // LogFile.RecordLogFile("coordList.add(new LatLng( "+ points + "));", Constants.LOG_POINTS);
         MainActivity.googleMap.clear(); //clears all Markers and Polylines
        PolylineOptions options = new PolylineOptions().width(2).color(Color.GREEN).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        line = MainActivity.googleMap.addPolyline(options); //add Polyline
    }
}