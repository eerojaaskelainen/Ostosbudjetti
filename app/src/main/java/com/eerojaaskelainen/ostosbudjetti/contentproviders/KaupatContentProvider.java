package com.eerojaaskelainen.ostosbudjetti.contentproviders;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.eerojaaskelainen.ostosbudjetti.databaseHelpers.Ostoskanta;

public class KaupatContentProvider extends ContentProvider {
    // Authority on osoite, jolla tämä provider löytyy. Sama tulee olla Manifestissa!
    public static final String AUTHORITY = "com.eerojaaskelainen.ostosbudjetti.kaupat";

    // Tässä itse datan hanskaava olio:
    private Ostoskanta ostoskanta;

    // Annetaan valmis osoiterunko käytettäväksi:
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");

    // URIhelperille constantteja:
    private static final int KAUPAT = 1;
    private static final int KAUPPA_ID = 10;
    private static final int KAUPPA_NIMI = 11;

    // URIhelperi itsessään: Annetaan defaultti match, eli ei osumaa.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Ja kiinnitetään matcherille urlit, joita se osaa hanskata:
    static {
        sUriMatcher.addURI(AUTHORITY,"stores",KAUPAT);      // Eli napsahtaa kun kutsutaan com.eerojaaskelainen.ostosbudjetti.kaupat/stores.
        sUriMatcher.addURI(AUTHORITY,"stores/#",KAUPPA_ID); // Risuaita merkitsee muuttujaa.
        sUriMatcher.addURI(AUTHORITY,"store/#",KAUPPA_NIMI);
    }

    public KaupatContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Julkiselle Content Providerille on tärkeää palauttaa oma uniikki tyyppitieto
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {

        // at the given URI.
        switch (sUriMatcher.match(uri)) {
            case KAUPAT:
                return "vnd.android.cursor.dir/com.eerojaaskelainen.ostosbudjetti.kauppa";
            case KAUPPA_ID:
            case KAUPPA_NIMI:
                return "vnd.android.cursor.item/com.eerojaaskelainen.ostosbudjetti.kauppa";
            default:
                return "vnd.android.cursor/com.eerojaaskelainen.ostosbudjetti.kauppa";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // Alustetaan tietokanta-olio:
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
        // Täällä tehdään tulostus asiasta. Eli sekä lista kauppoja, että yksittäinen kauppa palautetaan kursorina:

        Cursor osumat;
        // Ensin etsitään se, mitä halutaan etsiä:

        int kysely = sUriMatcher.match(uri);

        switch (kysely) {
            case KAUPAT:
                //com.eerojaaskelainen.ostosbudjetti.kaupat/stores:
                osumat = ostoskanta.haeKaupatCursor(projection,selection,selectionArgs,sortOrder,null,null);
                break;

            case KAUPPA_ID:
                // Kysytään kaupan ID:llä:
                //com.eerojaaskelainen.ostosbudjetti.kaupat/stores/NNN:
                String haettavaID = uri.getLastPathSegment();
                osumat = ostoskanta.haeKaupatCursor(projection,selection,selectionArgs, sortOrder, haettavaID, null);
                break;

            case KAUPPA_NIMI:
                // Kysytään kaupan nimellä:
                //com.eerojaaskelainen.ostosbudjetti.kaupat/store/XXXXYYYY:
                String haettavaKauppa = uri.getLastPathSegment();
                osumat = ostoskanta.haeKaupatCursor(projection,selection,selectionArgs, sortOrder, null,haettavaKauppa);
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
