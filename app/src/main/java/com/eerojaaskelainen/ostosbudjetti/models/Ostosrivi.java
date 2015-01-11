package com.eerojaaskelainen.ostosbudjetti.models;

import android.provider.BaseColumns;

/**
 * Määrittelee Ostosrivit -taulun muodostuksen tietokannassa.
 * Created by Eero on 3.1.2015.
 */

public final class Ostosrivi implements BaseColumns{

    public static final String TABLE_NAME = "ostoskori_rivit";
    public static final String OSTOSKORI = "ostoskori_id";
    public static final String TUOTE = "tuote_id";
    public static final String A_HINTA = "a_hinta";
    public static final String LKM = "lkm";
    public static final String RIVISUMMA = "summa";
    public static final String RIVISUMMA_GENERATOR = A_HINTA + "*" + LKM + " AS summa";

    // Sarakkeiden nimet taulunimen kera Joineille:
    public static final String FULL_ID = TABLE_NAME + "." + _ID;
    public static final String FULL_OSTOSKORI = TABLE_NAME + "." + OSTOSKORI;
    public static final String FULL_TUOTE = TABLE_NAME + "." + TUOTE;
    public static final String FULL_A_HINTA = TABLE_NAME + "." + A_HINTA;
    public static final String FULL_LKM = TABLE_NAME + "." + LKM;
    public static final String FULL_RIVISUMMA_GENERATOR = FULL_A_HINTA + "+" + FULL_LKM + " AS rowsum";


    /**
     * Sijainti-taulun luontilause:
     */
    public static final String TABLE_CREATE = "CREATE TABLE "+ TABLE_NAME +
            "("+
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            OSTOSKORI + " INTEGER, "+
            TUOTE + " INTEGER, "+
            A_HINTA + " DOUBLE, " +
            LKM + " DOUBLE, " +
            "FOREIGN KEY ("+ OSTOSKORI + ") REFERENCES " + Ostoskori.TABLE_NAME + " ("+ Ostoskori._ID + "), " +
            "FOREIGN KEY ("+ TUOTE + ") REFERENCES " + Tuote.TABLE_NAME + " (" + Tuote._ID + ")" +
            ");";

}
