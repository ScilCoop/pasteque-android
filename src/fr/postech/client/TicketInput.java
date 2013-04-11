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
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.data.CashData;
import fr.postech.client.data.CustomerData;
import fr.postech.client.data.SessionData;
import fr.postech.client.models.Catalog;
import fr.postech.client.models.Category;
import fr.postech.client.models.Product;
import fr.postech.client.models.Ticket;
import fr.postech.client.models.TicketLine;
import fr.postech.client.models.Session;
import fr.postech.client.models.User;
import fr.postech.client.widgets.CategoriesAdapter;
import fr.postech.client.widgets.ProductBtnItem;
import fr.postech.client.widgets.ProductsBtnAdapter;
import fr.postech.client.widgets.TicketLineItem;
import fr.postech.client.widgets.TicketLinesAdapter;

public class TicketInput extends Activity
    implements TicketLineEditListener, AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = "POS-Tech/TicketInput";

    private Catalog catalog;
    private Ticket ticket;
    private Category currentCategory;

    private TextView ticketLabel;
    private TextView ticketCustomer;
    private TextView ticketArticles;
    private TextView ticketTotal;
    private SlidingDrawer slidingDrawer;
    private ImageView slidingHandle;
    private ListView ticketContent;
    private Gallery categories;
    private GridView products;

    private static Catalog catalogInit;
    private static Ticket ticketInit;
    private static Ticket ticketSwitch;
    public static void setup(Catalog catalog, Ticket ticket) {
        catalogInit = catalog;
        ticketInit = ticket;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Init data
        boolean open = false;
        if (state != null) {
            // From state
            this.catalog = (Catalog) state.getSerializable("catalog");
            this.ticket = (Ticket) state.getSerializable("ticket");
            this.currentCategory = this.catalog.getRootCategories().get(0);
            open = state.getBoolean("drawerOpen");
        } else {
            // From scratch
            this.catalog = catalogInit;
            catalogInit = null;
            this.currentCategory = this.catalog.getRootCategories().get(0);
            if (ticketInit == null) {
                this.ticket = new Ticket();
            } else {
                this.ticket = ticketInit;
                ticketInit = null;
            }
            open = this.ticket.getArticlesCount() > 0;
        }
        // Set views
        setContentView(R.layout.products);
        this.ticketLabel = (TextView) this.findViewById(R.id.ticket_label);
        this.ticketCustomer = (TextView) this.findViewById(R.id.ticket_customer);
        this.ticketArticles = (TextView) this.findViewById(R.id.ticket_articles);
        this.ticketTotal = (TextView) this.findViewById(R.id.ticket_total);

        this.categories = (Gallery) this.findViewById(R.id.categoriesGrid);
        this.products = (GridView) this.findViewById(R.id.productsGrid);
        CategoriesAdapter catAdapt = new CategoriesAdapter(this.catalog.getRootCategories());
        this.categories.setAdapter(catAdapt);
        this.categories.setOnItemSelectedListener(this);
        this.categories.setSelection(0, false);
        ProductClickListener prdListnr = new ProductClickListener();
        this.products.setOnItemClickListener(prdListnr);
        this.products.setOnItemLongClickListener(prdListnr);

        this.slidingHandle = (ImageView) this.findViewById(R.id.handle);

        this.slidingDrawer = (SlidingDrawer) this.findViewById(R.id.drawer);
        this.slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                slidingHandle.setImageResource(R.drawable.slider_close);
            }
        });
        slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                slidingHandle.setImageResource(R.drawable.slider_open);
            }
        });
        if (open) {
            this.slidingDrawer.open();
        }

        this.ticketContent = (ListView) this.findViewById(R.id.ticket_content);
        this.ticketContent.setAdapter(new TicketLinesAdapter(this.ticket,
                                                             this));

        this.updateProducts();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ticketSwitch != null) {
            this.switchTicket(ticketSwitch);
            ticketSwitch = null;
            this.slidingDrawer.close();
        } else {
            this.updateTicketView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("catalog", this.catalog);
        outState.putSerializable("ticket", this.ticket);
        outState.putBoolean("drawerOpen", this.slidingDrawer.isOpened());
    }

    private void switchTicket(Ticket t) {
        this.ticket = t;
        this.ticketContent.setAdapter(new TicketLinesAdapter(this.ticket,
                                                             this));
        this.updateTicketView();
    }

    /** Request a switch to an other ticket. It will be effective
     * next time the activity is displayed
     */
    public static void requestTicketSwitch(Ticket t) {
        ticketSwitch = t;
    }

    private void updateTicketView() {
        String count = this.getString(R.string.ticket_articles,
                                      this.ticket.getArticlesCount());
        String total = this.getString(R.string.ticket_total,
                                      this.ticket.getTotalPrice());
        String label = this.getString(R.string.ticket_label,
                                      this.ticket.getLabel());
        if (this.ticket.getCustomer() != null) {
            String name = this.ticket.getCustomer().getName();
            this.ticketCustomer.setText(name);
            this.ticketCustomer.setVisibility(View.VISIBLE);
        } else {
            this.ticketCustomer.setVisibility(View.GONE);
        }
        this.ticketLabel.setText(label);
        this.ticketArticles.setText(count);
        this.ticketTotal.setText(total);
        ((TicketLinesAdapter)TicketInput.this.ticketContent.getAdapter()).notifyDataSetChanged();
    }

    private void updateProducts() {
        ProductsBtnAdapter prdAdapt = new ProductsBtnAdapter(this.catalog.getProducts(this.currentCategory));
        this.products.setAdapter(prdAdapt);
    }

    private class ProductClickListener
        implements OnItemClickListener, OnItemLongClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            ProductBtnItem item = (ProductBtnItem) v;
            Product p = item.getProduct();
            TicketInput.this.ticket.addProduct(p);
            TicketInput.this.updateTicketView();
        }
        
        public boolean onItemLongClick(AdapterView<?> parent, View v,
                                    int position, long id) {
            ProductBtnItem item = (ProductBtnItem) v;
            Product p = item.getProduct();
            AlertDialog.Builder b = new AlertDialog.Builder(TicketInput.this);
            b.setTitle(p.getLabel());
            String message = TicketInput.this.getString(R.string.prd_info_price,
                                                        p.getTaxedPrice());
            b.setMessage(message);
            b.setNeutralButton(android.R.string.ok, null);
            b.show();
            return true;
        }
    }

    public void payTicket(View v) {
        ProceedPayment.setup(this.ticket);
        Intent i = new Intent(this, ProceedPayment.class);
        this.startActivity(i);
    }

    public void addQty(TicketLine l) {
        this.ticket.adjustQuantity(l, 1);
        this.updateTicketView();
    }

    public void remQty(TicketLine l) {
        this.ticket.adjustQuantity(l, -1);
        this.updateTicketView();
    }

    public void delete(TicketLine l) {
        this.ticket.removeLine(l);
        this.updateTicketView();
    }

    /** Category selected */
    public void onItemSelected(AdapterView<?> parent, View v,
                               int position, long id) {
        CategoriesAdapter adapt = (CategoriesAdapter)
            this.categories.getAdapter();
        Category cat = (Category) adapt.getItem(position);
        this.currentCategory = cat;
        this.updateProducts();
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    protected void onActivityResult (int requestCode, int resultCode,
                                     Intent data) {
        switch (requestCode) {
        case TicketSelect.CODE_TICKET:
            switch (resultCode) {
            case Activity.RESULT_CANCELED:
                break;
            case Activity.RESULT_OK:
                this.switchTicket(SessionData.currentSession.getCurrentTicket());
                break;
            }
            break;
        case CustomerSelect.CODE_CUSTOMER:
            switch (resultCode) {
            case Activity.RESULT_CANCELED:
                break;
            case Activity.RESULT_OK:
                updateTicketView();
                try {
                    SessionData.saveSession(this);
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "Unable to save session", ioe);
                    Error.showError(R.string.err_save_session, this);
                }
                break;
            }
            break;
        }
    }


    private static final int MENU_CLOSE_CASH = 0;
    private static final int MENU_SWITCH_TICKET = 1;
    private static final int MENU_NEW_TICKET = 2;
    private static final int MENU_CUSTOMER = 3;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int i = 0;
        User cashier = SessionData.currentSession.getUser();
        if (cashier.hasPermission("com.openbravo.pos.panels.JPanelCloseMoney")) {
            MenuItem close = menu.add(Menu.NONE, MENU_CLOSE_CASH, i++,
                                      this.getString(R.string.menu_main_close));
            close.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        }
        if (Configure.getTicketsMode(this) == Configure.STANDARD_MODE) {
            MenuItem newTicket = menu.add(Menu.NONE, MENU_NEW_TICKET, i++,
                    this.getString(R.string.menu_new_ticket));
            newTicket.setIcon(android.R.drawable.ic_menu_add);
        }
        if (CustomerData.customers.size() > 0) {
            MenuItem customer = menu.add(Menu.NONE, MENU_CUSTOMER, i++,
                    this.getString(R.string.menu_assign_customer));
        }
        return i > 0;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	if (Configure.getTicketsMode(this) == Configure.STANDARD_MODE) {
	    MenuItem switchTkt = menu.findItem(MENU_SWITCH_TICKET);
	    if (SessionData.currentSession.hasWaitingTickets()) {
		if (switchTkt == null) {
		    switchTkt = menu.add(Menu.NONE, MENU_SWITCH_TICKET, 10,
					this.getString(R.string.menu_switch_ticket));
		    switchTkt.setIcon(android.R.drawable.ic_menu_rotate);
		}
	    } else {
		if (switchTkt != null) {
		    menu.removeItem(MENU_SWITCH_TICKET);
		}
	    }
	}
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_CLOSE_CASH:
            OpenCash.close(this);
            break;
        case MENU_NEW_TICKET:
            SessionData.currentSession.newTicket();
            try {
                SessionData.saveSession(this);
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Unable to save session", ioe);
                Error.showError(R.string.err_save_session, this);
            }
            this.switchTicket(SessionData.currentSession.getCurrentTicket());
            break;
        case MENU_SWITCH_TICKET:
            Intent i = new Intent(this, TicketSelect.class);
            this.startActivityForResult(i, TicketSelect.CODE_TICKET);
            break;
        case MENU_CUSTOMER:
            i = new Intent(this, CustomerSelect.class);
            CustomerSelect.setup(this.ticket.getCustomer() != null);
            this.startActivityForResult(i, CustomerSelect.CODE_CUSTOMER);
            break;
        }
        return true;
    }

}
