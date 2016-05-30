package fr.pasteque.client.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by svirch_n on 22/12/15.
 */
public class PastequeConfiguration {

    public final static String PRINTER_MODEL = "printer_model";
    public final static String PRINTER_DRIVER = "printer_driver";
    public final static String PRINTER_ADDRESS = "printer_address";
    private static final String PRINTER_CONNECT_TRY = "printer_connect_try";
    final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int BITMAP_BUFFER_SIZE = maxMemory / 2;
    public static final java.lang.String MAIL_ENABLED = "mail_enabled";

    public boolean isPrinterDriver(String driver) {
        return false;
    }

    public boolean is(String category, String value) {
        return getShared(category).equals(value);
    }

    public boolean isPrinterThreadAPriority() {
        return false;
    }

    public int getBitmapBufferSize() {
        return BITMAP_BUFFER_SIZE;
    }

    public boolean scannerIsAutoScan() {
        return true;
    }

    public static class PrinterDriver {
        public static final String STARMPOP = "StarMPop";
        public static final String EPSON = "EPSON ePOS";
        public static final String LKPXX = "LK-PXX";
        public static final String WOOSIM = "Woosim";
        public static final String POWAPOS = "PowaPOS";
    }

    private final SharedPreferences sharedPreferences;

    public PastequeConfiguration(Context appContext) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public String getPrinterDriver() {
        return getShared(PRINTER_DRIVER);
    }

    public String getPrinterAddress() {
        return getShared(PRINTER_ADDRESS);
    }

    public String getPrinterModel() {
        return getShared(PRINTER_MODEL);
    }

    public int getPrinterConnectTry() {
        return new Integer(getShared(PRINTER_CONNECT_TRY, "0"));
    }

    public boolean isMailEnabled() {
        return getBooleanShared(MAIL_ENABLED, false);
    }

    private boolean getBooleanShared(String category, boolean defaultValue) {
        return this.sharedPreferences.getBoolean(category, defaultValue);
    }

    private String getShared(String category) {
        return getShared(category, "");
    }

    private String getShared(String category, String defaultValue) {
        return this.sharedPreferences.getString(category, defaultValue);
    }


}
