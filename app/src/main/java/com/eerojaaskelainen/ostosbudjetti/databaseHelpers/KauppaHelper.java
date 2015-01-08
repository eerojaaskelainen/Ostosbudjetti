package com.eerojaaskelainen.ostosbudjetti.databaseHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;

import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;
import com.eerojaaskelainen.ostosbudjetti.models.Sijainti;

import java.util.Calendar;
import java.util.List;

/**
 * Tietokannan kaupat-taulua hanskaava luokka
 * Created by Eero on 30.12.2014.
 */
public class KauppaHelper {

    /**
     * Kaupan luonti valmiista oliosta.
     * @param writableDatabase  Tietokanta-ajuri
     * @param kauppa    Valmis kauppa-olio
     * @return
     * Palauttaa luodun kaupan ID-numeron.
     */
    public static long luoKauppa(SQLiteDatabase writableDatabase, Kauppa kauppa) {

        // Jos sijaintia ei ole annettu ollenkaan, loppuu tämä leikki tähän.
        if (kauppa.getSijainti() == null) throw new IllegalArgumentException("Location not defined!");

        // Etsitään sijainnin ID:
        Long sijaintiID = SijaintiHelper.haeSijaintiID(writableDatabase,kauppa.getSijainti());

        //Tutkitaan onko sijainti olemassa. Jos ei ole, niin luodaan sellainen:
        if (sijaintiID == null) {
            SijaintiHelper.luoSijainti(writableDatabase,kauppa.getSijainti());
        }

        // Ja luodaan kauppa:
        return luoKauppa(writableDatabase,kauppa.getNimi(),kauppa.getOsoite(),sijaintiID);
    }

    /**
     * Luodaan kauppa-rivi tietokantaan.
     * @param writableDatabase  Tietokanta-ajuri
     * @param nimi  Kaupan nimi
     * @param osoite    Kaupan osoite
     * @param sijainti_id   Kaupan paikkatiedon ID, joka tulee löytyä kannasta
     * @return
     * Uuden kaupan ID-tunnus
     */
    public static long luoKauppa(SQLiteDatabase writableDatabase,String nimi, String osoite, long sijainti_id) {

        // Tutkitaan, onko moisella ID:llä sijainti olemassa kannassa. Jos ei ole, niin stoppia.
        if (SijaintiHelper.tutkiOnkoSijaintiOlemassa(writableDatabase,sijainti_id)) {
            throw new IllegalArgumentException("Location id "+ sijainti_id + "was not found!");
        }

        // Sijainti on OK, Tutkitaan onko kauppa jo aiemmin olemassa, sen nimen sekä sijainnin perusteella siis:
        Cursor c = haeKaupatCursor(writableDatabase,
                new String[]{Kauppa.FULL_ID},
                Kauppa.FULL_SIJAINTI + "= ?",
                new String[]{Long.toString(sijainti_id)},
                null,
                null,
                nimi);
        // Tutkitaan, onko sama kauppa jo kannassa:
        if (c.getCount() >0) {
            //Löytyi. Ei siis laiteta uutta samanlaista...
            throw new IllegalArgumentException("Store '"+ nimi + " with the same location already exists!");
        }

        // Kauppaa ei ollut entuudestaan. Luodaan siis:
        ContentValues cV = new ContentValues();
        cV.put(Kauppa.NIMI,nimi);
        cV.put(Kauppa.OSOITE, osoite);
        cV.put(Kauppa.SIJAINTI, sijainti_id);

        return writableDatabase.insert(Kauppa.TABLE_NAME,null,cV);
    }

    /**
     * Hakee kaupan /kaupat kursorina. Tätä käytetään mm. Spinneri ja listview-adaptereissa Loaderin avustuksella.
     * @param readableDatabase  Tietokanta
     * @param projection    Sarakkeet, joita otetaan mukaan kyselyyn. Jos null, niin kaikki
     * @param selection     Rajaavat lausekkeet, eli WHERE. Jos mukana ?-merkkejä, käytä argseja!
     * @param selectionArgs Rajaavien lausekkeiden ?-merkkien korvaajat listana
     * @param sortOrder     Tulosten järjestys
     * @param kaupanID      Jos haetaan kaupan ID:n perusteella, riittää kun laittaa sen tähän. *
     * @param kaupanNimi    Jos haetaan kaupan nimen perusteella, riittää laitto tähän. *
     *
     *                      * Vain jompi kumpi.
     * @return
     * Palauttaa kursorin tuloksista
     */
    public static Cursor haeKaupatCursor(SQLiteDatabase readableDatabase, String[] projection, String selection, String[] selectionArgs, String sortOrder, String kaupanID, String kaupanNimi) {

        // Käytetään builderia. Se naittuu kivasti noihin argumentteihin.
        SQLiteQueryBuilder kysely = new SQLiteQueryBuilder();

        // Jos kysyttiin tiettyä kaupan ID:tä, niin laitetaan se tässä mukaan:
        if (kaupanID != null) {
            kysely.appendWhere(Kauppa.FULL_ID + "=" + kaupanID);
        }
        // TAI jos haetaan kaupan nimellä, niin tässä se mukaan:
        else if (kaupanNimi != null) {
            kysely.appendWhere(Kauppa.FULL_NIMI + "='" + kaupanNimi+"'");
        }

        // Oletussorttaus, jos semmosta ei annettu:
        if (sortOrder == null) {
            sortOrder = Kauppa.FULL_NIMI + " ASC";
        }

        // Tehdään joini kaupan ja sijainnin välille:
        // TODO: KOrjaa joini!
        kysely.setTables(Kauppa.TABLE_NAME + " LEFT OUTER JOIN " + Sijainti.TABLE_NAME + " ON " + Kauppa.SIJAINTI
                          + " = " + Sijainti.FULL_ID);

        // Ja palautellaan kursori:
        return kysely.query(readableDatabase,projection,selection,selectionArgs,null,null,sortOrder);
    }

    public static boolean onkoKauppaOlemassa(SQLiteDatabase db, long kauppa_id) {
            Cursor c = db.query(true, Kauppa.TABLE_NAME, new String[]{Kauppa._ID}, Kauppa._ID + "=" + kauppa_id, null, null, null, null, "1");
        return c.getCount() >0;
    }
}
