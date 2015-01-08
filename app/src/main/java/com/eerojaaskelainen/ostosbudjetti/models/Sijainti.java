package com.eerojaaskelainen.ostosbudjetti.models;

import android.provider.BaseColumns;

/**
 * Määrittelee sijainti-taulun muodostuksen tietokannassa.
 * Created by Eero on 30.12.2014.
 */
public final class Sijainti implements BaseColumns{

    public static final String TABLE_NAME = "sijainnit";
    public static final String LAT = "lat";
    public static final String LNG = "long";
    public static final String ZOOM = "zoom";

    // Sarakkeiden nimet taulunimen kera Joineille:
    public static final String FULL_ID = TABLE_NAME + "." + _ID;
    public static final String FULL_LAT = TABLE_NAME + "." + LAT;
    public static final String FULL_LNG = TABLE_NAME + "." + LNG;
    public static final String FULL_ZOOM = TABLE_NAME + "." + ZOOM;


    /**
     * Sijainti-taulun luontilause:
     */
    public static final String TABLE_CREATE = "CREATE TABLE "+ TABLE_NAME +
            "("+
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LAT + " DOUBLE, "+
            LNG + " DOUBLE, " +
            ZOOM + " TEXT " +
            ");";

}
