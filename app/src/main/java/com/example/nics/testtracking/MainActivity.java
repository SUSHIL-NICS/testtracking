package com.example.nics.testtracking;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import com.example.nics.testtracking.Database.MapUpdateDataBase;
import com.example.nics.testtracking.Util.BackgroundLocationService;
import com.example.nics.testtracking.Util.DirectionFinderListener;
import com.example.nics.testtracking.Util.GpsTracker;
import com.example.nics.testtracking.Util.LocationDto;
import com.example.nics.testtracking.Util.LocationService;
import com.example.nics.testtracking.Util.PreferenceHelper;
import com.example.nics.testtracking.Util.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
//import java.util.Locale;

import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

/**
 * Created by subrat on 13-07-2017.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,DirectionFinderListener
{
    //this variable used for log file
    private static final String TAG = MainActivity.class.getSimpleName();
    private List<Polyline> polylinePaths = new ArrayList<>();
    // class reference variable
    private BackgroundLocationService myService;
    public static GoogleMap googleMap;
    private PreferenceHelper preferenceHelper;
    private GpsTracker gpsTracker;
    private MapUpdateDataBase dbHelper;
    //reference variable used for lat-long,permission and status for start the service
    static boolean status;
    private Context context = MainActivity.this;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean back=false;
    private boolean ring=false;

    //widget field
    private static TextView timer_tv;
    public static Button start_btn;
    private Button stop_btn;
    public ImageButton camera;
    public ImageButton video;
    public ImageView showimage;
    public Button select_image;
    //For Timer
    public Handler mHandler;
    private Handler circleHandler = new Handler();
    private Handler drawHandler = new Handler();
    private static final int REFRESH_RATE = 100;
    private static final int TIME_INTERVAL = 3000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final int CAMERA_SELECT_IMAGE = 300;
    public static final int SELECTED_IMAGE = 3;

    private static String filePath;
    private int fileName;
    private File uploadImgFile = null;
    // directory name to store captured images and videos
    public static Uri fileUri; // file url to store image/video

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    int Seconds, Minutes,Hours,MilliSeconds ;
    private AlertDialog mInternetDialog;
    private static final int WIFI_ENABLE_REQUEST = 0x1006;
    private static final int LOCATION_ENABLE_REQUEST = 1;

    /*
    * ServiceConnection interface used to initialize BackgroundLocationService.class
    * class loading time
    */
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundLocationService.LocalBinder binder = (BackgroundLocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
        }
    };

    /*
    * this method used for start the BackgroundLocationService class.
    * when status==true
    */
    void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), BackgroundLocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
    }

    /*
       * this method used for stop the BackgroundLocationService class.
        * when status==false
        */
    void unbindService() {
        if (status == false)
            return;
        Intent i = new Intent(getApplicationContext(), BackgroundLocationService.class);
        unbindService(sc);
        status = false;
    }

    /*
     *  when application exit this call back method  will call
       and stop the BackgroundLocationService.class
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (status == true)
            Toast.makeText(this,"onDestory called",Toast.LENGTH_LONG).show();
            unbindService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(helloEzeMessageSentReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            registerReceiver(gpsReceiver,
                    new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
       // builder.detectFileUriExposure(); check in google for more

        getSupportActionBar().setTitle("Tracking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // initialize the class
        preferenceHelper = new PreferenceHelper(context);
        dbHelper = new MapUpdateDataBase(context);
        mHandler=new Handler();
        /*check the run time  PERMISSION_GRANTED or not
        *if permission is not GRANTED then go to inside the  checkPermission() and shw a dialog for
        * requestPermission
        */
        if (!checkPermission()) {
            requestPermission();
        }
        /*
        * this method used for initialization for SupportMapFragment
        * */
        intializeMap();
        /*
        * this method used for initialization for All xml ui widgets
        * */
        intializeUiElement();
        /*
        * this method used handel setOnClickListener
        * */
        handelUiElement();
        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
    }


   /* public Runnable startTimer = new Runnable() {
        public void run() {
            String hours = preferenceHelper.getValueFromSharedPrefs("hours");
            String minutes = preferenceHelper.getValueFromSharedPrefs("minutes");
            String seconds = preferenceHelper.getValueFromSharedPrefs("seconds");
            timer_tv.setText(hours + ":" + minutes + ":" + seconds);
            mHandler.postDelayed(this, REFRESH_RATE);
        }
    };
*/


    private Runnable circle=new Runnable(){
        @Override
        public void run(){
            circleHandler.removeCallbacks(circle);
            LatLng latLng = new LatLng(BackgroundLocationService.latitude, BackgroundLocationService.longitude);
            googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(100)
                    .strokeColor(Color.GRAY)
                    .strokeWidth(0.7f)
                    // Fill color of the circle
                    // 0x represents, this is an hexadecimal code
                    // 55 represents percentage of transparency. For 100% transparency, specify 00.
                    // For 0% transparency ( ie, opaque ) , specify ff
                    // The remaining 6 characters(00ff00) specify the fill color
                    .fillColor(0x303F51B5));
        }
    };
    /*
     * this method used for initialization for All xml ui widgets
     * */
    private void intializeUiElement() {
        timer_tv = (TextView) findViewById(R.id.Text_timer);
        start_btn = (Button) findViewById(R.id.button_start);
        select_image = (Button) findViewById(R.id.select_image);
        showimage=(ImageView)findViewById(R.id.showimage);
        stop_btn = (Button) findViewById(R.id.button_stop);
        camera=(ImageButton)findViewById(R.id.camera);
         video=(ImageButton)findViewById(R.id.video);
        camera.setVisibility(View.VISIBLE);
    }

    /*
     * this method used for initialization for SupportMapFragment
     * */
    private void intializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /*
   * convert lat,long to address
   * */
    public String getAddress(double latitude, double longitude) {
        //StringBuilder result = new StringBuilder();
        String add = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude,3);
            if (addresses.size() > 0) {
                android.location.Address address = addresses.get(0);
                /* To print all address
                *  for (int i = 0; i <address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }*/
                /*result.append(address.getLocality());
                result.append(address.getAdminArea()).append("\n");
                result.append(address.getSubAdminArea());
                result.append(address.getCountryName());*/
                {
                    for (int i = 0; i <=2; i++)
                        add += address.getAddressLine(i) + ",";
                }
            }
        } catch (IOException e) {
            // Log.e("tag", e.getMessage());
            e.printStackTrace();
        }
           // return result.toString();
        return add;
    }

  /* public void sendRequest() {

        try {
            if (BackgroundLocationService.destinationlat == null && BackgroundLocationService.destinationlong == null) {
                new DirectionFinder(this,getAddress(Double.parseDouble(GpsTracker.startlat), Double.parseDouble(GpsTracker.startlong)),"bellandur").execute();
                //new DirectionFinder(this, getAddress(Double.parseDouble(GpsTracker.startlat),Double.parseDouble(GpsTracker.startlong)),getAddress(Double.parseDouble(GpsTracker.startlat),Double.parseDouble(GpsTracker.startlong))).execute();
            } else {
                // new DirectionFinder(this, getAddress(Double.parseDouble(GpsTracker.startlat),Double.parseDouble(GpsTracker.startlong)),"bellandur").execute();)
                //new DirectionFinder(MainActivity.this, getAddress(Double.parseDouble(GpsTracker.startlat), Double.parseDouble(GpsTracker.startlong)), getAddress(Double.parseDouble(BackgroundLocationService.destinationlat), Double.parseDouble(BackgroundLocationService.destinationlong))).execute();
                new DirectionFinder(this, getAddress(Double.parseDouble(GpsTracker.startlat),Double.parseDouble(GpsTracker.startlong)),"bellandur").execute();
                // new DirectionFinder(this,Double.parseDouble(GpsTracker.startlat),Double.parseDouble(GpsTracker.startlong),12.9006337,77.6598651).execute();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
*/
    private void handelUiElement() {
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back=true;
                preferenceHelper.saveFloatValueToSharedPrefs("speed", 0.0);
                preferenceHelper.saveValueToSharedPrefs("cummulativeTime", "00");
                StartTime = SystemClock.uptimeMillis();
                mHandler.postDelayed(startTimer,0);
                //drawHandler.postDelayed(drawline,0);
                implementationMap();
                //sendRequest();
                stop_btn.setVisibility(View.VISIBLE);
                start_btn.setVisibility(View.GONE);
            }
        });
        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allertdialog(R.style.DialogAnimation_bottom, "Left - Right Animation!");
                //stopSaveLocationService();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeBuff += MillisecondTime;
                mHandler.removeCallbacks(startTimer);
                Log.v("time",TimeBuff+"");
                captureImage();
            }
        });
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeBuff += MillisecondTime;
                mHandler.removeCallbacks(startTimer);
                Log.v("time",TimeBuff+"");
                recordVideo();
            }
        });
        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromGallery();
            }
        });
            }
  /*@Override
    public void onResume() {
        super.onResume();
      mHandler.postDelayed(startTimer,0); //stop next runnable execution
          }*/
    /*@Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(startTimer);
    }*/

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }



 /* Capturing Camera Image will lauch camera app requrest image capture*/

    private void captureImage() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }


    private void fromGallery() {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//          intent.setType("image");
        intent.setType("image/*");
            startActivityForResult(intent, CAMERA_SELECT_IMAGE);
    }


