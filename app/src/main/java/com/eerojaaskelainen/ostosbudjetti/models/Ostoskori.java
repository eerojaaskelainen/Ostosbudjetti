package com.eerojaaskelainen.ostosbudjetti.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eero on 30.12.2014.
 */
public class Ostoskori implements Parcelable, BaseColumns {

    private long id;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getKauppa_id() {
        return kauppa_id;
    }

    public void setKauppa_id(long kauppa_id) {
        this.kauppa_id = kauppa_id;
    }

    private long kauppa_id;
    private Long pvm;


    /**
     * Palauttaa ostosajan Date-tyyppisenä oliona
     * @return
     */
    public Date getPvm() {
        if (pvm == null)
            return null;
        return new Date(pvm);
    }

    /**
     * Tallettaa ajan tietokantaan sopivana epoch aikana
     * @param pvm
     */
    public void setPvm(Date pvm) {
        if (pvm == null)
            return;

        this.pvm = pvm.getTime();
    }



    public Long getRaakaPvm() {
        return this.pvm;
    }
    // Tietokannan muodostinlauseet ja sarakenimet:
    public static final String TABLE_NAME = "ostoskorit";
    public static final String PVM = "pvm";
    public static final String KAUPPA = "kauppa";

    public static final String FULL_ID = TABLE_NAME + "." + _ID;
    public static final String FULL_PVM = TABLE_NAME + "."+ PVM;
    public static final String FULL_KAUPPA = TABLE_NAME + "."+KAUPPA;


    public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
            "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            PVM + " INT, "+
            KAUPPA + " INT, " +
            "FOREIGN KEY ("+ KAUPPA + ") REFERENCES " + Kauppa.TABLE_NAME + "("+ Kauppa._ID + ") " +
            ");";


    public Ostoskori() {
        this.id = -1;
        this.pvm = Calendar.getInstance().getTimeInMillis();
        kauppa_id =-1;
    }

    public Ostoskori(long id,long kauppa_id) {
        this.id = id;
        this.kauppa_id = kauppa_id;
        this.pvm = Calendar.getInstance().getTimeInMillis();
    }
    public Ostoskori(long id, long kauppa_id, Date pvm) {
        this.id = id;
        this.kauppa_id = kauppa_id;
        setPvm(pvm);
    }
    public Ostoskori(long id, long kauppa_id, long pvm) {
        this.id = id;
        this.kauppa_id = kauppa_id;
        this.pvm = pvm;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("d.M.yyyy");

        return "Ostoskori: {kauppa = "+ kauppa_id + ", pvm="+ formatter.format(getPvm()) + "}";
    }

    // Tässä Parceablen toteutukset:
    protected Ostoskori(Parcel in) {
        this.id = in.readLong();
        pvm = in.readLong();
        kauppa_id = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(pvm);
        dest.writeLong(kauppa_id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Ostoskori> CREATOR = new Parcelable.Creator<Ostoskori>() {
        @Override
        public Ostoskori createFromParcel(Parcel in) {
            return new Ostoskori(in);
        }

        @Override
        public Ostoskori[] newArray(int size) {
            return new Ostoskori[size];
        }
    };


    // Parceable loppuu

    public static final Ostoskori convertCursorToOstoskori(Cursor c) {

        if (c == null || c.getCount() <=0) return null;

        c.moveToFirst();

        Ostoskori ok = new Ostoskori(
                c.getLong(c.getColumnIndex(Ostoskori._ID)),
                c.getLong(c.getColumnIndex(Ostoskori.KAUPPA)),
                c.getLong(c.getColumnIndex(Ostoskori.PVM))
        );
        return ok;
    }

    /**
     * Tarkistaa, onko ostoskori kelvollinen talletettavaksi kantaan
     * @return  True jos kori kunnossa
     */
    public static boolean ostoskoriOnKelvollinen(Ostoskori ostoskori) {
        if (ostoskori == null)
            return false;
        if (ostoskori.getId() < 1)
            return false;
        if (ostoskori.getRaakaPvm() <1)
            return false;
        if (ostoskori.getKauppa_id() <1)
            return false;
        return true;
    }
}

