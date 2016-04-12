package fr.pasteque.client.viewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.pasteque.api.utils.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * Created by svirch_n on 22/12/15.
 */
public class PastequeConfiguration implements Configuration{

    public final static String PRINTER_MODEL = "printer_model";
    public final static String PRINTER_DRIVER = "printer_driver";
    public final static String PRINTER_ADDRESS = "printer_address";
    public static final String MAIL_ENABLED = "mail_enabled";
    public static final String USER = Pasteque.getStringResource(R.string.conf_user);
    public static final String PASSWORD = Pasteque.getStringResource(R.string.conf_password);
    public static final String MACHINE = Pasteque.getStringResource(R.string.conf_machine_name);
    public static final String HOST = Pasteque.getStringResource(R.string.conf_host);

    public boolean isPrinterDriver(String driver) {
        return false;
    }

    public boolean is(String category, String value) {
        return getShared(category).equals(value);
    }

    public String getLogin() {
        return getShared(USER);
    }

    public String getPassword() {
        return getShared(PASSWORD);
    }

    public String getHostname() {
        return getShared(HOST);
    }

    @Override
    public String getCharset() {
        return StandardCharsets.UTF_8.name();
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
