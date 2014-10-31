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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.pasteque.client.data.CashArchive;
import fr.pasteque.client.data.CashData;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.CompositionData;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.DataLoader;
import fr.pasteque.client.data.PlaceData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.data.StockData;
import fr.pasteque.client.data.TariffAreaData;
import fr.pasteque.client.data.UserData;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Composition;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Floor;
import fr.pasteque.client.models.User;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.Stock;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.sync.SendProcess;
import fr.pasteque.client.sync.SyncSend;
import fr.pasteque.client.sync.SyncUpdate;
import fr.pasteque.client.sync.UpdateProcess;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.ProgressPopup;
import fr.pasteque.client.widgets.UserBtnItem;
import fr.pasteque.client.widgets.UsersBtnAdapter;

public class Start extends TrackedActivity implements Handler.Callback {

    private static final String LOG_TAG = "Pasteque/Start";

    private GridView logins;
    private TextView status;
    private ProgressPopup syncPopup;

    private boolean syncErr;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CrashHandler.enableCrashHandler(this.getApplicationContext());
        setContentView(R.layout.connect);
        if (!DataLoader.loadAll(this)) {
            Error.showError(R.string.err_load_error, this);
        }
        SessionData.newSessionIfEmpty();
        this.status = (TextView) this.findViewById(R.id.status);
        UsersBtnAdapter adapt = new UsersBtnAdapter(UserData.users(this));
        this.logins = (GridView) this.findViewById(R.id.loginGrid);
        this.logins.setOnItemClickListener(new UserClickListener());
        this.refreshUsers();
        // Restore sync popup
        if (UpdateProcess.isStarted()) {
            this.syncPopup = new ProgressPopup(this);
            UpdateProcess.bind(this.syncPopup, this, new Handler(this));
        }
        if (SendProcess.isStarted()) {
            this.syncPopup = new ProgressPopup(this);
            SendProcess.bind(this.syncPopup, this, new Handler(this));
        }
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
        UpdateProcess.unbind();
        SendProcess.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateStatus();
    }
    
    /** Update status line */
    private void updateStatus() {
        String text = "";
        if (!Configure.isConfigured(this) && !Configure.isDemo(this)) {
            // Not configured
            text += this.getString(R.string.status_not_configured) + "\n";
        } else {
            View createAccount = this.findViewById(R.id.create_account);
            if (Configure.isDemo(this)) {
                // Demo mode
                text += this.getString(R.string.status_demo) + "\n";
                createAccount.setVisibility(View.VISIBLE);
            } else {
                // Regular mode, hide button
                createAccount.setVisibility(View.GONE);
            }
            if (!DataLoader.dataLoaded(this)) {
                // No data loaded
                text += this.getText(R.string.status_no_data) + "\n";
            }
            if (DataLoader.hasLocalData(this)) {
                // Local data
                text += this.getText(R.string.status_has_local_data) + "\n";
            }
            try {
                // Local archives
                int archiveCount = CashArchive.getArchiveCount(this);
                if (archiveCount > 0) {
                    text += this.getResources().getQuantityString(R.plurals.status_has_archive,
                            archiveCount, archiveCount) + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.status.setText(Html.fromHtml(text));
        View container = this.findViewById(R.id.status_container);
        if (text.equals("")) {
            // No text
            container.setVisibility(View.GONE);
        } else {
            // Remove last line feed and display text
            text = text.substring(0, text.length() - 1);
            this.status.setText(text);
            container.setVisibility(View.VISIBLE);
        }
    }

    /** Update users button grid */
    private void refreshUsers() {
        UsersBtnAdapter adapt = new UsersBtnAdapter(UserData.users(this));
        this.logins.setAdapter(adapt);
    }

    public void showCreateAccount(View v) {
        Uri uri = Uri.parse(this.getString(R.string.app_create_account_url));
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private class UserClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            UserBtnItem item = (UserBtnItem) v;
            User user = item.getUser();
            if (user.hasPassword() == true) {
                Intent pass = new Intent(Start.this, Password.class);
                pass.putExtra("User", user);
                startActivityForResult(pass, Password.CODE_PASSWORD);
            } else {
                Start.this.enterApp(user);
            }
        }
    }

    protected void onActivityResult (int requestCode, int resultCode,
                                     Intent data) {
	switch (requestCode) {
	case 0:
	    switch (resultCode) {
	    case Activity.RESULT_CANCELED:  
		break;
	    case Activity.RESULT_OK:
		this.goOn();
		break;
	    }
	    break;
	case TicketSelect.CODE_TICKET:
	    switch (resultCode) {
	    case Activity.RESULT_CANCELED:
		break;
	    case Activity.RESULT_OK:
		TicketInput.setup(CatalogData.catalog(this),
				  SessionData.currentSession(this).getCurrentTicket());
		Intent i = new Intent(Start.this, TicketInput.class);
		this.startActivity(i);
		break;
	    }
        break;
    case Password.CODE_PASSWORD:
        switch (resultCode) {
        case Activity.RESULT_CANCELED:
            break;
        case Activity.RESULT_OK:
            // User auth OK
            User user = (User) data.getSerializableExtra("User");
            this.enterApp(user);
            break;
        }
	}
    }

    /** Open app once user is picked */
    private void enterApp(User user) {
        SessionData.currentSession(Start.this).setUser(user);
        Cash c = CashData.currentCash(Start.this);
        if (c != null && !c.isOpened()) {
            // Cash is not opened
            Intent i = new Intent(Start.this, OpenCash.class);
            Start.this.startActivityForResult(i, 0);
            Start.this.overridePendingTransition(R.transition.fade_in,
                    R.transition.fade_out);
        } else if (c != null && c.isOpened() && !c.isClosed()) {
            // Cash is opened
            Start.this.goOn();
        } else if (c != null && c.isClosed()) {
            // Cash is closed
            Intent i = new Intent(Start.this, OpenCash.class);
            Start.this.startActivity(i);
            Start.this.overridePendingTransition(R.transition.fade_in,
                    R.transition.fade_out);
        } else {
            // Where is it?
            Log.e(LOG_TAG, "No cash while openning session. Cash is "
                    + c);
            Error.showError(R.string.err_no_cash, Start.this);
        }
    }

    /** Open ticket edition */
    private void goOn() {
        int mode = Configure.getTicketsMode(Start.this);
        switch (mode) {
        case Configure.RESTAURANT_MODE:
            // Always show tables
            Intent i = new Intent(Start.this, TicketSelect.class);
            Start.this.startActivity(i);
            Start.this.overridePendingTransition(R.transition.fade_in,
                    R.transition.fade_out);
            break;
        case Configure.STANDARD_MODE:
            if (SessionData.currentSession(this).hasWaitingTickets()) {
                // Go directly to first ticket
                Session currSession = SessionData.currentSession(this);
                TicketInput.setup(CatalogData.catalog(this),
                        currSession.getCurrentTicket());
                i = new Intent(Start.this, TicketInput.class);
                Start.this.startActivity(i);
                Start.this.overridePendingTransition(R.transition.fade_in,
                        R.transition.fade_out);
                break;
            }
            // else same thing as simple mode
        case Configure.SIMPLE_MODE:
            // Create a ticket if not existing and go to edit
            Session currSession = SessionData.currentSession(this);
            if (currSession.getCurrentTicket() == null) {
                Ticket t = currSession.newTicket();
            }
            TicketInput.setup(CatalogData.catalog(this),
                                currSession.getCurrentTicket());
            i = new Intent(Start.this, TicketInput.class);
            Start.this.startActivity(i);
            Start.this.overridePendingTransition(R.transition.fade_in,
                    R.transition.fade_out);
        }
    }

    public static void backToStart(Context ctx) {
	Intent i = new Intent(ctx, Start.class);
	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	ctx.startActivity(i);
    }

    private static final int MENU_SYNC_UPD_ID = 0;
    private static final int MENU_CONFIG_ID = 1;
    private static final int MENU_ABOUT_ID = 2;
    private static final int MENU_SYNC_SND_ID = 3;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem syncUpd = menu.add(Menu.NONE, MENU_SYNC_UPD_ID, 0,
                                    this.getString(R.string.menu_sync_update));
        syncUpd.setIcon(R.drawable.ico_maj);
        syncUpd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        MenuItem syncSnd = menu.add(Menu.NONE, MENU_SYNC_SND_ID, 1,
                                    this.getString(R.string.menu_sync_send));
        syncSnd.setIcon(R.drawable.ico_envoi_infos);
        syncSnd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        MenuItem config = menu.add(Menu.NONE, MENU_CONFIG_ID, 2,
                                   this.getString(R.string.menu_config));
        config.setIcon(R.drawable.ico_reglage);
        config.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        MenuItem about = menu.add(Menu.NONE, MENU_ABOUT_ID, 3,
                this.getString(R.string.menu_about));
        about.setIcon(R.drawable.ico_help);
        about.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!Configure.isConfigured(this) && !Configure.isDemo(this)) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
            try {
                if (CashArchive.getArchiveCount(this) > 0) {
                    menu.getItem(1).setEnabled(true);
                } else {
                    menu.getItem(1).setEnabled(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
            this.syncPopup = new ProgressPopup(this);
            UpdateProcess.start(this.getApplicationContext());
            UpdateProcess.bind(this.syncPopup, this, new Handler(this));
            break;
        case MENU_SYNC_SND_ID:
            Log.i(LOG_TAG, "Starting sending data");
            this.syncErr = false;
            this.syncPopup = new ProgressPopup(this);
            this.syncPopup.setButton(AlertDialog.BUTTON_NEUTRAL,
                    this.getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int which) {
                            SendProcess.stop();
                        }
                    });
            SendProcess.start(this.getApplicationContext());
            SendProcess.bind(this.syncPopup, this, new Handler(this));
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
        case SyncUpdate.SYNC_DONE:
            this.updateStatus();
            this.refreshUsers();
            break;
        case SyncSend.SYNC_ERROR:
            if (m.obj instanceof Exception) {
                // Response error (unexpected content)
                Log.i(LOG_TAG, "Server error " + m.obj);
                Error.showError(R.string.err_server_error, this);
            } else {
                // String user error
                String error = (String) m.obj;
                if ("Not logged".equals(error)) {
                    Log.i(LOG_TAG, "Not logged");
                    Error.showError(R.string.err_not_logged, this);
                } else {
                    Log.e(LOG_TAG, "Unknown server errror: " + error);
                    Error.showError(R.string.err_server_error, this);
                }
            }
            break;

        case SyncSend.SYNC_DONE:
            Log.i(LOG_TAG, "Sending data finished.");
            this.updateStatus();
            break;
        }
        return true;
    }
}
