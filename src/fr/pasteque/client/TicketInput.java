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
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.String;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.CompositionData;
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.data.TariffAreaData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.User;
import fr.pasteque.client.sync.TicketUpdater;
import fr.pasteque.client.utils.BarcodeInput;
import fr.pasteque.client.utils.ScreenUtils;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.CategoriesAdapter;
import fr.pasteque.client.widgets.ProductBtnItem;
import fr.pasteque.client.widgets.ProductsBtnAdapter;
import fr.pasteque.client.widgets.SessionTicketsAdapter;
import fr.pasteque.client.widgets.TicketLinesAdapter;

import com.mpowa.android.powapos.peripherals.*;
import com.mpowa.android.powapos.peripherals.platform.base.*;
import com.mpowa.android.powapos.peripherals.drivers.s10.PowaS10Scanner;
import com.mpowa.android.powapos.peripherals.drivers.tseries.PowaTSeries;
import com.mpowa.android.powapos.common.dataobjects.*;

public class TicketInput extends TrackedActivity
        implements TicketLineEditListener, AdapterView.OnItemSelectedListener,
        GestureDetector.OnGestureListener {

    private static final String LOG_TAG = "Pasteque/TicketInput";
    private static final int CODE_SCAN = 4;
    private static final int CODE_COMPO = 5;
    private static final int CODE_AREA = 6;
    private static final int CODE_INPUT = 7;
    private final Context context = this;

    private Catalog catalog;
    private Ticket ticket;
    private Category currentCategory;
    private BarcodeInput barcodeInput;
    private GestureDetector gestureDetector;
    private PowaPOS powa;
    private Timer powaStatusCheck;

    private TextView ticketLabel;
    private TextView ticketCustomer;
    private TextView ticketArticles;
    private TextView ticketTotal;
    private TextView tariffArea;
    private Button ticketAccess;
    private Button productAccess;
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Init data
        if (state != null) {
            // From state
            this.catalog = (Catalog) state.getSerializable("catalog");
            this.ticket = (Ticket) state.getSerializable("ticket");
            this.currentCategory = this.catalog.getAllCategories().get(0);
        } else {
            // From scratch
            this.catalog = catalogInit;
            catalogInit = null;
            this.currentCategory = this.catalog.getAllCategories().get(0);
            if (ticketInit == null) {
                this.ticket = new Ticket();
            } else {
                this.ticket = ticketInit;
                ticketInit = null;
            }
        }
        this.barcodeInput = new BarcodeInput();
        this.gestureDetector = new GestureDetector(this, this);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                return TicketInput.this.gestureDetector.onTouchEvent(e);
            }
        };
        // Set views
        setContentView(R.layout.products);
        this.ticketLabel = (TextView) this.findViewById(R.id.ticket_label);
        this.ticketCustomer = (TextView) this.findViewById(R.id.ticket_customer);
        this.ticketArticles = (TextView) this.findViewById(R.id.ticket_articles);
        this.ticketTotal = (TextView) this.findViewById(R.id.ticket_total);
        this.tariffArea = (TextView) this.findViewById(R.id.ticket_area);

        this.categories = (Gallery) this.findViewById(R.id.categoriesGrid);
        this.products = (GridView) this.findViewById(R.id.productsGrid);
        CategoriesAdapter catAdapt = new CategoriesAdapter(this.catalog.getAllCategories());
        this.categories.setAdapter(catAdapt);
        this.categories.setOnItemSelectedListener(this);
        this.categories.setSelection(0, false);
        ProductClickListener prdListnr = new ProductClickListener();
        this.products.setOnItemClickListener(prdListnr);
        this.products.setOnItemLongClickListener(prdListnr);
        this.products.setOnTouchListener(touchListener);
        // Hide new ticket/delete ticket on simple mode
        if (Configure.getTicketsMode(this) == Configure.SIMPLE_MODE) {
            this.findViewById(R.id.ticket_delete).setEnabled(false);
            this.findViewById(R.id.ticket_new).setEnabled(false);
        }

        // Check presence of barcode scanner
        Intent i = new Intent("com.google.zxing.client.android.SCAN");
        List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(i,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            this.findViewById(R.id.scan_customer).setVisibility(View.GONE);
        }

        this.ticketContent = (ListView) this.findViewById(R.id.ticket_content);
        this.ticketContent.setAdapter(new TicketLinesAdapter(this.ticket,
                this, true));
        this.ticketContent.setOnTouchListener(touchListener);
        // Check presence of tariff areas
        if (TariffAreaData.areas.size() == 0) {
            this.findViewById(R.id.change_area).setVisibility(View.GONE);
            this.tariffArea.setVisibility(View.GONE);
        }
        this.findViewById(R.id.btn_cart_back).setEnabled(false);
        this.updateProducts();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start timer to check rotation (every second after 3 seconds)
        if (this.powaStatusCheck == null) {
            this.powaStatusCheck = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        TicketInput.this.powa.requestMCURotationSensorStatus();
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "Rotation check failed", e);
                    }
                }
            };
            this.powaStatusCheck.schedule(task, 3000, 1000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ticketSwitch != null) {
            this.switchTicket(ticketSwitch);
            ticketSwitch = null;
        } else {
            this.updateTicketView();
        }
        // Init PowaPOS T25 for scanner and base
        this.powa = new PowaPOS(this, new PowaCallback());
        PowaMCU mcu = new PowaTSeries(this);
        this.powa.addPeripheral(mcu);
        PowaScanner scanner = new PowaS10Scanner(this);
        this.powa.addPeripheral(scanner);
        PowaTSeries base = new PowaTSeries(this);
        this.powa.addPeripheral(base);
        // Get and bind scanner
        List<PowaDeviceObject> scanners = this.powa.getAvailableScanners();
        if (scanners.size() > 0) {
            this.powa.selectScanner(scanners.get(0));
        } else {
            Log.w(LOG_TAG, "Scanner not found");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop timer
        if (this.powaStatusCheck != null) {
            this.powaStatusCheck.cancel();
            this.powaStatusCheck = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("catalog", this.catalog);
        outState.putSerializable("ticket", this.ticket);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Configure.getTicketsMode(this) == Configure.RESTAURANT_MODE
                && Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(this, null,
                    TicketUpdater.TICKETSERVICE_SEND
                            | TicketUpdater.TICKETSERVICE_ONE, this.ticket);
        }
        this.overridePendingTransition(R.transition.fade_in,
                R.transition.fade_out);
        this.powa.dispose();
    }

    private void switchTicket(Ticket t) {
        this.ticket = t;
        this.ticketContent.setAdapter(new TicketLinesAdapter(this.ticket,
                this, true));
        this.updateTicketView();
    }

    /**
     * Callback for delete ticket button
     */
    public void deleteTicket(View v) {
        // Show confirmation
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(this.getString(R.string.delete_ticket_title));
        String message = this.getResources().getQuantityString(
                R.plurals.delete_ticket_message,
                this.ticket.getArticlesCount(), this.ticket.getArticlesCount());
        b.setMessage(message);
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Session currSession = SessionData.currentSession(TicketInput.this);
                Ticket current = currSession.getCurrentTicket();
                for (Ticket t : currSession.getTickets()) {
                    if (t.getLabel().equals(current.getLabel())) {
                        currSession.getTickets().remove(t);
                        break;
                    }
                }
                if (currSession.getTickets().size() == 0) {
                    currSession.newTicket();
                } else {
                    currSession.setCurrentTicket(currSession.getTickets().get(currSession.getTickets().size() - 1));
                }
                switchTicket(currSession.getCurrentTicket());
                try {
                    SessionData.saveSession(TicketInput.this);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to save session", e);
                }
            }
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }

    /**
     * New ticket button callback
     */
    public void newTicket(View v) {
        Session currSession = SessionData.currentSession(this);
        currSession.newTicket();
        switchTicket(currSession.getCurrentTicket());
        try {
            SessionData.saveSession(this);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to save session", e);
        }
    }

    /**
     * Callback for ticket switch button
     */
    public void switchTicketBtn(View v) {

        if (Configure.getTicketsMode(this) != Configure.SIMPLE_MODE) {

            this.openSwitchTicket();
        }

    }

    /**
     * Request a switch to an other ticket. It will be effective
     * next time the activity is displayed
     */
    public static void requestTicketSwitch(Ticket t) {
        ticketSwitch = t;
    }

    private void updateTicketView() {
        // Update ticket info
        String count = this.getString(R.string.ticket_articles,
                this.ticket.getArticlesCount());
        String total = this.getString(R.string.ticket_total,
                this.ticket.getTotalPrice());
        String label = this.getString(R.string.ticket_label,
                this.ticket.getLabel());
        this.ticketLabel.setText(label);
        this.ticketArticles.setText(count);
        this.ticketTotal.setText(total);
        // Update customer info
        if (this.ticket.getCustomer() != null) {
            Customer cust = this.ticket.getCustomer();
            String name = null;
            if (cust.getPrepaid() > 0.005) {
                name = this.getString(R.string.customer_prepaid_label,
                        cust.getName(), cust.getPrepaid());
            } else {
                name = cust.getName();
            }
            this.ticketCustomer.setText(name);
            this.ticketCustomer.setVisibility(View.VISIBLE);
        } else {
            this.ticketCustomer.setVisibility(View.INVISIBLE);
        }
        // Update tariff area info
        ((TicketLinesAdapter) TicketInput.this.ticketContent.getAdapter()).notifyDataSetChanged();
        if (this.ticket.getTariffArea() != null) {
            this.tariffArea.setText(this.ticket.getTariffArea().getLabel());
        } else {
            this.tariffArea.setText(R.string.default_tariff_area);
        }
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
            final Product p = item.getProduct();
            TicketInput.this.productPicked(p);
        }

        public boolean onItemLongClick(AdapterView<?> parent, View v,
                                       int position, long id) {
            ProductBtnItem item = (ProductBtnItem) v;
            Product p = item.getProduct();
            AlertDialog.Builder b = new AlertDialog.Builder(TicketInput.this);
            b.setTitle(p.getLabel());
            String message = TicketInput.this.getString(R.string.prd_info_price,
                    p.getTaxedPrice(TicketInput.this.ticket.getTariffArea()));
            b.setMessage(message);
            b.setNeutralButton(android.R.string.ok, null);
            b.show();
            return true;
        }
    }

    /**
     * Trigger actions required before adding the product then add it
     * to the ticket
     */
    private void productPicked(final Product p) {
        if (CompositionData.isComposition(p)) {
            Intent i = new Intent(this, CompositionInput.class);
            CompositionInput.setup(this.catalog,
                    CompositionData.getComposition(p.getId()));
            this.startActivityForResult(i, CODE_COMPO);
        } else {
            /* If the product is scaled, then a message pops up and let
             * the user choose the weight
             */
            if (p.isScaled()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                alertDialogBuilder.setView(input);
                alertDialogBuilder.setTitle(p.getLabel());
                alertDialogBuilder
                        .setView(input)
                        .setIcon(R.drawable.scale)
                        .setMessage(R.string.scaled_products_info)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //On click, add the scaled product to the ticket
                                String getString = input.getText().toString();
                                if (!TextUtils.isEmpty(getString)) {
                                    addScaledProduct(p, getString);
                                }
                            }
                        })
                        .setNegativeButton(R.string.scaled_products_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //On click, dismiss the dialog
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                this.ticket.addProduct(p);
                this.updateTicketView();
            }
        }
    }

    private void readBarcode(String code) {
        boolean found = false;
        if (code.startsWith("c")) {
            // Scanned a customer card
            for (Customer c : CustomerData.customers) {
                if (code.equals(c.getCard())) {
                    this.ticket.setCustomer(c);
                    this.updateTicketView();
                    found = true;
                    break;
                }
            }
            if (!found) {
                String text = this.getString(R.string.customer_not_found,
                        code);
                Toast t = Toast.makeText(this, text,
                        Toast.LENGTH_LONG);
                t.show();
            }
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

    /**
     * Add scaled product to the ticket
     *
     * @param p     the product to add
     * @param input the weight in kg
     */
    private void addScaledProduct(Product p, String input) {
        double scale = Double.valueOf(input);
        TicketInput.this.ticket.addScaledProduct(p, scale);
        TicketInput.this.updateTicketView();
    }


    public void payTicket(View v) {
        ProceedPayment.setup(this.ticket);
        Intent i = new Intent(this, ProceedPayment.class);
        this.startActivity(i);
        this.overridePendingTransition(R.transition.slide_in,
                R.transition.fade_out);
    }

    public void scanBarcode(View v) {
        Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intentScan, CODE_SCAN);
    }

    public void changeArea(View v) {
        Intent i = new Intent(this, TariffAreaSelect.class);
        this.startActivityForResult(i, CODE_AREA);
    }

    public void addQty(TicketLine l) {
        this.ticket.adjustQuantity(l, 1);
        this.updateTicketView();
    }

    public void remQty(TicketLine l) {
        this.ticket.adjustQuantity(l, -1);
        this.updateTicketView();
    }

    /**
     * Modifies the weight of the product by asking the user a new one
     *
     * @param l the ticket's line
     */
    public void mdfyQty(final TicketLine l) {
        Product p = l.getProduct();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
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
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String recup = input.getText().toString();
                        double scale = Double.valueOf(recup);
                        TicketInput.this.ticket.adjustScale(l, scale);
                        TicketInput.this.updateTicketView();
                    }
                })
                .setNegativeButton(R.string.scaled_products_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void editProduct(final TicketLine l) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.ticket_item_edit, null);

        // Creating Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        layout.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        // Dialog Layout Modification
        ViewGroup table = (ViewGroup) layout.findViewById(R.id.table_characteristics);
        final int childCount = table.getChildCount();
        // TODO: Dynamically load dropdown menu
        for (int i = 0; i < childCount; ++i) {
            final View row = table.getChildAt(i);
            characLabelCreator((TextView) row.findViewById(R.id.row_characteristic_odd_label), i + 1);
            characLabelCreator((TextView) row.findViewById(R.id.row_characteristic_even_label), i + 3);
        }

        // Adding Product info in layout
        Product p = l.getProduct();
        // TODO: put this try catch in a static func in Product class
        try {
            Bitmap img;
            if (p.hasImage() && null != (img = ImagesData.getProductImage(context, p.getId()))) {
                ((ImageView) layout.findViewById(R.id.product_img)).setImageBitmap(img);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ((TextView) layout.findViewById(R.id.product_label)).setText(p.getLabel());
        ((EditText) layout.findViewById(R.id.tariff_edit)).setText(Double.toString(p.getPrice()));
        ((EditText) layout.findViewById(R.id.reduction_edit)).setText(Double.toString(0));

        dialog.show();
    }

    private static void characLabelCreator(TextView label, int labelNbr) {
        StringBuilder str = new StringBuilder(4);
        str.append(label.getText());
        str.append(' ');
        str.append(labelNbr);
        str.append(':');
        label.setText(str);
    }

    public void delete(TicketLine l) {
        this.ticket.removeLine(l);
        this.updateTicketView();
    }

    /**
     * Category selected
     */
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

    /**
     * Update the UI to switch ticket
     */
    public void openSwitchTicket() {
        // Send current ticket data in connected mode
        if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(getApplicationContext(),
                    null,
                    TicketUpdater.TICKETSERVICE_SEND
                            | TicketUpdater.TICKETSERVICE_ONE, ticket);
        }
        // Open ticket picker
        switch (Configure.getTicketsMode(this)) {
            case Configure.STANDARD_MODE:
                // Open selector popup
                try {
                    final ListPopupWindow popup = new ListPopupWindow(this);
                    ListAdapter adapter = new SessionTicketsAdapter(this);
                    popup.setAnchorView(this.ticketLabel);
                    popup.setAdapter(adapter);
                    popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            // TODO: handle connected mode on switch
                            Ticket t = SessionData.currentSession(TicketInput.this).getTickets().get(position);
                            TicketInput.this.switchTicket(t);
                            popup.dismiss();
                        }

                        public void onNothingSelected(AdapterView v) {
                        }
                    });
                    popup.setWidth(ScreenUtils.inToPx(2, this));
                    int ticketsCount = adapter.getCount();
                    int height = ScreenUtils.dipToPx(SessionTicketsAdapter.HEIGHT_DIP * Math.min(5, ticketsCount), this);
                    popup.setHeight(height);
                    popup.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Configure.RESTAURANT_MODE:
                // Open restaurant activity
                Intent i = new Intent(this, TicketSelect.class);
                this.startActivityForResult(i, TicketSelect.CODE_TICKET);
                break;
            default:
                //NOT AVAILABLE IN SIMPLE_MODE
                Log.wtf(LOG_TAG, "Swicth Ticket is not available mode " + Configure.getTicketsMode(this));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case TicketSelect.CODE_TICKET:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        break;
                    case Activity.RESULT_OK:
                        this.switchTicket(SessionData.currentSession(this).getCurrentTicket());
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
            case CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    String code = data.getStringExtra("SCAN_RESULT");
                    this.readBarcode(code);
                }
                break;
            case CODE_COMPO:
                if (resultCode == Activity.RESULT_OK) {
                    CompositionInstance compo = (CompositionInstance)
                            data.getSerializableExtra("composition");
                    this.ticket.addProduct(compo);
                    this.updateTicketView();
                }
                break;
            case CODE_AREA:
                if (resultCode == Activity.RESULT_OK) {
                    TariffArea area = (TariffArea) data.getSerializableExtra("tariffArea");
                    this.ticket.setTariffArea(area);
                    this.updateTicketView();
                }
                break;
            case CODE_INPUT:
                if (resultCode == Activity.RESULT_OK) {
                    int action = data.getIntExtra("action", 0);
                    switch (action) {
                        case KeypadInput.BARCODE:
                            String barcode = data.getStringExtra("input");
                            this.readBarcode(barcode);
                            break;
                        case KeypadInput.ADD:
                            double value = data.getDoubleExtra("input", 0.0);
                            Product p = new Product(null, "", "", value, "004", 0.0,
                                    false, false);
                            this.ticket.addProduct(p);
                            this.updateTicketView();
                            break;
                    }
                }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("down " + keyCode);
        if (keyCode == 160) { // numpad enter
            // Fuck off sa mere
            this.onKeyUp(KeyEvent.KEYCODE_ENTER, event);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Handle keyboard input for barcode scanning
     */
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        if (e1.getX() > (e2.getX() + 50) && velocityX < -1500) {
            // Swipe left
            this.payTicket(null);
            return true;
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


    private class PowaCallback extends PowaPeripheralCallback {
        public void onCashDrawerStatus(PowaPOSEnums.CashDrawerStatus status) {
        }

        public void onScannerInitialized(final PowaPOSEnums.InitializedResult result) {
        }

        public void onScannerRead(final String data) {
            TicketInput.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TicketInput.this.readBarcode(data);
                }
            });
        }

        public void onUSBDeviceAttached(final PowaPOSEnums.PowaUSBCOMPort port) {
        }

        public void onUSBDeviceDetached(final PowaPOSEnums.PowaUSBCOMPort port) {
        }

        public void onUSBReceivedData(PowaPOSEnums.PowaUSBCOMPort port,
                                      final byte[] data) {
        }

        public void onPrintJobCompleted(PowaPOSEnums.PrintJobResult result) {
        }

        @Override
        public void onRotationSensorStatus(PowaPOSEnums.RotationSensorStatus status) {
            if (status == PowaPOSEnums.RotationSensorStatus.ROTATED) {
                TicketInput.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TicketInput.this.powaStatusCheck != null) {
                            TicketInput.this.powaStatusCheck.cancel();
                            TicketInput.this.powaStatusCheck = null;
                        }
                        Intent createCustomer = new Intent(TicketInput.this,
                                CustomerCreate.class);
                        startActivity(createCustomer);
                    }
                });
            }
        }

        public void onMCUSystemConfiguration(Map<String, String> config) {
        }

        @Override
        public void onMCUBootloaderUpdateFailed(final PowaPOSEnums.BootloaderUpdateError error) {
        }

        @Override
        public void onMCUBootloaderUpdateStarted() {
        }

        @Override
        public void onMCUBootloaderUpdateProgress(final int progress) {
        }

        @Override
        public void onMCUBootloaderUpdateFinished() {
        }

        @Override
        public void onMCUInitialized(final PowaPOSEnums.InitializedResult result) {
        }

        @Override
        public void onMCUFirmwareUpdateStarted() {
        }

        @Override
        public void onMCUFirmwareUpdateProgress(final int progress) {
        }

        @Override
        public void onMCUFirmwareUpdateFinished() {
        }

    }

