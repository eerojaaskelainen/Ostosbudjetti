package com.eerojaaskelainen.ostosbudjetti.items;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

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
public class ProductItemFragment extends Fragment {

    // Kentät:
    protected EditText valmistajaLbl;
    protected EditText nimiLbl;
    protected EditText eanLbl;
    protected Spinner kategoria;
    private ImageButton viivakoodiBtn;

    public static final String TUOTE_EAN = "TuoteEAN";

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
    public static ProductItemFragment newInstance(String tuoteEAN) {
        ProductItemFragment fragment = new ProductItemFragment();
        if (tuoteEAN != null) {
            Bundle args = new Bundle();
            args.putString(TUOTE_EAN, tuoteEAN);
            fragment.setArguments(args);
        }
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

        valmistajaLbl = (EditText)fragmentti.findViewById(R.id.product_manufacturer_input);
        nimiLbl = (EditText)fragmentti.findViewById(R.id.product_name_input);
        eanLbl = (EditText)fragmentti.findViewById(R.id.product_ean_input);
        kategoria = (Spinner)fragmentti.findViewById(R.id.product_category_select);
        viivakoodiBtn = (ImageButton)fragmentti.findViewById(R.id.product_ean_barcode_btn);
        viivakoodiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Barcode.haeViivakoodi(ProductItemFragment.this);
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_item, container, false);

        alustaKentat(view);
        // Tultiin esivalitun tuotteen
        if (getArguments() != null) {
            haeTuoteKannasta(getArguments().getString(TUOTE_EAN), false);
        }
        else {
            // Ei tuotetta.
            alkuperainenTuote = new Tuote();
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
}
