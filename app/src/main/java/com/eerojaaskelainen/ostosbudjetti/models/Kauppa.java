package com.eerojaaskelainen.ostosbudjetti.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

/**
 * Created by Eero on 30.12.2014.
 */
public class Kauppa implements Parcelable, BaseColumns {

    public long getID() {return id;}
    public void setID(long id) {this.id = id;}

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getOsoite() {
        return osoite;
    }

    public void setOsoite(String osoite) {
        this.osoite = osoite;
    }

    public Location getSijainti() {
        return sijainti;
    }

    public void setSijainti(Location sijainti) {
        this.sijainti = sijainti;
    }

    private Long id;
    private String nimi;
    private String osoite;
    private Location sijainti;


    // Tietokannan muodostinlauseet ja sarakenimet:
    public static final String TABLE_NAME = "kaupat";
    public static final String NIMI = "nimi";
    public static final String OSOITE = "osoite";
    public static final String SIJAINTI = "sijainti_id";

    // Täydet sarakenimet, eli taulun nimi.sarakenimi:
    public static final String FULL_ID = TABLE_NAME + "." + _ID;
    public static final String FULL_NIMI = TABLE_NAME + "." + "nimi";
    public static final String FULL_OSOITE = TABLE_NAME + "." + "osoite";
    public static final String FULL_SIJAINTI = TABLE_NAME + "." + "sijainti_id";


    public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +
            "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            NIMI + " VARCHAR(100), "+
            OSOITE + " VARCHAR(50), " +
            SIJAINTI + " INT, "+
            "FOREIGN KEY (" + SIJAINTI + ") REFERENCES "+ Sijainti.TABLE_NAME + "(" + Sijainti._ID + ")" +
            ");";






    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(nimi);
        dest.writeString(osoite);
        if (sijainti == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            sijainti.writeToParcel(dest, flags);
        }
    }

    public Kauppa(Parcel in) {
        nimi = in.readString();
        osoite = in.readString();
        if (in.readByte() == 0x01) {
            sijainti =Location.CREATOR.createFromParcel(in);
        }
        else {
            sijainti = null;
        }
    }

    public Kauppa(String nimi, String osoite, Location sijainti) {
        this.nimi = nimi;
        this.osoite = osoite;
        this.sijainti = sijainti;
    }

    public Kauppa(String nimi, String osoite) {
        this.nimi = nimi;
        this.osoite = osoite;
        this.sijainti = null;
    }

    public Kauppa(String nimi) {
        this.nimi = nimi;
        this.osoite = "";
        this.sijainti = null;
    }
}
