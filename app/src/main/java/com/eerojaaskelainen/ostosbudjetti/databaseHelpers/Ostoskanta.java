package com.eerojaaskelainen.ostosbudjetti.databaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;
import com.eerojaaskelainen.ostosbudjetti.models.Ostosrivi;
import com.eerojaaskelainen.ostosbudjetti.models.Sijainti;
import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Eero on 30.12.2014.
 */
public class Ostoskanta extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ostoskanta.db";    // Tietokannan tiedostonimi
    private static final int DATABASE_VERSION = 1;                  // Tietokannan versio
    public static final String SQLHELPER_TAG = "Ostoskanta";

    public Ostoskanta(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(SQLHELPER_TAG, "Luodaan uusi tietokanta " + DATABASE_NAME + ", versio " + DATABASE_VERSION);

        // Talletetaan sijainti:
        Log.d(SQLHELPER_TAG,"Luodaan taulu " + Sijainti.TABLE_NAME + " komennolla "+ Sijainti.TABLE_CREATE);
        db.execSQL(Sijainti.TABLE_CREATE);

        // Talletetaan Kauppa:
        Log.d(SQLHELPER_TAG,"Luodaan taulu " + Kauppa.TABLE_NAME + " komennolla "+ Kauppa.TABLE_CREATE);
        db.execSQL(Kauppa.TABLE_CREATE);

        // Talletetaan ostoskori:
        Log.d(SQLHELPER_TAG,"Luodaan taulu " + Ostoskori.TABLE_NAME + " komennolla "+ Ostoskori.TABLE_CREATE);
        db.execSQL(Ostoskori.TABLE_CREATE);

        // Talletetaan tuote:
        Log.d(SQLHELPER_TAG,"Luodaan taulu " + Tuote.TABLE_NAME + " komennolla "+ Tuote.TABLE_CREATE);
        db.execSQL(Tuote.TABLE_CREATE);

        // Talletetaan ostoskorin rivi:
        Log.d(SQLHELPER_TAG,"Luodaan taulu " + Ostosrivi.TABLE_NAME + " komennolla "+ Ostosrivi.TABLE_CREATE);
        db.execSQL(Ostosrivi.TABLE_CREATE);


        AlustaTaulut(db);

    }

    /**
     * Jos tietokannan versio muuttuu, otetaan ja päivitetään tietokannan taulut.
     * Tämä metodi tuhoaa vanhan tietokannan, ja luo uuden käyttäen onCreate() luojaa.
     * @param db
     * Tietokanta-adapteri joka suorittaa ajon valittuun tietokantaan
     * @param oldVersion
     * Vanha tietokannan versio
     * @param newVersion
     * Uusi tietokannan versio
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLHELPER_TAG,"Päivitetään tietokanta versiosta " + oldVersion + " versioon " + newVersion + ". Tämä tuhoaa kaiken entisen datan!");

        db.execSQL("DROP TABLE IF EXISTS "+ Ostosrivi.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Ostoskori.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Kauppa.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Sijainti.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Tuote.TABLE_NAME);


        onCreate(db);
    }

    // Metodit:
    //---------------------------------------

    // Sijainnit:

    public long luoSijainti(Location sijainti) {
        return SijaintiHelper.luoSijainti(this.getWritableDatabase(),sijainti);
    }
    public long luoSijainti(double latitude, double longitude, float zoom) {
        return SijaintiHelper.luoSijainti(this.getWritableDatabase(),latitude, longitude, zoom);
    }
    public List<Sijainti> haeSijainnit() {
        return SijaintiHelper.haeSijainnit(this.getReadableDatabase());
    }

    // Kaupat:
    //---------------------------------------

    public long luoKauppa(Kauppa kauppa) {
        return KauppaHelper.luoKauppa(this.getWritableDatabase(),kauppa);
    }
    public long luoKauppa(String nimi, String osoite,long sijainti_id ) {
        return KauppaHelper.luoKauppa(this.getWritableDatabase(),nimi, osoite, sijainti_id);
    }

    /**
     * Hakee kursorin halutu(i)sta kaupasta/kaupoista.
     * @param projection    Mitä sarakkeita
     * @param selection     Minkä mukaan haetaan
     * @param selectionArgs Minkä mukaan haetaan, lista stringejä, jos edellä määriteltiin parametreihin ? merkkejä.
     * @param sortOrder     Missä järestyksessä? Oletus on kaupan nimi nousevassa järjestyksessä.
     * @param kaupanID      Jos haetaan kaupan ID:llä. NULL jos ei haeta.   TAI
     * @param haettavaKauppa Jos haetaan kaupan nimellä. NULL jos ei haeta.
     * @return              Palauttaa kursorin tuloksia.
     *
     */
    public Cursor haeKaupatCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder, String kaupanID, String haettavaKauppa) {
        return KauppaHelper.haeKaupatCursor(this.getReadableDatabase(), projection,selection,selectionArgs,sortOrder,kaupanID,haettavaKauppa);
    }

    public void AlustaTaulut(SQLiteDatabase db) {
        // Anna kaikille sama db! Muuten tulee IllegalStateException: getWritableDatabase called recursively.
        // Luodaan sijainnit:
        //------------------------------------
        Long lidlSijainti = SijaintiHelper.luoSijainti(db, 60.395294, 23.126995, 50);
        Long prismaSijainti = SijaintiHelper.luoSijainti(db, 60.394300, 23.081973, 50);

        // Luodaan kaupat:
        //-------------------------------------
        Long lidlKauppa = KauppaHelper.luoKauppa(db, "Lidl Salo", "Savipajankatu 3, 24260 Salo", lidlSijainti);
        Long prismaKauppa = KauppaHelper.luoKauppa(db, "Prisma Halikko", "Prismantie 2, 24800 Halikko", prismaSijainti);


        // Luodaan pari ostoslistaa:
        //--------------------------
        Calendar c = Calendar.getInstance();
        c.set(2014,11,24);

        Long lidlLista1 = OstoskoriHelper.luoOstoskori(db,lidlKauppa,c.getTimeInMillis());
        Long prismaLista1 = OstoskoriHelper.luoOstoskori(db,prismaKauppa,c.getTimeInMillis());

        c.set(2014,12,30);
        Long lidlLista2 = OstoskoriHelper.luoOstoskori(db,lidlKauppa,c.getTimeInMillis());


        // Luodaan tuotteita:
        //-------------------
        long tuote1 = TuoteHelper.lisaaTuote(db,"Rasvaton maito","Milbona","20064006");
        long tuote2 = TuoteHelper.lisaaTuote(db,"Rasvaton maito","Rainbow","6414893386495");
        long tuote3 = TuoteHelper.lisaaTuote(db,"Kevyt kasvirasvalevite 38%", "Becel","8722700246992");

        // Luodaan ostoslistoille rivejä:
        //---------------

        long lidlLista1Tuote1 = OstosriviHelper.luoOstosrivi(db,lidlLista1,tuote1,0.85,4);
        long prismaLista1Tuote1 = OstosriviHelper.luoOstosrivi(db,prismaLista1,tuote2,0.75,3);
        long prismaLista1Tuote2 = OstosriviHelper.luoOstosrivi(db,lidlLista1,tuote3,2,1);


    }

    public long luoOstoskori() {
        return OstoskoriHelper.luoOstoskori(this.getWritableDatabase(),null, Calendar.getInstance().getTimeInMillis());
    }
    public Ostoskori haeOstoskori(long ostoskoriID) {
        return OstoskoriHelper.haeOstoskori(this.getReadableDatabase(),ostoskoriID);
    }

    public Cursor haeOstoskoritCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder, String korinID) {
       return  OstoskoriHelper.haeOstoskoriCursor(this.getReadableDatabase(),projection,selection,selectionArgs,sortOrder,korinID);
    }

    public long luoOstosrivi(long ostoskoriID, long tuoteID, double ahinta, int lukumaara) {
        return OstosriviHelper.luoOstosrivi(this.getWritableDatabase(),ostoskoriID,tuoteID,ahinta,lukumaara);
    }
    public Cursor haeOstosrivitCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder, String koriID, String riviID) {
        return OstosriviHelper.haeOstosrivitCursor(this.getReadableDatabase(),projection,selection,selectionArgs,sortOrder,koriID,riviID);
    }

    public int muokkaaOstoskoria(String ostoskoriID, ContentValues arvot) {
        return OstoskoriHelper.muokkaaOstoskoria(this.getWritableDatabase(),ostoskoriID,arvot);
    }

    public int poistaOstoskori(String ostoskoriID) {
        return OstoskoriHelper.poistaOstoskori(this.getWritableDatabase(),ostoskoriID);
    }

    public int muokkaaOstosrivia(String ostosRiviID, ContentValues values) {
        return OstosriviHelper.muokkaaOstosrivia(this.getWritableDatabase(),ostosRiviID,values);
    }

    public Cursor haeTuotteetCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder, String ean) {
        return TuoteHelper.haeTuoteCursor(this.getReadableDatabase(),projection,selection,selectionArgs,sortOrder,null,ean);
    }

    public int muokkaaTuotetta(String tuoteID, ContentValues values) {
        return TuoteHelper.muokkaaTuotetta(this.getWritableDatabase(),tuoteID,values);
    }

    public long luoTuote(ContentValues values) {
        if (!(values.containsKey(Tuote.NIMI) || values.containsKey(Tuote.EAN) || values.containsKey(Tuote.VALMISTAJA)))
            throw new IllegalArgumentException("New product must contain "+ Tuote.NIMI + ", "+ Tuote.EAN + " and "+ Tuote.VALMISTAJA +" values!");


        return TuoteHelper.lisaaTuote(this.getWritableDatabase(),
                values.getAsString(Tuote.NIMI),
                values.getAsString(Tuote.VALMISTAJA),
                values.getAsString(Tuote.EAN));
    }
}