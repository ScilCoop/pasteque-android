package fr.pasteque.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import fr.pasteque.client.activities.TrackedActivity;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.fragments.RestaurantTicketSelectFragment;
import fr.pasteque.client.models.Place;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.User;
import fr.pasteque.client.sync.TicketUpdater;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 11:50.
 */
public class RestaurantTicketSelect extends TrackedActivity {

    private static final int MENU_CLOSE_CASH = 0;
    private static final int MENU_SYNC_TICKET = 1;
    private RestaurantTicketSelectFragment restaurantTicketSelectFragment;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            restaurantTicketSelectFragment = new RestaurantTicketSelectFragment();
            fragmentTransaction.add(android.R.id.content, restaurantTicketSelectFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestAllTickets();
    }

    private boolean isAutoSyncMode() {
        return (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int i = 0;
        User cashier = Data.Session.currentSession(this).getUser();
        if (cashier.hasPermission("fr.pasteque.pos.panels.JPanelCloseMoney")) {
            MenuItem close = menu.add(Menu.NONE, MENU_CLOSE_CASH, i++,
                    this.getString(R.string.menu_main_close));
            close.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
            case MENU_SYNC_TICKET:
                updateAllTickets();
                refreshList();
                break;
        }
        return true;
    }

    /**
     * End activity correctly according to ticket mode. Call once current
     * ticket is set in session
     */
    private void selectTicket(Ticket t) {
        Data.Session.currentSession(this).setCurrentTicket(t);
        this.setResult(Activity.RESULT_OK);
        Intent i = new Intent(this, Flavor.Transaction);
        this.startActivity(i);
    }


    public void accessPlace(Place place) {
        Ticket ticket = place.getAssignedTicket();
        if (ticket == null) {
            ticket = Data.Session.currentSession().newTicket(place);
            selectTicket(ticket);
        } else {
            requestTicket(ticket);
        }
    }

    /**
     * Smart ticket updater
     * Update only if the application is configured to update
     */
    private void requestTicket(Ticket t) {
        if (isAutoSyncMode()) {
            updateAndSelectTicket(t);
        } else {
            selectTicket(t);
        }
    }

    /**
     * Smart tickets updater
     * Update only if the application is configured to update
     */
    private void requestAllTickets() {
        if (isAutoSyncMode()) {
            updateAllTickets();
        } else {
            this.refreshList();
        }
    }

    /**
     * Update the tickets
     * And refresh the view
     */
    private void updateAllTickets() {
        new TicketUpdater().execute(
                getApplicationContext(),
                new DataHandler(Configure.getTicketsMode(this), null),
                TicketUpdater.TICKETSERVICE_UPDATE
                        | TicketUpdater.TICKETSERVICE_ALL);
    }

    /**
     * Update the ticket
     * And do the selectTicket(ticket) thing on response
     *
     * @param ticket to update
     */
    private void updateAndSelectTicket(Ticket ticket) {
        new TicketUpdater().execute(this,
                new DataHandler(Configure.getTicketsMode(this), ticket),
                TicketUpdater.TICKETSERVICE_UPDATE
                        | TicketUpdater.TICKETSERVICE_ONE, ticket.getId());
    }

    private void refreshList() {
        restaurantTicketSelectFragment.refreshView();
    }

    //Handle the request response
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
                    refreshList();
                    break;
                case TicketUpdater.TICKETSERVICE_UPDATE
                        | TicketUpdater.TICKETSERVICE_ONE:
                    Ticket t = (Ticket) msg.obj;
                    if (t != null) {
                        selectTicket(t);
                    } else {
                        // Nothing found from server, use local one
                        // TODO: make a difference from new ticket and deleted one
                        selectTicket(this.requestedTkt);
                    }
            }
        }
    }
}
