package com.eerojaaskelainen.ostosbudjetti.binders;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eerojaaskelainen.ostosbudjetti.models.Ostosrivi;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Eero on 11.1.2015.
 */
public class OstoskoriTuotelistaBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(Ostosrivi.A_HINTA)) {
            muunnaValuutaksi((TextView)view,cursor.getDouble(columnIndex));
            return true;
        }
        if (columnIndex == cursor.getColumnIndex(Ostosrivi.RIVISUMMA)) {
            muunnaValuutaksi((TextView)view,cursor.getDouble(columnIndex));
            return true;
        }
        return false;
    }

    /**
     * Muuntaa annetun luvun Localen mukaiseksi valuutaksi ja tallettaa sen annettuun tekstikenttään
     * @param v Tekstikenttä johon arvo talletetaan
     * @param maara Arvo joka muunnetaan valuutaksi
     */
    private static void muunnaValuutaksi(TextView v, double maara) {
        NumberFormat lukum = NumberFormat.getCurrencyInstance(Locale.getDefault());
        v.setText(lukum.format(maara));
    }
}
