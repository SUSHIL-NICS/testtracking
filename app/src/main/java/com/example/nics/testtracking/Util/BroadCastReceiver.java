package com.example.nics.testtracking.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

/**
 * Created by sushil on 12-01-2018.
 */

public class BroadCastReceiver extends BroadcastReceiver {

    /*
   * Runtime register
   * Because manifast register is depricated for higher version>=nougat*/
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "helloEzeMessage Message sent");
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                intent.setFlags(FLAG_RECEIVER_FOREGROUND);
                if(networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    Toast.makeText(context,"Connected Successfully Test",Toast.LENGTH_LONG).show();
                    //MainActivity.camera.setVisibility(View.VISIBLE);
                    Log.d("Network", "Internet YAY");
                } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                    Toast.makeText(context,"No Internet Connection Test",Toast.LENGTH_LONG).show();
                    //checkInternetConnection();
                    Log.d("Network", "No internet :(");
                }
            }
        }
}
