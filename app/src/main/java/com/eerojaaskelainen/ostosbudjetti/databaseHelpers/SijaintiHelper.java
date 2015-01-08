package com.eerojaaskelainen.ostosbudjetti.databaseHelpers;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;

import com.eerojaaskelainen.ostosbudjetti.models.Sijainti;

import java.util.List;

/**
 * Created by Eero on 30.12.2014.
 */
public class SijaintiHelper {

    public static long luoSijainti(SQLiteDatabase writableDatabase, Location sijainti) {

        ContentValues rA = new ContentValues();
        rA.put(Sijainti.LAT, sijainti.getLatitude());
        rA.put(Sijainti.LNG, sijainti.getLongitude());
        rA.put(Sijainti.ZOOM, sijainti.getAccuracy());

       return writableDatabase.insert(Sijainti.TABLE_NAME,null,rA);
    }

    public static List<Sijainti> haeSijainnit(SQLiteDatabase readableDatabase) {
        //TODO: Luo / tuhoa haeSijainnit-listanpalauttaja.
        throw new UnsupportedOperationException("Not made yet!");
    }


    public static Cursor haeSijainnitCursor(SQLiteDatabase readableDatabase,String[] projection, String selection, String[] selectionArgs, String sortOrder, String sijaintiID){
        SQLiteQueryBuilder haku = new SQLiteQueryBuilder();
        haku.setTables(Sijainti.TABLE_NAME);
        if (sijaintiID != null) {
            // Sijainti-id haettiin määritellysti:
            haku.appendWhere(Sijainti._ID + "=" + sijaintiID);
        }


        return haku.query(readableDatabase,projection,selection,selectionArgs,null,null,sortOrder);
    }

    public static boolean tutkiOnkoSijaintiOlemassa(SQLiteDatabase readableDatabase, long sijainti_id) {
        Cursor c = readableDatabase.rawQuery("SELECT DISTINCT "+ Sijainti._ID +
                    " FROM "+ Sijainti.TABLE_NAME + " WHERE " +
                    Sijainti._ID + "=" + sijainti_id,null);

        return (c.getCount()<=0);
    }

    public static long haeSijaintiID(SQLiteDatabase writableDatabase, Location sijainti) {
        Long tulos = null;

        String[] sarakkeet = new String[] {Sijainti._ID};
        String skooppi = Sijainti.LAT + "= ? AND "+ Sijainti.LNG + "=?";
        String[] skooppiArgut = new String[] {Double.toString(sijainti.getLatitude()), Double.toString(sijainti.getLongitude())};

        Cursor c = haeSijainnitCursor(writableDatabase,sarakkeet,skooppi,skooppiArgut,null,null);
        if (c.getCount()<=0) throw new Resources.NotFoundException("Location not found");

        return c.getLong(c.getColumnIndex(Sijainti._ID));
    }

    public static long luoSijainti(SQLiteDatabase writableDatabase, double latitude, double longitude, float zoom) {
        Location s = new Location("");
        s.setLatitude(latitude);
        s.setLongitude(longitude);
        s.setAccuracy(zoom);

        return luoSijainti(writableDatabase,s);
    }


}