/*
    Does not seems to be used
    private static final int MENU_SWITCH_TICKET = 0;
    private static final int MENU_NEW_TICKET = 1;
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ab_ticket_input, menu);

        if (CustomerData.customers.size() == 0) {
            menu.findItem(R.id.ab_menu_customer_list).setEnabled(false);
        }
        User cashier = SessionData.currentSession(this).getUser();
        if (cashier.hasPermission("fr.pasteque.pos.panels.JPanelCloseMoney")) {
            menu.findItem(R.id.ab_menu_close_session).setEnabled(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!ReceiptData.hasReceipts()
                || !SessionData.currentSession(this).getUser().hasPermission("sales.EditTicket")) {
            menu.findItem(R.id.ab_menu_past_ticket).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_menu_cashdrawer:
                TicketInput.this.powa.openCashDrawer();
                break;
            case R.id.ab_menu_manual_input:
                DialogFragment dial = new ManualInput();
                dial.show(getFragmentManager(), "Manual Input FRAG");
                break;
            case R.id.ab_menu_customer_list:
                Intent i = new Intent(this, CustomerSelect.class);
                CustomerSelect.setup(this.ticket.getCustomer() != null);
                this.startActivityForResult(i, CustomerSelect.CODE_CUSTOMER);
                break;
            case R.id.ab_menu_customer_add:
                Intent createCustomer = new Intent(this, CustomerCreate.class);
                startActivity(createCustomer);
                break;
            case R.id.ab_menu_calendar:
                java.util.Calendar starTime = Calendar.getInstance();

                Uri uri = Uri.parse("content://com.android.calendar/time/" +
                        String.valueOf(starTime.getTimeInMillis()));

                Intent openCalendar = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(openCalendar);
                break;
            case R.id.ab_menu_past_ticket:
                i = new Intent(this, ReceiptSelect.class);
                this.startActivity(i);
                break;
            case R.id.ab_menu_close_session:
                CloseCash.close(this);
                break;
            /*case MENU_NEW_TICKET:
                if (Configure.getSyncMode(this) == Configure.AUTO_SYNC_MODE) {
                    TicketUpdater.getInstance().execute(getApplicationContext(),
                            null,
                            TicketUpdater.TICKETSERVICE_SEND
                                    | TicketUpdater.TICKETSERVICE_ONE, ticket);
                }
                SessionData.currentSession(this).newTicket();
                try {
                    SessionData.saveSession(this);
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "Unable to save session", ioe);
                    Error.showError(R.string.err_save_session, this);
                }
                this.switchTicket(SessionData.currentSession(this).getCurrentTicket());
                break;
            case MENU_SWITCH_TICKET:
                this.openSwitchTicket();
                break;
            case OPEN_BROWSER_BNP:
                String url = "https://www.secure.bnpparibas.net/banque/portail/particulier/HomePage?type=site";
                Intent accessBnp = new Intent( Intent.ACTION_VIEW, android.net.Uri.parse( url ) );
                startActivity(accessBnp);
                break;*/
        }
        return true;
    }

}
