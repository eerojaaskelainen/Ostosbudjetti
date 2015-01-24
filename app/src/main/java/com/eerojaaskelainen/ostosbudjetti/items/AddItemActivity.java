package com.eerojaaskelainen.ostosbudjetti.items;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.OstoksetContentProvider;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;
import com.eerojaaskelainen.ostosbudjetti.models.Ostosrivi;
import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

import java.text.NumberFormat;

/**
 * AddItemActivity hallitsee näytön jossa uusi tuoterivi lisätään ostoskoriin.
 */
public class AddItemActivity extends ActionBarActivity implements ProductItemFragment.OnTuoteSelectedListener {
    private Ostoskori ostoskori;
    protected Ostosrivi ostosrivi;

    // Layoutin elementit:
    protected EditText aHintaLbl;
    protected EditText lkmLbl;
    protected Button saveBtn;
    protected ProductItemFragment tuoteFragment;



    public AddItemActivity() {
        ostosrivi = null;
        ostoskori = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        //Poimitaan elementit properteihin:
        poimiElementit();


        // Näkymää luodaan vanhasta (esim. suunta muuttunut):
        if (savedInstanceState!=null){
            ostoskori = savedInstanceState.getParcelable("Ostoskori");
            ostosrivi = savedInstanceState.getParcelable("Ostosrivi");
            tuoteFragment = (ProductItemFragment) getSupportFragmentManager().getFragment(savedInstanceState,"TuoteFragment");

            return;
        }

        // Haetaan näkymää kutsunut: Parametrinä on pakko olla ostoskori!
        haeOstoskoriArgumenteista(getIntent());





        // Tutkitaan, tullaanko tuoteriviltä, eli onko muokkaus kyseessä
        haeVanhaOstosriviArgumenteista(getIntent());

        if (ostosrivi == null)
        {
            // Kiville meni vanhan haku tai vanhaa ei ollut. Tehdään uusi
            ostosrivi = new Ostosrivi(ostoskori.getId());
            // Tehdään tyhjä Tuote-fragmentti:

            tuoteFragment = ProductItemFragment.newInstance(null,getIntent().getBooleanExtra(ProductItemFragment.LUETAAN_VIIVAKOODI_ALUKSI,false));
            saveBtn.setText(R.string.add);
        }

        //tuotePlaceholder.addView(tuoteFragment);
        getSupportFragmentManager().beginTransaction().add(R.id.additem_productfragment_placeholder,tuoteFragment).commit();
        setResult(RESULT_CANCELED); // Defaultti resultti on canceled. Muutetaan sitte tarvittaessa.

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("Ostoskori",ostoskori);
        outState.putParcelable("Ostosrivi",ostosrivi);
        getSupportFragmentManager().putFragment(outState,"TuoteFragment",tuoteFragment);

    }

    /**
     * Metodi hakee kutsujan lähettämistä argumenteista ostoskorin ja tallettaa sen luokan ominaisuudeksi.
     * @param kutsuja   Kutsujan intent.
     * @exception java.lang.IllegalArgumentException Jos ostoskoria ei löydy, tai ostoskoria ei voitu muodostaa.
     */
    private void haeOstoskoriArgumenteista(Intent kutsuja) {
        if (!kutsuja.hasExtra("Ostoskori")) {
            // Ei ollut parametriä.
            throw new IllegalArgumentException("No basket!");
        }
        ostoskori = kutsuja.getParcelableExtra("Ostoskori");
        if (ostoskori == null) {
            // Vääränlainen parametri, ei voitu määrittää Ostoskoriksi...
            throw new IllegalArgumentException("Invalid basket");
        }
    }

    /**
     * Poimitaan layoutista elementit luokan muuttujiin:
     */
    private void poimiElementit() {
        aHintaLbl = (EditText)findViewById(R.id.additem_unitprice_input);
        lkmLbl = (EditText)findViewById(R.id.additem_amount_input);
        saveBtn = (Button)findViewById(R.id.additem_ok);
    }

    private void haeVanhaOstosriviArgumenteista(Intent kutsuja) {
        if (kutsuja.hasExtra("OstosriviID")){
            // Oli siellä vanha rivi. Pimitaan arvot talteen:
            if (!haeRiviKannasta(kutsuja.getLongExtra("OstosriviID",-1))) {
                // Kiville meni haku. Ei tarvitse täytellä tietoja kun niitä ei saatu.
                return;
            }
            // Rivi löytyi! Puretaan se elementteihin:
            asetaVanhatArvot();
        }
    }

