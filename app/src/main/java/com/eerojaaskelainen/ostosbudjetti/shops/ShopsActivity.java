package com.eerojaaskelainen.ostosbudjetti.shops;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;

import java.util.Calendar;

public class ShopsActivity extends ActionBarActivity implements
        LocationListener {

    public static final String KAUPPA_ARGUMENT = "Kauppa";
    private Kauppa muokattavaKauppa;
    private Location uusiSijainti;
    private boolean tallennetaanSijainti;

    // Näytön elementit:
    protected TextView otsikko;
    protected EditText kaupanNimi;
    protected EditText kaupanOsoite;
    protected Button tallennaBtn;


    // Sijainnin pollaukseen:
    LocationManager locationManager;
    ProgressDialog odotusAnim;

    public ShopsActivity() {
        tallennetaanSijainti = false;
    }

    /**
     * Tarkistaa, onko kaupan tiedot kelvollisia tallennettaviksi
     * @return  Totuusarvo, onko kauppa validi
     */
    private boolean validoiData(){
        if (muokattavaKauppa == null)
            throw new IllegalArgumentException("muokattavaKauppa must be initialized!");

        if (kaupanNimi.getText().toString().trim().isEmpty()) return false;
        if (kaupanOsoite.getText().toString().trim().isEmpty()) return false;
        if (tallennetaanSijainti){
            if (uusiSijainti == null) return false;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops);
         setResult(RESULT_CANCELED);

        poimiElementit();
        if (getIntent().hasExtra(KAUPPA_ARGUMENT)) {
            // Edits the old one
            if (haeVanhaKauppaKannasta(getIntent().getLongExtra(KAUPPA_ARGUMENT,-1))) {
                asetteleKaupanTiedot();
            }
            else {
                // Vanhan kaupan haku failasi:
                muokattavaKauppa = new Kauppa();
            }
        }
        else {
            // New one
            muokattavaKauppa = new Kauppa();
        }
    }



    private void poimiElementit() {
        this.otsikko = (TextView)findViewById(R.id.store_title);
        this.kaupanNimi = (EditText)findViewById(R.id.store_name_input);
        this.kaupanOsoite = (EditText)findViewById(R.id.store_address_input);
        this.tallennaBtn = (Button)findViewById(R.id.store_save_button);
    }

    /**
     * Hakee kaupan tiedot kannasta ja tallettaa ne luokan ominaisuudeksi
     * @param kauppaID  Kaupan ID
     * @return  Totuusarvo; onnistuiko
     */
    private boolean haeVanhaKauppaKannasta(long kauppaID) {
        return false;
    }

    /**
     * Asettelee Kaupat-ominaisuudessa olevat tiedot näytön elementteihin
     */
    private void asetteleKaupanTiedot() {
        this.otsikko.setText(R.string.edit_shop_title);
        this.kaupanNimi.setText(this.muokattavaKauppa.getNimi());
        this.kaupanOsoite.setText(this.muokattavaKauppa.getOsoite());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shops, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Tallenna-painikkeen kuuntelija:
     * @param v
     */
    public void tallennaBtnClick(View v)
    {
        tallennaMuutokset();
    }


    /**
     * Kun paluunuolta painetaan, katsotaan kannan systeemit valmiiksi, ja vasta sitten mennään:
     */
    @Override
    public void onBackPressed() {
            poistutaan();
    }

    /**
     * Kun fyysistä paluunuolta painetaan, katsotaan kannan systeemit valmiiksi, ja vasta sitten mennään:
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
                poistutaan();
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * Poistutaan activitystä. Tarkistaa mahdollisesti muuttuneet tiedot ja kysyy tallennetaanko.
     * @return  true jos poistuminen sallitaan, muuten false.
     */
    protected void poistutaan()
    {
        if (tuoteMuuttunut()&! muokattavaKauppa.onUusi() ){
            // Tuote on muuttunut! Kysytään haluaako tallentaa vai poistutaanko vain.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_modifications_unsaved_title)
                    .setMessage(R.string.alert_confirmation_message)
                    .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
            return;
        }
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Muokattujen/uuden kaupan tallennus
     */
    private void tallennaMuutokset() {
        if (!validoiData()) {
            naytaVirhe(R.string.alert_invalid_field_message);
            return;
        }
        boolean tallennusOnnistui = false;

        tallennusOnnistui = true;/*  TODO: Tee kaupan muokkaus/luonti
        // Muodosta ContentValue -olio kantatalletusta varten:
        ContentValues cV = new ContentValues();
        cV.put(Kauppa.NIMI,kaupanNimi.getText().toString());
        cV.put(Kauppa.OSOITE,kaupanOsoite.getText().toString());
        //cV.put(Kauppa.SIJAINTI,null)

        // Tee kantatoiminnot:
        if (muokattavaKauppa.onUusi())
        {
            // Tallennetaan uusi kauppa kantaan:
            Uri muuttunutKauppa = getContentResolver().insert(
                    Uri.withAppendedPath(KaupatContentProvider.CONTENT_URI,"stores"),
                    cV
            );
            try {
                haeVanhaKauppaKannasta(Long.parseLong(muuttunutKauppa.getLastPathSegment()));
                tallennusOnnistui = true;
            }
            catch (NumberFormatException e)
            {
                tallennusOnnistui = false;
            }
        }
        else {
            //Kauppa on vanha
            tallennusOnnistui = (getContentResolver().update(
                                Uri.withAppendedPath(KaupatContentProvider.CONTENT_URI,String.format("stores/%s",muokattavaKauppa.getID())),
                                cV,null,null
                                ) !=1);
        }*/
        if (tallennusOnnistui)
        {
            setResult(RESULT_OK);
            finish();//asetteleKaupanTiedot();
        }
        else
        {
            naytaVirhe(R.string.alert_store_to_database_failed);
        }

    }

    /**
     * Näyttää virheen käyttäjälle.
     * Käyttäjällä voi joko palata muokkaamaan tai poistua näytöstä
     * @param message   Viestin Resource-ID
     */
    private void naytaVirhe(int message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_things_not_ok)
                .setMessage(message)
                .setPositiveButton(R.string.alert_invalid_field_positive_response, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Käyttäjä haluaa palata muokkaamaan:
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.alert_invalid_field_negative_response,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Käyttäjä haluaa poistua näytöstä:
                        finish();
                    }
                }).show();
    }

    /**
     * Tarkistaa, onko tallentamattomia muutoksia
     * @return  true jos muuttunut, muuten false
     */
    private boolean tuoteMuuttunut() {
        if (muokattavaKauppa.onUusi()) return true;
        if (!muokattavaKauppa.getNimi().equals(kaupanNimi.getText().toString())) return true;
        if (!muokattavaKauppa.getOsoite().equals((kaupanOsoite.getText().toString()))) return true;
        if (tallennetaanSijainti) {
            if (!muokattavaKauppa.getSijainti().equals(uusiSijainti)) return true;
        }
        return false;
    }

    // Lokaation hanskaukseen:
    public void lueLokaatio(View v) {
        odotusAnim = ProgressDialog.show(this,getString(R.string.progress_location_poll_title),getString(R.string.progress_location_poll_body),true);

        runOnUiThread(new Runnable(){
            public void run() {
                try {
                    PollaaLokaatio();
                } catch (Exception e) {
                    odotusAnim.dismiss();
                    PaivitaLokaatio();
                }
            }
        });
    }
    private void PollaaLokaatio(){

       if (locationManager == null) {
           locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
       }
           uusiSijainti = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

           if(uusiSijainti != null && uusiSijainti.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
              odotusAnim.dismiss();
              PaivitaLokaatio();
               //  otherwise wait for the update below
           }
           else {
               locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
           }

    }

    private void PaivitaLokaatio() {
        String lokaatio;
        if (uusiSijainti == null)
            lokaatio = "";
        else
            lokaatio = " "+ uusiSijainti.getLatitude() + "N, "+ uusiSijainti.getLongitude() + "E";

        ((TextView)findViewById(R.id.store_location_info)).setText(getString(R.string.store_location_label) + lokaatio );
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            locationManager.removeUpdates(this);
            odotusAnim.dismiss();
            PaivitaLokaatio();
        }
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        if (locationManager!=null){
            locationManager.removeUpdates(this);
            locationManager = null;
        }
        super.onPause();
    }

    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p/>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p/>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {    }
}
