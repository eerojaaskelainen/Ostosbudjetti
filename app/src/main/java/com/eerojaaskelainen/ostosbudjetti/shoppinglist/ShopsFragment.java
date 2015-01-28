package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.KaupatContentProvider;
import com.eerojaaskelainen.ostosbudjetti.models.Kauppa;
import com.eerojaaskelainen.ostosbudjetti.shops.ShopsActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShopsFragment.OnKauppaSelectedListener} interface
 * to handle interaction events.
 */
public class ShopsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private static final int UUSI_KAUPPA_RESULT = 11;
    // Kaupat sisältävää listaa varten pari muuttujaa:
    protected Button kaupat;

    // Aiemmin valitun kaupan ID - jos sellaista on.
    protected Long getEdellinenKauppaID() {
        if (getArguments()== null)
            return -1L;
        if (getArguments().containsKey(Kauppa._ID)) {
            // Olihan sielä. Ja napataan se adapteriin:
            return getArguments().getLong(Kauppa._ID, -1);
        }
        return -1L;
    }

    // Kun fragmentti luodaan, ja argumenttina saatu kauppa asetetaan spinnerille, ei laukaista eventtiä:
    private boolean ekaKerta = true;


    // Määritellään adaperi, ja siihen tarjottavat tiedot:
    private SimpleCursorAdapter kaupatAdapter;

    String[] sarakkeetAdapterille = {
            Kauppa.NIMI,
            Kauppa._ID
            //Kauppa.FULL_OSOITE
    };
    int[] kentatAdapterille = {
            android.R.id.text1,
            android.R.id.text2
    };

    // Tälle kuuntelijalle voi paiskata toteutuneet muutokset, ne menevät kutsuneelle Activitylle. Huom! Tässä tiedostossa oleva Listener-
    // rajapinta täytyy toteuttaa luovassa Activityssä!!!
    private OnKauppaSelectedListener mListener;


    public ShopsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View localView = inflater.inflate(R.layout.fragment_shops, container, false);

        // Tehdään adapteri kauppalistalle. Loader hoitaa Cursorin, eli nyt se on null:
        kaupatAdapter = new SimpleCursorAdapter(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item,
                            null,
                            sarakkeetAdapterille,
                            kentatAdapterille,
                            0); // Ei anneta arguja sen kummemmin.
        // Lisätään vielä näkymä kun lista on auki:

        // Napataan nappi talteen: Fragmentissa view tehdään tässä ylempänä.
        kaupat = (Button)localView.findViewById(R.id.shopsfragment_store_list);

        // Tehdään napille kuuntelija, jossa näytetään custom-dialogi:
        kaupat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())

                        .setTitle(R.string.shops_dialog_select_shop)
                        .setAdapter(kaupatAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor valittu = (Cursor)kaupatAdapter.getItem(which);
                                // Liipaistaan ostoskorinäytölle (EditShoppinglistActivity) eventti jossa viedään valittu kaupan ID:
                                kaupat.setText(valittu.getString(valittu.getColumnIndex(Kauppa.NIMI)));
                                mListener.onKauppaSelected(valittu.getLong(valittu.getColumnIndex(Kauppa._ID)));

                            }
                        })
                        .setPositiveButton(R.string.shops_dialog_add_new_shop_button_label,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // "Lisää uusi kauppa -nappulaa painettaessa avataan Shops activity, jossa kauppa voidaan luoda.
                                Intent kaupatI = new Intent(getActivity(), ShopsActivity.class);
                                startActivityForResult(kaupatI, EditShoppinglistActivity.KAUPPA_ACTIVITYREULT);
                            }
                        })
                        .create().show();
            }
        });

        // Napataan Uuden kaupan luontinappi:
        ((ImageButton)localView.findViewById(R.id.shopsfragment_add_new_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ShopsActivity.class);
                getActivity().startActivityForResult(i, EditShoppinglistActivity.KAUPPA_ACTIVITYREULT);
            }
        });
        // Ja käynnistetään loader, että saadaan sitä sisältöä tuohon spinneriin/adapteriin/kursoreihin:
        getLoaderManager().initLoader(1,null,this);

        return localView;
    }

    /**
     * Asetetaan spinnerille aiemmin valittu kauppa.
     * @param oletusKauppaID
     */
    private void asetaOletusKauppa(Long oletusKauppaID) {

        // Koska kyseessä on kursori, pitää rivit loopata läpi, ja katsoa osuiko...

        for (int i = 0; i < kaupatAdapter.getCount(); i++) {
            Cursor value = (Cursor) kaupatAdapter.getItem(i);       // Tätä kursoria ei voi sulkea, koska se sulkee silloin pääkursorinkin. Warning tulee, mutta ei voi mitään.
            long id = value.getLong(value.getColumnIndex(Kauppa._ID));  //Napataan tämän rivin kaupan ID.

            if (id == oletusKauppaID) { // Kauppa osui!
                kaupat.setText(value.getString(value.getColumnIndex(Kauppa.NIMI))); // Napataan kaupan nimi buttonille tekstiksi
                return;
            }
        }

    }

    /**
     * Kun fragment luodaan ja kytketään kutsuvaan Activityyn, voidaan tässä kytkeä activityn ja fragmentin välille kuuntelijasuhde.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnKauppaSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " Activityn tulee implementoida OnKauppaSelectedListener!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Tämä interface pitää toteuttaa siinä Activityssä, joka fragmentin luo.
     */
    public interface OnKauppaSelectedListener {
        public void onKauppaSelected(long kauppaID);
    }


    // Nämä tulevat Loaderista, jonka tämä activity siis toteuttaa:
    // Loader vaaditaan Content Providerin datan sulavaan hakuun.

    /**
     * Activityssä on vain yksi LoaderManager, joka hanskaa kaikki activityn/Fragmentin kursorit eri loadereissa.
     * Eri loaderit erotellaan toisistaan ID:n perusteella. Createssa luodaan Loaderit jotka sitten päivittelee kursoreita tarvittaessa.
     * @param id    Kursorin ID, eli mitä osiota tällä kertaa etsitään.
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Kerran tässä activityssä ei ladata kuin yksi kama kannasta, ei ID:tä tarvita.

        // Tehdään kursorille loader, joka siis käsittelee tämän datan käsittelyn. Tässä siis haetaan lista kaupoista:
        return new CursorLoader(
                getActivity(),  // Konteksti, jossa pelataan
                Uri.withAppendedPath(KaupatContentProvider.CONTENT_URI,"stores"),   // Data Providerin osoite ja haettavan tietueen polku
                null,
                null,
                null,
                null);

        // Jos olisi lisää, niin tehtäisiin jokaiselle kursorille oma ID ja switch-casella teetettäis lisää...

        //return null;
    }

    /**
     * Kun argumenttina tuotu loaderi on valmistunut. Eli täällä sitä kursoria voidaan käytellä.
     * @param cursorLoader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (kaupatAdapter != null && cursor != null) {
            // Adapteri ei ole käytössä (joko alkutila tai kutsuttu ao. onLoaderReset() -metodia), dataa on. Joten paukase adapterille dataa!
            kaupatAdapter.swapCursor(cursor); // Annetaan kursori adapterin syötäväksi.

            // Ja kassotaan onko tullut vanhaa kauppanumeroa:
            if (getEdellinenKauppaID() >0) {
                // Olihan sielä. Naksautetaan spinneriin se valituksi:
                asetaOletusKauppa(getEdellinenKauppaID());
            }
        }
    }

    /**
     * Kun argumenttina oleva loaderi nollataan, eli kun siinä olevaan kursoriin haetaan uutta dataa eri arvoilla.
     * Täällä nollataan adapteri lopullisesti, tyypillisesti lasetetaan NULL arvo sille.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (kaupatAdapter != null) {
            kaupatAdapter.swapCursor(null);
        }
    }
}
