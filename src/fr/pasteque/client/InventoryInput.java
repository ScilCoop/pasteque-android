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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.CashData;
import fr.pasteque.client.data.CashRegisterData;
import fr.pasteque.client.data.StockData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.Inventory;
import fr.pasteque.client.models.Inventory.InventoryItem;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.User;
import fr.pasteque.client.sync.TicketUpdater;
import fr.pasteque.client.utils.BarcodeInput;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.CategoriesAdapter;
import fr.pasteque.client.widgets.ProductBtnItem;
import fr.pasteque.client.widgets.ProductsBtnAdapter;
import fr.pasteque.client.widgets.InventoryAdapter;

public class InventoryInput extends TrackedActivity
    implements InventoryLineEditListener, AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = "Pasteque/Inventory";

    private static final int CODE_SCAN = 4;

    private Inventory inventory;
    private int stockType;
    private Catalog catalog;
    private Category currentCategory;
    private BarcodeInput barcodeInput;

    private ListView stockContent;
    private Gallery categories;
    private GridView products;
    private SlidingDrawer slidingDrawer;
    private ImageView slidingHandle;
    private Button ticketAccess;
    private Button productAccess;

    private static Catalog catalogInit;
    public static void setup(Catalog catalog) {
        catalogInit = catalog;
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
            this.currentCategory = this.catalog.getAllCategories().get(0);
            this.stockType = state.getInt("stockType");
            this.inventory = (Inventory) state.getSerializable("inventory");
            open = state.getBoolean("drawerOpen");
        } else {
            // From scratch
            this.catalog = catalogInit;
            catalogInit = null;
            this.currentCategory = this.catalog.getAllCategories().get(0);
            try {
                this.inventory = new Inventory(StockData.getLocationId(this,
                                CashRegisterData.current(this).getLocationId()));
                this.stockType = Inventory.STOCK_AVAILABLE;
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: this is like a fatal error
            }
        }
        this.barcodeInput = new BarcodeInput();
        // Set views
        setContentView(R.layout.inventory);
        this.categories = (Gallery) this.findViewById(R.id.categoriesGrid);
        this.products = (GridView) this.findViewById(R.id.productsGrid);
        CategoriesAdapter catAdapt = new CategoriesAdapter(this.catalog.getAllCategories());
        this.categories.setAdapter(catAdapt);
        this.categories.setOnItemSelectedListener(this);
        this.categories.setSelection(0, false);
        ProductClickListener prdListnr = new ProductClickListener();
        this.products.setOnItemClickListener(prdListnr);

        this.slidingDrawer = (SlidingDrawer) this.findViewById(R.id.drawer);
        this.slidingHandle = (ImageView) this.findViewById(R.id.handle);
        this.ticketAccess = (Button) this.findViewById(R.id.ticket_access);
        if (this.ticketAccess != null) {
            this.ticketAccess.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InventoryInput.this.slidingHandle.performClick();
                    }
                });
        }
        this.productAccess = (Button) this.findViewById(R.id.productAccess);
        if (this.productAccess != null) {
            this.productAccess.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InventoryInput.this.slidingHandle.performClick();
                    }
                });
        }

        if (open && this.slidingDrawer != null) {
            this.slidingDrawer.open();
        }
        // Check presence of barcode scanner
        Intent i = new Intent("com.google.zxing.client.android.SCAN");
        List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(i,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            this.findViewById(R.id.scan_customer).setVisibility(View.GONE);
        }

        this.stockContent = (ListView) this.findViewById(R.id.inventory_content);
        this.updateInventoryView();
        this.updateProducts();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("catalog", this.catalog);
        outState.putSerializable("inventory", this.inventory);
        outState.putInt("stockType", this.stockType);
    }

    private void updateInventoryView() {
        // Update inventory list
        this.stockContent.setAdapter(new InventoryAdapter(this.inventory,
                        this.stockType, this, this));
    }

    private void updateProducts() {
        ProductsBtnAdapter prdAdapt = new ProductsBtnAdapter(this.catalog.getProducts(this.currentCategory));
        this.products.setAdapter(prdAdapt);
    }

    private class ProductClickListener
        implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                int position, long id) {
            ProductBtnItem item = (ProductBtnItem) v;
            final Product p = item.getProduct();
            InventoryInput.this.productPicked(p);
        }
    }

    /** Trigger actions required before adding the product then add it
     * to the ticket
     */
    private void productPicked(final Product p) {
        /* If the product is scaled, then a message pops up and let
         * the user choose the weight
         */
        if(p.isScaled()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            alertDialogBuilder.setView(input);
            alertDialogBuilder.setTitle(p.getLabel());
            alertDialogBuilder
                    .setView(input)
                    .setIcon(R.drawable.scale)
                    .setMessage(R.string.scaled_products_info)
                    .setCancelable(false)
                    .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //On click, add the scaled product to the ticket
                                String getString = input.getText().toString();
                                if (!TextUtils.isEmpty(getString)) {
                                    addScaledProduct(p, getString);
                                }
                            }
                        })
                    .setNegativeButton(R.string.scaled_products_cancel,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //On click, dismiss the dialog
                                dialog.cancel();
                            }
                        });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            this.inventory.addProduct(p, this.stockType);
            this.updateInventoryView();
        }
    }

    private void readBarcode(String code) {
        boolean found = false;
        if (code.startsWith("c")) {
            // Scanned a customer card, does nothing
        } else {
            // Other scan, assumed to be product
            Catalog cat = CatalogData.catalog(this);
            Product p = cat.getProductByBarcode(code);
            if (p != null) {
                this.productPicked(p);
                String text = this.getString(R.string.barcode_found,
                        p.getLabel());
                Toast t = Toast.makeText(this, text,
                        Toast.LENGTH_SHORT);
                t.show();
            } else {
                String text = this.getString(R.string.barcode_not_found,
                        code);
                Toast t = Toast.makeText(this, text,
                        Toast.LENGTH_LONG);
                t.show();
            }
        }

    }

    /** Add scaled product to the ticket
     * @param p the product to add
     * @param input the weight in kg
     */
    private void addScaledProduct(Product p, String input) {
        double scale = Double.valueOf(input);
        this.inventory.addProduct(p, scale, this.stockType);
        this.updateInventoryView();
    }

    /** Validate current stock and enter next one (or save all) */
    public void validate(View v) {
        switch (this.stockType) {
        case Inventory.STOCK_AVAILABLE:
            this.stockType = Inventory.STOCK_LOST;
            this.updateInventoryView();
            break;
        case Inventory.STOCK_LOST:
            // End
            Intent i = new Intent();
            i.putExtra("inventory", this.inventory);
            this.setResult(Activity.RESULT_OK, i);
            this.finish();
        }
    }

    public void scanBarcode(View v) {
        Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intentScan, CODE_SCAN);
    }

    public void addQty(InventoryItem l) {
        this.inventory.addProduct(l, 1, this.stockType);
        this.updateInventoryView();
    }

    public void remQty(InventoryItem l) {
        this.inventory.addProduct(l, -1.0, this.stockType);
        this.updateInventoryView();
    }

    /** Modifies the weight of the product by asking the user a new one
     * @param the ticket's line
     */
    public void mdfyQty(final InventoryItem l) {
        Catalog cat = CatalogData.catalog(this);
        Product p = cat.getProduct(l.getProductId());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setTitle(p.getLabel());
        alertDialogBuilder
            .setView(input)
            .setIcon(R.drawable.scale)
            .setMessage(R.string.scaled_products_info)
            .setCancelable(false)
            .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String recup = input.getText().toString();
                    double scale = Double.valueOf(recup);
                    InventoryInput.this.inventory.setQuantity(l, scale,
                            InventoryInput.this.stockType);
                    InventoryInput.this.updateInventoryView();
                }
              })
            .setNegativeButton(R.string.scaled_products_cancel,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void delete(InventoryItem l) {
        this.inventory.remove(l, this.stockType);
        this.updateInventoryView();
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
        case CODE_SCAN:
            if (resultCode == Activity.RESULT_OK) {
                String code = data.getStringExtra("SCAN_RESULT");
                this.readBarcode(code);
            }
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 160) { // numpad enter
            // Fuck off sa mere
            this.onKeyUp(KeyEvent.KEYCODE_ENTER, event);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** Handle keyboard input for barcode scanning */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.barcodeInput.append(keyCode, event)) {
            if (this.barcodeInput.isTerminated()) {
                String barcode = this.barcodeInput.getInput();
                this.readBarcode(barcode);
            }
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }
}
