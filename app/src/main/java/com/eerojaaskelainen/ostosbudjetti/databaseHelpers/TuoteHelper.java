package com.eerojaaskelainen.ostosbudjetti.databaseHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

/**
 * Created by Eero on 3.1.2015.
 */
public class TuoteHelper {

    public static Cursor haeTuoteCursor(SQLiteDatabase readableDatabase,String[] projection, String selection, String[] selectionArgs, String sortOrder, String tuoteID,String tuoteEAN) {
        SQLiteQueryBuilder kysely = new SQLiteQueryBuilder();
        kysely.setTables(Tuote.TABLE_NAME);

        String limit = null;

        if (tuoteID != null) {
            kysely.setDistinct(true);
            kysely.appendWhere(Tuote._ID + "=" + tuoteID);
            limit = "1";
        }
        if (tuoteEAN != null) {
            kysely.setDistinct(true);
            kysely.appendWhere(Tuote.EAN + "=" + tuoteEAN);
            limit = "1";
        }

        projection = lisaaViimeisinhinta(projection);

        Cursor c = kysely.query(
                readableDatabase,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder,
                limit
        );
        c.moveToFirst();
        return c;
    }

    private static String[] lisaaViimeisinhinta(String[] projection) {
        if (projection == null) {
            return new String[] {Tuote._ID,
                    Tuote.NIMI,
                    Tuote.EAN,
                    Tuote.VALMISTAJA,
                    Tuote.VIIMEISINHINTA_CONSTRUCT};
        }
        else {
            String[] tulos = new String[projection.length+1];
            for (int i=0; i< projection.length;i++) {
                tulos[i] = projection[i];
            }
            tulos[projection.length] = Tuote.VIIMEISINHINTA_CONSTRUCT;
            return  tulos;
        }
    }

    public static final Cursor haeValmistajanTuotteet(SQLiteDatabase readableDatabase,String[] projection, String selection, String[] selectionArgs, String sortOrder, String valmistaja) {
        //TODO: Tee valmistajan tuotelistahaku!
        throw new UnsupportedOperationException("Valmistajan tuotelistahakua ei ole tehty viel");
    }


    public static boolean onkoTuotetta(SQLiteDatabase writableDatabase, long tuote_id) {
        Cursor c = haeTuoteCursor(writableDatabase,
                new String[]{Tuote._ID},
                null,
                null,
                null,
                Long.toString(tuote_id),
                null);

        return (c.getCount()>0);
    }
    public static boolean onkoTuotetta(SQLiteDatabase writableDatabase, String EANkoodi) {
        Cursor c = haeTuoteCursor(writableDatabase,
                new String[]{Tuote._ID},
                null,
                null,
                null,
                null,
                EANkoodi);

        return (c.getCount()>0);
    }

    public static long lisaaTuote(SQLiteDatabase writableDatabase,String tuotenimi,String valmistaja, String EANkoodi) {
        if (onkoTuotetta(writableDatabase,EANkoodi)) {
            // Tuote löytyi samalla EAN-koodilla. Ei onnistu!
            throw new IllegalArgumentException("Product with the same EAN-code ("+ EANkoodi + ") exists!");
        }
        // Muuten lisätään:
        ContentValues cV = new ContentValues();
        cV.put(Tuote.EAN,EANkoodi);
        cV.put(Tuote.VALMISTAJA,valmistaja);
        cV.put(Tuote.NIMI,tuotenimi);
        return writableDatabase.insert(Tuote.TABLE_NAME,null,cV);
    }

    public static int muokkaaTuotetta(SQLiteDatabase writableDatabase, String tuoteID, ContentValues values) {
        //TODO: Tee tuotteen muokkausmetodi!
        throw new UnsupportedOperationException("TUotemuokkaust ei viel tehty!");
    }

    public static Cursor haeValmistajatCursor(SQLiteDatabase readableDatabase, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (projection == null) projection = new String[]{Tuote._ID, Tuote.VALMISTAJA};

        return readableDatabase.query(true, Tuote.TABLE_NAME,
                projection, selection, selectionArgs,Tuote.VALMISTAJA, null, sortOrder, null);
    }
}
