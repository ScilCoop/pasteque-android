/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.pasteque.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import fr.pasteque.client.utils.*;
import fr.pasteque.client.utils.Error;

//Deprecation concerns the PreferenceFragment
@SuppressWarnings("deprecation")
public class Configure extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    public static final int STATUS_ACCOUNT = 0;
    public static final int STATUS_DEMO = 1;
    public static final int STATUS_NONE = 2;

    public static final int SIMPLE_MODE = 0;
    public static final int STANDARD_MODE = 1;
    public static final int RESTAURANT_MODE = 2;

    public static final int MANUAL_SYNC_MODE = 0;
    public static final int AUTO_SYNC_MODE = 1;

    public static final String ERROR = "Error";

    /* Default values
     * Don't forget to update /res/xml/configure.xml to set the same
     * default value */
    private static final String DEMO_HOST = "my.pasteque.coop/6";
    private static final int DEMO_USER = R.string.demo_user;
    private static final int DEMO_PASSWORD = R.string.demo_password;
    private static final int DEMO_CASHREGISTER = R.string.demo_cash;
    private static final String DEFAULT_USER = "";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_CASHREGISTER = "Caisse";
    private static final String DEFAULT_PRINTER_CONNECT_TRY = "3";
    private static final boolean DEFAULT_SSL = true;
    private static final boolean DEFAULT_DISCOUNT = true;
    private static String LABEL_STATUS = "status";

    private ListPreference printerDrivers;
    private ListPreference printerModels;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set default values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains("payleven")) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("payleven", Compat.hasPaylevenApp(this));
            edit.apply();
        }
        // Load preferences
        this.addPreferencesFromResource(R.xml.configure);
        this.printerDrivers = (ListPreference) this.findPreference("printer_driver");
        this.printerModels = (ListPreference) this.findPreference("printer_model");
        this.printerDrivers.setOnPreferenceChangeListener(this);
        this.updatePrinterPrefs(null);

        ListPreference card_processor = (ListPreference) this.findPreference("card_processor");
        card_processor.setOnPreferenceChangeListener(this);
        this.updateCardProcessorPreferences(null);
        if (this.comesFromError()) {
            this.showError(this.getError());
        }
        if (Configure.isDemo(this)) {
            findPreference("user").setEnabled(false);
            findPreference("password").setEnabled(false);
        }
    }

    /**
     * Display an AlertDialog
     * Based on Error.showError() but Configuration is not a TrackedActivity
     * @param message to display
     */
    private void showError(String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.error_title);
        b.setMessage(message);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setCancelable(true);
        b.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Configure.this.invalidateError();
            }
        });
        b.show();
    }

    private void invalidateError() {
        getIntent().removeExtra(Configure.ERROR);
    }

    private boolean comesFromError() {
        return getIntent().hasExtra(Configure.ERROR);
    }

    private String getError() {
        return getIntent().getStringExtra(Configure.ERROR);
    }

    private static String getString(Context ctx, int id) {
        return ctx.getResources().getString(id);
    }

    private void updateCardProcessorPreferences(String newValue) {
        if (newValue == null) {
            newValue = Configure.getCardProcessor(this);
        }

        ListPreference card_processor = (ListPreference) this.findPreference("card_processor");

        EditTextPreference atos_address = (EditTextPreference) this.findPreference("worldline_address");
        EditTextPreference xengo_userid = (EditTextPreference) this.findPreference("xengo_userid");
        EditTextPreference xengo_password = (EditTextPreference) this.findPreference("xengo_password");
        EditTextPreference xengo_terminalid = (EditTextPreference) this.findPreference("xengo_terminalid");

        atos_address.setEnabled("atos_classic".equals(newValue));
        xengo_userid.setEnabled("atos_xengo".equals(newValue));
        xengo_password.setEnabled("atos_xengo".equals(newValue));
        xengo_terminalid.setEnabled("atos_xengo".equals(newValue));


        card_processor.setSummary(newValue);
        int i = 0;
        for (CharSequence entry : card_processor.getEntryValues()) {
            if (newValue.equals(entry)) {
                card_processor.setSummary(card_processor.getEntries()[i]);
            }
            i++;
        }
    }

    private void updatePrinterPrefs(Object newValue) {
        if (newValue == null) {
            newValue = Configure.getPrinterDriver(this);
        }
        if (newValue.equals("None")) {
            this.printerModels.setEnabled(false);
        } else if (newValue.equals("EPSON ePOS")) {
            this.printerModels.setEnabled(true);
            this.printerModels.setEntries(R.array.config_printer_model_epson_epos);
            this.printerModels.setEntryValues(R.array.config_printer_model_epson_epos_values);
            this.printerModels.setValueIndex(0);
        } else if (newValue.equals("LK-PXX")) {
            this.printerModels.setEnabled(true);
            this.printerModels.setEntries(R.array.config_printer_model_lk_pxx);
            this.printerModels.setEntryValues(R.array.config_printer_model_lk_pxx_values);
            this.printerModels.setValueIndex(0);
        } else if (newValue.equals("Woosim")) {
            this.printerModels.setEnabled(true);
            this.printerModels.setEntries(R.array.config_printer_model_woosim);
            this.printerModels.setEntryValues(R.array.config_printer_model_woosim_values);
            this.printerModels.setValueIndex(0);
        } else if (newValue.equals("PowaPOS")) {
            this.printerModels.setEnabled(true);
            this.printerModels.setEntries(R.array.config_printer_model_powapos);
            this.printerModels.setEntryValues(R.array.config_printer_model_powapos_values);
            this.printerModels.setValueIndex(0);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("printer_driver")) {
            // On printer driver update, change models
            if (newValue.equals("EPSON ePOS")
                    && !Compat.isEpsonPrinterCompatible()) {
                Toast t = Toast.makeText(this, R.string.not_compatible,
                        Toast.LENGTH_SHORT);
                t.show();
                return false;
            } else if ((newValue.equals("LK-PXX")
                    && !Compat.isLKPXXPrinterCompatible())
                    || (newValue.equals("Woosim")
                    && !Compat.isWoosimPrinterCompatible())) {
                Toast t = Toast.makeText(this, R.string.not_compatible,
                        Toast.LENGTH_SHORT);
                t.show();
                return false;
            }
            this.updatePrinterPrefs(newValue);
        } else if ("card_processor".equals(preference.getKey())) {
            if ("payleven".equals(newValue) && !Compat.hasPaylevenApp(this)) {
                // Trying to enable payleven without app: download
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.config_payleven_download_title);
                b.setMessage(R.string.config_payleven_download_message);
                b.setIcon(android.R.drawable.ic_dialog_info);
                b.setNegativeButton(android.R.string.cancel, null);
                b.setPositiveButton(R.string.config_payleven_download_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                Intent i = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=de.payleven.androidphone"));
                                Configure.this.startActivity(i);
                            }
                        });
                b.show();
                return false;
            }

            this.updateCardProcessorPreferences((String) newValue);
        }
        return true;
    }

    public static boolean isConfigured(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return !prefs.getString("host", "").equals("");
    }

    public static String getHost(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("host", DEMO_HOST);
    }

    public static boolean getSsl(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("ssl", DEFAULT_SSL);
    }

    public static boolean getDiscount(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("discount", DEFAULT_DISCOUNT);
    }

    public static String getUser(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("user", DEFAULT_USER);
    }

    public static String getPassword(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("password", DEFAULT_PASSWORD);
    }

    public static String getMachineName(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("machine_name", DEFAULT_CASHREGISTER);
    }

    public static boolean getCheckStockOnClose(Context ctx) {
        return false; // TODO: add config value for CheckStockOnClose
    }

    public static int getTicketsMode(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return Integer.parseInt(prefs.getString("tickets_mode",
                String.valueOf(SIMPLE_MODE)));
    }

    public static String getPrinterDriver(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("printer_driver", "None");
    }

    public static String getPrinterModel(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("printer_model", "");
    }

    public static String getPrinterAddress(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("printer_address", "").toUpperCase();
    }

    public static int getPrinterConnectTry(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String strPref = prefs.getString("printer_connect_try",
                DEFAULT_PRINTER_CONNECT_TRY);
        try {
            return Integer.parseInt(strPref);
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULT_PRINTER_CONNECT_TRY);
        }
    }

    public static int getSyncMode(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return Integer.parseInt(prefs.getString("sync_mode",
                String.valueOf(MANUAL_SYNC_MODE)));
    }

    private static final int MENU_IMPORT_ID = 0;
    private static final int MENU_DEBUG_ID = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int i = 0;
        MenuItem imp = menu.add(Menu.NONE, MENU_IMPORT_ID, i++,
                this.getString(R.string.menu_cfg_import));
        imp.setIcon(android.R.drawable.ic_menu_revert);
        MenuItem dbg = menu.add(Menu.NONE, MENU_DEBUG_ID, i,
                this.getString(R.string.menu_cfg_debug));
        dbg.setIcon(android.R.drawable.ic_menu_report_image);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_IMPORT_ID:
                // Get properties file
                // TODO: check external storage state and access
                File path = Environment.getExternalStorageDirectory();
                path = new File(path, "pasteque");
                File file = new File(path, "pasteque.properties");
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast t = Toast.makeText(this,
                            R.string.cfg_import_file_not_found,
                            Toast.LENGTH_SHORT);
                    t.show();
                    return true;
                }
                Properties props = new Properties();
                try {
                    props.load(fis);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast t = Toast.makeText(this,
                            R.string.cfg_import_read_error,
                            Toast.LENGTH_SHORT);
                    t.show();
                    return true;
                }
                // Load props
                String host = props.getProperty("host", DEMO_HOST);
                String machineName = props.getProperty("machine_name",
                        null);
                String ticketsMode = props.getProperty("tickets_mode",
                        "simple");
                String user = props.getProperty("user", null);
                String password = props.getProperty("password",
                        null);
                String location = props.getProperty("stock_location", "");
                String printDrv = props.getProperty("printer_driver",
                        "None");
                String printModel = props.getProperty("printer_model",
                        "");
                String printAddr = props.getProperty("printer_address",
                        "");
                String printCtxTry = props.getProperty("printer_connect_try", DEFAULT_PRINTER_CONNECT_TRY);
                // Save
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("host", host);
                edit.putString("machine_name", machineName);
                // Set tickets mode, simple by default
                switch (ticketsMode) {
                    case "restaurant":
                        edit.putString("tickets_mode",
                                String.valueOf(RESTAURANT_MODE));
                        break;
                    case "standard":
                        edit.putString("tickets_mode",
                                String.valueOf(STANDARD_MODE));
                        break;
                    default:
                        edit.putString("tickets_mode",
                                String.valueOf(SIMPLE_MODE));
                        break;
                }
                edit.putString("user", user);
                edit.putString("password", password);
                edit.putString("stock_location", location);
                edit.putString("printer_driver", printDrv);
                edit.putString("printer_model", printModel);
                edit.putString("printer_address", printAddr);
                edit.putString("printer_connect_try", printCtxTry);
                edit.apply();
                Toast t = Toast.makeText(this, R.string.cfg_import_done,
                        Toast.LENGTH_SHORT);
                t.show();
                // Reset activity to reload values
                this.finish();
                Intent i = new Intent(this, Configure.class);
                this.startActivity(i);
                break;
            case MENU_DEBUG_ID:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.cfg_debug_alert_title);
                b.setMessage(R.string.cfg_debug_alert_message);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setNegativeButton(android.R.string.cancel, null);
                b.setPositiveButton(R.string.cfg_debug_alert_continue,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent i = new Intent(Configure.this, Debug.class);
                                Configure.this.startActivity(i);
                            }
                        });
                b.show();
                break;
        }
        return true;
    }

    public static String getCardProcessor(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("card_processor", null);
    }

    public static String getWorldlineAddress(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("worldline_address", "");
    }

    public static String getXengoUserId(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("xengo_userid", "");
    }

    public static String getXengoTerminalId(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("xengo_terminalid", "");
    }

    public static String getXengoPassword(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("xengo_password", "");
    }

    private static void set(Context ctx, String label, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit()
                .putString(label, value)
                .apply();
    }

    private static void set(Context ctx, String label, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit()
                .putInt(label, value)
                .apply();
    }

    public static void setUser(Context ctx, String user) {
        Configure.set(ctx, "user", user);
    }

    public static void setPassword(Context ctx, String psswd) {
        Configure.set(ctx, "password", psswd);
    }

    public static void setCashRegister(Context ctx, String cash) {
        Configure.set(ctx, "machine_name", cash);
    }

    public static void setStatus(Context ctx, int status) {
        Configure.set(ctx, LABEL_STATUS, status);
    }

    public static int getStatus(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt(LABEL_STATUS, Configure.STATUS_NONE);
    }

    public static void setDemo(Context ctx) {
        Configure.setAccount(ctx,
                getString(ctx, DEMO_USER),
                getString(ctx, DEMO_PASSWORD),
                getString(ctx, DEMO_CASHREGISTER),
                true);
    }

    private static void setAccount(Context ctx, String user, String pwd, String cash, boolean isDemo) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("password", pwd);
        edit.putString("user", user);
        edit.putString("machine_name", cash);
        if (isDemo) {
            edit.putInt(LABEL_STATUS, STATUS_DEMO);
        } else {
            edit.putInt(LABEL_STATUS, STATUS_ACCOUNT);
        }
        edit.apply();
    }

    public static void setAccount(Context ctx, String user, String passwd) {
        Configure.setAccount(ctx, user, passwd, DEFAULT_CASHREGISTER, false);
    }

    public static void setAccount(Context ctx, String user, String passwd, String cash) {
        Configure.setAccount(ctx, user, passwd, cash, false);
    }

    public static void invalidateAccount(Context ctx) {
        setUser(ctx, getString(ctx, DEMO_USER));
        setPassword(ctx, getString(ctx, DEMO_PASSWORD));
        setStatus(ctx, STATUS_NONE);
    }

    /**
     * Very important function!
     * Start.removeLocalData rely on this on
     * @param ctx the application's context
     * @return <code>true</code> if the current account is a demo
     */
    public static boolean isDemo(Context ctx) {
        return getStatus(ctx) == STATUS_DEMO;
    }

    public static boolean noAccount(Context ctx) {
        return getStatus(ctx) == STATUS_NONE;
    }

    public static boolean isAccount(Context ctx) {
        return getStatus(ctx) == STATUS_ACCOUNT;
    }

/*

    public static boolean getPayleven(Context ctx) {
		return "payleven".equals(getCardProcessor(ctx));

		// Old code enabled payleven automatically if the app was installed
//        boolean defaultVal = Compat.hasPaylevenApp(ctx);
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
//        boolean payleven = prefs.getBoolean("payleven", defaultVal);
//        return payleven;
    }*/
}
