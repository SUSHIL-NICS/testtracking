package com.example.nics.testtracking;

import java.text.SimpleDateFormat;

/**
 * Created by subrat on 17-07-2017.
 */

public final class Constants {
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    public static final long ACTIVE_UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final long ACTIVE_UPDATE_INTERVALL = 20000;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS =10000;
    public static final float SMALLEST_DISPLACEMENT = 200;
    // A fast frequency ceiling in milliseconds
    public static final long ACTIVE_FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Stores the lat / long pairs in a text file
    public static final String LOCATION_FILE = "sdcard/location.txt";
    // Stores the connect / disconnect data in a text file
    public static final String LOG_FILE = "sdcard/log.txt";
    public static final String LOG_POINTS = "sdcard/logpoints.txt";
    public static final String LOG_DISTANCE_FILE = "sdcard/LOG_DISTANCE.txt";
    public static final String IMAGE_FOLDER = "TEST TRACKING";

    public static final SimpleDateFormat dateFormatForRide = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat dateFormateForShow  = new SimpleDateFormat(" dd-MM-yyyy HH:mm:ss");
    public static final String ADD_RECORD = "0";
    public static final String UPDATE_RECORD ="1";
    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }
}