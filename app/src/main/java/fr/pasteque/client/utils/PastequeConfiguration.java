package fr.pasteque.client.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by svirch_n on 22/12/15.
 */
public class PastequeConfiguration {

    private final static String PRINTER_MODEL = "printer_model";

    private final SharedPreferences sharedPreferences;
    private final Context context;

    public PastequeConfiguration(Context appContext) {
        this.context = appContext;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public String getPrinterModel() {
        return this.sharedPreferences.getString(PRINTER_MODEL, "");
    }
}
