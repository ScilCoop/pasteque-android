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

import java.io.IOError;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import android.widget.Toast;
import com.mpowa.android.sdk.common.dataobjects.PowaDeviceObject;
import com.mpowa.android.sdk.powapos.core.abstracts.PowaScanner;
import com.mpowa.android.sdk.powapos.drivers.s10.PowaS10Scanner;
import com.mpowa.android.sdk.powapos.drivers.tseries.PowaTSeries;

import fr.pasteque.client.data.*;
import fr.pasteque.client.models.*;
import fr.pasteque.client.sync.SendProcess;
import fr.pasteque.client.sync.SyncSend;
import fr.pasteque.client.sync.SyncUpdate;
import fr.pasteque.client.sync.UpdateProcess;
import fr.pasteque.client.utils.PastequePowaPos;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.utils.Error;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import fr.pasteque.client.widgets.ProgressPopup;
import fr.pasteque.client.widgets.UserBtnItem;
import fr.pasteque.client.widgets.UsersBtnAdapter;

public class Start extends TrackedActivity implements Handler.Callback {

    private static final String LOG_TAG = "Pasteque/Start";

    private GridView logins;
    private TextView status;
    private View button;
    private ProgressPopup syncPopup;

    private boolean syncErr;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.enableCrashHandler(this.getApplicationContext());
        setContentView(R.layout.connect);
        if (!Data.loadAll(this)) {
            Error.showError(R.string.err_load_error, this);
        }
        Data.Session.newSessionIfEmpty();
        this.button = this.findViewById(R.id.connectButton);
        this.button.setOnClickListener(new ConnectClickListener());
        this.logins = (GridView) this.findViewById(R.id.loginGrid);
        this.logins.setOnItemClickListener(new UserClickListener());
        this.status = (TextView) findViewById(R.id.status);
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
        startPowa();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UpdateProcess.unbind();
        SendProcess.unbind();
        stopPowa();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.invalidateOptionsMenu();
        this.updateStatus();
    }

    @Override
    public void onBackPressed() {
        if (Configure.isAccount(this)) {
            setResult(LoginActivity.LEAVE);
        } else {
            this.disconnect();
            return;
        }
        super.onBackPressed();
    }

    private boolean noLoadedData() {
        return !Data.dataLoaded(this);
    }

    private boolean hasLocalData() {
        return Data.hasLocalData(this);
    }

    private void startPowa() {
        Context context = getApplicationContext();
        PastequePowaPos.getSingleton().create(context, null, null);
        PowaTSeries pos = new PowaTSeries(context, false);
        PastequePowaPos.getSingleton().addPeripheral(pos);

        PowaScanner scanner = new PowaS10Scanner(context);
        PastequePowaPos.getSingleton().addPeripheral(scanner);

        // Get and bind scanner
        List<PowaDeviceObject> scanners = PastequePowaPos.getSingleton().getAvailableScanners();
        if (scanners.size() > 0) {
            PastequePowaPos.getSingleton().selectScanner(scanners.get(0));
        } else {
            Log.w(LOG_TAG, "Scanner not found");
        }
    }

    private void stopPowa() {
        PastequePowaPos.getSingleton().dispose();
    }

    /**
     * Update status line
     */
    private String getStatusText() {
        String result = "";
        String bullet = "\u2022 ";
        String separator = System.getProperty("line.separator");
        if (Configure.isDemo(this))
            result += bullet + getString(R.string.status_demo) + separator;
        if (Data.hasCashOpened(this)) {
            result += bullet + this.getText(R.string.status_has_local_data) + separator;
        }
        int count = CashArchive.getArchiveCount(this);
        if (count > 0) {
            result += bullet + this.getResources().getQuantityString(R.plurals.status_has_archive,
                    count, count) + separator;
        }
        if (result.isEmpty()) {
            result = separator + getString(R.string.greatings) + " " + Configure.getUser(this) + "!";
        }
        return result;
    }

    private void updateStatus() {
        if (this.noLoadedData() || this.checkAccount()) {
            this.displayFirstConnect(true);
        } else {
            this.displayFirstConnect(false);
        }
        this.status.setText(getStatusText());
        if (this.status.getText().length() == 0) {
            this.status.setVisibility(View.GONE);
        } else {
            this.status.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkAccount() {
        return !new Login(Configure.getUser(this),
                Configure.getPassword(this),
                Configure.getMachineName(this)).equals(Data.Login.getLogin(this));
    }

    private void displayFirstConnect(boolean shouldBeFirstConnect) {
        if (shouldBeFirstConnect) {
            this.button.setVisibility(View.VISIBLE);
            this.logins.setVisibility(View.INVISIBLE);
        } else {
            this.button.setVisibility(View.INVISIBLE);
            this.logins.setVisibility(View.VISIBLE);
        }
    }

    private void invalidateAccount() {
        Version.invalidate();
        Configure.invalidateAccount(this);
    }

    private void disconnect() {
        if (Configure.isDemo(this)) {
            this.removeLocalData();
        }
        if (Data.hasCashOpened(this)) {
            Toast.makeText(this, getString(R.string.err_cash_opened), Toast.LENGTH_LONG).show();
        } else if (this.hasLocalData()) {
            Toast.makeText(this, getString(R.string.err_local_data), Toast.LENGTH_LONG).show();
        } else {
            this.invalidateAccount();
            setResult(LoginActivity.PROCEED);
            this.finish();
        }
    }

    /**
     * Remove localData of the application.
     * Must never be used if the account is not a Demonstration Account
     */
    private void removeLocalData() {
        //Double check if is demo
        if (Configure.isDemo(this)) {
            Data.Receipt.clear(this);
            CloseCash.closeCashNoMatterWhat(this);
            CashArchive.clear(this);
        }
    }

    private void onLoginError(int stringId) {
        Intent intent = new Intent(this, Configure.class);
        intent.putExtra(Configure.ERROR, getString(stringId));
        startActivity(intent);
    }

    private void startUpdateProcess() {
        Log.i(LOG_TAG, "Starting update");
        this.syncPopup = new ProgressPopup(this);
        UpdateProcess.start(this);
        UpdateProcess.bind(this.syncPopup, this, new Handler(this));
    }

    /**
     * Update users button grid
     */
    private void refreshUsers() {
        UsersBtnAdapter adapt = new UsersBtnAdapter(Data.User.users(this));
        this.logins.setAdapter(adapt);
    }

    private class UserClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            UserBtnItem item = (UserBtnItem) v;
            User user = item.getUser();
            if (user.hasPassword()) {
                Intent pass = new Intent(Start.this, Password.class);
                pass.putExtra("User", user);
                startActivityForResult(pass, Password.CODE_PASSWORD);
            } else {
                Start.this.enterApp(user);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
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
                        Intent i = new Intent(Start.this, Flavor.Transaction);
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

    /**
     * Open app once user is picked
     */
    private void enterApp(User user) {
        Data.Session.currentSession(Start.this).setUser(user);
        Cash c = Data.Cash.currentCash(Start.this);
        if (c != null && !c.isOpened()) {
            // Cash is not opened
            Intent i = new Intent(Start.this, OpenCash.class);
            Start.this.startActivityForResult(i, 0);
            Start.this.overridePendingTransition(R.transition.fade_in,
                    R.transition.fade_out);
        } else if (c != null && !c.isClosed()) {
            // Cash is opened
            Start.this.goOn();
        } else if (c != null) {
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

    /**
     * Open ticket edition
     */
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
                if (Data.Session.currentSession(this).hasWaitingTickets()) {
                    // Go directly to first ticket
                    i = new Intent(Start.this, Flavor.Transaction);
                    Start.this.startActivity(i);
                    Start.this.overridePendingTransition(R.transition.fade_in,
                            R.transition.fade_out);
                    break;
                }
                // else same thing as simple mode
            case Configure.SIMPLE_MODE:
                // Create a ticket if not existing and go to edit
                Session currSession = Data.Session.currentSession(this);
                if (currSession.getCurrentTicket() == null) {
                    currSession.newTicket();
                }
                i = new Intent(Start.this, Flavor.Transaction);
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
    private static final int MENU_DISCONNECT_ID = 4;

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

        MenuItem disconnect = menu.add(Menu.NONE, MENU_DISCONNECT_ID, 3,
                this.getString(R.string.menu_disconnect));
        disconnect.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem about = menu.add(Menu.NONE, MENU_ABOUT_ID, 4,
                this.getString(R.string.menu_about));
        about.setIcon(R.drawable.ico_help);
        about.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(true);
        menu.getItem(1).setEnabled(CashArchive.hasArchives(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SYNC_UPD_ID:
                this.startUpdateProcess();
                break;
            case MENU_SYNC_SND_ID:
                Log.i(LOG_TAG, "Starting sending data");
                this.syncErr = false;
                this.syncPopup = new ProgressPopup(this);
                this.syncPopup.setButton(DialogInterface.BUTTON_NEUTRAL,
                        this.getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
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
            case MENU_DISCONNECT_ID:
                this.disconnect();
                break;
            case MENU_CONFIG_ID:
                Intent i = new Intent(this, Configure.class);
                this.startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Handle for synchronization progress
     */
    @Override
    public boolean handleMessage(Message m) {
        switch (m.what) {
            case SyncUpdate.SYNC_DONE:
                this.updateStatus();
                this.refreshUsers();
                break;
            case SyncUpdate.CASHREG_SYNC_NOTFOUND:
                this.onLoginError(R.string.err_cashreg_not_found);
                break;
            case SyncUpdate.SYNC_ERROR_NOT_LOGGED:
                this.onLoginError(R.string.err_not_logged);
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
                        Log.e(LOG_TAG, "Unknown server error: " + error);
                        Error.showError(R.string.err_server_error, this);
                    }
                }
                break;

            case SyncSend.SYNC_DONE:
                Log.i(LOG_TAG, "Sending data finished.");
                this.updateStatus();
                this.invalidateOptionsMenu();
                if (Data.Customer.resolvedIds.size() > 0) {
                    // Clearing temp id on sync success
                    Data.Customer.resolvedIds.clear();
                    try {
                        Data.Customer.save(this);
                        Log.i(LOG_TAG, "Sync Done: Local ids are cleared");
                    } catch (IOError e) {
                        e.printStackTrace();
                        Log.i(LOG_TAG, "Sync Done: Could not save cleared customer data", e);
                    }
                }
                break;
        }
        return true;
    }

    private class ConnectClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Start.this.startUpdateProcess();
        }
    }
}
