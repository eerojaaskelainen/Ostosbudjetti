package com.eerojaaskelainen.ostosbudjetti.shops;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;

public class ShopsActivity extends ActionBarActivity {

    public static final String KAUPPA_ARGUMENT = "Kauppa";
    private Kauppa muokattavaKauppa;
    private Location uusiSijainti;
    private boolean tallennetaanSijainti;

    // Näytön elementit:
    protected TextView otsikko;
    protected EditText kaupanNimi;
    protected EditText kaupanOsoite;
    protected Button tallennaBtn;

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
        if (tuoteMuuttunut()){
            // Tuote on muuttunut! Kysytään haluaako tallentaa vai poistutaanko vain.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_modifications_unsaved_title)
                    .setMessage(R.string.alert_confirmation_message)
                    .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tallennaMuutokset();
                        }
                    })
                    .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
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
}
