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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.data.ReceiptData;
import fr.postech.client.data.ProductData;
import fr.postech.client.data.SessionData;
import fr.postech.client.data.UserData;
import fr.postech.client.models.User;
import fr.postech.client.models.Product;
import fr.postech.client.models.Session;
import fr.postech.client.models.Ticket;
import fr.postech.client.widgets.UserBtnItem;
import fr.postech.client.widgets.UsersBtnAdapter;

public class Start extends Activity implements Handler.Callback {

    private GridView logins;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Load session
        try {
            Session s = SessionData.loadSession(this);
            Session.currentSession = s;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Load receipts
        try {
            ReceiptData.load(this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Load products
        try {
            ProductData.load(this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Load users
        try {
            UserData.load(this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        setContentView(R.layout.connect);
        // Load users
        UsersBtnAdapter adapt = new UsersBtnAdapter(UserData.users);
        this.logins = (GridView) this.findViewById(R.id.loginGrid);
        this.logins.setOnItemClickListener(new UserClickListener());
        this.refreshUsers();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            SessionData.saveSession(Session.currentSession, this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void refreshUsers() {
        UsersBtnAdapter adapt = new UsersBtnAdapter(UserData.users);
        this.logins.setAdapter(adapt);
    }

    private class UserClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            UserBtnItem item = (UserBtnItem) v;
            User user = item.getUser();
            Session.currentSession.setUser(user);
            if (Session.currentSession.getCurrentTicket() == null) {
                // Create a ticket if there isn't anyone
                Ticket t = Session.currentSession.newTicket();
            }
            TicketInput.setup(ProductData.products,
                              Session.currentSession.getCurrentTicket());
            Intent i = new Intent(Start.this, TicketInput.class);
            Start.this.startActivity(i);
        }
    }

    private static final int MENU_SYNC_ID = 0;
    private static final int MENU_CONFIG_ID = 1;
    private static final int MENU_ABOUT_ID = 2;
    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        MenuItem sync = menu.add( Menu.NONE, MENU_SYNC_ID, 0,
                                  this.getString( R.string.menu_sync ) );
        sync.setIcon( android.R.drawable.ic_menu_rotate );
        MenuItem about = menu.add( Menu.NONE, MENU_ABOUT_ID, 2,
                                   this.getString( R.string.menu_about ) );
        about.setIcon( android.R.drawable.ic_menu_info_details );
        MenuItem config = menu.add( Menu.NONE, MENU_CONFIG_ID, 1,
                                   this.getString( R.string.menu_config ) );
        config.setIcon( android.R.drawable.ic_menu_preferences );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected ( MenuItem item ) {
        switch (item.getItemId()) {
        case MENU_SYNC_ID:
            // Sync
            Sync sync = new Sync(this, new Handler(this));
            sync.startSync();
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
        case Sync.CATALOG_SYNC_DONE:
            List<Product> products = (List) m.obj;
            ProductData.products = products;
            try {
                ProductData.save(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        case Sync.USERS_SYNC_DONE:
            List<User> users = (List) m.obj;
            UserData.users = users;
            try {
                UserData.save(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.refreshUsers();
            break;
        case Sync.SYNC_DONE:
            // Synchronization finished
        }
        return true;
    }
}
