package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.eerojaaskelainen.ostosbudjetti.R;
import com.eerojaaskelainen.ostosbudjetti.binders.OstoskoriTuotelistaBinder;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.OstoksetContentProvider;
import com.eerojaaskelainen.ostosbudjetti.items.AddItemActivity;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;
import com.eerojaaskelainen.ostosbudjetti.models.Ostosrivi;
import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

/**
 * TuotelistaFragment näyttää ostoskorin sisältämät tuotteet.
 */
public class ProductsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    // Ostoskorin ID:
    protected long ostoskoriID;
    private ListView tuotelista;

    // Adapteri tuotelistalle:
    private SimpleCursorAdapter tuotelistaAdapter;
    // Ja kentät adapterille:
    private final String[] tuotelistaAdapterSarakkeet = {Tuote.NIMI,
            Ostosrivi.A_HINTA,
            Ostosrivi.LKM,
            "summa"};
    private final int[] tuotelistaAdapterKentat = {R.id.cart_row_product_name,
            R.id.cart_row_product_unitprice,
            R.id.cart_row_product_amount,
            R.id.cart_row_product_total};

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
       /* tuotelista.setLongClickable(true);
        tuotelista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Kun tuoteriviä pidetään painettuna, avataan valikko josta käyttäjä voi joko poistaa tai muuttaa riviä.
                Object tuotenimi = tuotelistaAdapter.getItem(position);

               
                return true;
            }
        });*/
        registerForContextMenu(tuotelista); // Rekisteröidään tarjoamaan contextmenun kuuntelijalle eventtejä.s
    }

    public void paivitaTuotelista() {
        getLoaderManager().restartLoader(1,null,this);
    }
    // Luodaan tuotelistalle Adapter, sekä Loader käyttämään sitä
    private void luoTuotelistaAdapterit() {
        tuotelistaAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.shopping_cart_row,     // Oma tyyli tuoteriville
                null,
                tuotelistaAdapterSarakkeet,
                tuotelistaAdapterKentat,
                0
                );
        tuotelistaAdapter.setViewBinder(new OstoskoriTuotelistaBinder());   // Binderi hanskaamaan valuuttamuunnokset
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
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Nollataan adapteri:
        if (tuotelistaAdapter != null)
            tuotelistaAdapter = null;
    }

    /**
     * Kun luodaan pitkän painalluksen valikkoa, tämä liipaistaan kun käyttäjä painaa jotain elementtiä tarpeeksi pitkään.
     * Vaatii myös listan rekisteröinnin registerForContextMenu(list); -komennolla.
     * @link http://www.mikeplate.com/2010/01/21/show-a-context-menu-for-long-clicks-in-an-android-listview/
     * @param menu  Menu mitä luodaan
     * @param v     Elementti, joka menua kutsui
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()== R.id.shoppinglist_products_list) {
           // AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Muokkaa riviä");
            String[] menuItems = getResources().getStringArray(R.array.cart_row_contextmenu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);    // (ei ryhmittelyä,järjestysnro, idnro, teksti)
            }
        }
    }

    /**
     * Kun luodaan pitkän painalluksen valikkoa, tämä liipaistaan kun käyttäjä valitsee toiminnon kontekstivalikosta
     * 1. vaihe tehdään yllä olevassa onCreateContextMenu -metodissa.
     * @link http://www.mikeplate.com/2010/01/21/show-a-context-menu-for-long-clicks-in-an-android-listview/
     * @param item  Valittu valinta
     * @return  Booleanin, toteutettiinko valinnan toteutus täällä vai ei.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
            case 0: //Muokkaa
                // Avataan tuotenäyttö ja viedään arguna rivin id:
                Intent i = new Intent(getActivity(),AddItemActivity.class);
                i.putExtra("OstosriviID",tuotelistaAdapter.getItemId(info.position));
                // Haetaan kori:
                EditShoppinglistActivity a = (EditShoppinglistActivity)getActivity();
                i.putExtra("Ostoskori",a.ostoskori);
                getActivity().startActivityForResult(i,EditShoppinglistActivity.OSTOSLISTA_ACTIVITYREULT);  // Laita fragmentissa getactivity, muuten requestCode on mitäsattuu.
                return true;

            case 1: // Poista
                poistaTuoteRivi(tuotelistaAdapter.getItemId(info.position));
                return true;
        }
        /*int menuItemIndex = item.getItemId();                                           //Menun valitun valinnan ID
        String[] menuItems = getResources().getStringArray(R.array.cart_row_contextmenu); // Resurssista kaivettu array menun valinnoista
        String menuItemName = item.toString();                                            // Menun valittu valinta Stringinä
        Cursor valittu = (Cursor)tuotelistaAdapter.getItem(info.position);              // Valitun listarivin kohdalla oleva tietue Cursorina
        String listItemName = valittu.getString(valittu.getColumnIndex(Tuote.NIMI));    Valitun listarivin tietueessa olevan tuotteen nimi
        */

        //Toast.makeText(getActivity(),String.format("Selected %s for item (%d): %s (id= %d)", menuItemName,menuItemIndex, listItemName,tuotelistaAdapter.getItemId(info.position)),Toast.LENGTH_LONG).show();
        return false;
    }

    private void poistaTuoteRivi(long riviID) {
        //TODO: Lisää tuoterivin poistometodi
    }
}
