package com.example.nics.testtracking.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.nics.testtracking.Util.LocationDto;

import java.util.ArrayList;

/**
 * Created by sushil on 13-07-2017.
 */

public class MapUpdateDataBase extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "MapTracker";
    public final static int DATABASE_VERSION = 1;
    public SQLiteDatabase database;
    private static final String LOCATION_TABLE = "LatLng_Table";
    private static final String LOCATION_ID = "_id";
    private static final String LOCATION_LATITUDE = "latitude";
    private static final String LOCATION_LONGITUDE = "longitude";
    private static final String LOCATION_CUMMTIME = "cummTime";
    private static final String LOCATION_STARTDATE = "startDate";
    private static final String LOCATION_ENDDATE = "endDate";
    private static final String LOCATION__FLAG = "flag";

    private final static String LATLNG_QUERY = "create table " + LOCATION_TABLE + "(" + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LOCATION_LATITUDE + " TEXT,"  + LOCATION_LONGITUDE + " TEXT,"
            + LOCATION_STARTDATE + " TEXT,"  + LOCATION_ENDDATE + " TEXT,"
            + LOCATION_CUMMTIME + " TEXT,"  + LOCATION__FLAG + " TEXT)";

    public MapUpdateDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL(LATLNG_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertLocation(LocationDto locationDto) {
        database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(LOCATION_LATITUDE,locationDto.getLatitude());
        value.put(LOCATION_LONGITUDE,locationDto.getLongitude());
        value.put(LOCATION_STARTDATE,locationDto.getStartDateTime());
        value.put(LOCATION_CUMMTIME,locationDto.getCumnTime());
        value.put(LOCATION_ENDDATE,locationDto.getEndDateTime());
        value.put(LOCATION__FLAG, locationDto.getFlag());
        database.insert(LOCATION_TABLE, null, value);
        Log.i("insert Database"," insert successfully");
        close();
    }

    public ArrayList<LocationDto> getLocation(String data) {
        ArrayList<LocationDto> items = new ArrayList<>();
        database = getReadableDatabase();
        Cursor c = database.rawQuery("select * from " + LOCATION_TABLE + " where " + LOCATION__FLAG + " =?", new String[]{data});
        if (c != null) {
            int count=c.getCount();
            if (count != 0) {
                c.moveToFirst();
                do {
                    LocationDto item = new LocationDto();
                    item.setLatitude(c.getString(c.getColumnIndex(LOCATION_LATITUDE)));
                    item.setLongitude(c.getString(c.getColumnIndex(LOCATION_LONGITUDE)));
                    item.setStartDateTime(c.getString(c.getColumnIndex(LOCATION_STARTDATE)));
                    item.setCumnTime(c.getString(c.getColumnIndex(LOCATION_CUMMTIME)));
                    item.setEndDateTime(c.getString(c.getColumnIndex(LOCATION_ENDDATE)));
//                    item.setFlag(c.getString(c.getColumnIndex(LOCATION__FLAG)));
                    items.add(item);
                } while (c.moveToNext());
            }
        }
        close();
        return items;
    }
   /* public void updateLocation(LocationDto name){
        database=this.getWritableDatabase();
        ContentValues value=new ContentValues();
        value.put(LOCATION__FLAG, name.getFlag());
        database.update(LOCATION_TABLE,value,LOCATION__FLAG+" =?",new String[]{name.getFlag()});
        Log.i("update Database"," update successfully");
        close();
    }*/
   public void updateLocation(String name){
       database=this.getReadableDatabase();
       ContentValues value=new ContentValues();
       value.put(LOCATION__FLAG, name);
       database.update(LOCATION_TABLE,value,LOCATION__FLAG+" =?",new String[]{name});
       Log.i("update Database"," update successfully");
       close();
   }
   /* public void deleteRecord(String contact) {
        database = this.getWritableDatabase();
        database.delete(LOCATION_TABLE, LOCATION__FLAG + " =?", new String[]{contact});
        Log.i("delete Database"," delete successfully");
        database.close();
    }*/

    public void deleteRecord() {
        database = this.getReadableDatabase();
        database.delete(LOCATION_TABLE,null,null);
        Log.i("delete Database"," delete successfully");
        database.close();
    }

    public int getItemCount(){
        database = getReadableDatabase();
        int count=0;
        Cursor c = database.rawQuery("select * from " + LOCATION_TABLE ,null);
        if (c != null)
            count=c.getCount();
        return count;
    }
}
