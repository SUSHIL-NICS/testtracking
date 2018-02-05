package com.example.nics.testtracking.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sushil on 13-07-2017.
 */

public class PreferenceHelper {
  private Context context;
    private SharedPreferences sharedPreferences;
    public PreferenceHelper(Context context) {
        this.context=context;
         sharedPreferences=context.getSharedPreferences("map",Context.MODE_PRIVATE);
    }

    public boolean saveValueToSharedPreference(String keyname, String value) {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(keyname,value);
        editor.commit();
        return true;
    }

    public String getValueFromSharedPreference(String keyName) {
        return sharedPreferences.getString(keyName, "");
    }

    public String getValueFromSharedPrefs(String KeyName) {
        return sharedPreferences.getString(KeyName, "");
    }

    public boolean saveValueToSharedPrefs(String KeyName, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KeyName, value);
        editor.commit();
        return true;
    }
    public boolean saveFloatValueToSharedPrefs(String KeyName, double value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KeyName, (float) value);
        editor.commit();
        return true;
    }
    public float getFloatValueToSharedPrefs(String KeyName) {
        return sharedPreferences.getFloat(KeyName,0.0f);
    }
}
