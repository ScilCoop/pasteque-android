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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import fr.postech.client.data.CatalogData;
import fr.postech.client.data.PlaceData;
import fr.postech.client.data.SessionData;
import fr.postech.client.models.Place;
import fr.postech.client.models.Ticket;
import fr.postech.client.models.Session;
import fr.postech.client.models.User;
import fr.postech.client.widgets.RestaurantTicketsAdapter;

public class TicketSelect extends Activity
    implements ExpandableListView.OnChildClickListener,
               AdapterView.OnItemClickListener {

    private ListView list;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        switch(Configure.getTicketsMode(this)) {
        case Configure.STANDARD_MODE:
            // TODO: do it
            break;
        case Configure.RESTAURANT_MODE:
            setContentView(R.layout.ticket_select_restaurant);
            this.list = (ListView) this.findViewById(R.id.tickets_list);
            ((ExpandableListView)this.list).setAdapter(new RestaurantTicketsAdapter(PlaceData.floors));
            ((ExpandableListView)this.list).setOnChildClickListener(this);
            break;
        }
        
    }

    public void onResume() {
        super.onResume();
        // Refresh data
        if (Configure.getTicketsMode(this) == Configure.RESTAURANT_MODE) {
            ExpandableListView exlist = (ExpandableListView) this.list;
            RestaurantTicketsAdapter adapt = (RestaurantTicketsAdapter)exlist.getExpandableListAdapter();
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

    private void selectTicket(Ticket t) {

    }

    public void onItemClick(AdapterView<?> parent, View v, int position,
                            long id) {
        
    }

    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        ExpandableListView exlist = (ExpandableListView) this.list;
        ExpandableListAdapter adapt = exlist.getExpandableListAdapter();
        Place p = (Place) adapt.getChild(groupPosition, childPosition);
        Session currSession = SessionData.currentSession;
        // Check if a ticket is already there
        for (Ticket t : currSession.getTickets()) {
            if (t.getLabel().equals(p.getName())) {
                // It's there, get it now!
                TicketInput.requestTicketSwitch(t);
                TicketInput.setup(CatalogData.catalog, t);
                Intent i = new Intent(this, TicketInput.class);
                this.startActivity(i);
                return true;
            }
        }
        // No ticket for this table
        Ticket t = currSession.newTicket(p);
        TicketInput.requestTicketSwitch(t);
        TicketInput.setup(CatalogData.catalog, t);
        Intent i = new Intent(this, TicketInput.class);
        this.startActivity(i);
        return true;
    }

    private static final int MENU_CLOSE_CASH = 0;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int i = 0;
        User cashier = SessionData.currentSession.getUser();
        if (cashier.hasPermission("com.openbravo.pos.panels.JPanelCloseMoney")) {
            MenuItem close = menu.add(Menu.NONE, MENU_CLOSE_CASH, i++,
                                      this.getString(R.string.menu_main_close));
            close.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        }
        return i > 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_CLOSE_CASH:
            OpenCash.close(this);
            break;
        }
        return true;
    }

}