/*
Recording video
 */

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name
        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }


    /**
     * Creating file uri to store image/video*/

    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /* * returning image / video*/

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Constants.IMAGE_FOLDER);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(Constants.IMAGE_FOLDER, "Oops! Failed create "
                        + Constants.IMAGE_FOLDER + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }
        else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }else if (type == SELECTED_IMAGE) {
            mediaFile = new File(mediaStorageDir.getAbsolutePath());
        }else {
            return null;
        }
        return mediaFile;
    }


    /* *
     * Receiving activity result method will be called after closing the camera
     * used to get Response after image clicked
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE || requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE ||
                requestCode == CAMERA_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {

                try {
                    showimage.setVisibility(View.VISIBLE);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),options);
                    showimage.setImageBitmap(bitmap);
                   // showimage.setImageURI(fileUri);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                /*
                * select from files
                * */
                try {
                    Uri imageUri = data.getData();
                    showimage.setImageURI(imageUri);
                    //this is working 9.2.20
                    /*Uri imageCaptureUri = getOutputMediaFileUri(SELECTED_IMAGE);
                    uploadImgFile = new File(imageCaptureUri.getPath());


                    if (file != null) {
                        if (uploadImgFile.length() >= 2000000) {
                            Toast.makeText(MainActivity.this, "Please select a file less than 2MB", Toast.LENGTH_SHORT).show();
                        } else {
                            Uri imageSelectUri = data.getData();
                            showimage.setImageURI(imageSelectUri);
                        }
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // successfully captured the image
                // display it in image view
                Toast.makeText(getApplicationContext(), "Captured successfully", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Your Image");
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StartTime = SystemClock.uptimeMillis();
                        mHandler.postDelayed(startTimer,0);
                        Toast.makeText(getApplicationContext(),"Image Send Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StartTime = SystemClock.uptimeMillis();
                        mHandler.postDelayed(startTimer,0);
                        Toast.makeText(getApplicationContext(),"Image Canceled", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.activity_camera, null);
                dialog.setView(dialogLayout);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(dialogLayout);
                ImageView imgPreview=(ImageView)dialog.findViewById(R.id.cameraview);
                try {
                    imgPreview.setVisibility(View.VISIBLE);

                    // bimatp factory
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    // downsizing image as it throws OutOfMemory Exception for larger
                    // images
                    options.inSampleSize = 12;

                    final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),options);

                    imgPreview.setImageBitmap(bitmap);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                dialog.show();
                 /*Intent intent=new Intent(MainActivity.this,CameraActivity.class);
                startActivity(intent);*/
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled Capture", Toast.LENGTH_SHORT)
                        .show();
                StartTime = SystemClock.uptimeMillis();
                mHandler.postDelayed(startTimer,0);
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public String getAbsolutePath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void implementationMap() {
        try {
            gpsTracker = new GpsTracker(context);
            if (gpsTracker.canGetLocation()) {
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                preferenceHelper.saveValueToSharedPreference("lat", "" + latitude);
                preferenceHelper.saveValueToSharedPreference("long", "" + longitude);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.setBuildingsEnabled(true);
                googleMap.addMarker(new MarkerOptions());
                //  googleMap.setTrafficEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
                //showing current Location in google map
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(final CameraPosition arg0) {
                        startLocationSaveService();
                        if (status == false){
                            bindService();
                        }
                       circleHandler.postDelayed(circle,1000);

                    }
                });
            } else {
                Toast.makeText(context, "network not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }


    private void startLocationSaveService() {
        try {
            Log.e("startLocationSave", "inside inside");
            Intent intent = new Intent(this, LocationService.class);
            this.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double getLatitude() {
        return Double.parseDouble(preferenceHelper.getValueFromSharedPrefs("lat"));
    }

    private double getLongitude() {
        return Double.parseDouble(preferenceHelper.getValueFromSharedPrefs("long"));
    }


    /*
    * this method will be chk the checkPermission() granted or not
    * if (!checkPermission())
    * return true
    * else
    * return false
    * */
    private boolean checkPermission() {
        int fineLocation = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int storage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return fineLocation == PackageManager.PERMISSION_GRANTED && storage == PackageManager.PERMISSION_GRANTED && camera == PackageManager.PERMISSION_GRANTED;
    }
    /*
    * this method is ask to user request the requestPermission()
    * */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean camera = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted || storage || camera) {
                        Toast.makeText(context, "Permission Granted, Now you can access location data and storage", Toast.LENGTH_LONG).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)||
                                    shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        gpsTracker = new GpsTracker(context);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.addMarker(new MarkerOptions().position(latLng).title(getAddress(latitude, longitude))/*.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location))*/);
        }
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);*/
    }

    public void allertdialog(int animateresource,String msg) {

        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(context);
        // Setting Dialog Title
        mAlertDialog.create();
        mAlertDialog.setTitle(msg);
        //mAlertDialog.getWindow.getAttributes().windowAnimations = animateresource;
        mAlertDialog.setTitle("Do You Really Want To Abort The Ride")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopSaveLocationService();
                        start_btn.setVisibility(View.VISIBLE);
                        stop_btn.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }


    private void stopSaveLocationService() {
        LocationDto locationDto = new LocationDto();
        double latitude=0.0;
        double longitude=0.0;
        GpsTracker gpsTracker = new GpsTracker(context);
        if (gpsTracker.canGetLocation) {
            latitude= gpsTracker.getLatitude();
            longitude= gpsTracker.getLongitude();
        }
        String msg = BackgroundLocationService.latitude + "," + BackgroundLocationService.longitude;
        LogFile.RecordLogFile("Stop Background service"+",: "+msg  + ",time :" +preferenceHelper.getValueFromSharedPrefs("cummulativeTime"), Constants.LOG_FILE);
        locationDto.setLatitude(String.valueOf(BackgroundLocationService.latitude));
        locationDto.setLongitude(String.valueOf(BackgroundLocationService.longitude));
        locationDto.setCumnTime(preferenceHelper.getValueFromSharedPrefs("cummulativeTime"));
        locationDto.setEndDateTime(Constants.dateFormateForShow.format(new Date().getTime()));
        locationDto.setFlag(Constants.ADD_RECORD);
        serviceForSaveLocationDetails(locationDto);
        mHandler.removeCallbacks(startTimer);
        /*
        * */
        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
        if (status == true)
            unbindService();
        gpsTracker.closeGPS();
        preferenceHelper.saveValueToSharedPrefs("hours", "00");
        preferenceHelper.saveValueToSharedPrefs("minutes", "00");
        preferenceHelper.saveValueToSharedPrefs("seconds", "00");
        preferenceHelper.saveValueToSharedPrefs("cummulativeTime", "00");
        String hours = preferenceHelper.getValueFromSharedPrefs("hours");
        String minutes = preferenceHelper.getValueFromSharedPrefs("minutes");
        String seconds = preferenceHelper.getValueFromSharedPrefs("seconds");
        timer_tv.setText(hours + ":" + minutes + ":" + seconds);
        Intent startIntent=new Intent(MainActivity.this,ResultActivity.class);
        startActivity(startIntent);
        this.finish();
    }

    private void serviceForSaveLocationDetails(LocationDto locationDto) {
        dbHelper.insertLocation(locationDto);
    }
    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        return mDateFormat.format(new Date());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(back==false) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                super.onBackPressed();
                mHandler.removeCallbacks(startTimer);
                Intent intent = new Intent(this, LocationService.class);
                stopService(intent);
                if (status == true)
                    unbindService();
                gpsTracker.closeGPS();
                preferenceHelper.saveValueToSharedPrefs("hours", "00");
                preferenceHelper.saveValueToSharedPrefs("minutes", "00");
                preferenceHelper.saveValueToSharedPrefs("seconds", "00");
                preferenceHelper.saveValueToSharedPrefs("cummulativeTime", "00");
                String hours = preferenceHelper.getValueFromSharedPrefs("hours");
                String minutes = preferenceHelper.getValueFromSharedPrefs("minutes");
                String seconds = preferenceHelper.getValueFromSharedPrefs("seconds");
                timer_tv.setText(hours + ":" + minutes + ":" + seconds);
                dbHelper.deleteRecord();
                this.finish();
                return;
            }
            else { Toast.makeText(getBaseContext(), "Tap again to exit", Toast.LENGTH_SHORT).show(); }
            mBackPressed = System.currentTimeMillis();

        }
        else
        {
            allertdialog(R.style.DialogAnimation_bottom, "Left - Right Animation!");
            dbHelper.deleteRecord();
        }
    }


    /*
     * new methods for polyline*/
    @Override
    public void onDirectionFinderStart() {
        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();

        for (Route route : routes) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(googleMap.addPolyline(polylineOptions));

        }
    }


    public Runnable startTimer = new Runnable() {
       @Override
        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Hours=Seconds/3600;

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            timer_tv.setText("" + String.format("%02d", Hours) +":"
                    + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds));
           Toast.makeText(MainActivity.this,""+MillisecondTime,Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(startTimer, 10);
        }
    };
    /*private Runnable drawline=new Runnable(){
        @Override
        public void run() {
            redrawLine();
            drawHandler.postDelayed(this,0);
            }
        };*/

  /*  *//*
    * Runtime register
    * Because manifast register is depricated for higher version>=nougat*//*
    private BroadcastReceiver helloEzeMessageSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "helloEzeMessage Message sent");
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                intent.setFlags(FLAG_RECEIVER_FOREGROUND);
                if(networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    Toast.makeText(context,"Connected Successfully Test",Toast.LENGTH_LONG).show();
                    camera.setVisibility(View.VISIBLE);
                    Log.d("Network", "Internet YAY");
                } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                    Toast.makeText(context,"No Internet Connection Test",Toast.LENGTH_LONG).show();
                    camera.setVisibility(View.INVISIBLE);
                    checkInternetConnection();
                    Log.d("Network", "No internet :(");
                }
            }
        }
    };*/

    private BroadcastReceiver helloEzeMessageSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "helloEzeMessage Message sent");
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();

            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Toast.makeText(context,"Connected Successfully Test",Toast.LENGTH_LONG).show();
                camera.setVisibility(View.VISIBLE);
                Log.d("Network", "Internet YAY");
            } else {
                Toast.makeText(context,"No Internet Connection Test",Toast.LENGTH_LONG).show();
                camera.setVisibility(View.INVISIBLE);
                //checkInternetConnection();
               /* Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.vogella.com"));
               // i.setData(Uri.parse("http://www.vigella.com"));
                startActivity(i);*/
                showNoInternetDialog();
                Log.d("Network", "No internet :(");
            }
        }
    };

    /*for gps connection*/
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //do something
                Toast.makeText(MainActivity.this, " Connected ", Toast.LENGTH_LONG).show();
            } else {
                //do something else
                Toast.makeText(MainActivity.this, " Lost GPS..! ", Toast.LENGTH_LONG).show();
                EnableGPSIfPossible();
            }
        }    };
    @Override
    protected void onResume() {
        super.onResume();
       // Toast.makeText(this,"onResume called Test",Toast.LENGTH_LONG).show();
        //used for higher version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(helloEzeMessageSentReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            registerReceiver(gpsReceiver,
                    new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }
    }

    @Override
    protected void onStop()
    {
        //Toast.makeText(this,"onStop called Test",Toast.LENGTH_LONG).show();
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            unregisterReceiver(helloEzeMessageSentReceiver);
            unregisterReceiver(gpsReceiver);
        }
    }

    /*
    * for internet connection*/
    private void checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {

        } else {
            showNoInternetDialog();
        }
    }
   /*
    * for gps connection*/
       private boolean EnableGPSIfPossible()
    {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            askUserToOpenGPS();
            return true;
        }
        return false;
    }

    /**
     *
     * show settings to open GPS
     */
    private void showNoInternetDialog() {

        if (mInternetDialog != null && mInternetDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internet Disabled!");
        builder.setMessage("No active Internet connection found.");
        builder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(gpsOptionsIntent, WIFI_ENABLE_REQUEST);
                ///startActivity(gpsOptionsIntent);
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
     *
     * show settings to open GPS
     */
    public void askUserToOpenGPS() {
        // TODO Auto-generated method stub
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        mAlertDialog.setTitle("Location not available, Open GPS?").setCancelable(false)
                //.setCanceledOnTouchOutside(false)
                .setMessage("Activate GPS to use location services?")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                       /// startActivityForResult(intent,LOCATION_ENABLE_REQUEST);
                        startActivity(intent);
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
                        //MainActivity.this.finish();
                    }
                }).show();
    }
}

/*class ArrayShift{
   public static void main(String args[]){
       int input1 =4;
       int input2[]=new int[input1];
       int input3=2;
       int output[]=new int[input1];
       for(int i=0;i<input1;i++){
           input2[i]=i+1;
       }
       System.out.println(Arrays.toString(input2));
       int k=input2[input3];
       System.out.println(k);
       for(int j=0;j<input3;j++){
          
       }
   }
}*/
