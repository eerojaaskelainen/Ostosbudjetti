package com.eerojaaskelainen.ostosbudjetti.models;

import android.provider.BaseColumns;

/**
 * Määrittelee Tuote -taulun muodostuksen tietokannassa.
 * Created by Eero on 3.1.2015.
 */

public class Tuote implements BaseColumns{

    private long tuoteID;
    private String valmistaja;
    private String tuotenimi;
    private String eanKoodi;
    private Double viimeisinAhinta;

    public long getTuoteID() {
        return tuoteID;
    }

    public void setTuoteID(long tuoteID) {
        this.tuoteID = tuoteID;
    }

    public String getValmistaja() {
        return valmistaja;
    }

    public void setValmistaja(String valmistaja) {
        this.valmistaja = valmistaja;
    }

    public String getTuotenimi() {
        return tuotenimi;
    }

    public void setTuotenimi(String tuotenimi) {
        this.tuotenimi = tuotenimi;
    }

    public String getEanKoodi() {
        return eanKoodi;
    }

    public void setEanKoodi(String eanKoodi) {
        this.eanKoodi = eanKoodi;
    }

    public Double getViimeisinAhinta() {
        return viimeisinAhinta;
    }

    public void setViimeisinAhinta(Double viimeisinAhinta) {
        this.viimeisinAhinta = viimeisinAhinta;
    }

    public Tuote() {
        tuoteID =-1;
        valmistaja = null;
        tuotenimi = null;
        eanKoodi = null;
        viimeisinAhinta = -1D;
    }



    public static final String TABLE_NAME = "tuotteet";
    public static final String EAN = "tuote_ean";
    public static final String NIMI = "tuotenimi";
    public static final String VALMISTAJA = "valmistaja";
    public static final String VIIMEISINHINTA = "viimeisin_hinta";
    //public static final String LUOKITUS = "luokitus_id";

    // Sarakkeiden nimet taulunimen kera Joineille:
    public static final String FULL_ID = TABLE_NAME + "." + _ID;
    public static final String FULL_EAN = TABLE_NAME + "." + EAN;
    public static final String FULL_NIMI = TABLE_NAME + "." + NIMI;
    public static final String FULL_VALMISTAJA = TABLE_NAME + "." + VALMISTAJA;
    //public static final String FULL_LUOKITUS = TABLE_NAME + "." + LUOKITUS;

    public static final String VIIMEISINHINTA_CONSTRUCT = "(SELECT "+ Ostosrivi.A_HINTA + " FROM " + Ostosrivi.TABLE_NAME +
                                                        " WHERE " + Tuote.FULL_ID + " = "+ Ostosrivi.FULL_TUOTE +
                                                        " ORDER BY " + Ostosrivi.FULL_ID +" DESC LIMIT 1) AS " + VIIMEISINHINTA;

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
