package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eerojaaskelainen.ostosbudjetti.AddItemActivity;
import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.databaseHelpers.Ostoskanta;
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
    //Ostoskanta kanta;


    protected Ostoskori ostoskori;

    protected TextView pvmTV;
    protected TextView kloTV;
    //protected ListView ostokset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_shoppinglist);

        // Haetaan näkymää kutsunut: Parametrinä on pakko olla ostoskori!
        if (savedInstanceState == null || ostoskori == null) {
            Intent kutsuja = getIntent();
            if (!kutsuja.hasExtra("Ostoskori")) {
                // Ei ollut parametriä.
                throw new IllegalArgumentException("No basket!");
            }

            ostoskori = kutsuja.getParcelableExtra("Ostoskori");
            if (ostoskori == null)
                ostoskori = new Ostoskori();
        }
        else {
            ostoskori = savedInstanceState.getParcelable("Ostoskori");
        }


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

    private void asetaKaupatFragmentti() {
        ShopsFragment kauppaFragmentti = new ShopsFragment();
        // Jos kauppa on jo valittu (eli muokataan vanhaa), viedään sen ID listalle:
        if (ostoskori.getKauppa_id() >=0) {
            Bundle argut = new Bundle();
            argut.putLong(Kauppa._ID,ostoskori.getKauppa_id());
            kauppaFragmentti.setArguments(argut);
        }
        // Ja aloitetaan fragmentin tuotto:
        getSupportFragmentManager().beginTransaction().add(R.id.shoppinglistedit_shops,kauppaFragmentti).commit();
    }

    private void asetaAika() {
        if (ostoskori.getPvm() == null)
            ostoskori.setPvm(new Date(System.currentTimeMillis()));

        DateFormat pvmFormat = DateFormat.getDateInstance(DateFormat.SHORT);

        DateFormat kloFormat = new SimpleDateFormat("HH:mm");
        pvmTV.setText(pvmFormat.format(ostoskori.getPvm()));
        kloTV.setText(kloFormat.format(ostoskori.getPvm()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_shoppinglist, menu);
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
     * Avaa uuden tuotteen lisäyksen
     *
     * @param item
     */
    public void lisaaUusiTuote_Click(MenuItem item) {

        //TODO: Miten uuden ostoskorin tallennus kantaan, missä vaiheessa???
        /*if (ostoskori.getId()==-1)
            Toast.makeText(this,"Ostoskoria ei ole")
        */// TODO: Lisää käsittelijä, joka katsoo asetuksista, mikä on käyttäjän oletusvalinta uuden tuotteen lisäämiselle...
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
              outState.putParcelable("Ostoskori",ostoskori);

              super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
                if (savedInstanceState.containsKey("Ostoskori"))
                    ostoskori = savedInstanceState.getParcelable("Ostoskori");

                super.onRestoreInstanceState(savedInstanceState);
    }
    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Rivi lisätty. Päivitä listaus
            //TODO: Päivitä listaus.
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

        ostoskori.setPvm(c.getTime());
        asetaAika();
    }

    /**
     * Kun kauppalistan sisältävästä fragmentista napataan kauppa, tulee siitä eventtinä kaupan ID;
     * @param kauppaID
     */
    @Override
    public void onKauppaSelected(long kauppaID) {
        //TODO: Tee kaupan vaihdon metodit kuntoon!
        Toast.makeText(this,"Kauppa: "+ kauppaID, Toast.LENGTH_LONG).show();
    }
}
