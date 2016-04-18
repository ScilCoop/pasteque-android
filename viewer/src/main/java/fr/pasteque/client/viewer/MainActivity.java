package fr.pasteque.client.viewer;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.pasteque.api.API;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.models.TicketModel;
import fr.pasteque.client.viewer.fragment.TicketLineFragment;
import fr.pasteque.client.viewer.models.Product;
import fr.pasteque.client.viewer.models.Ticket;
import fr.pasteque.client.viewer.models.SharedTicketsHolder;
import fr.pasteque.client.viewer.models.TicketLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 17:01.
 */
public class MainActivity extends FragmentActivity {

    private static final int MENU_CONFIG_ID = 1;
    private static final int REFRESH_ID = 2;

    private SharedTicketsHolder sharedTicketsHolder = new SharedTicketsHolder();
    private Loop loop = new Loop(Pasteque.getConf().getDelay(), new Runnable() {
        @Override
        public void run() {
            download();
        }
    });
    private int request;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loop.start();
        download();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem config = menu.add(Menu.NONE, MENU_CONFIG_ID, 1,
                this.getString(R.string.menu_config));
        config.setIcon(R.drawable.ico_reglage);
        config.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem item = menu.add(Menu.NONE, REFRESH_ID, 0, "Refresh");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_CONFIG_ID) {
            Intent i = new Intent(this, Configure.class);
            this.startActivity(i);
            return true;
        } else if (item.getItemId() == REFRESH_ID) {
            download();
        }
        return false;
    }

    public void download() {
        final API api = new API(Pasteque.getConfiguration());
        final SharedTicketsHolder finalSharedTicketsHolder = this.sharedTicketsHolder;
        api.Tickets.getAllSharedTicket(new API.Handler<List<TicketModel>>() {
            @Override
            public void result(final List<TicketModel> object) {
                finalSharedTicketsHolder.updating();
                for (TicketModel ticketModel : object) {
                    final Ticket ticket = new Ticket(ticketModel);
                    finalSharedTicketsHolder.update(ticket);
                    for (TicketLine ticketLineModel : ticket.lines) {
                        final Product product = ticketLineModel.product;
                        request();
                        api.Products.getProduct(product.id, new API.Handler<ProductModel>() {
                            @Override
                            public void result(ProductModel data) {
                                product.copy(data);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        update();
                                    }
                                });
                            }
                        });
                    }
                }
                finalSharedTicketsHolder.updated();
            }
        });
    }

    private synchronized void request() {
        this.request++;
    }

    private synchronized void update() {
        request--;
        if (request == 0) {
            ((TicketLineFragment) getFragmentManager().findFragmentByTag(getResources().getString(R.string.ticket_line_tag))).notifyDataSetInvalidated();
        }
    }

    public SharedTicketsHolder getSharedTicketsHolder() {
        return this.sharedTicketsHolder;
    }
}