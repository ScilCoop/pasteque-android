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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.data.CashData;
import fr.postech.client.data.CatalogData;
import fr.postech.client.data.DataLoader;
import fr.postech.client.data.ReceiptData;
import fr.postech.client.data.SessionData;
import fr.postech.client.data.UserData;
import fr.postech.client.models.Cash;
import fr.postech.client.models.Catalog;
import fr.postech.client.models.User;
import fr.postech.client.models.Session;
import fr.postech.client.models.Ticket;
import fr.postech.client.widgets.UserBtnItem;
import fr.postech.client.widgets.UsersBtnAdapter;

public class Start extends Activity implements Handler.Callback {

    private static final String LOG_TAG = "POS-TECH/Start";

    private GridView logins;
    private TextView status;

    private boolean syncErr;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CrashHandler.enableCrashHandler(this.getApplicationContext());
        setContentView(R.layout.connect);
        if (!DataLoader.loadAll(this)) {
            Error.showError(R.string.err_reload, this);
        }
        this.status = (TextView) this.findViewById(R.id.status);
        UsersBtnAdapter adapt = new UsersBtnAdapter(UserData.users);
        this.logins = (GridView) this.findViewById(R.id.loginGrid);
        this.logins.setOnItemClickListener(new UserClickListener());
        this.refreshUsers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            SessionData.saveSession(this);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Unable to save session on exit", ioe);
            Error.showError(R.string.err_save_session, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateStatus();
    }
    
    /** Update status line */
    private void updateStatus() {
        String text = "";
        if (!Configure.isConfigured(this)) {
            text += this.getString(R.string.status_not_configured) + "\n";
        } else {
            if (!DataLoader.dataLoaded()) {
                text += this.getText(R.string.status_no_data) + "\n";
            }
            if (DataLoader.hasDataToSend()) {
                text += this.getText(R.string.status_has_local_data) + "\n";
            }
        }
        this.status.setText(text);
        if (text.equals("")) {
            // No text
            this.status.setVisibility(View.GONE);
        } else {
            // Remove last line feed and display text
            text = text.substring(0, text.length() - 1);
            this.status.setText(text);
            this.status.setVisibility(View.VISIBLE);
        }
    }

    /** Update users button grid */
    private void refreshUsers() {
        UsersBtnAdapter adapt = new UsersBtnAdapter(UserData.users);
        this.logins.setAdapter(adapt);
    }

