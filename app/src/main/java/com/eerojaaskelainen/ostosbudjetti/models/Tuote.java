package com.eerojaaskelainen.ostosbudjetti.models;

import android.provider.BaseColumns;

/**
 * Määrittelee Tuote -taulun muodostuksen tietokannassa.
 * Created by Eero on 3.1.2015.
 */

public final class Tuote implements BaseColumns{

    public static final String TABLE_NAME = "tuotteet";
    public static final String EAN = "tuote_ean";
    public static final String NIMI = "tuotenimi";
    public static final String VALMISTAJA = "valmistaja";
    //public static final String LUOKITUS = "luokitus_id";

    // Sarakkeiden nimet taulunimen kera Joineille:
    public static final String FULL_ID = TABLE_NAME + "." + _ID;
    public static final String FULL_EAN = TABLE_NAME + "." + EAN;
    public static final String FULL_NIMI = TABLE_NAME + "." + NIMI;
    public static final String FULL_VALMISTAJA = TABLE_NAME + "." + VALMISTAJA;
    //public static final String FULL_LUOKITUS = TABLE_NAME + "." + LUOKITUS;


    /**
     * Sijainti-taulun luontilause:
     */
    public static final String TABLE_CREATE = "CREATE TABLE "+ TABLE_NAME +
            "("+
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            EAN + " INTEGER, "+
            NIMI + " VARCHAR(200), "+
            VALMISTAJA + " VARCHAR(200) "+
           // LUOKITUS + " INTEGER, " +
           // "FOREIGN KEY ("+ LUOKITUS + ") REFERENCES " + Luokitus.TABLE_NAME + " ("+ Luokitus._ID + ")" +
            ");";

}
