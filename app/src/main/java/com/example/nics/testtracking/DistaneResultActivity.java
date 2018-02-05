package com.example.nics.testtracking;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nics.testtracking.Database.MapUpdateDataBase;
import com.example.nics.testtracking.Util.LocationDto;
import com.example.nics.testtracking.Util.RideDto;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DistaneResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ResultActivity.class.getSimpleName();
    private static final int REQUEST_COARSE_LOCATION = 1001;
    private GoogleMap googleMap;
    LocationDto locationDto;
    private MapUpdateDataBase dbHelper;
    ArrayList<LocationDto> locationListDto;
    private Map<String, String> markerMap = new HashMap<>();
    private TextView tvTime;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvDistance;
    private ArrayList<RideDto> rideList;
    private double totalDistance=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHelper = new MapUpdateDataBase(this);
        locationDto = new LocationDto();

        initialiseUiElement();
        if (!checkPermission()) {
            requestPermission();
        }
        intializeMap();

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

    private void initialiseUiElement() {
        tvTime = (TextView) findViewById(R.id.time_tv);
        tvStartDate = (TextView) findViewById(R.id.startDate_tv);
        tvEndDate = (TextView) findViewById(R.id.endDate_tv);
        tvDistance = (TextView) findViewById(R.id.distance_tv);

    }
    private void getAllRecordFromLocationDb() {
        locationListDto = dbHelper.getLocation(Constants.ADD_RECORD);
        if (locationListDto.size() > 0) {
            LocationDto locationDto = null;
            rideList = new ArrayList<>();
            for (int i = 0; i < locationListDto.size(); i++) {
                locationDto = locationListDto.get(i);
                Log.i(TAG + " latitude", locationDto.getLatitude());
                Log.i(TAG + " longitude", locationDto.getLongitude());
                Log.i(TAG + " StartDate", "" + locationDto.getStartDateTime());
                Log.i(TAG + " endDate", "" + locationDto.getEndDateTime());
                Log.i(TAG + " commonDate", "" + locationDto.getCumnTime());
                RideDto rideDto = new RideDto();
                rideDto.setLatitude(locationDto.getLatitude());
                rideDto.setLongitude(locationDto.getLongitude());
                rideList.add(rideDto);
            }
            if (!rideList.isEmpty()) {
                for(int i=0;i<rideList.size()-1;i++){
                    calcDistance(rideList.get(i), rideList.get(i+1));
                }
                placeLatLongToMap();
                tvStartDate.setText(locationListDto.get(0).getStartDateTime());
                tvEndDate.setText(locationDto.getEndDateTime());
                int ll = Integer.parseInt(locationDto.getCumnTime());
                String l = getDurationTime(ll);
                tvTime.setText(l);
            }
        }
    }

    private String getDurationTime(int time) {

        int hrs = time / 3600;
        int mins = (time % 3600) / 60;
        time = time % 60;
        return twoDigitsString(hrs) + " : " + twoDigitsString(mins) + " : " + twoDigitsString(time);
    }

    private String twoDigitsString(int number) {
        if (number == 0) {

            return "00";
        }
        if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private void placeLatLongToMap() {
        PolylineOptions pOptions = new PolylineOptions()
                .width(7)
                .color(Color.RED)
                .geodesic(true);

        for (RideDto rideDto : rideList) {
            pOptions.add(new LatLng(Double.parseDouble(rideDto.getLatitude()), Double.parseDouble(rideDto.getLongitude())));
        }
        RideDto rideDto = rideList.get(0);
        LatLng latLng = new LatLng(Double.parseDouble(rideDto.getLatitude()), Double.parseDouble(rideDto.getLongitude()));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        googleMap.addPolyline(pOptions);
    }



    private void calcDistance(RideDto rideDto, RideDto rideDto1) {
        getDistanceInfo(rideDto.getLatitude(),rideDto.getLongitude(),rideDto1.getLatitude(),rideDto1.getLongitude());

    }
    private void getDistanceInfo(String sourcelat, String sourcelng,
                                 String destinationlat, String destinationlng) {
        try {
            String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
                    + sourcelat
                    + ","
                    + sourcelng
                    + "&destination="
                    + destinationlat
                    + ","
                    + destinationlng
                    + "&mode=driving&sensor=false";
            Log.i(TAG, "Url " + url);
            JSONParser jsonParser=new JSONParser();
            jsonParser.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onBackPressed() {
        dbHelper.deleteRecord();
        int i = dbHelper.getItemCount();
        Log.i(TAG + " Database Count :", "size --" + i);
        rideList.clear();
        RideDto rideDto=new RideDto();
        rideDto.setLongitude("");
        rideDto.setLatitude("");
        locationDto.setStartDateTime("");
        locationDto.setEndDateTime("");
        locationDto.setLatitude("");
        locationDto.setLongitude("");
        locationDto.setCumnTime("0");
        Intent intent = new Intent(DistaneResultActivity.this, MainActivity.class);
        startActivity(intent);
        this.finish();

    }

    private boolean checkPermission() {
        int fineLocation = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int storage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return fineLocation == PackageManager.PERMISSION_GRANTED && storage == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted && storage) {
                        Toast.makeText(DistaneResultActivity.this, "Permission Granted, Now you can access location data and storage", Toast.LENGTH_LONG).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_COARSE_LOCATION);
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
        new AlertDialog.Builder(DistaneResultActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void intializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_resultActivity);
        mapFragment.getMapAsync(this);
    }

    private void implementation() {

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setTrafficEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        getAllRecordFromLocationDb();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //show point on ur location
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
        this.googleMap.setMyLocationEnabled(true);
        implementation();
    }


    public class JSONParser extends AsyncTask<String, Void, String> {
        Double dist = 0.0;
        Boolean isDistanceinKm = false;
        HttpResponse httpResponse;
        String strHttpEntity = null;
        @Override
        protected String doInBackground(String... strings) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(strings[0]);
            try {
                httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                strHttpEntity = EntityUtils.toString(httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return strHttpEntity;
        }
        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.i(TAG+"onPostExecute",s);
                try {
                    JSONObject responseJSONObject = new JSONObject(s);
                    if (responseJSONObject != null && responseJSONObject.has("routes")) {
                        JSONArray routesJSONArray = responseJSONObject.getJSONArray("routes");
                        if (routesJSONArray != null && routesJSONArray.length() > 0) {
                            JSONObject routesJSONObject = routesJSONArray.getJSONObject(0);
                            if (routesJSONObject != null && routesJSONObject.has("legs")) {
                                JSONArray legsJSONArray = routesJSONObject.getJSONArray("legs");
                                if (legsJSONArray != null && legsJSONArray.length() > 0) {
                                    JSONObject stepsJSONObject = legsJSONArray.getJSONObject(0);
                                    if (stepsJSONObject != null && stepsJSONObject.has("distance")) {
                                        JSONObject distanceJSONObject = stepsJSONObject.getJSONObject("distance");
                                        Log.i(TAG, "distance " + distanceJSONObject.toString());
                                        if (distanceJSONObject != null && distanceJSONObject.has("text")) {
                                            String distanceText = distanceJSONObject.getString("text");
                                            Log.i("Distance text", distanceText);
                                            if (distanceText != null) {
                                                if (distanceText.contains("km")
                                                        || distanceText.contains("KM")
                                                        || distanceText.contains("Km")) {
                                                    isDistanceinKm = true;
                                                    Log.i(TAG, "isDistanceinKm " + isDistanceinKm);
                                                }
                                                dist = Double.parseDouble(distanceText.replaceAll("[^\\.0123456789]", ""));
                                                if (!isDistanceinKm) {
                                                    Double kilometers = 0.0;
                                                    kilometers = dist / 1000;
                                                    dist = kilometers;
                                                    Log.i(TAG+"isDistanceinKm",""+dist);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            totalDistance+=dist;
            LogFile.RecordLogFile("location changed Distance"+": "+totalDistance+" time: "+new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a").format(new Date()),Constants.LOG_DISTANCE_FILE);
            tvDistance.setText(""+totalDistance);

        }
    }

}
