/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

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
package fr.postech.client;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class Configure extends PreferenceActivity {

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
        // Load preferences
        this.addPreferencesFromResource(R.layout.configure);
    }

    public static boolean isConfigured(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return !prefs.getString("host", "").equals("");
    }

    public static String getHost(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("host", "");
    }
    
    private static String defaultMachineName() {
        return Build.PRODUCT + "-" + Build.DEVICE;
    }

    public static String getMachineName(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("machine_name", defaultMachineName());
    }

    private static final int MENU_IMPORT_ID = 0;
    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        int i = 0;
        MenuItem imp = menu.add(Menu.NONE, MENU_IMPORT_ID, i++,
                                this.getString(R.string.menu_cfg_import));
        imp.setIcon(android.R.drawable.ic_menu_revert);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected ( MenuItem item ) {
        switch (item.getItemId()) {
        case MENU_IMPORT_ID:
            // Get properties file
            // TODO: check external storage state and access
            File path = Environment.getExternalStorageDirectory();
            path = new File(path, "postech");
            File file = new File(path, "postech.properties");
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
            String host = props.getProperty("host", "");
            String machineName = props.getProperty("machine_name", "");
            // Save
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("machine_name", defaultMachineName());
            edit.commit();
            if (!host.equals("")) {
                edit.putString("host", host);
            }
            if (!machineName.equals("")) {
                edit.putString("machine_name", machineName);
            }
            edit.commit();
            Toast t = Toast.makeText(this, R.string.cfg_import_done,
                                     Toast.LENGTH_SHORT);
            t.show();
            // Reset activity to reload values
            this.finish();
            Intent i = new Intent(this, Configure.class);
            this.startActivity(i);
            break;
        }
        return true;
    }
    
}