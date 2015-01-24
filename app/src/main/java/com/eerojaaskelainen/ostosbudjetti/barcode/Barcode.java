package com.eerojaaskelainen.ostosbudjetti.barcode;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.eerojaaskelainen.ostosbudjetti.R;

import java.util.List;

//import android.app.Fragment;

/**
 * Created by Eero on 16.1.2015.
 */
public class Barcode {
    // Tutkitaan, onko käyttäjän laitteessa tietämäämme lukijaa:

    public static final int BARCODE_REQUEST = 202;

    public static final boolean haeViivakoodi(Fragment activity) {
        if (onkoIntentSaatavilla(activity.getActivity(), "com.google.zxing.client.android.SCAN")) {
            // Käynnistetään applikaatio, joka lukee koodin:
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            activity.startActivityForResult(intent,BARCODE_REQUEST);
        }
        else {
            // Skanneriohjelmaa ei ole asennettuna!
            Toast.makeText(activity.getActivity(), R.string.barcode_scanner_not_found,Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private static final boolean onkoIntentSaatavilla(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List resolveInfo =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;

    }

    /**
     * Kutsutaan käyttävän activityn onActivityResultissa.
     * Tutkii skannauksen ja palauttaa joko validin koodin tai tyhjän jos koodia ei luettu.
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public static String onActivityResult(int requestCode, int resultCode, Intent data) {
        // Tutkitaan, oliko aktio viivakoodiskannerista, ja mistä niistä?
        String tulos = null;

        if (data == null)
            return null;
        if (data.getAction().equals("com.google.zxing.client.android.SCAN")) {
            String skannattuEan = data.getStringExtra("SCAN_RESULT");
            if (skannattuEan.length() > 3) {
                // Järkevän(?) kokoinen koodi on saatu. Lähetetään koodi tutkittavaksi, jos kannasta samalla koodilla löytyy sama tuote:
                tulos = skannattuEan;
            }
        }
        return tulos;
    }
}
