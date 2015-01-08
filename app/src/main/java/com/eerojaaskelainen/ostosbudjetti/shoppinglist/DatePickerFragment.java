package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Eero on 30.12.2014.
 */
public class DatePickerFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Napataan oletusaika:
        final Calendar c = Calendar.getInstance();

        if (!getArguments().isEmpty()) {
            // Otetaan argumenttina tulleesta ajasta aikaleima, josta tehdään oletusaika:
            try {
                c.setTimeInMillis(getArguments().getLong("oletusaika"));
            } catch (Exception e) {}
        }


        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Luodaan uusi dialogi ja palautetaan se kyselijälle:

        // Katsotaan, kuka on kyselijänä:

        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day);

    }


}
