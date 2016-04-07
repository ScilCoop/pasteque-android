package fr.pasteque.client.viewer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 17:59.
 */
public class Configure extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.configure);
    }
}
