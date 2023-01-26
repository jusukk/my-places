package fi.lab.myplaces;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "places.db";
    private static final String TABLE_NAME = "places";
    private static final String KEY_TITLE = "title";
    private static final String KEY_LATITUDE = "latitude ";
    private static final String KEY_LONGITUDE = "longitude ";
    private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+"("
            + KEY_TITLE + " TEXT,"
            + KEY_LATITUDE + " TEXT,"
            + KEY_LONGITUDE + " TEXT)";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME;
    public DBHandler(@Nullable Context context) {
        super(context, DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void addPlace(MyMarker myMaker){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String latitude = String.valueOf(myMaker.getLatLng().latitude);
        String longitude = String.valueOf(myMaker.getLatLng().longitude);

        values.put(KEY_TITLE, myMaker.getTitle());
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);

        db.insert(TABLE_NAME,null,values);
    }

    public List<MyMarker> getAllPlaces(){
        List<MyMarker> placeList = new LinkedList<>();
        String selectQuery = "SELECT * from "+TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);

        while(cursor.moveToNext()){
            String title = cursor.getString(0);
            // Convert String to LatLng coordinates
            double latitude = Double.parseDouble(cursor.getString(1));
            double longitude = Double.parseDouble(cursor.getString(2));
            LatLng latLng = new LatLng(latitude, longitude);

            MyMarker myMarker = new MyMarker(title,latLng);
            placeList.add(myMarker);
        }
        return placeList;
    }

    public void clearPlaces(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,null,new String[]{});
    }

}
