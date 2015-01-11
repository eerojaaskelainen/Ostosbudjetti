package com.eerojaaskelainen.ostosbudjetti.databaseHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;

/**
 * Created by Eero on 2.1.2015.
 */
public class OstoskoriHelper {

    public static Ostoskori haeOstoskori(SQLiteDatabase readableDatabase, long ostoskoriID) {
        Cursor c = haeOstoskoriCursor(readableDatabase,
                new String[]{Ostoskori._ID,Ostoskori.KAUPPA,Ostoskori.PVM},
                null,
                null,
                null,
                Long.toString(ostoskoriID));
        if (c.getCount() <=0)
            return null;

        return Ostoskori.convertCursorToOstoskori(c);

    }
    public static Cursor haeOstoskoriCursor(SQLiteDatabase readableDatabase,String[] projection, String selection, String[] selectionArgs, String sortOrder, String korinID) {
        SQLiteQueryBuilder kysely = new SQLiteQueryBuilder();
        //kysely.setTables(Ostoskori.TABLE_NAME + " NATURAL JOIN " + Kauppa.TABLE_NAME);
        kysely.setTables(Ostoskori.TABLE_NAME + " LEFT JOIN " + Kauppa.TABLE_NAME + " ON " + Ostoskori.FULL_KAUPPA + " = "+ Kauppa.FULL_ID);

        if (projection == null) {
            // Asetetaan selectiin oletuksena kaikki sarakenimet, koska LEFT join päästää läpi usean _id -kentän...
            projection = new String[] {
              Ostoskori.FULL_ID, Ostoskori.FULL_KAUPPA, Ostoskori.FULL_PVM,
              Kauppa.FULL_NIMI, Kauppa.FULL_OSOITE, Kauppa.FULL_SIJAINTI
            };
        }
        String limit = null;

        if (korinID != null) {
            kysely.setDistinct(true);
            kysely.appendWhere(Ostoskori.FULL_ID + "=" + korinID);
            limit = "1";
        }

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

        return c;
    }


protected static final boolean onkoOstoskoria(SQLiteDatabase db, long ostoskori_id) {
    Cursor c = haeOstoskoriCursor(db, new String[] {Ostoskori._ID}, null,null,null,Long.toString(ostoskori_id));

    return (c.getCount()>0);
}

    protected static final boolean onkoOstoskoria(SQLiteDatabase db,long kauppa_id, long pvm) {
        Cursor c = db.query(Ostoskori.TABLE_NAME,
                new String[] {Ostoskori._ID},
                Ostoskori.KAUPPA + "=? AND "+ Ostoskori.PVM + "=?",
                new String[]{Long.toString(kauppa_id),
                Long.toString(pvm)},
                null,null,null);

        return (c.getCount() >0);
    }

    public static Long luoOstoskori(SQLiteDatabase writableDatabase, Long kauppa_id, long pvm) {


        ContentValues cv = new ContentValues();
        if (kauppa_id != null) {
            if (!KauppaHelper.onkoKauppaOlemassa(writableDatabase, kauppa_id))
                throw new IllegalArgumentException("Store with ID " + kauppa_id + " was not found!");
            cv.put(Ostoskori.KAUPPA,kauppa_id);
        }
        cv.put(Ostoskori.PVM,pvm);

        /*if (onkoOstoskoria(writableDatabase,kauppa_id,pvm)) {
            throw new IllegalArgumentException("Cart with store id " + kauppa_id + " and timestamp of "+ pvm + " already exists!");
        }*/

        return writableDatabase.insert(Ostoskori.TABLE_NAME,null,cv);
    }

    public static int muokkaaOstoskoria(SQLiteDatabase writableDatabase, String ostoskoriID, ContentValues arvot) {
        if (ostoskoriID == null)
            throw new IllegalArgumentException("Shopping basket id must be set!");

        try {
            Long.parseLong(ostoskoriID);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Shopping basket id must be numeric!");
        }

        return writableDatabase.update(Ostoskori.TABLE_NAME,arvot,Ostoskori._ID + " = "+ostoskoriID,null);
    }

    public static int poistaOstoskori(SQLiteDatabase writableDatabase, String ostoskoriID) {
        if (ostoskoriID == null)
            throw new IllegalArgumentException("Shopping basket ID must be!");

        try {
            Long.parseLong(ostoskoriID);
        } catch (Exception e) {
            throw new IllegalArgumentException("Shopping basket id must be numeric!");
        }
        return writableDatabase.delete(Ostoskori.TABLE_NAME, Ostoskori._ID + " = " + ostoskoriID, null);
    }
}
