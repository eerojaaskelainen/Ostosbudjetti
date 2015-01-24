package com.eerojaaskelainen.ostosbudjetti;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created by Eero on 24.1.2015.
 */
public class CursorLoaderAutoCompleteTextView extends AutoCompleteTextView {
    public CursorLoaderAutoCompleteTextView(Context context) {
        super(context);
    }

    public CursorLoaderAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CursorLoaderAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

   /* public CursorLoaderAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        // Filtering is done with CursorLoader
    }
}
