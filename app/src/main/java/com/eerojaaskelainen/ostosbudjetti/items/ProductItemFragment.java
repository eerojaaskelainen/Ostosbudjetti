package com.eerojaaskelainen.ostosbudjetti.items;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eerojaaskelainen.ostosbudjetti.CursorLoaderAutoCompleteTextView;
import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.barcode.Barcode;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.OstoksetContentProvider;
import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

//import android.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.eerojaaskelainen.ostosbudjetti.items.ProductItemFragment.OnTuoteSelectedListener} interface
 * to handle interaction events.
 * Use the {@link ProductItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Kentät:
    // Valmistajat -autocomplete:
    protected CursorLoaderAutoCompleteTextView valmistajaLbl;
    private CursorAdapter valmistajatCursor;
    private final String[] valmistajatCurKentat = {
            Tuote.VALMISTAJA
    };
    private final int[] valmistajatLayoutKentat = {
            android.R.id.text1
    };
    protected static final int valmistajatMinimumTreshold = 3;

    //Keräävät listat:
    protected LinearLayout valmistajaLayout;
    protected LinearLayout nimiLayout;
    protected LinearLayout eanLayout;
    protected ImageButton showHideButton;

    // muut:
    protected EditText nimiLbl;
    protected EditText eanLbl;
    //protected Spinner kategoria;
    private ImageButton viivakoodiBtn;

    public static final String TUOTE_EAN = "TuoteEAN";
    public static final String LUETAAN_VIIVAKOODI_ALUKSI = "Luetaan viivakoodi aluksi";

    private OnTuoteSelectedListener mListener;

    private Tuote alkuperainenTuote;    // Jos kyselyn mukana tuli tuotteen EAN-koodi, talletettiin kannasta löytynyt tuote tähän

    private boolean tuotettaMuutettu(){ // Lippu, jos tuotteen tietoja on menty muuttamaan.
        if (alkuperainenTuote == null) return true; // Vanhaa ei ole, eli kaikki muutokset ovat uutta
        if (!valmistajaLbl.getText().toString().equals(alkuperainenTuote.getValmistaja()))
                return true;
        if (!(nimiLbl.getText().toString().equals(alkuperainenTuote.getTuotenimi())))
                return true;
        if (!(eanLbl.getText().toString().equals(alkuperainenTuote.getEanKoodi()))) {
            // Haetaan mahdollinen olemassa oleva tuote kannasta EAN-koodilla:
            return !(haeTuoteKannasta(eanLbl.getText().toString(),true));  //Jos tuote löytyi, ei (vielä) olla muutettu mitään
        }
        /*if (!(kategoria.getSelectedItem().equals(alkuperainenTuote.getKategria())))
                return true;*/  //TODO: Implementoi kategorian tarkistus!

        return false;
    }
    private boolean tuoteOnUusi() {     // Lippu, jos tuote on uusi (ei löydy kannasta)
        if (alkuperainenTuote == null){
            return true;
        }
        return (alkuperainenTuote.getTuoteID() <1);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tuoteEAN Muokattavan rivin TuoteID, jota näytetään/muokataan
     * @return A new instance of fragment ProductItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductItemFragment newInstance(String tuoteEAN,boolean luetaanViivakoodi) {
        ProductItemFragment fragment = new ProductItemFragment();
        Bundle args = new Bundle();
        if (tuoteEAN != null) {
            args.putString(TUOTE_EAN, tuoteEAN);
        }
        args.putBoolean(LUETAAN_VIIVAKOODI_ALUKSI,luetaanViivakoodi);
        fragment.setArguments(args);
        return fragment;
    }

    public ProductItemFragment() {
        // Required empty public constructor
        alkuperainenTuote = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Haetaan kenttien elementit sekä sijoitetaan argumenttina annetun tuotteen tiedot elementteihin, jos sellainen saatiin.
     * @param fragmentti Fragmentti, jota olemme luomassa
     */
    private void alustaKentat(View fragmentti) {

        valmistajaLayout = (LinearLayout)fragmentti.findViewById(R.id.product_manufacturer_layout);
        nimiLayout = (LinearLayout)fragmentti.findViewById(R.id.product_name_layout);
        eanLayout = (LinearLayout)fragmentti.findViewById(R.id.product_ean_layout);
        showHideButton = (ImageButton)fragmentti.findViewById(R.id.product_showhide_button);

        valmistajaLbl = (CursorLoaderAutoCompleteTextView)fragmentti.findViewById(R.id.product_manufacturer_input);
        nimiLbl = (EditText)fragmentti.findViewById(R.id.product_name_input);
        eanLbl = (EditText)fragmentti.findViewById(R.id.product_ean_input);
        //kategoria = (Spinner)fragmentti.findViewById(R.id.product_category_select);
        viivakoodiBtn = (ImageButton)fragmentti.findViewById(R.id.product_ean_barcode_btn);
        viivakoodiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Barcode.haeViivakoodi(ProductItemFragment.this);
            }
        });

        // IMEAction kuuntelijaa tuotenimelle:
        nimiLbl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    // Kun Nimi-kentässä painetaan seuraavaa, asetetaan fokus seuraavaan kenttään:
                    mListener.asetaFocusTuotteenJalkeen();
                    return true;    // Eventti hanskattu
                }
                else return false;  // Muissa tapauksissa antaa oletuskäsittelijän ottaa ohjat.
            }
        });
        LuoValmistajaEhdotukset(); // Luodaan adapterit ja loaderit kuntoon valmistaja -autocomplete-kenttää varten.

        // Event handleri mahdollista listan aukaisua varten:
        nimiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muutaKenttienNakyvyys(false);
            }
        });
        // Oletustoiminto buttonille:
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muutaKenttienNakyvyys(true);
            }
        });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_item, container, false);

        alustaKentat(view);
        String ean = getArguments().getString(TUOTE_EAN,null);


        if (ean!= null){
            // Tultiin esivalitun tuotteen kautta
            haeTuoteKannasta(ean, false);
        }
        else {
            // Ei tuotetta.
            alkuperainenTuote = new Tuote();
        }

        if (getArguments().getBoolean(LUETAAN_VIIVAKOODI_ALUKSI,false)){
                // Jos halutaan heti alkuun lukea viivakoodi
                Barcode.haeViivakoodi(ProductItemFragment.this);
        }


       return view;
    }

    /**
     * Palauttaa tuotteen ID-numeron mikäli tuotteen tiedot ovat kunnossa
     * @return  ID-numero TAI null jos tuotteet eivät kunnossa.
     */
    public String getTuoteId() {
        if (!tuotettaMuutettu()) return Long.toString(alkuperainenTuote.getTuoteID());  // Jos käyttäjä ei ole tuotteeseen kajonnut, riittää alkuperäisen ID:n palautus.

        // Käyttäjä on lisännyt/muokannut tuotetta:

        if (onkoSyotteetOK()) {
            // Syötteet oli kunnossa.
            // Tehdään tuotteen tallennus (lisäys/muutos) kantaan:
            if (tallennaTuote(!tuoteOnUusi())) {
                // Tallennus onnistui.
                // Anna tuotteen ID takaisin:
                return Long.toString(alkuperainenTuote.getTuoteID());
            }
        }
        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTuoteSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTuoteSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            String skannattu = Barcode.onActivityResult(requestCode, resultCode, data);
            if (skannattu != null){
               kasitteleEANLisays(skannattu);
            }
        }
        catch (Exception e)
        {
            mListener.naytaVirhe(R.string.alert_barcode_scan_error,false);
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTuoteSelectedListener {
        public void naytaVirhe(int virheteksti, final boolean lopetetaanko);
        public void tuoteSelected(Tuote valittuTuote);
        public void asetaFocusTuotteenJalkeen();
    }

    /**
     * Metodi tallentaa päivitetyn/uuden tuotteen kantaan. Jos luodaan uutta, hakee metodi tuotteen kannasta luokan ominaisuuteen, jolloin tiedetään tuotteen ID.
     * @param paivitetaan   Määrittää, päivitetäänkö tietuetta, vai lisätäänkö uutta.
     * @return  Onnistuiko tallennus
     */
    private boolean tallennaTuote(boolean paivitetaan) {

        ContentValues cV = new ContentValues();
            cV.put(Tuote.NIMI,nimiLbl.getText().toString());
            cV.put(Tuote.VALMISTAJA,valmistajaLbl.getText().toString());
            cV.put(Tuote.EAN,eanLbl.getText().toString());

        if (paivitetaan) {

            if (alkuperainenTuote.getTuoteID() <1) throw new IllegalArgumentException("Product ID invalid!");   // Kerran tuotetta päivitetään, on sillä oltava kannassa ID!

            int muuttunut = getActivity().getContentResolver().update(
                    Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,String.format("products/#",alkuperainenTuote.getTuoteID()))
                    ,cV,null,null);
            if (muuttunut ==1) {
                // Muutos OK!
                return true;
            }
            // Muutos ei onnistunut:
            mListener.naytaVirhe(R.string.alert_product_store_failed,false);
            return false;
        }
        else {
            // Luodaan uutta:
            Uri luotu = getActivity().getContentResolver().insert(
                    Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI, "products"),
                    cV);
            if (luotu == null) {
                mListener.naytaVirhe(R.string.alert_product_store_failed,false);
                return false;
            }
            // Ja haetaan tuote kannasta (jotta saadaan tuotteelle myös ID!)
            return haeTuoteKannasta(eanLbl.getText().toString(),false);
        }

    }

    /**
     * Metodi etsii kannasta EAN-koodilla olevaa tuotetta
     * @param eanKoodi  EAN-koodi jolla tuotetta etsitään
     * @param hiljaa Näytetäänkö virheilmoitus, jos tuotetta ei löydy
     */
    private boolean haeTuoteKannasta(String eanKoodi, boolean hiljaa) {
        this.alkuperainenTuote = null;
        if (eanKoodi == null || eanKoodi.isEmpty())
            return false;

        Cursor edTuote = getActivity().getContentResolver().query(
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI, String.format("products/%s",eanKoodi)),
                null,null,null,null);
        if (edTuote.getCount()==0) {
            edTuote.close();

            if (!hiljaa)
                mListener.naytaVirhe(R.string.alert_product_not_found_from_database,false);
            return false;
        }

        Tuote tulos = new Tuote();
        tulos.setTuoteID(edTuote.getLong(edTuote.getColumnIndex(Tuote._ID)));
        tulos.setValmistaja(edTuote.getString(edTuote.getColumnIndex(Tuote.VALMISTAJA)));
        tulos.setEanKoodi(edTuote.getString(edTuote.getColumnIndex(Tuote.EAN)));
        tulos.setTuotenimi(edTuote.getString(edTuote.getColumnIndex(Tuote.NIMI)));
        tulos.setViimeisinAhinta(edTuote.getDouble(edTuote.getColumnIndex(Tuote.VIIMEISINHINTA)));

        this.alkuperainenTuote = tulos;

        // Asetellaan nätisti alkuperäiset tuotteen tiedot kenttiin:
        valmistajaLbl.setText(alkuperainenTuote.getValmistaja());
        nimiLbl.setText(alkuperainenTuote.getTuotenimi());
        eanLbl.setText(alkuperainenTuote.getEanKoodi());

        // Piilotetaan ehdotukset koska niitä ei nyt tarvita:
        valmistajaLbl.dismissDropDown();

        //Piilotetaan muut kentät toistaiseksi:
        muutaKenttienNakyvyys(true);

        return true;
    }
    /**
     * Tarkistaa käyttäjän antamat syötteet kenttiin. Samalla tarkistaa myös tuotteen olemassaolon
     * @return  Totuusarvo, oliko syötteet kunnossa
     */
    private boolean onkoSyotteetOK() {
        //Tarkistetaan syötekentät:
        if (valmistajaLbl.getText().toString().isEmpty())
            return false;
        if (nimiLbl.getText().toString().isEmpty())
            return false;
        if (eanLbl.getText().toString().isEmpty())
            return false;   //TODO: Kysy miksi EAN on tyhjä!



        return true;
    }

    /**
     * Yrittää hakea tekstikenttään syötetyn EAN-koodin mukaista tuotetta kannasta.
     */
    private void kasitteleEANLisays(String eanKoodi) {
        if (eanKoodi == null || eanKoodi.isEmpty())
            return;
        //eanLbl.setText(eanKoodi);
        if (!haeTuoteKannasta(eanKoodi,true)){
            //Tuotetta ei ollut valmiiksi kannassa.
            eanLbl.setText(eanKoodi);
        }
        else {
            mListener.tuoteSelected(alkuperainenTuote);
        }
    }

    private void muutaKenttienNakyvyys(boolean piilossa) {
        int nakyvyys;
        if (piilossa) {
            // Muutetaan nappula avaamaan kentät:
            showHideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    muutaKenttienNakyvyys(false);
                }
            });
            showHideButton.setImageResource(android.R.drawable.arrow_down_float);

            nakyvyys = View.GONE;
        }
        else {
            // Muutetaan nappula piilottamaan kentät:
            showHideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    muutaKenttienNakyvyys(true);
                }
            });
            showHideButton.setImageResource(android.R.drawable.arrow_up_float);

            nakyvyys = View.VISIBLE;
        }

        valmistajaLayout.setVisibility(nakyvyys);
        eanLayout.setVisibility(nakyvyys);
    }



    // Valmistaja-kentän listausjutut:

    private void LuoValmistajaEhdotukset()
    {
        valmistajatCursor = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                valmistajatCurKentat,
                valmistajatLayoutKentat,
                0);

        valmistajaLbl.setAdapter(valmistajatCursor);
        // Määritetään onClickListener (Tällä saadaan fiksumpaa dataa talletettua valmistajakenttään:
        valmistajaLbl.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor)parent.getItemAtPosition(position);
                valmistajaLbl.setText(c.getString(c.getColumnIndex(Tuote.VALMISTAJA)));
                // c = null;
            }
        });
        // Määritetään filtteröinti (eli kun kirjoitetaan tekstiä, niin suodatetaan tuloksia sitä mukaa):
        // HUOM! Normaali filtteri ei ilmeisesti pelaa fiksusti, kun kursori tulee Loaderin tarjoamana. Siksi implementoidaan oma tekstikenttä ja kuunnellaan sitä.
        valmistajaLbl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>= valmistajatMinimumTreshold) {
                    // Vähimmäismäärä merkkejä kirjoitettu, joten tehdään suodatus:
                    Bundle args = new Bundle();
                    args.putString(Tuote.VALMISTAJA, s.toString());
                    getLoaderManager().restartLoader(1, args, ProductItemFragment.this);
                }
            }
        });


        getLoaderManager().initLoader(1,null,this);

    }

    /**
     * Loaderi hanskaa valmistajaehdotuksen listauksen toimituksen
     * @param i     mikä kursori halutaan ladata (tässä ei merkitystä)
     * @param bundle    Mahdolliset argumentit (esim. Valmistajan filtteri)
     * @return  Palauttaa managerille (joka tätä kutsuu) luodun Loaderin.
     */
    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        String rajaus = null;

        if (bundle != null){
            rajaus = Tuote.VALMISTAJA + " LIKE '%"+ bundle.getString(Tuote.VALMISTAJA,null) + "%'";
        }

        return new CursorLoader(
                getActivity(),
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"manufacturers"),
                null,
                rajaus,null,null
        );
    }

    /**
     * Kun loader on saanut latauksen valmiiksi, aktivoidaan kursori.
     * @param loader    Lataaja, joka on toimittanut latauksen
     * @param c         Kursori, joka on valmis asetettavaksi
     */
    @Override
    public void onLoadFinished(Loader loader, Cursor c) {
        if (valmistajatCursor != null && c != null) {
            valmistajatCursor.swapCursor(c);    // otetaan kursori käyttöön
        }
    }

    /**
     * Kun loader käskee nollaamaan adapterin (esim. kun näyttö tuhotaan):
     * @param loader    Lataaja joka halutaan poistettavan.
     */
    @Override
    public void onLoaderReset(Loader loader) {
            if (valmistajatCursor != null)
            {
                valmistajatCursor = null;
            }
    }

}
