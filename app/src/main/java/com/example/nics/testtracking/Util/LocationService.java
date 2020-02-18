package com.example.nics.testtracking.Util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.nics.testtracking.Constants;
import com.example.nics.testtracking.Database.MapUpdateDataBase;
import com.example.nics.testtracking.LogFile;
import com.example.nics.testtracking.MainActivity;
import com.example.nics.testtracking.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.PRIORITY_MAX;

/**
 * Created by subrat on 18-07-2017.
 */

public class LocationService extends Service {
    private static final String TAG=LocationService.class.getSimpleName();
    Context context = LocationService.this;
    LocationDto locationDto = new LocationDto();
    //For Timer
    private long startTime;
    private long elapsedTime;
    private long savedTime;
    private final int REFRESH_RATE = 100;
    private MapUpdateDataBase dbHelper;
    private PreferenceHelper preferenceHelper;
    private Timer myTimer;
    private Timer showTimer;
    boolean status=false;

    public LocationService() {

    }

    @Override
    public void onCreate() {
        Log.e("UserDetailsService", "inside inside");
        preferenceHelper = new PreferenceHelper(context);
        dbHelper = new MapUpdateDataBase(context);
        startTime();
        setTimer();
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.i("onStartCommand", "MyService Started");
        // Let it continue running until it is stopped.
        try {
            /*if (intent.getAction().equals("com.logitek.bikerapp.util.action.startforeground")) {
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setAction("com.logitek.bikerapp.util.action.startforeground");
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);*/
                Notification notification = new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_location)
                        .setContentTitle("TEST TRACKING")
                        .setContentText("TEST TRACKING")
                        .setPriority(PRIORITY_MAX).build();
                        //.setContentIntent(pendingIntent).build();
                startForeground(101, notification);
           /* }else if (intent.getAction().equals(
                    "com.logitek.bikerapp.util.action.stopforeground")) {
                Log.i(TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }*/

        }catch (Exception e){
            e.printStackTrace();
            Log.e("Exception", "MyService onStartCommand");
        }
        return START_STICKY;
    }

//     native java Timer
    private void setTimer() {
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                insertRideToLocalDb();
            }
        },0,15000);
    }

    private void insertRideToLocalDb() {
        float speed=preferenceHelper.getFloatValueToSharedPrefs("speed");
        Log.e("latitude", ""+speed);
        if (speed==0.0 && status==false ){
            status=true;
            Log.e("latitude", "" + getLatitude());
            Log.e("longitude", "" + getLongitude());
            locationDto.setLatitude(getLatitude());
            locationDto.setLongitude(getLongitude());
            locationDto.setCumnTime(preferenceHelper.getValueFromSharedPrefs("cummulativeTime"));
            locationDto.setStartDateTime(Constants.dateFormateForShow.format(new Date().getTime()));
            locationDto.setFlag(Constants.ADD_RECORD);
            String msg = locationDto.getLatitude() + "," + locationDto.getLongitude();
            LogFile.RecordLogFile("update data to local db"+": "+msg+" sped: "+speed+" time: "+preferenceHelper.getValueFromSharedPrefs("cummulativeTime"), Constants.LOG_FILE);
            serviceForSaveLocationDetails(locationDto);
        }else {
            if (speed >= 5.0) {
                Log.e("latitude", "" + getLatitude());
                Log.e("longitude", "" + getLongitude());
                locationDto.setLatitude(getLatitude());
                locationDto.setLongitude(getLongitude());
                locationDto.setCumnTime(preferenceHelper.getValueFromSharedPrefs("cummulativeTime"));
                locationDto.setStartDateTime(Constants.dateFormateForShow.format(new Date().getTime()));
                locationDto.setFlag(Constants.ADD_RECORD);
                String msg = locationDto.getLatitude() + "," + locationDto.getLongitude();
                LogFile.RecordLogFile(" update data base  Background service " + ": " + msg + "speed: "+ speed + "time: "  + preferenceHelper.getValueFromSharedPrefs("cummulativeTime"), Constants.LOG_FILE);
                serviceForSaveLocationDetails(locationDto);
            }
        }
    }

    private void serviceForSaveLocationDetails(LocationDto locationDto) {
        dbHelper.insertLocation(locationDto);
    }


    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        return mDateFormat.format(new Date());
    }

    private void startTime() {
        long sTime = SystemClock.uptimeMillis();
        //startTime = Long.parseLong(String.valueOf(sTime)) ;
        startTime=sTime;
        showTimer = new Timer();
        showTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                elapsedTime = System.currentTimeMillis() - startTime;
                long elpTime = savedTime + elapsedTime;
                updateTimer(elpTime);
            }
        },0, REFRESH_RATE);

    }


    private void updateTimer(float time) {
        long secs = (long) (time / 1000);
        long mins = (long) ((time / 1000) / 60);
        long hrs = (long) (((time / 1000) / 60) / 60);

		/* Convert the seconds to String
         * and format to ensure it has
		 * a leading zero when required
		 */
        secs = secs % 60;
        String seconds = String.valueOf(secs);
        if (secs == 0) {
            seconds = "00";
        }
        if (secs < 10 && secs > 0) {
            seconds = "0" + seconds;
        }

		/* Convert the minutes to String and format the String */

        mins = mins % 60;
        String minutes = String.valueOf(mins);
        if (mins == 0) {
            minutes = "00";
        }
        if (mins < 10 && mins > 0) {
            minutes = "0" + minutes;
        }

    	/* Convert the hours to String and format the String */

        String hours = String.valueOf(hrs);
        if (hrs == 0) {
            hours = "00";
        }
        if (hrs < 10 && hrs > 0) {
            hours = "0" + hours;
        }
        preferenceHelper.saveValueToSharedPrefs("hours", hours);
        preferenceHelper.saveValueToSharedPrefs("minutes", minutes);
        preferenceHelper.saveValueToSharedPrefs("seconds", seconds);
        Log.i(TAG+" Hrs", "" + hrs);
        Log.i(TAG+" tMins", "" + mins);
        Log.i(TAG+" Secs", "" + secs);
        long totalTime = (hrs * 3600) + (mins * 60) + secs;
        String cummulativeTime = "" + totalTime;
        preferenceHelper.saveValueToSharedPrefs("cummulativeTime", cummulativeTime);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        myTimer.cancel();
        showTimer.cancel();
        Log.i("onDestroy", "MyService Stopped");
    }


    public String getLatitude() {
        return preferenceHelper.getValueFromSharedPrefs("lat");
    }

    private String getLongitude() {

        return preferenceHelper.getValueFromSharedPrefs("long");
    }
}