    private class UserClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            UserBtnItem item = (UserBtnItem) v;
            User user = item.getUser();
            SessionData.currentSession.setUser(user);
            if (SessionData.currentSession.getCurrentTicket() == null) {
                // Create a ticket if there isn't anyone
                Ticket t = SessionData.currentSession.newTicket();
            }
            try {
                SessionData.saveSession(Start.this);
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Unable to save session", ioe);
                Error.showError(R.string.err_save_session,
                                Start.this);
            }
            Cash c = CashData.currentCash;
            if (c != null && !c.isOpened()) {
                // Cash is not opened
                Intent i = new Intent(Start.this, OpenCash.class);
                Start.this.startActivity(i);
            } else if (c != null && c.isOpened() && !c.isClosed()) {
                // Cash is opened
                TicketInput.setup(CatalogData.catalog,
                                  SessionData.currentSession.getCurrentTicket());
                Intent i = new Intent(Start.this, TicketInput.class);
                Start.this.startActivity(i);
            } else if (c != null && c.isClosed()) {
                // Cash is closed
                Intent i = new Intent(Start.this, OpenCash.class);
                Start.this.startActivity(i);
            } else {
                // Where is it?
                Log.e(LOG_TAG, "No cash while openning session. Cash is "
                      + c);
                Error.showError(R.string.err_no_cash, Start.this);
            }
        }
    }

    private static final int MENU_SYNC_UPD_ID = 0;
    private static final int MENU_CONFIG_ID = 1;
    private static final int MENU_ABOUT_ID = 2;
    private static final int MENU_SYNC_SND_ID = 3;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem syncUpd = menu.add(Menu.NONE, MENU_SYNC_UPD_ID, 0,
                                    this.getString(R.string.menu_sync_update));
        syncUpd.setIcon(android.R.drawable.ic_menu_rotate);
        MenuItem syncSnd = menu.add(Menu.NONE, MENU_SYNC_SND_ID, 1,
                                    this.getString(R.string.menu_sync_send));
        syncSnd.setIcon(android.R.drawable.ic_menu_upload);
        MenuItem about = menu.add(Menu.NONE, MENU_ABOUT_ID, 2,
                                  this.getString(R.string.menu_about));
        about.setIcon(android.R.drawable.ic_menu_info_details);
        MenuItem config = menu.add(Menu.NONE, MENU_CONFIG_ID, 3,
                                   this.getString(R.string.menu_config));
        config.setIcon( android.R.drawable.ic_menu_preferences );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!Configure.isConfigured(this)) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
            if (DataLoader.hasDataToSend()) {
                menu.getItem(1).setEnabled(true);
            } else {
                menu.getItem(1).setEnabled(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SYNC_UPD_ID:
            // Sync
            Log.i(LOG_TAG, "Starting update");
            this.syncErr = false;
            SyncUpdate syncUpdate = new SyncUpdate(this, new Handler(this));
            syncUpdate.startSyncUpdate();
            break;
        case MENU_SYNC_SND_ID:
            Log.i(LOG_TAG, "Starting sending data");
            this.syncErr = false;
            SyncSend syncSnd = new SyncSend(this, new Handler(this),
                                            ReceiptData.getReceipts(),
                                            CashData.currentCash);
            syncSnd.startSyncSend();
            break;
        case MENU_ABOUT_ID:
            // About
            About.showAbout(this);
            break;
        case MENU_CONFIG_ID:
            Intent i = new Intent(this, Configure.class);
            this.startActivity(i);
            break;
        }
        return true;
    }

    /** Handle for synchronization progress */
    public boolean handleMessage(Message m) {
        switch (m.what) {
        case SyncUpdate.CONNECTION_FAILED:
        case SyncSend.CONNECTION_FAILED:
            if (m.obj instanceof Exception) {
                Log.i(LOG_TAG, "Connection error", ((Exception)m.obj));
                Error.showError(R.string.err_connection_error, this);
            } else {
                Log.i(LOG_TAG, "Server error " + m.obj);
                Error.showError(R.string.err_server_error, this);
            }
            break;
            
        case SyncUpdate.CATALOG_SYNC_DONE:
            Catalog catalog = (Catalog) m.obj;
            CatalogData.catalog = catalog;
            try {
                CatalogData.save(this);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save catalog", e);
                Error.showError(R.string.err_save_catalog, this);
            }
            break;
        case SyncUpdate.USERS_SYNC_DONE:
            List<User> users = (List) m.obj;
            UserData.users = users;
            try {
                UserData.save(this);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save users", e);
                Error.showError(R.string.err_save_users, this);
            }
            this.refreshUsers();
            break;
        case SyncUpdate.CASH_SYNC_DONE:
            Cash cash = (Cash) m.obj;
            boolean save = false;
            if (CashData.currentCash == null) {
                CashData.currentCash = cash;
                save = true;
            } else if (CashData.mergeCurrent(cash)) {
                save = true;
            } else {
                // TODO: Cash conflict!
            }
            if (save) {
                try {
                    CashData.save(this);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to save cash", e);
                    Error.showError(R.string.err_save_cash, this);
                }
            }
            break;
        case SyncUpdate.SYNC_DONE:
            // Synchronization finished
            Log.i(LOG_TAG, "Update sync finished.");
            this.updateStatus();
            break;

        case SyncSend.EPIC_FAIL:
            Error.showError(R.string.err_sync, this);
            break;
        case SyncSend.RECEIPTS_SYNC_DONE:
            Log.i(LOG_TAG, "Receipts sent, clearing them.");
            ReceiptData.clear(this);
            break;
        case SyncSend.RECEIPTS_SYNC_FAILED:
            Log.e(LOG_TAG, "Receipts sync error. Server returned:");
            Log.e(LOG_TAG, (String)m.obj);
            this.syncErr = true;
            break;
        case SyncSend.CASH_SYNC_DONE:
            Cash newCash = (Cash) m.obj;
            if (!CashData.mergeCurrent(newCash)) {
                // The server send us back a new cash
                // (meaning the previous is closed)
                CashData.currentCash = newCash;
                CashData.dirty = false;
            }
            CashData.dirty = false;
            try {
                CashData.save(this);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save cash", e);
                Error.showError(R.string.err_save_cash, this);
            }
            break;
        case SyncSend.CASH_SYNC_FAILED:
            Log.e(LOG_TAG, "Cash sync error. Server returned:");
            Log.e(LOG_TAG, (String)m.obj);
            this.syncErr = true;
            break;
        case SyncSend.SYNC_DONE:
            Log.i(LOG_TAG, "Sending data finished.");
            this.updateStatus();
            if (this.syncErr) {
                Error.showError(R.string.err_sync, this);
                this.syncErr = false; // Reset for next time
            }
            break;
        }
        return true;
    }
}
