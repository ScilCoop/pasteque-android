package fr.pasteque.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mpowa.android.sdk.common.base.PowaEnums.ConnectionState;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;

import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.CompositionData;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.fragments.CatalogFragment;
import fr.pasteque.client.fragments.CustomerSelectDialog;
import fr.pasteque.client.fragments.ManualInputDialog;
import fr.pasteque.client.fragments.PaymentFragment;
import fr.pasteque.client.fragments.ProductScaleDialog;
import fr.pasteque.client.fragments.TicketFragment;
import fr.pasteque.client.fragments.ViewPageFragment;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.User;
import fr.pasteque.client.printing.PrinterConnection;
import fr.pasteque.client.utils.PastequePowaPos;
import fr.pasteque.client.utils.TrackedActivity;

public class Transaction extends TrackedActivity
        implements CatalogFragment.Listener,
        ProductScaleDialog.Listener,
        ManualInputDialog.Listener,
        TicketFragment.Listener,
        PaymentFragment.Listener,
        CustomerSelectDialog.Listener,
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
    private final TransPage[] PAGES = new TransPage[]{
            new TransPage(0.65f, CatalogFragment.class),
            new TransPage(0.35f, TicketFragment.class),
            new TransPage(0.65f, PaymentFragment.class)};

    // Data
    private Context mContext;
    private Ticket mPendingTicket;
    private TransactionPagerAdapter mPagerAdapter;
    private PrinterConnection mPrinter;
    private boolean mbPrintEnabled;
    private boolean mbPaymentClosed;

    // Views
    private ViewPager mPager;

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

    private final Handler.Callback mPrinterCallback = new Handler.Callback() {
        private void endPayment() {
            PaymentFragment p = getPaymentFragment();
            p.finish();
            mbPaymentClosed = false;
            disposePaymentFragment(p);
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
                        disablePrinting();
                        Error.showError(R.string.print_no_connexion, Transaction.this);
                    }
                    return true;
                default:
                    return false;
            }
        }
    };

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPrinter();
    }

    @Override
    public void onResume() {
        super.onResume();
        PastequePowaPos.getSingleton().addCallback(LOG_TAG, new TransPowaCallback());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case COMPOSITION:
                if (resultCode == Activity.RESULT_OK) {
                    CompositionInstance compo = (CompositionInstance)
                            data.getSerializableExtra("composition");
                    addACompoToTicket(compo);
                }
                break;
            case CUSTOMER_CREATE:
                if (resultCode == Activity.RESULT_OK) {
                    if (mPager.getCurrentItem() != CATALOG_FRAG) {
                        updatePaymentFragment(null, null);
                    }
                    if (CustomerData.customers.size() == 1 && getActionBar() != null) {
                        invalidateOptionsMenu();
                    }
                    try {
                        SessionData.saveSession(mContext);
                    } catch (IOException ioe) {
                        Log.e(LOG_TAG, "Unable to save session", ioe);
                        Error.showError(R.string.err_save_session, this);
                    }
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
                        mPendingTicket = SessionData.currentSession(mContext).getCurrentTicket();
                        mPager.setCurrentItem(CATALOG_FRAG);
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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
        PastequePowaPos.getSingleton().removeCallback(LOG_TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPrinter();
    }

    /*
     *  INTERFACE
     */

    @Override
    public void onCfProductClicked(Product p, Catalog catData) {
        registerAProduct(p, catData);
    }

    @Override
    public boolean onCfProductLongClicked(Product p) {
        TicketFragment ticket = getTicketFragment();
        String message = getString(R.string.prd_info_price,
                p.getPriceIncTax(ticket.getTariffArea()));
        disposeTicketFragment(ticket);

        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle(p.getLabel());
        b.setMessage(message);
        b.setNeutralButton(android.R.string.ok, null);
        b.show();
        return true;
    }

    @Override
    public void onPsdPositiveClick(Product p, double weight) {
        if (weight > 0) {
            addAScaledProductToTicket(p, weight);
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
    public boolean onPfPrintReceipt(Receipt r) {
        mbPaymentClosed = true;
        // Check printer
        if (mPrinter != null && mbPrintEnabled) {
            mPrinter.printReceipt(r);
            /*ProgressDialog progress = new ProgressDialog(mContext);
            progress.setIndeterminate(true);
            progress.setMessage(getString(R.string.print_printing));
            progress.show();*/
            return true;
        }
        return false;
    }

    @Override
    public void onPfCustomerListClick() {
        showCustomerList();
    }

    @Override
    public Receipt onPfSaveReceipt(ArrayList<Payment> p) {
        TicketFragment t = getTicketFragment();
        Ticket ticketData = t.getTicketData();
        // Create and save the receipt and remove from session
        Session currSession = SessionData.currentSession(mContext);
        User u = currSession.getUser();
        final Receipt r = new Receipt(ticketData, p, u);
        ReceiptData.addReceipt(r);
        try {
            ReceiptData.save(mContext);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to save receipts", e);
            Error.showError(R.string.err_save_receipts, this);
        }
        currSession.closeTicket(ticketData);
        try {
            SessionData.saveSession(mContext);
        } catch (IOException ioe) {
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
        Session currSession = SessionData.currentSession(mContext);
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
            SessionData.saveSession(mContext);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Unable to save session", ioe);
            Error.showError(R.string.err_save_session, this);
        }
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
                }
                disposeTicketFragment(ticket);
                invalidateOptionsMenu();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ab_ticket_input, menu);

        if (CustomerData.customers.size() == 0) {
            menu.findItem(R.id.ab_menu_customer_list).setEnabled(false);
        }
        User cashier = SessionData.currentSession(mContext).getUser();
        if (cashier.hasPermission("fr.pasteque.pos.panels.JPanelCloseMoney")) {
            menu.findItem(R.id.ab_menu_close_session).setEnabled(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!ReceiptData.hasReceipts()
                || !SessionData.currentSession(mContext).getUser().hasPermission("sales.EditTicket")) {
            menu.findItem(R.id.ab_menu_past_ticket).setVisible(false);
        }
        if (mPager.getCurrentItem() != CATALOG_FRAG) {
            menu.findItem(R.id.ab_menu_manual_input).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_menu_cashdrawer:
                PastequePowaPos.getSingleton().openCashDrawer();
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
                this.startActivity(receiptSelect);
                break;
            case R.id.ab_menu_close_session:
                CloseCash.close(this);
                break;
            /*
            case OPEN_BROWSER_BNP:
                String url = "https://www.secure.bnpparibas.net/banque/portail/particulier/HomePage?type=site";
                Intent accessBnp = new Intent( Intent.ACTION_VIEW, android.net.Uri.parse( url ) );
                startActivity(accessBnp);
                break;
            case MENU_BARCODE:
                scanBarcode(null);
                break;
            */
        }
        return true;
    }

    /*
     *  PRIVATES
     */

    // CONSTRUCTION RELATED FUNCTIONS

    private void initPrinter() {
        mPrinter = new PrinterConnection(new Handler(mPrinterCallback));
        try {
            if (!mPrinter.connect(mContext)) {
                disablePrinting();
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Unable to connect to printer", e);
            fr.pasteque.client.Error.showError(R.string.print_no_connexion, this);
            disablePrinting();
        }
    }

    private void stopPrinter() {
        if (mPrinter != null) {
            try {
                mPrinter.disconnect();
                mPrinter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private void disablePrinting() {
        mPrinter = null;
    }

    // CUSTOMER RELATED FUNCTIONS

    private void createNewCustomer() {
        Intent createCustomer = new Intent(mContext, CustomerCreate.class);
        startActivityForResult(createCustomer, CUSTOMER_CREATE);
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
        if (CompositionData.isComposition(p)) {
            Intent i = new Intent(mContext, CompositionInput.class);
            CompositionInput.setup(catData, CompositionData.getComposition(p.getId()));
            startActivityForResult(i, COMPOSITION);
        } else if (p.isScaled()) {
            // If the product is scaled, asks the weight
            ProductScaleDialog dial = ProductScaleDialog.newInstance(p);
            dial.setDialogListener(this);
            dial.show(getFragmentManager(), ProductScaleDialog.TAG);
        } else {
            addAProductToTicket(p);
        }
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

    private void readBarcode(String code) {
        // Is it a customer card ?
        for (Customer c : CustomerData.customers) {
            if (code.equals(c.getCard())) {
                onCustomerPicked(c);
                return;
            }
        }
        // Is it a product ?
        Catalog cat = CatalogData.catalog(mContext);
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

    private class TransPowaCallback extends PowaPOSCallback {

        @Override
        public void onMCUInitialized(PowaPOSEnums.InitializedResult initializedResult) {

        }

        @Override
        public void onMCUFirmwareUpdateStarted() {

        }

        @Override
        public void onMCUFirmwareUpdateProgress(int i) {

        }

        @Override
        public void onMCUFirmwareUpdateFinished() {

        }

        @Override
        public void onMCUBootloaderUpdateStarted() {

        }

        @Override
        public void onMCUBootloaderUpdateProgress(int i) {

        }

        @Override
        public void onMCUBootloaderUpdateFinished() {

        }

        @Override
        public void onMCUBootloaderUpdateFailed(PowaPOSEnums.BootloaderUpdateError bootloaderUpdateError) {

        }

        @Override
        public void onMCUSystemConfiguration(Map<String, String> map) {

        }

        @Override
        public void onUSBDeviceAttached(PowaPOSEnums.PowaUSBCOMPort powaUSBCOMPort) {
        }

        @Override
        public void onUSBDeviceDetached(PowaPOSEnums.PowaUSBCOMPort powaUSBCOMPort) {

        }

        @Override
        public void onCashDrawerStatus(PowaPOSEnums.CashDrawerStatus cashDrawerStatus) {

        }

        @Override
        public void onRotationSensorStatus(PowaPOSEnums.RotationSensorStatus rotationSensorStatus) {
            if (rotationSensorStatus == PowaPOSEnums.RotationSensorStatus.ROTATED) {
                Transaction.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Transaction.this.createNewCustomer();
                    }
                });
            }
        }

        @Override
        public void onScannerInitialized(PowaPOSEnums.InitializedResult initializedResult) {
        }

        @Override
        public void onScannerRead(String s) {
            final String code = s;
            Transaction.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Transaction.this.readBarcode(code);
                }
            });
        }

        @Override
        public void onPrintJobResult(PowaPOSEnums.PrintJobResult printJobResult) {
        }

        @Override
        public void onUSBReceivedData(PowaPOSEnums.PowaUSBCOMPort powaUSBCOMPort, byte[] bytes) {
        }

        @Override
        public void onMCUConnectionStateChanged(ConnectionState state) {
        }

        @Override
        public void onPrinterOutOfPaper() {
        }
    }
}
