package fr.pasteque.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import fr.pasteque.client.data.CompositionData;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.fragments.ManualInputDialog;
import fr.pasteque.client.fragments.PaymentFragment;
import fr.pasteque.client.fragments.ProductScaleDialog;
import fr.pasteque.client.fragments.CatalogFragment;
import fr.pasteque.client.fragments.TicketFragment;
import fr.pasteque.client.fragments.ViewPageFragment;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.User;
import fr.pasteque.client.utils.TrackedActivity;

public class Transaction extends TrackedActivity
        implements CatalogFragment.Listener,
        ProductScaleDialog.Listener,
        ManualInputDialog.Listener,
        TicketFragment.Listener,
        ViewPager.OnPageChangeListener {

    //List of codes. Java enums sucks...
    private static final int COMPOSITION = 1;
    private static final int CUSTOMER_SELECT = 2;
    private static final int CUSTOMER_CREATE = 3;

    private static final String LOG_TAG = "Pasteque/Transaction";
    private static final int CATALOG_FRAG = 0;
    private static final int TICKET_FRAG = 1;
    private static final int PAYMENT_FRAG = 2;
    private final TransPage[] PAGES = new TransPage[]{
            new TransPage(0.65f, CatalogFragment.class),
            new TransPage(0.35f, TicketFragment.class),
            new TransPage(0.65f, PaymentFragment.class)};

    private Context mContext;
    private ViewPager mPager;
    private TransactionPagerAdapter mPagerAdapter;
    private Customer mCurrentCustomer;

    private class TransPage {
        // Between 0.0 - 1.0
        private float mWidth;
        private String mTag;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        mPagerAdapter = new TransactionPagerAdapter(getFragmentManager());
        mPager = new ViewPager(mContext);
        // There is View.generateViewId() but min_api < 17
        mPager.setId(R.id.transaction_view_pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setBackgroundResource(R.color.main_bg);
        mPager.setOnPageChangeListener(this);
        setContentView(mPager);
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
            case CUSTOMER_SELECT:
                if (resultCode == Activity.RESULT_OK) {
                    TicketFragment ticket = getTicketFragment();
                    ticket.updateView();
                    disposeTicketFragment(ticket);
                    try {
                        SessionData.saveSession(this);
                    } catch (IOException ioe) {
                        Log.e(LOG_TAG, "Unable to save session", ioe);
                        Error.showError(R.string.err_save_session, this);
                    }
                }
                break;
            case CUSTOMER_CREATE:
                if (resultCode == Activity.RESULT_OK) {
                    Session sessionData = SessionData.currentSession(mContext);
                    mCurrentCustomer = sessionData.getCurrentTicket().getCustomer();
                    if (CustomerData.customers.size() == 1 && getActionBar() != null) {
                        invalidateOptionsMenu();
                    }
                    try {
                        SessionData.saveSession(this);
                    } catch (IOException ioe) {
                        Log.e(LOG_TAG, "Unable to save session", ioe);
                        Error.showError(R.string.err_save_session, this);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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
                p.getTaxedPrice(ticket.getTariffArea()));
        disposeTicketFragment(ticket);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(p.getLabel());
        b.setMessage(message);
        b.setNeutralButton(android.R.string.ok, null);
        b.show();
        return true;
    }

    @Override
    public void onPsdPositiveClick(Product p, double weight) {
        addAScaledProductToTicket(p, weight);
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
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        switch (i) {
            case CATALOG_FRAG: {
                TicketFragment ticket = getTicketFragment();
                ticket.setState(TicketFragment.CHECKIN_STATE);
                ticket.updatePageState();
                disposeTicketFragment(ticket);
                invalidateOptionsMenu();
                break;
            }
            case TICKET_FRAG:
            case PAYMENT_FRAG: {
                TicketFragment ticket = getTicketFragment();
                ticket.setState(TicketFragment.CHECKOUT_STATE);
                ticket.updatePageState();
                disposeTicketFragment(ticket);
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
        if (mPager.getCurrentItem() != CATALOG_FRAG) {
            menu.findItem(R.id.ab_menu_manual_input).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_menu_cashdrawer:
                //TODO: Implement this
                //TicketInput.this.powa.openCashDrawer();
                break;
            case R.id.ab_menu_manual_input:
                DialogFragment dial = new ManualInputDialog();
                dial.show(getFragmentManager(), ManualInputDialog.TAG);
                break;
            case R.id.ab_menu_customer_list:
                Intent customerSelect = new Intent(this, CustomerSelect.class);
                CustomerSelect.setup(mCurrentCustomer != null);
                this.startActivityForResult(customerSelect, CUSTOMER_SELECT);
                break;
            case R.id.ab_menu_customer_add:
                Intent createCustomer = new Intent(this, CustomerCreate.class);
                startActivityForResult(createCustomer, CUSTOMER_CREATE);
                break;
            case R.id.ab_menu_calendar:
                java.util.Calendar starTime = Calendar.getInstance();

                Uri uri = Uri.parse("content://com.android.calendar/time/" +
                        String.valueOf(starTime.getTimeInMillis()));

                Intent openCalendar = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(openCalendar);
                break;
            case R.id.ab_menu_past_ticket:
                Intent receiptSelect = new Intent(this, ReceiptSelect.class);
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


    /*
     *  To be used with dispose function
     *  i.e:    SomeFragment sFrag = getSomeFragment();
     *          // Code using sFrag
     *          disposeSomeFragment(sFrag);
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

    /**
     * Asks for complementary product information before adding it to ticket
     *
     * @param p       Product to be added
     * @param catData The current Catalog for data comparison
     */
    private void registerAProduct(Product p, Catalog catData) {
        // TODO: COMPOSITION NOT TESTED
        if (CompositionData.isComposition(p)) {
            Intent i = new Intent(this, CompositionInput.class);
            CompositionInput.setup(catData, CompositionData.getComposition(p.getId()));
            this.startActivityForResult(i, COMPOSITION);
        } else if (p.isScaled()) {
            // If the product is scaled, asks the weight
            ProductScaleDialog dial = ProductScaleDialog.newInstance(p);
            dial.show(getFragmentManager(), ProductScaleDialog.TAG);
        } else {
            addAProductToTicket(p);
        }
    }

    // Only suitable for adding one product at a time because updateView is heavy
    private void addAProductToTicket(Product p) {
        TicketFragment ticket = getTicketFragment();
        ticket.addProduct(p);
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
        ticket.updateView();
        disposeTicketFragment(ticket);
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
}
