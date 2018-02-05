package com.example.nics.testtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

/**
 * Created by sushil on 21-08-2017.
 */
public class InternetConnectionBroadcast extends BroadcastReceiver
{
   /* private Handler handler = new Handler();
    public static String state;
    private Context netcontext;
    public void onReceive(Context context, Intent intent)
    {
        netcontext = context;
        NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

        if(null != info)
        {
            state = getNetworkStateString(info.getState());
            Log.i("----------Network State",state);
        }
    }

    private String getNetworkStateString(NetworkInfo.State state)
    {
        String stateString = "Unknown";
        switch(state)
        {
            case CONNECTED:         stateString = "Connected";      break;

            case CONNECTING:        stateString = "Connecting";     break;

            case DISCONNECTED:

                stateString = "Disconnected";
                handler.removeCallbacks(sendUpdatesToUI);
                handler.post(sendUpdatesToUI);

                break;

            case DISCONNECTING:     stateString = "Disconnecting";  break;

            case SUSPENDED:         stateString = "Suspended";      break;

            default:                stateString = "Unknown";        break;
        }
        return stateString;
    }


    private Runnable sendUpdatesToUI = new Runnable()
    {
        public void run()
        {
            Toast.makeText(netcontext,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show();
            MainActivity.camera.setVisibility(View.INVISIBLE);
        }
    };*/

   //CHECKING FOR BOTH WIFI AND INTERNET CONNECTION
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            intent.setFlags(FLAG_RECEIVER_FOREGROUND);
            if(networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                Toast.makeText(context,"Connected Successfully",Toast.LENGTH_LONG).show();
                //MainActivity.camera.setVisibility(View.VISIBLE);
                Log.d("Network", "Internet YAY");
            } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                Toast.makeText(context,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show();
               // MainActivity.camera.setVisibility(View.INVISIBLE);
                Log.d("Network", "No internet :(");
            }
        }
    }


/*  // CHECKING INDIVIDUALLY FOR WIFI AND INTERNET CONNECTION
   @Override
public void onReceive(final Context context, final Intent intent) {
    final ConnectivityManager connMgr = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);

    final android.net.NetworkInfo wifi = connMgr
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    final android.net.NetworkInfo mobile = connMgr
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    if (mobile.isConnected()) {
        Toast.makeText(context,"Internet Connection",Toast.LENGTH_LONG).show();
    }else if  ( wifi.isConnected()) {
        Toast.makeText(context,"Wifi Connection",Toast.LENGTH_LONG).show();
    }else {
        Toast.makeText(context,"No Connection TestTrackingMap",Toast.LENGTH_LONG).show();
       // Snackbar.make(get,"No Connection",Snackbar.LENGTH_LONG).show();
    }}*/
}