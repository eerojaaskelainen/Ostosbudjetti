package com.eerojaaskelainen.ostosbudjetti.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

/**
 * Määrittelee Ostosrivit -taulun muodostuksen tietokannassa.
 * Created by Eero on 3.1.2015.
 */

public final class Ostosrivi implements BaseColumns, Parcelable{

    private long ostoskoriID = -1;
    private long riviID = -1;
    private long tuoteID = -1;
    private String tuoteEAN = null;
    private double aHinta = -1;
    private double lkm = -1;

    public Ostosrivi(long ostoskoriID) {
        if (ostoskoriID <=0) throw new IllegalArgumentException("basket ID must be valid! "+ ostoskoriID + " is not.");
        this.ostoskoriID = ostoskoriID;
    }

    public long getOstoskoriID() {
        return ostoskoriID;
    }

    public void setOstoskoriID(long ostoskoriID) {
        this.ostoskoriID = ostoskoriID;
    }

    public long getRiviID() {
        return riviID;
    }

    public void setRiviID(long riviID) {
        this.riviID = riviID;
    }

    public long getTuoteID() {
        return tuoteID;
    }

    public void setTuoteID(long tuoteID) {
        this.tuoteID = tuoteID;
    }

    public String getTuoteEAN() {
        return tuoteEAN;
    }

    public void setTuoteEAN(String tuoteEAN) {
        this.tuoteEAN = tuoteEAN;
    }

    public double getaHinta() {
        return aHinta;
    }

    public void setaHinta(double aHinta) {
        this.aHinta = aHinta;
    }

    public double getLkm() {
        return lkm;
    }

    public void setLkm(double lkm) {
        this.lkm = lkm;
    }

    public double getRivisumma() {
        if (aHinta == -1 || lkm == -1) return -1;
        return aHinta * lkm;
    }

public static Ostosrivi muunnaCursorOstosriviksi(Cursor c){
    if (c.getCount()==0)return null;

        c.moveToFirst();

        Ostosrivi tulos = new Ostosrivi(c.getLong(c.getColumnIndex(OSTOSKORI)));
        tulos.setRiviID(c.getLong(c.getColumnIndex(_ID)));
        tulos.setTuoteID(c.getLong(c.getColumnIndex(TUOTE)));
        tulos.setTuoteEAN(c.getString(c.getColumnIndex(Tuote.EAN)));
        tulos.setaHinta(c.getDouble(c.getColumnIndex(A_HINTA)));
        tulos.setLkm(c.getDouble(c.getColumnIndex(LKM)));


        return tulos;
}

public static ContentValues muunnaOstosriviValueiksi(Ostosrivi o)
{
    if (o.getTuoteID() ==-1 || o.getOstoskoriID() ==-1 )
        return null;

    ContentValues cV = new ContentValues();
    if (o.getRiviID()>0)
        cV.put(_ID,o.getRiviID());

    cV.put(OSTOSKORI,o.getOstoskoriID());
    cV.put(TUOTE,o.getTuoteID());
    cV.put(A_HINTA,o.getaHinta());
    cV.put(LKM,o.getLkm());

    return cV;
}


    public static final String TABLE_NAME = "ostoskori_rivit";
    public static final String OSTOSKORI = "ostoskori_id";
    public static final String TUOTE = "tuote_id";
    public static final String A_HINTA = "a_hinta";
    public static final String LKM = "lkm";
    public static final String RIVISUMMA = "summa";
    public static final String RIVISUMMA_GENERATOR = A_HINTA + "*" + LKM + " AS "+RIVISUMMA;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(ostoskoriID);
        dest.writeLong(riviID);
        dest.writeLong(tuoteID);
        dest.writeString(tuoteEAN);
        dest.writeDouble(aHinta);
        dest.writeDouble(lkm);
    }

    public Ostosrivi(Parcel in)
    {
        ostoskoriID = in.readLong();
        riviID = in.readLong();
        tuoteID = in.readLong();
        tuoteEAN = in.readString();
        aHinta = in.readDouble();
        lkm = in.readDouble();
    }
}
