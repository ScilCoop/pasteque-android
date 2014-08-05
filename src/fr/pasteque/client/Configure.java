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

import fr.pasteque.client.utils.Compat;

import android.app.AlertDialog;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configure extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    public static final int SIMPLE_MODE = 0;
    public static final int STANDARD_MODE = 1;
    public static final int RESTAURANT_MODE = 2;

    /* Default values
     * Don't forget to update /res/layout/configure.xml to set the same
     * default value */
    private static final String DEMO_HOST = "my.pasteque.coop/4";
    private static final String DEMO_USER = "demo";
    private static final String DEMO_PASSWORD = "demo";
    private static final String DEFAULT_PRINTER_CONNECT_TRY = "3";
    private static final boolean DEFAULT_SSL = true;

    private ListPreference printerDrivers;
    private ListPreference printerModels;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set default values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains("machine_name")) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("machine_name", defaultMachineName());
            edit.commit();
        }
        if (!prefs.contains("payleven")) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("payleven", Compat.hasPaylevenApp(this));
            edit.commit();
        }
        // Load preferences
        this.addPreferencesFromResource(R.layout.configure);
        this.printerDrivers = (ListPreference) this.findPreference("printer_driver");
        this.printerModels = (ListPreference) this.findPreference("printer_model");
        CheckBoxPreference pl = (CheckBoxPreference) this.findPreference("payleven");
        this.printerDrivers.setOnPreferenceChangeListener(this);
        pl.setOnPreferenceChangeListener(this);
        this.updatePrinterPrefs(null);
    }

    private void updatePrinterPrefs(Object newValue) {
        if (newValue == null) {
            newValue = this.getPrinterDriver(this);
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
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("printer_driver")) {
            // On printer driver update, change models
            if (newValue.equals("EPSON ePOS")
                    && !Compat.isEpsonPrinterCompatible()) {
                Toast t = Toast.makeText(this, R.string.not_compatible,
                        Toast.LENGTH_SHORT);
                t.show();
                return false;
            } else if (newValue.equals("LK-PXX")
                    && !Compat.isLKPXXPrinterCompatible()) {
                Toast t = Toast.makeText(this, R.string.not_compatible,
                        Toast.LENGTH_SHORT);
                t.show();
                return false;
            }
            this.updatePrinterPrefs(newValue);
        } else if (preference.getKey().equals("payleven")) {
            if (((Boolean)newValue).booleanValue() == true
                    && !Compat.hasPaylevenApp(this)) {
                // Trying to enable payleven without app: download
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.config_payleven_download_title);
                b.setMessage(R.string.config_payleven_download_message);
                b.setIcon(android.R.drawable.ic_dialog_info);
                b.setNegativeButton(android.R.string.cancel, null);
                b.setPositiveButton(R.string.config_payleven_download_ok,
                        new DialogInterface.OnClickListener() {
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
        }
        return true;
    }

    public static boolean isConfigured(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return !prefs.getString("host", "").equals("");
    }

    public static boolean isDemo(Context ctx) {
        return DEMO_HOST.equals(Configure.getHost(ctx))
               && DEMO_USER.equals(Configure.getUser(ctx))
               && DEMO_PASSWORD.equals(Configure.getPassword(ctx));
    }

    public static String getHost(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("host", DEMO_HOST);
    }

    public static boolean getSsl(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("ssl", DEFAULT_SSL);
    }

    public static String getUser(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("user", DEMO_USER);
    }

    public static String getPassword(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("password", DEMO_PASSWORD);
    }

    private static String defaultMachineName() {
        return Build.PRODUCT + "-" + Build.DEVICE;
    }

    public static String getMachineName(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("machine_name", defaultMachineName());
    }

    public static int getTicketsMode(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return Integer.parseInt(prefs.getString("tickets_mode",
                                                String.valueOf(SIMPLE_MODE)));
    }

    /** Get associated stock location. Default "" */
    public static String getStockLocation(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("stock_location", "");
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

    public static boolean getPayleven(Context ctx) {
        boolean defaultVal = Compat.hasPaylevenApp(ctx);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean payleven = prefs.getBoolean("payleven", defaultVal);
        return payleven;
    }

    private static final int MENU_IMPORT_ID = 0;
    private static final int MENU_DEBUG_ID = 1;
    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        int i = 0;
        MenuItem imp = menu.add(Menu.NONE, MENU_IMPORT_ID, i++,
                                this.getString(R.string.menu_cfg_import));
        imp.setIcon(android.R.drawable.ic_menu_revert);
        MenuItem dbg = menu.add(Menu.NONE, MENU_DEBUG_ID, i++,
                                this.getString(R.string.menu_cfg_debug));
        dbg.setIcon(android.R.drawable.ic_menu_report_image);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected ( MenuItem item ) {
        switch (item.getItemId()) {
        case MENU_IMPORT_ID:
            // Get properties file
            // TODO: check external storage state and access
            File path = Environment.getExternalStorageDirectory();
            path = new File(path, "pasteque");
            File file = new File(path, "pasteque.properties");
            FileInputStream fis = null;
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
                    defaultMachineName());
            String ticketsMode = props.getProperty("tickets_mode",
                    "simple");
            String user = props.getProperty("user", DEMO_USER);
            String password = props.getProperty("password",
                    DEMO_PASSWORD);
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
            if (ticketsMode.equals("restaurant")) {
                edit.putString("tickets_mode",
                        String.valueOf(RESTAURANT_MODE));
            } else if (ticketsMode.equals("standard")) {
                edit.putString("tickets_mode",
                        String.valueOf(STANDARD_MODE));
            } else {
                edit.putString("tickets_mode",
                        String.valueOf(SIMPLE_MODE));
            }
            edit.putString("user", user);
            edit.putString("password", password);
            edit.putString("stock_location", location);
            edit.putString("printer_driver", printDrv);
            edit.putString("printer_model", printModel);
            edit.putString("printer_address", printAddr);
            edit.putString("printer_connect_try", printCtxTry);
            edit.commit();
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
    
}
