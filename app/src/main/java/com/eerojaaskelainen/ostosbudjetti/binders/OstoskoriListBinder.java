package com.eerojaaskelainen.ostosbudjetti.binders;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
//import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;

import java.text.DateFormat;
import java.util.Date;

/**
 * Binderi ajetaan jokaisen listarivin elementin kanssa, eli siellä voi tehdä oman arvon asetuksen.
 * Tässä muutamme aikaleiman ymmärrettävämpään muotoon (dd.mm.yyyy klo hh:mm).
 * Created by Eero on 6.1.2015.
 */
public class OstoskoriListBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(Ostoskori.PVM))  {
            // Tämä on päivämäärä-kenttä, jota nyt luodaan.
            // Muunna kursorissa oleva epoch visuaalisempaan muotoon:
            TextView v = (TextView)view;

            asetaEpochAika(v,new Date(cursor.getLong(cursor.getColumnIndex(Ostoskori.PVM))));

            v.setText(cursor.getString(cursor.getColumnIndex(Kauppa.NIMI)) + " (" + v.getText() + ")");
            return true;    // Muutimme ulkoasua, eli Adapter saa luvan käyttää tätä muutosta.
        }
        // Muissa riveissä menköön cursorin omalla bindausmenetelmällä:
        return false;
    }

    /**
     * Muunna kannassa oleva aikaleima ihmisystävälliseen muotoon:
     * @param muokkausPvm
     * @param riviPvm
     */
    private static void asetaEpochAika(TextView muokkausPvm, Date riviPvm) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
        muokkausPvm.setText(df.format(riviPvm));
    }
}
