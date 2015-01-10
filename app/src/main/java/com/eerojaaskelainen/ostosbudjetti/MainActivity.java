package com.eerojaaskelainen.ostosbudjetti;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
//import android.widget.SimpleCursorAdapter;

import com.eerojaaskelainen.ostosbudjetti.binders.OstoskoriListBinder;
import com.eerojaaskelainen.ostosbudjetti.contentproviders.OstoksetContentProvider;
import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;
import com.eerojaaskelainen.ostosbudjetti.shoppinglist.EditShoppinglistActivity;


public class MainActivity extends ActionBarActivity implements
        // Nyt vanhaKori on Button, ei tarvii näitä: AdapterView.OnItemSelectedListener,
                                                                LoaderManager.LoaderCallbacks<Cursor>{

    // Nyt vanhaKori on Button, ei tarvii näitä: private Spinner vanhatKoritLista;
    private Button vanhatKoritLista;

    // Helpompi tapa oli hoitaa Binderillä tuo päivämääräviritys: private OstoskoriAdapter vanhatKoritAdapter;
    private SimpleCursorAdapter vanhatKoritAdapter;
    private static final String[] vanhatKoritAdapterSarakkeet = {Ostoskori.PVM};
    private static final int[] vanhatKoritAdapterKentat = {android.R.id.text1};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taytaVanhatKoritLista();
        // Nyt vanhaKori on Button, ei tarvii näitä: vanhatKoritLista.setOnItemSelectedListener(this);

        // Viritetään "Valitse vanha kori:" -nappula avaamaan dialogi valintaa varten:
        vanhatKoritLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.main_select_previous_cart_title))
                        .setAdapter(vanhatKoritAdapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Cursor valittu = (Cursor)vanhatKoritAdapter.getItem(which);
                                long valitunKorinID = valittu.getLong(valittu.getColumnIndex(Ostoskori._ID));
                                Ostoskori valittuKori = haeOstoskori(valitunKorinID);
                                avaaOstoskori(valittuKori);
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    private void taytaVanhatKoritLista() {
        // Nyt vanhaKori on Button, ei tarvii näitä: vanhatKoritLista = (Spinner)findViewById(R.id.main_avaaedellinen_list);
        vanhatKoritLista = (Button)findViewById(R.id.main_avaaedellinen_list);

        /*// Omalla OstoskoriAdapterilla:
        vanhatKoritAdapter = new OstoskoriAdapter(

                this,
                null,
                0
        );
        */

        vanhatKoritAdapter = new SimpleCursorAdapter(
                this.getBaseContext(),
                android.R.layout.simple_spinner_dropdown_item,
                //android.R.layout.simple_spinner_item,
                null,
                vanhatKoritAdapterSarakkeet,
                vanhatKoritAdapterKentat,
                0
        );
        vanhatKoritAdapter.setViewBinder(new OstoskoriListBinder() );

       // Nyt vanhaKori on Button, ei tarvii näitä: vanhatKoritAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // Nyt vanhaKori on Button, ei tarvii näitä:  vanhatKoritLista.setAdapter(vanhatKoritAdapter);

        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void uusiKori_Click(View view) {
        avaaOstoskori(null);
    }

    /**
     * Hakee ostoskori-olion Content Providerista
     * @param ostoskorinID
     * Haettavan ostoskorin ID
     * @return
     * Palauttaa Ostoskori-olion.
     */
    private Ostoskori haeOstoskori(long ostoskorinID) {
        Cursor curKori = this.getContentResolver().query(
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets/"+ostoskorinID),
                null,null,null,null);

        return Ostoskori.convertCursorToOstoskori(curKori);

    }


    // Nyt vanhaKori on Button, ei tarvii näitä: private boolean initial = true;
    /*
     * Hanskaa pudotusvalikoiden (tässä tapauksessa vanhojen korien) valinnan eventin
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    // Nyt vanhaKori on Button, ei tarvii näitä:
    /* @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (initial) {
            initial = false;
            return;
        }
        Cursor valittu = (Cursor)parent.getItemAtPosition(position);
        long valitunKorinID = valittu.getLong(valittu.getColumnIndex(Ostoskori._ID));
        Ostoskori valittuKori = kanta.haeOstoskori(valitunKorinID);
        avaaOstoskori(valittuKori);


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
*/
    /**
     * Luo uuden Activityn, jossa näytetään valitun / tai uuden ostoskorin sisältö:
     * @param ostoskori Ostoskori, jonka sisältö näytetään
     */
    private void avaaOstoskori(Ostoskori ostoskori) {

        Intent i = new Intent(this,EditShoppinglistActivity.class);
        if (ostoskori != null)
            i.putExtra("Ostoskori",ostoskori);
        startActivityForResult(i, 100);      // ForResult siksi, että palatessa päivitetään listaus vanhoista.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Päivitetään lista vanhoista ostoskoreista:
        getSupportLoaderManager().restartLoader(1,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Luodaan loaderi ostoskorilistalle:
        return new CursorLoader(this,
                Uri.withAppendedPath(OstoksetContentProvider.CONTENT_URI,"baskets"),
                null,
                null,
                null,
                Ostoskori.FULL_PVM + " DESC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            // Ostoskorilistan luonti valmis. Otetaan käyttöön:
        if (vanhatKoritAdapter != null && cursor != null) {
            vanhatKoritAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (vanhatKoritAdapter != null)
            vanhatKoritAdapter = null;
    }
}
