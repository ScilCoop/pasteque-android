package fr.pasteque.client;

import java.io.IOError;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.Toast;

import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;

import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.fragments.CatalogFragment;
import fr.pasteque.client.fragments.CustomerInfoDialog;
import fr.pasteque.client.fragments.CustomerSelectDialog;
import fr.pasteque.client.fragments.ManualInputDialog;
import fr.pasteque.client.fragments.PaymentFragment;
import fr.pasteque.client.fragments.ProductScaleDialog;
import fr.pasteque.client.fragments.TicketFragment;
import fr.pasteque.client.fragments.ViewPageFragment;
import fr.pasteque.client.models.*;
import fr.pasteque.client.drivers.printer.PrinterConnection;
import fr.pasteque.client.utils.*;
import fr.pasteque.client.utils.Error;
import fr.pasteque.client.utils.exception.NotFoundException;

import static fr.pasteque.client.utils.PastequeConfiguration.*;

public class Transaction extends POSConnectedTrackedActivity
        implements CatalogFragment.Listener,
        ProductScaleDialog.Listener,
        ManualInputDialog.Listener,
        TicketFragment.Listener,
        PaymentFragment.Listener,
        CustomerSelectDialog.Listener,
        CustomerInfoDialog.CustomerListener,
        ViewPager.OnPageChangeListener {

    // Activity Result code
    private static final int COMPOSITION = 1;
    private static final int CUSTOMER_SELECT = 2;
    private static final int CUSTOMER_CREATE = 3;
    private static final int RESTAURANT_TICKET_FINISH = 4;

    //  SERIALIZE STRING
    private static final String PRINT_STATE = "printEnabled";
    private static final String PAYMENT_CLOSED = "paymentClosed";

    private static final String LOG_TAG = "Pasteque/Transaction";
    private static final int CATALOG_FRAG = 0;
    private static final int TICKET_FRAG = 1;
    private static final int PAYMENT_FRAG = 2;
    private static final long SCANNERTIMER = 500;
    public static final int PAST_TICKET_FOR_RESULT = 0;
    private final TransPage[] PAGES = new TransPage[]{
            new TransPage(0.65f, CatalogFragment.class),
            new TransPage(0.35f, TicketFragment.class),
            new TransPage(0.65f, PaymentFragment.class)};

    // Data
    private Context mContext;
    private Ticket mPendingTicket;
    private TransactionPagerAdapter mPagerAdapter;
    private boolean mbPrintEnabled;
    private boolean mbPaymentClosed;

    // Views
    private ViewPager mPager;
    private String barcode = "";
    private long lastBarCodeTime;
    private CustomerInfoDialog customerInfoDialog;

    // Others
    private class TransPage {
        // Between 0.0 - 1.0
        private float mWidth;
        private Class<? extends ViewPageFragment> mPageFragment;

        public TransPage(float width, @NonNull Class<? extends ViewPageFragment> pageFragment) {
            mWidth = width;
            mPageFragment = pageFragment;
        }

        public float getWidth() {
            return mWidth;
        }

        public Class<? extends ViewPageFragment> getPageFragment() {
            return mPageFragment;
        }
    }

    //  FUNCTIONS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reuse(savedInstanceState);

        mContext = this;
        mPagerAdapter = new TransactionPagerAdapter(getFragmentManager());
        mPager = new ViewPager(mContext);
        // There is View.generateViewId() but min_api < 17
        mPager.setId(R.id.transaction_view_pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setBackgroundResource(R.color.main_bg);
        mPager.setOnPageChangeListener(this);
        setContentView(mPager);
        //TODO: Check presence of barcode scanner
        /*Intent i = new Intent("com.google.zxing.client.android.SCAN");
        List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(i,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() != 0) {
            this.findViewById(R.id.scan_customer).setVisibility(View.GONE);
        }*/
        //TODO: Check presence of tariff areas
        /*if (TariffAreaData.areas.size() == 0) {
            this.findViewById(R.id.change_area).setVisibility(View.GONE);
            this.tariffArea.setVisibility(View.GONE);
        }*/
        this.enableActionBarTitle();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (customerInfoDialog != null && customerInfoDialog.isVisible()) {
            Log.d("Pasteque", "salut");
            customerInfoDialog.looseKeyboardFocus();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (!returnToCatalogueView()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PAST_TICKET_FOR_RESULT:
                if (data != null) {
                    String ticketId = data.getStringExtra(ReceiptSelect.TICKET_ID_KEY);
                    try {
                        getTicketFragment().onTicketRefund(getTicketFromTicketId(ticketId));
                    } catch (NotFoundException ignore) {
                    }
                }
                break;
            case COMPOSITION:
                if (resultCode == Activity.RESULT_OK) {
                    CompositionInstance compo = (CompositionInstance)
                            data.getSerializableExtra("composition");
                    addACompoToTicket(compo);
                }
                break;
            // TODO: TEST restaurant implementation.
            case RESTAURANT_TICKET_FINISH:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        // Back to start
                        finish();
                        break;
                    case Activity.RESULT_OK:
                        mPendingTicket = Data.Session.currentSession(mContext).getCurrentTicket();
                        mPager.setCurrentItem(CATALOG_FRAG);
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private Ticket getTicketFromTicketId(String ticketId) throws NotFoundException {
        List<Receipt> list = Data.Receipt.getReceipts(Pasteque.getAppContext());
        for (Receipt receipt : list) {
            Ticket ticket = receipt.getTicket();
            if (ticket.getId().equals(ticketId)) {
                return ticket;
            }
        }
        throw new NotFoundException();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(PRINT_STATE, mbPrintEnabled);
        state.putBoolean(PAYMENT_CLOSED, mbPaymentClosed);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean returnToCatalogueView() {
        if (getCatalogFragment().displayProducts()) {
            getCatalogFragment().setCategoriesVisible();
            return true;
        }
        return false;
    }

    /*
     *  INTERFACE
     */

    @Override
    public void onCfProductClicked(Product p, Catalog catData) {
        registerAProduct(p, catData);
    }

    @Override
    public boolean onCfProductLongClicked(final Product p) {
        TicketFragment ticket = getTicketFragment();
        String message = getString(R.string.prd_info_price,
                ticket.getTicketData().getGenericPrice(p, CalculPrice.Type.TAXE | CalculPrice.Type.DISCOUNT));
        disposeTicketFragment(ticket);

        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle(p.getLabel());
        b.setMessage(message);
        b.setPositiveButton(android.R.string.ok, null);
        b.setNeutralButton(R.string.refund, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                registerAProductReturn(p);
                Transaction.this.returnToCatalogueView();
            }
        });
        b.show();
        return true;
    }

    @Override
    public void OnCfCatalogViewChanged(boolean catalogIsVisible, Category category) {
        if (catalogIsVisible) {
            this.setActionBarTitle(getString(R.string.catalog));
        } else if (category != null) {
            this.setActionBarTitle(category.getLabel());
        } else {
            this.setActionBarTitle(getString(R.string.no_category));
        }
        this.setActionBarHomeVisibility(!catalogIsVisible);
    }

    @Override
    public void onPsdPositiveClick(Product p, double weight, boolean isProductReturned) {
        if (weight > 0) {
            if (isProductReturned) {
                addAScaledProductReturnToTicket(p, weight);
            } else {
                addAScaledProductToTicket(p, weight);
            }
        }
    }

    @Override
    public void onMidProductCreated(Product product) {
        addAProductToTicket(product);
    }

    @Override
    public void onMidProductPick(Product product) {
        CatalogFragment cat = getCatalogFragment();
        registerAProduct(product, cat.getCatalogData());
        disposeCatalogFragment(cat);
    }

    @Override
    public void onTfCheckInClick() {
        mPager.setCurrentItem(CATALOG_FRAG);
    }

    @Override
    public void onTfCheckOutClick() {
        mPager.setCurrentItem(PAYMENT_FRAG);
    }

    @Override
    public void onPfPrintReceipt(final Receipt receipt) {
        mbPaymentClosed = true;
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) {
                manager.printReceipt(receipt);
            }
        });
    }

    @Override
    public void onPfCustomerListClick() {
        showCustomerList();
    }

    @Override
    public Receipt onPfSaveReceipt(ArrayList<Payment> p) {
        TicketFragment t = getTicketFragment();
        Ticket ticketData = t.getTicketData();
        ticketData.setTicketId(String.valueOf(Data.TicketId.newTicketId()));
        // Create and save the receipt and remove from session
        Session currSession = Data.Session.currentSession(mContext);
        User u = currSession.getUser();
        final Receipt r = new Receipt(ticketData, p, u);
        if (Configure.getDiscount(mContext)) {
            r.setDiscount(Data.Discount.getADiscount());
        }
        Data.Receipt.addReceipt(r);
        Data.TicketId.ticketClosed(mContext);
        try {
            Data.Receipt.save(mContext);
        } catch (IOError e) {
            Log.e(LOG_TAG, "Unable to save receipts", e);
            Error.showError(R.string.err_save_receipts, this);
        }
        currSession.closeTicket(ticketData);
        try {
            Data.Session.save(mContext);
        } catch (IOError ioe) {
            Log.e(LOG_TAG, "Unable to save session", ioe);
            Error.showError(R.string.err_save_session, this);
        }
        disposeTicketFragment(t);
        return r;
    }

    @Override
    public void onPfFinished() {
        PaymentFragment payment = getPaymentFragment();
        payment.resetPaymentList();
        disposePaymentFragment(payment);
        Session currSession = Data.Session.currentSession(mContext);
        this.returnToCatalogueView();
        // Return to a new ticket edit
        switch (Configure.getTicketsMode(mContext)) {
            case Configure.SIMPLE_MODE:
                mPendingTicket = currSession.newTicket();
                mPager.setCurrentItem(CATALOG_FRAG);
                break;
            case Configure.STANDARD_MODE:
                if (!currSession.hasTicket()) {
                    mPendingTicket = currSession.newTicket();
                    mPager.setCurrentItem(CATALOG_FRAG);
                } else {
                    // Pick last ticket
                    currSession.setCurrentTicket(currSession.getTickets().get(currSession.getTickets().size() - 1));
                    mPendingTicket = currSession.getCurrentTicket();
                    mPager.setCurrentItem(CATALOG_FRAG);
                }
                break;
            case Configure.RESTAURANT_MODE:
                Intent i = new Intent(mContext, TicketSelect.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(i, RESTAURANT_TICKET_FINISH);
                break;
        }
    }

    @Override
    public void onCustomerPicked(Customer customer) {
        TicketFragment tFrag = getTicketFragment();
        tFrag.setCustomer(customer);
        tFrag.updateView();
        if (mPager.getCurrentItem() != CATALOG_FRAG) {
            updatePaymentFragment(tFrag, null);
        }
        disposeTicketFragment(tFrag);
        try {
            Data.Session.save(mContext);
        } catch (IOError ioe) {
            Log.e(LOG_TAG, "Unable to save session", ioe);
            Error.showError(R.string.err_save_session, this);
        }
    }

    @Override
    public void onCustomerCreated(Customer customer) {
        if (Data.Customer.customers.size() == 1 && getActionBar() != null) {
            invalidateOptionsMenu();
        }
        onCustomerPicked(customer);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        switch (i) {
            case CATALOG_FRAG: {
                TicketFragment ticket = getTicketFragment();
                ticket.setState(TicketFragment.CHECKIN_STATE);
                ticket.updatePageState();
                if (mPendingTicket != null) {
                    ticket.switchTicket(mPendingTicket);
                    mPendingTicket = null;
                }
                disposeTicketFragment(ticket);
                invalidateOptionsMenu();
                setActionBarTitleVisibility(true);
                break;
            }
            case TICKET_FRAG:
            case PAYMENT_FRAG: {
                TicketFragment t = getTicketFragment();
                t.setState(TicketFragment.CHECKOUT_STATE);
                t.updatePageState();
                updatePaymentFragment(t, null);
                disposeTicketFragment(t);
                invalidateOptionsMenu();
                setActionBarTitleVisibility(false);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    /*
     * ACTION MENU RELATED
     */

    @SuppressWarnings("ResourceType")
    private void setActionBarTitleVisibility(boolean visibile) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (visibile) {
                actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_TITLE);
                if (getCatalogFragment().getCurrentCategory() != null) {
                    this.setActionBarHomeVisibility(true);
                } else {
                    this.setActionBarHomeVisibility(false);
                }
            } else {
                actionBar.setDisplayOptions(actionBar.getDisplayOptions() & ~ActionBar.DISPLAY_SHOW_TITLE);
                this.setActionBarHomeVisibility(false);
            }
        }
    }

    private void enableActionBarTitle() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            //noinspection ResourceType
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | actionBar.getDisplayOptions());
        }
    }

    private void setActionBarHomeVisibility(boolean isVisible) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(isVisible);
            actionBar.setDisplayHomeAsUpEnabled(isVisible);
        }
    }

    private void setActionBarTitle(String value) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null && value != null) {
            actionBar.setTitle(value);
        }
    }

    private void cleanLastScanIfRequired() {
        long current = System.currentTimeMillis();
        if (current - this.lastBarCodeTime > Transaction.SCANNERTIMER) {
            this.barcode = "";
        }
        this.lastBarCodeTime = current;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.cleanLastScanIfRequired();
        if (this.barcode == null) {
            this.barcode = "";
        }
        this.barcode += event.getNumber();
        if (this.barcode.length() == 13) {
            Log.i("keyboard", this.barcode);
            if (BarcodeCheck.ean13(this.barcode)) {
                this.readBarcode(this.barcode);
            } else {
                Toast.makeText(this, getString(R.string.err_wrong_ean13), Toast.LENGTH_SHORT).show();
            }
            this.barcode = "";
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ab_ticket_input, menu);

        if (Data.Customer.customers.size() == 0) {
            menu.findItem(R.id.ab_menu_customer_list).setEnabled(false);
        }
        User cashier = Data.Session.currentSession(mContext).getUser();
        if (cashier.hasPermission("fr.pasteque.pos.panels.JPanelCloseMoney")) {
            menu.findItem(R.id.ab_menu_close_session).setEnabled(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!Data.Receipt.hasReceipts()
                || !Data.Session.currentSession(mContext).getUser().hasPermission("sales.EditTicket")) {
            menu.findItem(R.id.ab_menu_past_ticket).setVisible(false);
        }
        if (mPager.getCurrentItem() != CATALOG_FRAG) {
            menu.findItem(R.id.ab_menu_manual_input).setEnabled(false);
        }
        if (!deviceManagerHasCashDrawer()) {
            menu.findItem(R.id.ab_menu_cashdrawer).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.returnToCatalogueView();
                break;
            case R.id.ab_menu_cashdrawer:
                //TODO: clean this out by displaying Dialog if issue
                getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
                    @Override
                    public void execute(POSDeviceManager manager) {
                        manager.openCashDrawer();
                    }
                });
                break;
            case R.id.ab_menu_manual_input:
                DialogFragment dial = new ManualInputDialog();
                dial.show(getFragmentManager(), ManualInputDialog.TAG);
                break;
            case R.id.ab_menu_customer_list:
                showCustomerList();
                break;
            case R.id.ab_menu_customer_add:
                createNewCustomer();
                break;
            case R.id.ab_menu_calendar:
                java.util.Calendar starTime = Calendar.getInstance();

                Uri uri = Uri.parse("content://com.android.calendar/time/" +
                        String.valueOf(starTime.getTimeInMillis()));

                Intent openCalendar = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(openCalendar);
                break;
            case R.id.ab_menu_past_ticket:
                Intent receiptSelect = new Intent(mContext, ReceiptSelect.class);
                this.startActivityForResult(receiptSelect, PAST_TICKET_FOR_RESULT);
                break;
            case R.id.ab_menu_close_session:
                CloseCash.close(this);
                break;
            default:
                return false;

            /*
                case MENU_BARCODE:
                scanBarcode(nulClabil);
                break;
            */
        }
        return true;
    }

    /*
     *  PRIVATES
     */

    // THIS CLASS DATA RELATED FUNCTIONS

    private void reuse(Bundle savedState) {
        if (savedState == null) {
            mbPrintEnabled = true;
            mbPaymentClosed = false;
        } else {
            mbPrintEnabled = savedState.getBoolean(PRINT_STATE);
            mbPaymentClosed = savedState.getBoolean(PAYMENT_CLOSED);
        }
    }

    // CUSTOMER RELATED FUNCTIONS

    private void createNewCustomer() {
        customerInfoDialog = CustomerInfoDialog.newInstance(true, null);
        customerInfoDialog.setDialogCustomerListener(this);
        customerInfoDialog.show(getFragmentManager());
    }

    private void showCustomerList() {
        TicketFragment t = getTicketFragment();
        boolean bSetup = t.getCustomer() != null;
        disposeTicketFragment(t);
        CustomerSelectDialog dialog = CustomerSelectDialog.newInstance(bSetup);
        dialog.setDialogListener(this);
        dialog.show(getFragmentManager(), CustomerSelectDialog.TAG);
    }

    // PRODUCT RELATED FUNCTIONS

    /**
     * Asks for complementary product information before adding it to ticket
     *
     * @param p       Product to be added
     * @param catData The current Catalog for data comparison
     */
    private void registerAProduct(Product p, Catalog catData) {
        // TODO: COMPOSITION NOT TESTED
        if (Data.Composition.isComposition(p)) {
            Intent i = new Intent(mContext, CompositionInput.class);
            CompositionInput.setup(catData, Data.Composition.getComposition(p.getId()));
            startActivityForResult(i, COMPOSITION);
        } else if (p.isScaled()) {
            askForAScaledProduct(p, false);
        } else {
            addAProductToTicket(p);
        }
    }

    private void registerAProductReturn(Product p) {
        if (Data.Composition.isComposition(p)) {
            Toast.makeText(this, getString(R.string.refund_composition), Toast.LENGTH_LONG).show();
        } else if (p.isScaled()) {
            askForAScaledProduct(p, true);
        } else {
            addAProductReturnToTicket(p);
        }
    }

    void askForAScaledProduct(Product p, boolean isReturnProduct) {
        // If the product is scaled, asks the weight
        ProductScaleDialog dial = ProductScaleDialog.newInstance(p, isReturnProduct);
        dial.setDialogListener(this);
        dial.show(getFragmentManager(), ProductScaleDialog.TAG);
    }

    // Only suitable for adding one product at a time because updateView is heavy
    private void addAProductToTicket(Product p) {
        TicketFragment ticket = getTicketFragment();
        ticket.scrollTo(ticket.addProduct(p));
        ticket.updateView();
        disposeTicketFragment(ticket);
    }

    private void addACompoToTicket(CompositionInstance compo) {
        TicketFragment ticket = getTicketFragment();
        ticket.addProduct(compo);
        ticket.updateView();
        disposeTicketFragment(ticket);
    }

    private void addAScaledProductToTicket(Product p, double weight) {
        TicketFragment ticket = getTicketFragment();
        ticket.addScaledProduct(p, weight);
        ticket.scrollDown();
        ticket.updateView();
        disposeTicketFragment(ticket);
    }

    private void addAProductReturnToTicket(Product p) {
        TicketFragment ticket = getTicketFragment();
        ticket.scrollTo(ticket.addProductReturn(p));
        ticket.updateView();
        disposeTicketFragment(ticket);
    }

    private void addAScaledProductReturnToTicket(Product p, double weight) {
        TicketFragment ticket = getTicketFragment();
        ticket.addScaledProductReturn(p, weight);
        ticket.scrollDown();
        ticket.updateView();
        disposeTicketFragment(ticket);
    }

    private void readBarcode(String code) {
        // It is a DISCOUNT Barcode
        if (code.startsWith(Barcode.Prefix.DISCOUNT)) {
            try {
                Discount disc = Data.Discount.findFromBarcode(code);
                if (disc.isValid()) {
                    TicketFragment ticketFragment = getTicketFragment();
                    ticketFragment.setDiscountRate(disc.getRate());
                    ticketFragment.updateView();
                    disposeTicketFragment(ticketFragment);
                    Log.i(LOG_TAG, "Discount: " + disc.getTitle(this.getApplicationContext()) + ", added");
                } else {
                    Toast.makeText(mContext, getString(R.string.discount_outdated), Toast.LENGTH_LONG).show();
                }
            } catch (NotFoundException e) {
                Log.e(LOG_TAG, "Discount not found", e);
            }
            // Can not be something else
            return;
        }

        // Is it a customer card ?
        for (Customer c : Data.Customer.customers) {
            if (code.equals(c.getCard())) {
                onCustomerPicked(c);
                return;
            }
        }
        // Is it a product ?
        Catalog cat = Data.Catalog.catalog(mContext);
        Product p = cat.getProductByBarcode(code);
        if (p != null) {
            CatalogFragment catFrag = getCatalogFragment();
            registerAProduct(p, catFrag.getCatalogData());
            disposeCatalogFragment(catFrag);
            String text = getString(R.string.barcode_found, p.getLabel());
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            return;
        }

        // Nothing found
        String text = getString(R.string.barcode_not_found, code);
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
    }

    //  FRAGMENT RELATED FUNCTIONS

    /**
     * To be used with dispose function
     * i.e:    SomeFragment sFrag = getSomeFragment();
     * // Code using sFrag
     * disposeSomeFragment(sFrag);
     */
    private CatalogFragment getCatalogFragment() {
        return (CatalogFragment) mPagerAdapter.getFragment(mPager, CATALOG_FRAG);
    }

    private TicketFragment getTicketFragment() {
        return (TicketFragment) mPagerAdapter.getFragment(mPager, TICKET_FRAG);
    }

    private PaymentFragment getPaymentFragment() {
        return (PaymentFragment) mPagerAdapter.getFragment(mPager, PAYMENT_FRAG);
    }

    private void disposeCatalogFragment(CatalogFragment frag) {
        mPagerAdapter.destroyForcedItem(mPager, CATALOG_FRAG, frag);
    }

    private void disposeTicketFragment(TicketFragment frag) {
        mPagerAdapter.destroyForcedItem(mPager, TICKET_FRAG, frag);
    }

    private void disposePaymentFragment(PaymentFragment frag) {
        mPagerAdapter.destroyForcedItem(mPager, PAYMENT_FRAG, frag);
    }

    private void updatePaymentFragment(TicketFragment t, PaymentFragment p) {
        boolean bDisposeTicket = false;
        boolean bDisposePayment = false;
        if (t == null) {
            t = getTicketFragment();
            bDisposeTicket = true;
        }
        if (p == null) {
            p = getPaymentFragment();
            bDisposePayment = true;
        }
        p.setCurrentCustomer(t.getCustomer());
        p.setTotalPrice(t.getTicketPrice());
        p.setTicketPrepaid(t.getTicketPrepaid());
        p.resetInput();
        p.updateView();
        if (bDisposeTicket) disposeTicketFragment(t); // If layout is accepted per android doc
        if (bDisposePayment) disposePaymentFragment(p);
    }

    /*
     *  ADAPTERS
     */

    private class TransactionPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> mFragmentReferences;
        SparseBooleanArray mWasForced;

        public TransactionPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentReferences = new SparseArray<Fragment>();
            mWasForced = new SparseBooleanArray();
        }

        @Override
        public Fragment getItem(int position) {
            Class<? extends ViewPageFragment> cls = PAGES[position].getPageFragment();
            ViewPageFragment result;
            try {
                Method newInstance = cls.getMethod("newInstance", int.class);
                result = (ViewPageFragment) newInstance.invoke(null, position);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new RuntimeException(cls.getName() +
                        " must implement static newInstance function");
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
            return result;
        }

        @Override
        public int getCount() {
            return PAGES.length;
        }

        @Override
        public float getPageWidth(int position) {
            return PAGES[position].getWidth();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragmentReferences.put(position, fragment);
            mWasForced.put(position, false);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragmentReferences.delete(position);
            mWasForced.delete(position);
            super.destroyItem(container, position, object);
        }

        /*
         *  The ViewPager handles it's own fragment lifecycle
         *  and FragmentStatePagerAdapter automatically saves fragment states upon deletion.
         *  The getFragment method force the fragment instance to be able to manipulate it
         *  even when it's outside of the view + page limit.
         */
        public Fragment getFragment(ViewGroup container, int position) {
            Fragment frag = mFragmentReferences.get(position, null);
            if (frag == null) {
                frag = (Fragment) instantiateItem(container, position);
                mWasForced.put(position, true);
            }
            return frag;
        }

        /*
         *  This method should be called at the end of any method using getFragment as
         *  a C++ destructor to optimise memory consumption.
         */
        public void destroyForcedItem(ViewGroup container, int position, Object object) {
            if (mWasForced.get(position, false)) {
                destroyItem(container, position, object);
            }
        }
    }

    /*
     *  CALLBACK
     */

    private class PrinterCallback implements Handler.Callback {

        private void endPayment() {
            mbPaymentClosed = false;
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PrinterConnection.PRINT_DONE: {
                    endPayment();
                    return true;
                }
                case PrinterConnection.PRINT_CTX_ERROR: {
                    Exception e = (Exception) msg.obj;
                    Log.w(LOG_TAG, "Unable to connect to printer", e);
                    if (mbPaymentClosed) {
                        Toast.makeText(mContext, R.string.print_no_connexion,
                                Toast.LENGTH_LONG).show();
                        endPayment();
                    } else {
                        Error.showError(R.string.print_no_connexion, Transaction.this);
                    }
                    return true;
                }
                case PrinterConnection.PRINT_CTX_FAILED:
                    // Give up
                    if (mbPaymentClosed) {
                        Toast.makeText(mContext, R.string.print_no_connexion,
                                Toast.LENGTH_LONG).show();
                        endPayment();
                    } else {
                        Error.showError(R.string.print_no_connexion, Transaction.this);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onDeviceManagerEvent(final DeviceManagerEvent event) {
        Transaction.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (event.what) {
                    case DeviceManagerEvent.BaseRotation:
                        Transaction.this.onBaseRotation(event);
                        break;
                    case DeviceManagerEvent.ScannerReader:
                        Transaction.this.readBarcode(event.getString());
                        break;
                }
            }
        });
        return false;
    }

    private void onBaseRotation(DeviceManagerEvent event) {
        if (event.extraEquals(PowaPOSEnums.RotationSensorStatus.ROTATED)) {
            createNewCustomer();
        }
    }
}
