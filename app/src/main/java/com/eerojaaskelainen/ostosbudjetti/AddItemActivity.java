package com.eerojaaskelainen.ostosbudjetti;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.eerojaaskelainen.ostosbudjetti.models.Ostoskori;

/**
 * AddItemActivity hallitsee näytön jossa uusi tuoterivi lisätään ostoskoriin.
 */
public class AddItemActivity extends ActionBarActivity {

    private Ostoskori ostoskori;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Haetaan näkymää kutsunut: Parametrinä on pakko olla ostoskori!
        Intent kutsuja = getIntent();
        if (!kutsuja.hasExtra("Ostoskori")) {
            // Ei ollut parametriä.
            throw new IllegalArgumentException("No basket!");
        }

        ostoskori = kutsuja.getParcelableExtra("Ostoskori");

        if (ostoskori == null) {
            // Vääränlainen parametri, ei voitu määrittää Ostoskoriksi...
            throw new IllegalArgumentException("Invalid basket");
        }
        setResult(RESULT_CANCELED); // Defaultti resultti on canceled. Muutetaan sitte tarvittaessa.
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

}
