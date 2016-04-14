package fr.pasteque.client.viewer;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.pasteque.api.API;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.models.TicketModel;
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

    private List<String> content = new ArrayList<>();
    private SharedTicketsHolder sharedTicketsHolder = new SharedTicketsHolder();
    private Loop loop = new Loop(Pasteque.getConf().getDelay(), new Runnable() {
        @Override
        public void run() {
            download();
        }
    });

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
                for (TicketModel ticketModel : object) {
                    final Ticket ticket = new Ticket(ticketModel);
                    finalSharedTicketsHolder.update(ticket);
                    for (TicketLine ticketLineModel : ticket.lines) {
                        final Product product = ticketLineModel.product;
                        api.Products.getProduct(product.id, new API.Handler<ProductModel>() {
                            @Override
                            public void result(ProductModel data) {
                                product.copy(data);
                                api.Images.getProduct(product.id, new API.Handler<byte[]>() {
                                            @Override
                                            public void result(final byte[] image) {
                                                final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                                                product.image = image;
                                                product.bitmap = bitmap;
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        update();
                                                    }
                                                });
                                            }
                                        }

                                );
                            }
                        });
                    }
                }
            }
        });
    }

    private void update() {
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        container.removeAllViewsInLayout();
        for (Ticket ticket :sharedTicketsHolder) {
            for (TicketLine line : ticket.lines) {
                ViewGroup productHolder = (ViewGroup) getLayoutInflater().inflate(R.layout.product_holder, null);
                ((ImageView) productHolder.findViewById(R.id.img)).setImageBitmap(line.product.bitmap);
                ((TextView) productHolder.findViewById(R.id.id)).setText(line.product.label);
                ((TextView) productHolder.findViewById(R.id.qtt)).setText(line.quantity);
                container.addView(productHolder);
            }
        }
    }
}