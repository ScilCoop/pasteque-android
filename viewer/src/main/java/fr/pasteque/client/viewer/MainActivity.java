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
import fr.pasteque.api.parser.Parser;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.models.TicketLineModel;
import fr.pasteque.api.models.TicketModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 17:01.
 */
public class MainActivity extends FragmentActivity {

    private static final int MENU_CONFIG_ID = 1;
    private List<String> content = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem config = menu.add(Menu.NONE, MENU_CONFIG_ID, 1,
                this.getString(R.string.menu_config));
        config.setIcon(R.drawable.ico_reglage);
        config.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_CONFIG_ID) {
            Intent i = new Intent(this, Configure.class);
            this.startActivity(i);
            return true;
        }
        return false;
    }

    public void download(View view) {
        final API api = new API(Pasteque.getConfiguration());
        api.Tickets.getAllSharedTicket(new API.Handler<List<TicketModel>>() {
            @Override
            public void result(final List<TicketModel> object) {
                for (TicketModel ticketModel : object) {
                    for (TicketLineModel ticketLineModel : ticketModel.lines) {
                        final ProductModel productModel = ticketLineModel.product;
                        api.Products.getProduct(productModel.id, new API.Handler<ProductModel>() {
                            @Override
                            public void result(ProductModel data) {
                                productModel.copy(data);
                                api.Images.getProduct(productModel.id, new API.Handler<byte[]>() {
                                            @Override
                                            public void result(final byte[] image) {
                                                final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                                                productModel.image = image;
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addProduct(productModel, bitmap);
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

    private void addProduct(ProductModel result, Bitmap bitmap) {
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        ViewGroup productHolder = (ViewGroup) getLayoutInflater().inflate(R.layout.product_holder, null);
        ((ImageView) productHolder.findViewById(R.id.img)).setImageBitmap(bitmap);
        ((TextView) productHolder.findViewById(R.id.id)).setText(result.label);
        container.addView(productHolder);
    }
}