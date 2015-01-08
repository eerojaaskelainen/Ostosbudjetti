package com.eerojaaskelainen.ostosbudjetti.shoppinglist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by Eero on 30.12.2014.
 */
public class TimePickerFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Alustetaan pickeri näyttämään tämän hetkistä aikaa:

        final Calendar c = Calendar.getInstance();
        if (!getArguments().isEmpty()) {
            // Otetaan argumenttina tulleesta ajasta aikaleima, josta tehdään oletusaika:
            try {
                c.setTimeInMillis(getArguments().getLong("oletusaika"));
            } catch (Exception e) {}
        }

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Luodaan uusi dialogi ja palautetaan se kyselijälle:

        return new TimePickerDialog(getActivity(),(TimePickerDialog.OnTimeSetListener)getActivity(),hour,minute, DateFormat.is24HourFormat(getActivity()));

    }


}