    /**
     * Poimitaan annetulla rivin ID:llä ostosrivi ja asetetaan se luokan ominaisuudeksi.
     * @param ostosriviID   Haettavan ostosrivin ID
     * @return  Palauttaa onnistuessaan truen. Muuten näyttää virheilmoituksen ja palauttaa falsen.
     */
    private boolean haeRiviKannasta(long ostosriviID) {
        ostosrivi = null;
        Cursor rivi = getContentResolver().query(
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,String.format("baskets/%d/rows/%d",ostoskori.getId(),ostosriviID)),
                null,null,null,null
        );
        if (rivi.getCount()!=1) {
            // Riviä ei löytynyt! Lopetetaan heti alkuunsa.
            naytaVirhe(R.string.alert_basket_row_not_found,true);

            rivi.close();
            //ostosrivi = new Ostosrivi(ostoskori.getId());   // Luodaan kuitenkin uusi rivi jottei suoritus tukehdu.
            return false;
        }
        ostosrivi = Ostosrivi.muunnaCursorOstosriviksi(rivi);
        rivi.close();
        return true;

    }

    /**
     * Hakee parametrina annetun rivin tiedot ja asettelee ne kenttiin
     */
    private void asetaVanhatArvot() {
        tuoteFragment = ProductItemFragment.newInstance(ostosrivi.getTuoteEAN(),
                getIntent().getBooleanExtra(ProductItemFragment.LUETAAN_VIIVAKOODI_ALUKSI,false));
        aHintaLbl.setText(Double.toString(ostosrivi.getaHinta()));
        lkmLbl.setText(Double.toString(ostosrivi.getLkm()));
        paivitaTotal();
        saveBtn.setText(R.string.save);
    }


    /**
     * Event handler Tallenna/Lisää uusi -napin painallukselle
     * @param v Nappi, joka liipaisi.
     */
   public void onTallennaClick(View v) {

        // Tutki että rivit ja tuote ovat kunnossa:
        if (!validoiOstosrivi()) {
            return;
        }
        // Yritä tallentaa.
        if (!tallennaRivi()) {
            // Tallennus failasi:
            naytaTallennusVirhe();
            return;
        }
        setResult(RESULT_OK);
        finish();
   }

    private boolean tallennaRivi() {
        ContentValues cV = Ostosrivi.muunnaOstosriviValueiksi(ostosrivi);
        if (cV ==null) return false;

        if (!ostosriviOnUusi()) {
            // Päivitetään vanhaa:
            long talletettu = getContentResolver().update(
                    Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,String.format("baskets/%d/rows/%d",ostoskori.getId(),ostosrivi.getRiviID())),
                    cV,null,null);

            return (talletettu ==1);
        }
        else {
            // Lisätään uutta:
            Uri lisatty = getContentResolver().insert(
                    Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,String.format("baskets/%d/rows/new", ostoskori.getId())),
                    cV);

            return lisatty!= null;
        }
    }


    private boolean validoiOstosrivi() {
        if (!onkoSyotteetOK()){
            naytaTietoPuuttuuVirhe(R.string.alert_invalid_field_message);
            return false;
        }
        // Poimitaan (ja samalla tarkistetaan syötteiden kunto) tuotteesta ID:
        String tuoteID = tuoteFragment.getTuoteId();
        if (tuoteID == null){
            //Kunnollista tuotetta ei saatu, eli ei kelpaa rivin talletus!
            naytaTietoPuuttuuVirhe(R.string.alert_invalid_product);
            return false;
        }

        // Tuote kunnossa. Talletetaan riviin:
        ostosrivi.setTuoteID(Long.parseLong(tuoteID));
        // Talletetaan inputit riviin:
        ostosrivi.setLkm(Double.parseDouble(lkmLbl.getText().toString()));
        ostosrivi.setaHinta(Double.parseDouble(aHintaLbl.getText().toString()));

        return true;
    }

    /**
     * Tarkistaa käyttäjän antamat syötteet kenttiin. Samalla tarkistaa myös tuotteen olemassaolon
     * @return
     */
    private boolean onkoSyotteetOK() {
        //Tarkistetaan syötekentät:
        try {
            if (Double.parseDouble(aHintaLbl.getText().toString()) <=0)
                return false;
            if (Double.parseDouble(lkmLbl.getText().toString())<=0)
                return false;
        }
        catch (Exception e) {
            return false;
        }
       return true;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
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


    public void naytaVirhe(int virheteksti, final boolean lopetetaanko) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_things_not_ok)
                .setMessage(virheteksti)
                .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (lopetetaanko)
                            finish();
                        dialog.cancel();
                    }
                }).show();
    }

    /**
     * Tuote fragmentti liipaisee tämän kun uusi viivakoodi skannataan tai tuote muuten tallentuu
     * @param valittuTuote
     */
    @Override
    public void tuoteSelected(Tuote valittuTuote) {
        if (aHintaLbl.getText().toString().isEmpty() && ostosriviOnUusi()){
            // Hintaa ei ole vielä laiteltu, ja rivikin on uusi.
            // Laitetaan pohjaksi viimeisin tuotteen yksikköhinta:
            aHintaLbl.setText(valittuTuote.getViimeisinAhinta().toString());
            paivitaTotal();
        }
    }

    private boolean ostosriviOnUusi() {
        if (ostosrivi == null) return true;
        return ostosrivi.getRiviID()<=0;
    }

    private void naytaTallennusVirhe() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_things_not_ok)
                .setMessage("Rivin tallennus ei jostain syystä onnistunut.\rHaluatko yrittää uudelleen?")
                .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Kyllä halutaan.
                        onTallennaClick(null);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void naytaTietoPuuttuuVirhe(int virheteksti) {
        new AlertDialog.Builder(this)
                .setTitle("Tietoja puuttuu")
                .setMessage(virheteksti)
                .setPositiveButton(R.string.alert_invalid_field_positive_response,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.alert_invalid_field_negative_response, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    public void onPlusClicked(View v)
    {
        try {
            lkmLbl.setText(Double.toString(Double.parseDouble(lkmLbl.getText().toString()) + 1));
            paivitaTotal();
        }
        catch (Exception e)
        {}
    }
    public void onMinusClicked(View v)
    {
        //TODO: Tarkista ettei mennä miinukselle!
        try {

            lkmLbl.setText(Double.toString(Double.parseDouble(lkmLbl.getText().toString()) - 1));
            paivitaTotal();
        }
        catch (Exception e)
        {}
    }
    private void paivitaTotal() {
        try {
            NumberFormat f = NumberFormat.getCurrencyInstance();
            double summa = Double.parseDouble(aHintaLbl.getText().toString()) * Double.parseDouble(lkmLbl.getText().toString());

            ((TextView) findViewById(R.id.additem_total)).setText(getString(R.string.total_lbl) + f.format(summa));
        }
        catch (Exception e)
        {
            ((TextView) findViewById(R.id.additem_total)).setText("");
        }
    }
}
