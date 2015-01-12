package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eerojaaskelainen.ostosbudjetti.AddItemActivity;
import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.OstoksetContentProvider;
import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class EditShoppinglistActivity extends ActionBarActivity implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        ShopsFragment.OnKauppaSelectedListener // Kauppalistan muutoskuuntelija
{

    public static final String TAG = "EditShoppingListActivity";
    public static final int OSTOSLISTA_ACTIVITYREULT = 20;
    // Yksilöity luku aktivityn tuloskäsittelyyn:
    public static final int KAUPPA_ACTIVITYREULT = 30;
    // Yksilöity luku ostoslistan tuloskäsittelyyn
    //Ostoskanta kanta;


    protected Ostoskori ostoskori;

    protected TextView pvmTV;
    protected TextView kloTV;
    //protected ListView ostokset;

    // Tämä on poistumista varten
    private boolean poistutaanActivitysta = false;

    /**
     * Kun activityä tapetaan, poimitaan sitä ennen ostoskori talteen tilakoneeseen:
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("Ostoskori",ostoskori);

        super.onSaveInstanceState(outState);
    }

    /**
     * Tilakoneesta on löytynyt edeltävä tila. Otetaan käyttöön sen ostoskori.
     * Alunperin ideana oli korjata ostoskorin hukkaan joutuminen uuden ostosrivin lisäyksen aikana,
     * mutta tämän hanskaa nyt Manifestissa oleva argumentti android:launchMode="singleTop".
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("Ostoskori"))
            ostoskori = savedInstanceState.getParcelable("Ostoskori");

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_shoppinglist);

        // Poimitaan / Tekaistaan ostoskori:
        //--------------------------------------------------------------------------
        // Tutkitaan tilakone, tullaanko esim. uuden rivin lisäyksestä:
        if (savedInstanceState == null || ostoskori == null) {
            //
            // Poimitaan mahdollinen edeltävä ostoskori, tai sitten luodaan uusi.
            poimiOstoskoriKyselijaltaTaiDefault();
        }
        else {
            ostoskori = savedInstanceState.getParcelable("Ostoskori");
        }

        // Ostoskori OK, jatketaan:
        //--------------------------------------------------------------------------


        // Noudetaan näytön elementit:
        pvmTV = (TextView) findViewById(R.id.shoppinglistedit_date);
        kloTV = (TextView) findViewById(R.id.shoppinglistedit_time);
        //ostokset = (ListView) findViewById(R.id.shoppinglistedit_list);


        // Asetetaan näytön aikakentät vastaamaan muokattavaa koria:
        asetaAika();

        // Aktivoidaan fragmentit:
        //--------------------------------------------
        // Jos fragmentti olis staattista, menisi se XML:n kautta. Mutta nyt viedään sille valittu kauppa:

        // Mut jos tullaan aiemmasta, ei tarvitse tehä uuvelleen. Muuten voi olla päällekäisiä fragmentteja?
        if (savedInstanceState == null) {
            // Listataan kaupat:
            asetaKaupatFragmentti();

            // Listataan samalla tyylillä ostoskorin sisältö: Mutta vain jos ostoskori on validi!!!
            if (ostoskori.getId() >0)
                asetaTuotteetFragmentti();
        }
    }

    /**
     * Poimitaan ostoskori mahdollisesti kutsujan argumentista.
     * Jos koria ei ole, luodaan uusi ostoskori kantaan ja aletaan käyttää sitä.
     */
    private void poimiOstoskoriKyselijaltaTaiDefault() {

            Intent kutsuja = getIntent();
            if (!kutsuja.hasExtra("Ostoskori")) {
                // Ei ollut parametriä. Uutta ostoskoria luodaan: Eli luo kantaan tyhjä kori:
                luoOstoskori();
                return;
            }
            // Ainakin sen niminen parametri löytyi. Yritetään
            ostoskori = kutsuja.getParcelableExtra("Ostoskori");
            if (ostoskori == null) { // Korin luonti ei onnistunut oikein:
                throw new IllegalArgumentException("Basket could not be converted!!!");
            }
    }

    /**
     * Luo kantaan uuden ostoskorin, jonka sitten palauttaa tähän käsiteltäväksi. Huomaa, että tässä vaiheessa
     * EI talleteta kaupan tietoa! (Koska sitä ei tiedetä.)
     * @return  Luotu ostoskori
     */
    private void luoOstoskori() {
        Uri luodunKorinID = this.getContentResolver().insert(
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets/new"),null);

        if (luodunKorinID == null)
            throw new NullPointerException("Basket creation were not successfull!");

        Cursor ostoskoriC = this.getContentResolver().query(luodunKorinID,null,null,null,null);
        if (ostoskoriC == null || ostoskoriC.getCount()==0)
            throw new NullPointerException("Created basket could not be found!");

        ostoskori = Ostoskori.convertCursorToOstoskori(ostoskoriC);
        ostoskoriC.close();

    }


     /**
     * Käynnistetään tuotelistaus-fragmentti, jossa tuotteet listattuna.
     */
    private void asetaTuotteetFragmentti() {

        ProductsListFragment tuotelista = new ProductsListFragment();

        // Viedään ostoskorin ID argumenttina, jotta voidaan lisätä /etsiä tuotteita:
        Bundle argut = new Bundle();
        argut.putLong(Ostoskori._ID,ostoskori.getId());
        tuotelista.setArguments(argut);

        // Ja aukastaan fragmentti (HUOM! Fragmentin tulee olla support.v4.app.fragment! muuten ei osaa castata):
        getSupportFragmentManager().beginTransaction().add(R.id.shoppinglistedit_list,tuotelista).commit();

    }

    /**
     * Käynnistetään kaupan valinta-fragmentti, jossa kaupat listattuna:
     */
    private void asetaKaupatFragmentti() {
        ShopsFragment kauppaFragmentti = new ShopsFragment();
        // Jos kauppa on jo valittu (eli muokataan vanhaa), viedään sen ID listalle:
        if (ostoskori.getKauppa_id() >0) {
            Bundle argut = new Bundle();
            argut.putLong(Kauppa._ID,ostoskori.getKauppa_id());
            kauppaFragmentti.setArguments(argut);
        }
        // Ja aloitetaan fragmentin tuotto:
        getSupportFragmentManager().beginTransaction().add(R.id.shoppinglistedit_shops,kauppaFragmentti).commit();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_shoppinglist, menu);
        return true;
    }

    /**
     * Eventti liipaistaan kun käyttäjä tekee valinnan menubarissa
     * @param item  Valittu elementti valikossa
     * @return  Boolean, onko eventti käsitelty
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        switch (item.getItemId()) {
            case android.R.id.home: // Jos painettiin paluunuolta, käsitellään poistuminen kunnolla:
                if (haeOstoskorinRivit()==0) // jos korissa ei ole mitään, varmistetaan että käyttäjä haluaa tallentaa tyhjän korin.
                    poistutaanTyhjana();
                else
                    poistutaan();   // Korissa on tuotteita, poistutaan normirutiinilla
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Avaa uuden tuotteen lisäyksen mahdollistavan activityn:
     *
     * @param item
     */
    public void lisaaUusiTuote_Click(MenuItem item) {
        if (ostoskori.getId()<1) {
            // Jos ostoskoria ei ole. Tätä ei pitäisi tapahtua, mutta varmuuden vuoksi!
            Toast.makeText(this,"Ostoskoria ei ole!",Toast.LENGTH_LONG);
            return;
        }

       // TODO: Lisää käsittelijä, joka katsoo asetuksista, mikä on käyttäjän oletusvalinta uuden tuotteen lisäämiselle...
        lisaaTuote_Perus();

        //lisaaTuote_Viivakoodi();
    }


    private void lisaaTuote_Viivakoodi() {
        //TODO: Tee käsittelijä viivakoodin lukijan näytille.
    }

    /**
     * Avaa normaalin lisäysikkunan, jossa tuotteen tiedot lisätään käsin.
     */
    private void lisaaTuote_Perus() {
        Intent i = new Intent(this, AddItemActivity.class);
        i.putExtra("Ostoskori", ostoskori);

        startActivityForResult(i, 100);

    }


    /**
     * Kun paluunuolta painetaan, katsotaan kannan systeemit valmiiksi, ja vasta sitten mennään:
     */
    @Override
    public void onBackPressed() {
        if (haeOstoskorinRivit()==0) // jos korissa ei ole mitään, varmistetaan että käyttäjä haluaa tallentaa tyhjän korin.
            poistutaanTyhjana();
        else
            poistutaan();   // Korissa on tuotteita, poistutaan normirutiinilla
    }

    /**
     * Kun fyysistä paluunuolta painetaan, katsotaan kannan systeemit valmiiksi, ja vasta sitten mennään:
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if (haeOstoskorinRivit()==0)    // jos korissa ei ole mitään, varmistetaan että käyttäjä haluaa tallentaa tyhjän korin.
                poistutaanTyhjana();
            else
                poistutaan();   // Korissa on tuotteita, poistutaan normirutiinilla
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Jos käyttäjä aikoo poistua niin, ettei korissa ole yhtään tuotetta, näytetään kysely.
     * Jos käyttäjä vastaa kyselyyn myöntävästi, jatketaan normaalit poistumisrutiinit.
     * Muuten keskeytetään ajo.
     */
    private void poistutaanTyhjana() {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_things_not_ok))
                .setMessage(getString(R.string.alert_no_rows_in_basket))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    // OK käskyttää normaalia poistumisrutiinia, eli talletetaan tyhjä kori:
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        poistutaan();
                    }
                })
                .setNeutralButton(R.string.alert_no_rows_no_button, new DialogInterface.OnClickListener() {
                    // Käyttäjä ei halua koria tallettaa. Tuhotaan kori kannasta ja suljetaan:
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(
                                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets/"+ ostoskori.getId()),
                                null,null);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)   // Cancel perutaan toiminto
                .show();
    }
    /**
     * Ennen Activitystä poistumista tarkistetaan ostoskori, talletetaan se kantaan ja vasta sitten poistutaan.
     * Jos joku menee pieleen, näytetään käyttäjälle varmistus haluaako hän keskeyttää toiminnon.
     */
    private void poistutaan() {
        String varoitus = null;

        if (!Ostoskori.ostoskoriOnKelvollinen(ostoskori)) {      // Paluu sallitaan vain jos kori on kelvollinen tai käyttäjä haluaa peruuttaa homman:
            varoitus = getString(R.string.alert_basket_invalid);
        }

        else {
            // ostoskori on OK. Kokeillaan tallettaa muutokset kantaan:
            if (!TallennaOstoskoriKantaan())
                varoitus = getString(R.string.alert_basket_store_failed);
        }

        if (varoitus != null) {
            // Halutaan varmistaa että poistutaan, vaikka tavara ei ole ookoo:
            AlertDialog.Builder varmistus = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_things_not_ok))
                .setMessage(varoitus + getString(R.string.alert_confirmation_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    // OK toimittaa activityn loppuun, eli vaikka homma failaa, jatketaan vaan:
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no,null);   // Cancel ei tee mitään.
            varmistus.show();
        }
        else {
            // Kaikki kunnossa. Tuhotaan activity
            finish();
        }
    }

    /**
     * Haetaan kannasta ostoskorin tuotteiden lukumäärä
     * @return  Ostosrivien lukumäärän
     */
    private int haeOstoskorinRivit() {
        Cursor ostokset = this.getContentResolver().query(
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets/"+ ostoskori.getId() + "/rows"),
                null,null,null,null
        );
        int maara = ostokset.getCount();
        ostokset.close();
        return maara;
    }

    private boolean TallennaOstoskoriKantaan() {

        ContentValues cV = new ContentValues();
            cV.put(Ostoskori.KAUPPA,ostoskori.getKauppa_id());
            cV.put(Ostoskori.PVM,ostoskori.getRaakaPvm());

        // Paukastaan päivittäen ostoskori kantaan:
        int paivitettyjenOstoskorienLkm = this.getContentResolver().update(
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets/update/"+ostoskori.getId()),
                cV,
                null,null);

        if (paivitettyjenOstoskorienLkm <1)
            return false;
        if (paivitettyjenOstoskorienLkm >1)
            throw new IllegalStateException("Multiple rows updated when updating the shopping basket!");

        // Tallenna korin rivit:
        int paivitettyjenRivienLkm = 0;     //TODO: Tee ostoskorin rivien päivitys kantaan!

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case KAUPPA_ACTIVITYREULT:
                    // Kun uusi kauppa on luotu:
                    // TODO: Hanskaa kaupan valinnan käsittelyt

                break;
                //case OSTOSLISTA_ACTIVITYREULT:
                // TODO: Hanskaa ostosrivin lisäyksen käsittelyt
                // Rivi lisätty. Päivitä listaus
                //break;
            }
        }
    }

    /**
     * Käyttäjän klikatessa päivämäärää, avataan datepicker activity
     *
     * @param view
     */
    public void muokkaaPvm(View view) {
        DialogFragment poimuri = new DatePickerFragment();
        Bundle argumentit = new Bundle(1);
        argumentit.putLong("oletusaika", ostoskori.getRaakaPvm());

        poimuri.setArguments(argumentit);
        poimuri.show(getFragmentManager(), TAG);
    }

    /**
     * Kun käyttäjä on valinnut päivämäärän, ajetaan tämä:
     *
     * @param view
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        //Kirjataan valittu päivämäärä talteen:

        Calendar c = Calendar.getInstance();
        c.setTime(ostoskori.getPvm());
        c.set(year, monthOfYear, dayOfMonth);

        if (c.after(Calendar.getInstance())) {
            // Ei myöhempää aikaa, mitä nyt on!
            Toast.makeText(this,R.string.alert_date_longer_than_today,Toast.LENGTH_LONG).show();
            return;
        }
        ostoskori.setPvm(c.getTime());
        asetaAika();
    }

    /**
     * Käyttäjän klikatessa kellonaikaa, avataan timepicker activity
     *
     * @param view
     */
    public void muokkaaAika(View view) {
        DialogFragment poimuri = new TimePickerFragment();
        Bundle argumentit = new Bundle(1);
        argumentit.putLong("oletusaika", ostoskori.getRaakaPvm());

        poimuri.setArguments(argumentit);
        poimuri.show(getFragmentManager(), TAG);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Kirjataan valittu kellonaika talteen:

        Calendar c = Calendar.getInstance();
        c.setTime(ostoskori.getPvm());
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        if (c.after(Calendar.getInstance())) {
            // Ei myöhempää aikaa, mitä nyt on!
            Toast.makeText(this,R.string.alert_date_longer_than_today,Toast.LENGTH_LONG).show();
            return;
        }
        ostoskori.setPvm(c.getTime());
        asetaAika();
    }

    /**
     * Kun käyttäjä on valinnut kellonajan (TimePickerFragmentista).
     * Asetetaan ostoskorin kellonaika vastaamaan valintaa.
     */
    private void asetaAika() {
        if (ostoskori.getPvm() == null)
            ostoskori.setPvm(new Date(System.currentTimeMillis()));

        DateFormat pvmFormat = DateFormat.getDateInstance(DateFormat.SHORT);

        DateFormat kloFormat = new SimpleDateFormat("HH:mm");
        pvmTV.setText(pvmFormat.format(ostoskori.getPvm()));
        kloTV.setText(kloFormat.format(ostoskori.getPvm()));
    }

    /**
     * Kun kauppalistan sisältävästä fragmentista napataan kauppa, tulee siitä eventtinä kaupan ID;
     * @param kauppaID
     */
    @Override
    public void onKauppaSelected(long kauppaID) {
        // Talletetaan kaupan ID ostoskoriin:
        ostoskori.setKauppa_id(kauppaID);
    }
}
