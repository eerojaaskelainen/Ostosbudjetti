package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.OstoksetContentProvider;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;
import com.eerojaaskelainen.ostosbudjetti.models.Ostosrivi;
import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    // Ostoskorin ID:
    protected long ostoskoriID;
    private ListView tuotelista;

    // Adapteri tuotelistalle:
    private SimpleCursorAdapter tuotelistaAdapter;
    // Ja kentät adapterille:
    private final String[] tuotelistaAdapterSarakkeet = {Tuote.NIMI, Ostosrivi._ID};
    private final int[] tuotelistaAdapterKentat = {android.R.id.text1,android.R.id.text2};

    public ProductsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null || !getArguments().containsKey(Ostoskori._ID)) {
            throw new IllegalArgumentException("Shopping cart ID must be defined as argument!");
        }
        ostoskoriID = getArguments().getLong(Ostoskori._ID,-1);
        if (ostoskoriID == -1)
            throw new IllegalArgumentException("Shopping cart ID is invalid!");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_products_list, container, false);

        // Viritetään kuntoon tuotelista-elementti:
        alustaTuotelista(v);

        return v;
    }

    /**
     * Alustetaan tuotelista, adapterit, loaderi:
     * @param v
     */
    private void alustaTuotelista(View v) {
        // Poimitaan korilistan elementti:
        tuotelista = (ListView)v.findViewById(R.id.shoppinglist_products_list);

        // Tehdään listalle sisällöntarjoaja:
        luoTuotelistaAdapterit();
        // Tehdään listalle käsittelijät:
        tuotelista.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Kun tuoteriviä napsautetaan:
                //TODO: Tee tuoterivin napsautus
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // Luodaan tuotelistalle Adapter, sekä Loader käyttämään sitä
    private void luoTuotelistaAdapterit() {
        tuotelistaAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_2,
                null,
                tuotelistaAdapterSarakkeet,
                tuotelistaAdapterKentat,
                0
                );

        tuotelista.setAdapter(tuotelistaAdapter);
        getLoaderManager().initLoader(1,null,this);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Aktivoidaan Loaderi ostosriveille:
        return  new CursorLoader(
                getActivity(),
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets/"+ Long.toString(ostoskoriID)+"/rows"),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        // Ja sitten aletaan paukuttamaan toimintaa:
        if (tuotelistaAdapter != null && cursor != null) {
            // Adapteri ei ole käytössä (joko alkutila tai kutsuttu ao. onLoaderReset() -metodia), dataa on. Joten paukase adapterille dataa!
            tuotelistaAdapter.swapCursor(cursor); // Annetaan kursori adapterin syötäväksi.

            // Ja kassotaan onko tullut vanhaa kauppanumeroa:
            // TODO: katso vanha kauppanumero!
            /*if (getEdellinenKauppaID() >0) {
                // Olihan sielä. Naksautetaan spinneriin se valituksi:
                asetaOletusKauppa(getEdellinenKauppaID());
            }*/
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Nollataan adapteri:
        if (tuotelistaAdapter != null)
            tuotelistaAdapter = null;
    }
}
