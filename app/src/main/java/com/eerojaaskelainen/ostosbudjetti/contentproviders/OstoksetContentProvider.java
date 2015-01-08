package com.eerojaaskelainen.ostosbudjetti.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.eerojaaskelainen.ostosbudjetti.databaseHelpers.Ostoskanta;
import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;

public class OstoksetContentProvider extends ContentProvider {

    // Authority on osoite, jolla tämä provider löytyy. Sama tulee olla Manifestissa!
    public static final String AUTHORITY = "com.eerojaaskelainen.ostosbudjetti.ostokset";

    // Tässä itse datan hanskaava olio:
    private Ostoskanta ostoskanta;

    // Annetaan valmis osoiterunko käytettäväksi:
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");

    // URIhelperille constantteja:
    private static final int OSTOSKORIT = 1;
    private static final int OSTOSKORI_ID = 10;
    private static final int OSTOSKORI_UUSI = 11;

    private static final int OSTOSKORIN_RIVIT = 2;
    private static final int OSTOSKORIN_RIVI_ID = 20;
    private static final int OSTOSKORIN_RIVI_UUSI = 21;

    // URIhelperi itsessään: Annetaan defaultti match, eli ei osumaa.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Ja kiinnitetään matcherille urlit, joita se osaa hanskata:
    static {
        sUriMatcher.addURI(AUTHORITY,"baskets",OSTOSKORIT);      // Eli napsahtaa kun kutsutaan com.eerojaaskelainen.ostosbudjetti.kaupat/stores.
        sUriMatcher.addURI(AUTHORITY,"baskets/new",OSTOSKORI_UUSI);
        sUriMatcher.addURI(AUTHORITY,"baskets/#",OSTOSKORI_ID); // Risuaita merkitsee muuttujaa.

        sUriMatcher.addURI(AUTHORITY,"baskets/#/rows",OSTOSKORIN_RIVIT);
        sUriMatcher.addURI(AUTHORITY,"baskets/#/rows/new",OSTOSKORIN_RIVI_UUSI);
        sUriMatcher.addURI(AUTHORITY,"baskets/#/rows/#",OSTOSKORIN_RIVI_ID);

    }





    public OstoksetContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (sUriMatcher.match(uri)) {
            case OSTOSKORIT:
                return "vnd.android.cursor.dir/com.eerojaaskelainen.ostosbudjetti.ostoskori";
            case OSTOSKORI_UUSI:
            case OSTOSKORI_ID:
                return "vnd.android.cursor.item/com.eerojaaskelainen.ostosbudjetti.ostoskori";
            case OSTOSKORIN_RIVIT:
                return "vnd.android.cursor.dir/com.eerojaaskelainen.ostosbudjetti.ostosrivi";
            case OSTOSKORIN_RIVI_UUSI:
            case OSTOSKORIN_RIVI_ID:
                return "vnd.android.cursor.item/com.eerojaaskelainen.ostosbudjetti.ostosrivi";
            default:
                throw new IllegalArgumentException("Incorrect URI: "+ uri);
        }
    }

    /**
     * Luo uuden ostoskorin tai ostosrivin.
     * @param uri   URI jolla tultiin.
     * @param values    Arvot joita talletetaan
     * @return  Luodun rivin URI:n
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long luotuID;

        switch (sUriMatcher.match(uri)) {
            case OSTOSKORI_UUSI:
                if (!values.containsKey("store_id"))
                    throw new IllegalArgumentException("store_id was not found!");

                luotuID = ostoskanta.luoOstoskori(values.getAsLong("store_id"));
                break;
            case OSTOSKORIN_RIVI_UUSI:

                luotuID = ostoskanta.luoOstosrivi(
                        values.getAsLong("basket_id"),
                        values.getAsLong("product_id"),
                        values.getAsDouble("unit_price"),
                        values.getAsInteger("amount")
                );

                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri palauta = null;
        if (luotuID != -1) {
            // Palautetaan luodun rivin URI
            palauta = ContentUris.withAppendedId(CONTENT_URI,luotuID);
            // Informeeraa resolverille että nyt muuttui listat:
            getContext().getContentResolver().notifyChange(palauta,null);
        }
        return palauta;
    }

    @Override
    public boolean onCreate() {
        // Muista alustaa Content Provider tässä! Muuten paukkuu nullpointerexceptionia...
        ostoskanta = new Ostoskanta(getContext());
        return false;
    }

    /**
     * Täällä sitten tehdään haut, eli REST-tyyppisesti GET.
     * Koska tätä kutsutaan aina kun halutaan hakea, on pakko tutkia mitä haetaan. Sen homman hanskaa UriMatcher.
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor osumat;

        // Haetaan etsittävä asia urista:
        int kysely = sUriMatcher.match(uri);

        switch (kysely) {
            case OSTOSKORIT:
                //com.eerojaaskelainen.ostosbudjetti.ostokset/baskets:
                osumat = ostoskanta.haeOstoskoritCursor(projection, selection, selectionArgs, sortOrder, null);
                break;
            case OSTOSKORI_ID:
                // Kysytään korin ID:llä:
                //com.eerojaaskelainen.ostosbudjetti.ostokset/baskets/NNN:
                String haettavaID = uri.getLastPathSegment();
                osumat = ostoskanta.haeOstoskoritCursor(projection, selection, selectionArgs, sortOrder, haettavaID);
                break;
            case OSTOSKORIN_RIVIT:
            case OSTOSKORIN_RIVI_ID:
                // Ostoskorin rivejä kysytään:
                //com.eerojaaskelainen.ostosbudjetti.ostokset/baskets/NNN/rows:
                String koriID = uri.getPathSegments().get(1);
                try {
                    Long.parseLong(koriID);
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Malformed basket ID: "+ koriID);
                }

                String riviID = null;
                if (kysely == OSTOSKORIN_RIVI_ID) {
                    riviID = uri.getLastPathSegment();
                }

                osumat = ostoskanta.haeOstosrivitCursor(projection,selection,selectionArgs,sortOrder,koriID,riviID);
                break;

            default:
                // Ei osunut yksikään tunnetuista:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Annetaan kuuntelijoille infona osoite, jota täkytään kun kama muuttuu kursorissa:
        osumat.setNotificationUri(getContext().getContentResolver(),uri);

        // Ja kursori lähtee:
        return osumat;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void shutdown() {
        super.shutdown();
        ostoskanta.close();
    }
}
