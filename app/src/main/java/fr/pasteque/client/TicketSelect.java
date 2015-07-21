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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.io.IOException;

import fr.pasteque.client.data.PlaceData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.models.Place;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.User;
import fr.pasteque.client.sync.TicketUpdater;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.ProgressPopup;
import fr.pasteque.client.widgets.RestaurantTicketsAdapter;
import fr.pasteque.client.widgets.SessionTicketsAdapter;

public class TicketSelect extends TrackedActivity implements
        ExpandableListView.OnChildClickListener,
        AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "Pasteque/TicketSelect";
    public static final int CODE_TICKET = 2;

    private ListView list;
    private ProgressPopup syncPopup;
    // TODO: quick and dirty way to block multi-click, should use syncPopup
    private boolean loading;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        switch (Configure.getTicketsMode(this)) {
        case Configure.STANDARD_MODE:
            setContentView(R.layout.ticket_select_standard);
            this.list = (ListView) this.findViewById(R.id.tickets_list);
            this.list.setAdapter(new SessionTicketsAdapter(this));
            this.list.setOnItemClickListener(this);
            break;
        case Configure.RESTAURANT_MODE:
            setContentView(R.layout.ticket_select_restaurant);
            this.list = (ListView) this.findViewById(R.id.tickets_list);
            ((ExpandableListView) this.list)
                    .setAdapter(new RestaurantTicketsAdapter(PlaceData.floors));
            ((ExpandableListView) this.list).setOnChildClickListener(this);
            if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
                TicketUpdater.getInstance().execute(this,
                        new DataHandler(Configure.getTicketsMode(this), null),
                        TicketUpdater.TICKETSERVICE_UPDATE
                        | TicketUpdater.TICKETSERVICE_ALL);
            }
            break;
        }

    }

    @Override
	public void onResume() {
        super.onResume();
        // Refresh data
        if (Configure.getTicketsMode(this) == Configure.RESTAURANT_MODE) {
            ExpandableListView exlist = (ExpandableListView) this.list;
            RestaurantTicketsAdapter adapt = (RestaurantTicketsAdapter) exlist
                    .getExpandableListAdapter();
            boolean[] expanded = new boolean[adapt.getGroupCount()];
            for (int i = 0; i < adapt.getGroupCount(); i++) {
                expanded[i] = exlist.isGroupExpanded(i);
            }
            exlist.setAdapter(new RestaurantTicketsAdapter(PlaceData.floors));
            for (int i = 0; i < adapt.getGroupCount(); i++) {
                if (expanded[i]) {
                    exlist.expandGroup(i);
                } else {
                    exlist.collapseGroup(i);
                }
            }
        }
    }

    public void refreshList() {
        switch (Configure.getTicketsMode(this)) {
        case Configure.STANDARD_MODE:
            this.list = (ListView) this.findViewById(R.id.tickets_list);
            this.list.setAdapter(new SessionTicketsAdapter(this));
            this.list.setOnItemClickListener(this);
            break;
        case Configure.RESTAURANT_MODE:
            this.list = (ListView) this.findViewById(R.id.tickets_list);
            ((ExpandableListView) this.list)
                    .setAdapter(new RestaurantTicketsAdapter(PlaceData.floors));
            ((ExpandableListView) this.list).setOnChildClickListener(this);
            break;
        }
    }

    /** End activity correctly according to ticket mode. Call once current
     * ticket is set in session
     */
    private void selectTicket(Ticket t) {
        this.loading = false;
        switch (Configure.getTicketsMode(this)) {
        case Configure.STANDARD_MODE:
            SessionData.currentSession(this).setCurrentTicket(t);
            this.setResult(Activity.RESULT_OK);
            // Kill
            this.finish();
            break;
        case Configure.RESTAURANT_MODE:
            SessionData.currentSession(this).setCurrentTicket(t);
            Intent i = new Intent(this, Transaction.class);
            this.startActivity(i);
            break;
        }
    }

    @Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (this.loading) {
            return;
        }
        Ticket t = SessionData.currentSession(this).getTickets().get(position);
        if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(this,
                    new DataHandler(Configure.getTicketsMode(this), t),
                    TicketUpdater.TICKETSERVICE_UPDATE
                    | TicketUpdater.TICKETSERVICE_ONE, t.getId());
            this.loading = true;
        } else {
            this.selectTicket(t);
        }
    }

    @Override
	public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        if (this.loading) {
            return true;
        }
        ExpandableListView exlist = (ExpandableListView) this.list;
        ExpandableListAdapter adapt = exlist.getExpandableListAdapter();
        Place p = (Place) adapt.getChild(groupPosition, childPosition);
        Session currSession = SessionData.currentSession(this);
        // Check if a ticket is already there
        for (Ticket t : currSession.getTickets()) {
            if (t.getId().equals(p.getId())) {
                // It's there, get it now!
                if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
                    TicketUpdater.getInstance().execute(this,
                            new DataHandler(Configure.getTicketsMode(this), t),
                            TicketUpdater.TICKETSERVICE_UPDATE
                            | TicketUpdater.TICKETSERVICE_ONE, t.getId());
                    this.loading = true;
                } else {
                    this.selectTicket(t);
                }
                return true;
            }
        }
        // No ticket for this table
        Ticket t = currSession.newTicket(p);
        if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(this,
                    new DataHandler(Configure.getTicketsMode(this), t),
                    TicketUpdater.TICKETSERVICE_UPDATE
                    | TicketUpdater.TICKETSERVICE_ONE, t.getId());
            this.loading = true;
        } else {
            this.selectTicket(t);
        }
        return true;
    }

    private static final int MENU_CLOSE_CASH = 0;
    private static final int MENU_NEW_TICKET = 1;
    private static final int MENU_SYNC_TICKET = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int i = 0;
        User cashier = SessionData.currentSession(this).getUser();
        if (cashier.hasPermission("fr.pasteque.pos.panels.JPanelCloseMoney")) {
            MenuItem close = menu.add(Menu.NONE, MENU_CLOSE_CASH, i++,
                    this.getString(R.string.menu_main_close));
            close.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        if (Configure.getTicketsMode(this) == Configure.STANDARD_MODE) {
            MenuItem newTicket = menu.add(Menu.NONE, MENU_NEW_TICKET, i++,
                    this.getString(R.string.menu_new_ticket));
            newTicket.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
            MenuItem syncTicket = menu.add(Menu.NONE, MENU_SYNC_TICKET, i++,
                    this.getString(R.string.menu_sync_ticket));
            syncTicket.setIcon(R.drawable.ic_menu_update);
            syncTicket.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        return i > 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_CLOSE_CASH:
            CloseCash.close(this);
            break;
        case MENU_NEW_TICKET:
            SessionData.currentSession(this).newTicket();
            try {
                SessionData.saveSession(this);
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Unable to save session", ioe);
                Error.showError(R.string.err_save_session, this);
            }
            this.setResult(Activity.RESULT_OK);
            this.finish();
            break;
        case MENU_SYNC_TICKET:
            TicketUpdater.getInstance().execute(
                    getApplicationContext(),
                    new DataHandler(Configure.getTicketsMode(this), null),
                    TicketUpdater.TICKETSERVICE_UPDATE
                            | TicketUpdater.TICKETSERVICE_ALL);
            refreshList();
            break;
        }
        return true;
    }

    private class DataHandler extends Handler {

        private int ticketMode;
        private Ticket requestedTkt;

        public DataHandler(int ticketMode, Ticket requestedTkt) {
            super();
            this.ticketMode = ticketMode;
            this.requestedTkt = requestedTkt;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TicketUpdater.TICKETSERVICE_UPDATE
                    | TicketUpdater.TICKETSERVICE_ALL:
                TicketSelect.this.refreshList();
                break;
            case TicketUpdater.TICKETSERVICE_UPDATE
                    | TicketUpdater.TICKETSERVICE_ONE:
                Ticket t = (Ticket) msg.obj;
                if (t != null) {
                    TicketSelect.this.selectTicket(t);
                } else {
                    // Nothing found from server, use local one
                    // TODO: make a difference from new ticket and deleted one
                    TicketSelect.this.selectTicket(this.requestedTkt);
                }
            }
        }
    }
}
